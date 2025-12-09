package de.treestack.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DomainRoleIdpMapperResolveClientRoleTest {

    @Mock
    RealmModel realm;
    @Mock
    ClientModel client;
    @Mock
    ClientModel clientAB;
    @Mock
    RoleModel role;

    @Test
    void when_clientAndRoleExist_expect_returnsClientRole() {
        // Arrange
        when(realm.getName()).thenReturn("test");
        when(realm.getClientByClientId("app")) .thenReturn(client);
        when(client.getRole("reader")) .thenReturn(role);

        // Act
        RoleModel resolved = DomainRoleIdpMapper.resolveClientRole(realm, "app.reader");

        // Assert
        assertSame(role, resolved);
        verify(realm).getClientByClientId("app");
        verify(client).getRole("reader");
    }

    @Test
    void when_noDot_expect_returnsNull() {
        when(realm.getName()).thenReturn("test");
        assertNull(DomainRoleIdpMapper.resolveClientRole(realm, "realmRoleName"));
    }

    @Test
    void when_clientMissing_expect_returnsNull() {
        when(realm.getClientByClientId("missing")).thenReturn(null);
        assertNull(DomainRoleIdpMapper.resolveClientRole(realm, "missing.role"));
    }

    @Test
    void when_roleMissing_expect_returnsNull() {
        when(realm.getClientByClientId("app")).thenReturn(client);
        when(client.getRole("unknown")).thenReturn(null);
        assertNull(DomainRoleIdpMapper.resolveClientRole(realm, "app.unknown"));
    }

    @Test
    void when_multipleDotsPresent_expect_triesAllPossibleSplits() {
        // Arrange
        when(realm.getName()).thenReturn("test");

        // Input: "a.b.c". The method will try:
        // 1) clientId="a", roleName="b.c" → client missing
        // 2) clientId="a.b", roleName="c" → client exists, role exists → return
        when(realm.getClientByClientId("a")).thenReturn(null);
        when(realm.getClientByClientId("a.b")).thenReturn(clientAB);
        when(clientAB.getRole("c")).thenReturn(role);

        // Act
        RoleModel resolved = DomainRoleIdpMapper.resolveClientRole(realm, "a.b.c");

        // Assert
        assertSame(role, resolved);
        verify(realm).getClientByClientId("a");
        verify(realm).getClientByClientId("a.b");
        verify(clientAB).getRole("c");
    }

    @Test
    void when_roleSegmentsAreMissing_expect_skipsEmpty() {
        when(realm.getName()).thenReturn("test");

        // Leading dot → empty client id
        assertNull(DomainRoleIdpMapper.resolveClientRole(realm, ".role"));

        // Trailing dot → empty role name
        assertNull(DomainRoleIdpMapper.resolveClientRole(realm, "client."));

        // Double dot inside → first split hits empty role part after first '.', second split hits empty client part before second '.'
        assertNull(DomainRoleIdpMapper.resolveClientRole(realm, "client..role"));
    }
}
