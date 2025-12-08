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
    void returnsClientRole_whenClientAndRoleExist() {
        when(realm.getName()).thenReturn("test");
        when(realm.getClientByClientId("app")) .thenReturn(client);
        when(client.getRole("reader")) .thenReturn(role);

        RoleModel resolved = DomainRoleIdpMapper.resolveClientRole(realm, "app.reader");

        assertSame(role, resolved);
        verify(realm).getClientByClientId("app");
        verify(client).getRole("reader");
    }

    @Test
    void returnsNull_whenNoDotOrNoMatch() {
        when(realm.getName()).thenReturn("test");

        // No dot → method will iterate and never enter the body, produce warning and return null
        assertNull(DomainRoleIdpMapper.resolveClientRole(realm, "realmRoleName"));

        // Dot present but no client
        when(realm.getClientByClientId("missing")).thenReturn(null);
        assertNull(DomainRoleIdpMapper.resolveClientRole(realm, "missing.role"));

        // Client exists but role missing
        when(realm.getClientByClientId("app")).thenReturn(client);
        when(client.getRole("unknown")).thenReturn(null);
        assertNull(DomainRoleIdpMapper.resolveClientRole(realm, "app.unknown"));
    }

    @Test
    void triesAllPossibleSplits_whenMultipleDots_present() {
        when(realm.getName()).thenReturn("test");

        // Input: "a.b.c". The method will try:
        // 1) clientId="a", roleName="b.c" → client missing
        // 2) clientId="a.b", roleName="c" → client exists, role exists → return
        when(realm.getClientByClientId("a")).thenReturn(null);
        when(realm.getClientByClientId("a.b")).thenReturn(clientAB);
        when(clientAB.getRole("c")).thenReturn(role);

        RoleModel resolved = DomainRoleIdpMapper.resolveClientRole(realm, "a.b.c");

        assertSame(role, resolved);
        verify(realm).getClientByClientId("a");
        verify(realm).getClientByClientId("a.b");
        verify(clientAB).getRole("c");
    }

    @Test
    void skipsEmptyClientOrRoleSegments_andReturnsNull() {
        when(realm.getName()).thenReturn("test");

        // Leading dot → empty client id
        assertNull(DomainRoleIdpMapper.resolveClientRole(realm, ".role"));

        // Trailing dot → empty role name
        assertNull(DomainRoleIdpMapper.resolveClientRole(realm, "client."));

        // Double dot inside → first split hits empty role part after first '.', second split hits empty client part before second '.'
        assertNull(DomainRoleIdpMapper.resolveClientRole(realm, "client..role"));
    }
}
