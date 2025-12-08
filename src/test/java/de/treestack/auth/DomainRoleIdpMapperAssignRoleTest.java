package de.treestack.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.ClientModel;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DomainRoleIdpMapperAssignRoleTest {

    @Mock
    RealmModel realm;
    @Mock
    UserModel user;
    @Mock
    IdentityProviderMapperModel mapperModel;
    @Mock
    RoleModel matchedRole;
    @Mock
    RoleModel fallbackRole;
    @Mock
    ClientModel client;

    Map<String, String> cfg;

    @BeforeEach
    void setUp() {
        cfg = new HashMap<>();
    }

    @Test
    void assignsMatchedClientRole_whenDomainMatches() {
        cfg.put("allowedDomains", "example.com");
        cfg.put("matchedRole", "app.viewer");
        when(mapperModel.getConfig()).thenReturn(cfg);
        when(user.getEmail()).thenReturn("user@example.com");
        when(user.getUsername()).thenReturn("alice");

        when(realm.getRole("app.viewer")).thenReturn(null); // ensure realm role path is skipped
        when(realm.getClientByClientId("app")).thenReturn(client);
        when(client.getRole("viewer")).thenReturn(matchedRole);
        when(user.hasRole(matchedRole)).thenReturn(false);

        DomainRoleIdpMapper.assignRole(realm, user, mapperModel);

        verify(user).grantRole(matchedRole);
        verify(user, never()).grantRole(fallbackRole);
    }

    @Test
    void assignsFallbackClientRole_whenDomainDoesNotMatch() {
        cfg.put("allowedDomains", "example.com");
        cfg.put("matchedRole", "realm-role");
        cfg.put("fallbackRole", "client-x.reader");
        when(mapperModel.getConfig()).thenReturn(cfg);
        when(user.getEmail()).thenReturn("user@other.net");
        when(user.getUsername()).thenReturn("bob");

        // matched role is a realm role but irrelevant here; ensure resolution returns null
        when(realm.getRole("realm-role")).thenReturn(null);
        when(realm.getRole("client-x.reader")).thenReturn(null);
        when(realm.getClientByClientId("client-x")).thenReturn(client);
        when(client.getRole("reader")).thenReturn(fallbackRole);
        when(user.hasRole(fallbackRole)).thenReturn(false);

        DomainRoleIdpMapper.assignRole(realm, user, mapperModel);

        verify(user).grantRole(fallbackRole);
        verify(user, never()).grantRole(matchedRole);
    }

    @Test
    void doesNothing_whenClientOrClientRoleMissing() {
        cfg.put("allowedDomains", "example.com");
        cfg.put("matchedRole", "missing-client.viewer");
        when(mapperModel.getConfig()).thenReturn(cfg);
        when(user.getEmail()).thenReturn("user@example.com");

        // Case 1: client missing
        when(realm.getRole("missing-client.viewer")).thenReturn(null);
        when(realm.getClientByClientId("missing-client")).thenReturn(null);

        DomainRoleIdpMapper.assignRole(realm, user, mapperModel);
        verify(user, never()).grantRole(any());

        // Case 2: client exists but role missing
        reset(user, realm, client);
        when(mapperModel.getConfig()).thenReturn(cfg);
        when(user.getEmail()).thenReturn("user@example.com");
        when(realm.getRole("missing-client.viewer")).thenReturn(null);
        when(realm.getClientByClientId("missing-client")).thenReturn(client);
        when(client.getRole("viewer")).thenReturn(null);

        DomainRoleIdpMapper.assignRole(realm, user, mapperModel);
        verify(user, never()).grantRole(any());
    }

    @Test
    void doesNotGrantClientRole_ifUserAlreadyHasIt() {
        cfg.put("allowedDomains", "example.com");
        cfg.put("matchedRole", "my-client.admin");
        when(mapperModel.getConfig()).thenReturn(cfg);
        when(user.getEmail()).thenReturn("owner@example.com");

        when(realm.getRole("my-client.admin")).thenReturn(null);
        when(realm.getClientByClientId("my-client")).thenReturn(client);
        when(client.getRole("admin")).thenReturn(matchedRole);
        when(user.hasRole(matchedRole)).thenReturn(true);

        DomainRoleIdpMapper.assignRole(realm, user, mapperModel);

        verify(user, never()).grantRole(matchedRole);
    }

    @Test
    void assignsMatchedRole_whenDomainMatches() {
        cfg.put("allowedDomains", "example.com");
        cfg.put("matchedRole", "role-matched");
        when(mapperModel.getConfig()).thenReturn(cfg);
        when(user.getEmail()).thenReturn("user@example.com");
        when(user.getUsername()).thenReturn("alice");
        when(realm.getRole("role-matched")).thenReturn(matchedRole);
        when(user.hasRole(matchedRole)).thenReturn(false);

        DomainRoleIdpMapper.assignRole(realm, user, mapperModel);

        verify(user).grantRole(matchedRole);
        verify(user, never()).grantRole(fallbackRole);
    }

    @Test
    void assignsFallbackRole_whenDomainDoesNotMatch_andFallbackConfigured() {
        cfg.put("allowedDomains", "example.com test.org");
        cfg.put("matchedRole", "role-matched");
        cfg.put("fallbackRole", "role-fallback");
        when(mapperModel.getConfig()).thenReturn(cfg);
        when(user.getEmail()).thenReturn("user@other.net");
        when(user.getUsername()).thenReturn("alice");
        when(realm.getRole("role-matched")).thenReturn(matchedRole);
        when(realm.getRole("role-fallback")).thenReturn(fallbackRole);
        when(user.hasRole(fallbackRole)).thenReturn(false);

        DomainRoleIdpMapper.assignRole(realm, user, mapperModel);

        verify(user).grantRole(fallbackRole);
        verify(user, never()).grantRole(matchedRole);
    }

    @Test
    void doesNothing_whenEmailIsNullOrInvalid() {
        when(user.getEmail()).thenReturn(null);
        DomainRoleIdpMapper.assignRole(realm, user, mapperModel);
        verify(user, never()).grantRole(ArgumentMatchers.any());

        reset(user);
        when(user.getEmail()).thenReturn("no-at-symbol");
        DomainRoleIdpMapper.assignRole(realm, user, mapperModel);
        verify(user, never()).grantRole(ArgumentMatchers.any());
    }

    @Test
    void handlesCaseAndWhitespaceInDomains() {
        cfg.put("allowedDomains", "  Example.com   TEST.Org  ");
        cfg.put("matchedRole", "role-matched");
        when(mapperModel.getConfig()).thenReturn(cfg);
        when(user.getEmail()).thenReturn("bob@TeSt.ORG");
        when(user.getUsername()).thenReturn("alice");
        when(realm.getRole("role-matched")).thenReturn(matchedRole);
        when(user.hasRole(matchedRole)).thenReturn(false);

        DomainRoleIdpMapper.assignRole(realm, user, mapperModel);

        verify(user).grantRole(matchedRole);
    }

    @Test
    void doesNotGrantRole_ifUserAlreadyHasIt() {
        cfg.put("allowedDomains", "example.com");
        cfg.put("matchedRole", "role-matched");
        when(mapperModel.getConfig()).thenReturn(cfg);
        when(user.getEmail()).thenReturn("user@example.com");
        when(realm.getRole("role-matched")).thenReturn(matchedRole);
        when(user.hasRole(matchedRole)).thenReturn(true);

        DomainRoleIdpMapper.assignRole(realm, user, mapperModel);

        verify(user, never()).grantRole(matchedRole);
    }

    @Test
    void doesNothing_whenConfiguredRoleNamesResolveToNull() {
        cfg.put("allowedDomains", "example.com other.net");
        cfg.put("matchedRole", "role-matched");
        cfg.put("fallbackRole", "role-fallback");
        when(mapperModel.getConfig()).thenReturn(cfg);
        when(user.getEmail()).thenReturn("user@other.net");
        // realm.getRole returns null by default

        DomainRoleIdpMapper.assignRole(realm, user, mapperModel);

        verify(user, never()).grantRole(any());
    }
}
