package de.treestack.auth;

import jakarta.annotation.Nullable;
import org.jboss.logging.Logger;
import org.keycloak.broker.provider.AbstractIdentityProviderMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.*;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Keycloak Identity Provider mapper that assigns a realm role based on the email domain
 * of the federated/brokered user. If the email's domain matches one of the configured
 * allowed domains, the configured matched role is granted. Otherwise, an optional fallback
 * role can be granted.
 *
 * <p>Configuration properties:</p>
 * <ul>
 *   <li><code>allowedDomains</code> (string): Space separated list of allowed email domains
 *   (e.g. "example.com example.org"). Comparison is case-insensitive.</li>
 *   <li><code>matchedRole</code> (role): The realm role to grant when the user's email domain
 *   is contained in <code>allowedDomains</code>.</li>
 *   <li><code>fallbackRole</code> (role): Optional realm role to grant when the user's email
 *   domain does not match the allowed list.</li>
 * </ul>
 */
public class DomainRoleIdpMapper extends AbstractIdentityProviderMapper {

    private static final Logger LOG = Logger.getLogger(DomainRoleIdpMapper.class);

    public static final String PROVIDER_ID = "domain-role-idp-mapper";

    private static final String CFG_DOMAINS = "allowedDomains";
    private static final String CFG_DOMAIN_MATCH_MODE = "domainMatchMode";
    private static final String CFG_MATCHED_ROLE = "matchedRole";
    private static final String CFG_FALLBACK_ROLE = "fallbackRole";

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES;

    static {
        var props = new ArrayList<ProviderConfigProperty>();

        // Keycloak doesn't support MULTIVALUED_STRING_TYPE for IdP mappers
        // Maybe related to this? https://github.com/keycloak/keycloak/issues/30168
        var domains = new ProviderConfigProperty();
        domains.setName(CFG_DOMAINS);
        domains.setLabel("Allowed E-Mail Domain(s)");
        domains.setHelpText("Multiple domains can be separated by space");
        domains.setType(ProviderConfigProperty.STRING_TYPE);

        var matchedRole = new ProviderConfigProperty();
        matchedRole.setName(CFG_MATCHED_ROLE);
        matchedRole.setLabel("Role for Matching Domains");
        matchedRole.setType(ProviderConfigProperty.ROLE_TYPE);

        var fallbackRole = new ProviderConfigProperty();
        fallbackRole.setName(CFG_FALLBACK_ROLE);
        fallbackRole.setLabel("Fallback Role");
        fallbackRole.setType(ProviderConfigProperty.ROLE_TYPE);

        var matchMode = new ProviderConfigProperty();
        matchMode.setName(CFG_DOMAIN_MATCH_MODE);
        matchMode.setLabel("Domain Match Mode");
        matchMode.setHelpText("Defines how email domains are matched against the configured domain list. Possible " +
                "values are 'exact' for exact domain matches (e.g. example.org), 'wildcard' supports * as a " +
                "placeholder (e.g. *.example.org.de) and 'regex' allows full Java regular expressions but " +
                "also carries the highest risk of misconfiguration.");
        matchMode.setType(ProviderConfigProperty.LIST_TYPE);
        matchMode.setOptions(List.of("Exact", "Wildcard", "Regex"));
        matchMode.setDefaultValue("Exact");

        props.add(domains);
        props.add(matchMode);
        props.add(matchedRole);
        props.add(fallbackRole);

        CONFIG_PROPERTIES = Collections.unmodifiableList(props);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Email Domain → Role Mapper";
    }

    @Override
    public String getDisplayCategory() {
        return "Role Importer";
    }

    @Override
    public String getHelpText() {
        return "Assigns a role if the user's email domain matches a configured list, otherwise assigns an optional fallback role.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    @Override
    public String[] getCompatibleProviders() {
        return new String[]{ANY_PROVIDER};
    }

    @Override
    public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        LOG.debugf("updateBrokeredUser invoked for user=%s, realm=%s, brokeredId=%s", user.getUsername(), realm.getName(), context.getBrokerUserId());
        assignRole(realm, user, mapperModel);
    }

    /**
     * Assign a role to the user according to the mapper configuration and the user's email domain.
     */
    static void assignRole(
            RealmModel realm,
            UserModel user,
            IdentityProviderMapperModel mapperModel) {

        String email = user.getEmail();
        if (!isValidEmail(email)) {
            LOG.debugf("Skipping role assignment for user=%s due to missing/invalid email: %s", user.getUsername(), email);
            return;
        }

        String domain = extractDomain(email);
        MapperConfig cfg = loadConfig(realm, mapperModel);

        LOG.debugf("User %s has email domain '%s'. Allowed domains configured: %s; matchedRole=%s; fallbackRole=%s",
                user.getUsername(),
                domain,
                cfg.allowedDomains(),
                cfg.matchedRole != null ? cfg.matchedRole.getName() : null,
                cfg.fallbackRole != null ? cfg.fallbackRole.getName() : null);

        if (cfg.allowedDomains().isEmpty()) {
            LOG.warnf("No allowed domains configured for mapper '%s' in realm '%s'", mapperModel.getName(), realm.getName());
        }

        DomainMatchMode mode = DomainMatchMode.from(mapperModel.getConfig().get(CFG_DOMAIN_MATCH_MODE));
        if (matchesDomain(domain, cfg.allowedDomains(), mode)) {
            grantRole(user, cfg.matchedRole);
        } else {
            grantRole(user, cfg.fallbackRole);
        }
    }

    static boolean matchesDomain(
            String domain,
            Set<String> configuredDomains,
            DomainMatchMode mode) {

        if (domain == null || configuredDomains == null || configuredDomains.isEmpty()) {
            return false;
        }

        return switch (mode) {
            case EXACT -> configuredDomains.contains(domain);

            case WILDCARD -> configuredDomains.stream()
                    .map(pattern -> pattern.replace(".", "\\.")
                            .replace("*", ".*"))
                    .anyMatch(domain::matches);

            case REGEX -> configuredDomains.stream().anyMatch(pattern -> {
                try {
                    return domain.matches(pattern);
                } catch (Exception e) {
                    LOG.warnf(e, "Failed to match '%s'", pattern);
                    return false;
                }
            });
        };
    }

    static void grantRole(UserModel user, @Nullable RoleModel role) {
        if (role == null) {
            LOG.debugf("No role configured; no role changes for user %s", user.getUsername());
            return;
        }
        if (user.hasRole(role)) {
            LOG.debugf("User %s already has role %s; no action taken", user.getUsername(), role.getName());
            return;
        }
        LOG.infof("Granting role %s to user %s", role, user.getUsername());
        user.grantRole(role);
    }

    /**
     * Load and normalize configuration values from the mapper model.
     */
    static MapperConfig loadConfig(RealmModel realm, IdentityProviderMapperModel mapperModel) {
        Map<String, String> cfg = mapperModel.getConfig();
        RoleModel matched = findRole(realm, cfg.get(CFG_MATCHED_ROLE));
        RoleModel fallback = findRole(realm, cfg.get(CFG_FALLBACK_ROLE));

        LOG.tracef("Loaded mapper config for realm=%s: allowedDomains='%s', matchedRole='%s', fallbackRole='%s'",
                realm.getName(), cfg.get(CFG_DOMAINS), cfg.get(CFG_MATCHED_ROLE), cfg.get(CFG_FALLBACK_ROLE));

        return new MapperConfig(
                parseAllowedDomains(cfg.get(CFG_DOMAINS)),
                matched,
                fallback
        );
    }

    static boolean isValidEmail(@Nullable String email) {
        return email != null && email.contains("@");
    }

    static String extractDomain(String email) {
        return email.substring(email.indexOf('@') + 1).toLowerCase();
    }

    static Set<String> parseAllowedDomains(@Nullable String rawDomains) {
        return Optional.ofNullable(rawDomains)
                .map(s -> Arrays.asList(s.split(" ")))
                .orElse(List.of())
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    static @Nullable RoleModel findRole(RealmModel realm, @Nullable String roleName) {
        if (roleName == null) {
            LOG.debugf("No role configured (null) while resolving role in realm '%s'", realm.getName());
            return null;
        }

        RoleModel roleModel = realm.getRole(roleName);
        if (roleModel != null) {
            LOG.tracef("Resolved realm role '%s' in realm '%s'", roleName, realm.getName());
            return roleModel;
        }

        roleModel = resolveClientRole(realm, roleName);
        if (roleModel != null) {
            return roleModel;
        }

        LOG.warnf("Configured role '%s' not found in realm '%s' (as realm or client role)", roleName, realm.getName());
        return null;
    }

    /**
     * Resolve a client role given a composite role name in the format "clientId.roleName".
     */
    static @Nullable RoleModel resolveClientRole(RealmModel realm, String roleName) {
        // Keycloak's ProviderConfigProperty stores the role as 'namespaced' strings like "roleName"
        // or "clientId.roleName". Both client IDs and role names can contain dots, so this is ambiguous.

        int index = roleName.indexOf('.');

        while (index > 0 && index < roleName.length() - 1) {
            String clientId = roleName.substring(0, index);
            String clientRoleName = roleName.substring(index + 1);

            ClientModel client = realm.getClientByClientId(clientId);
            if (client != null) {
                RoleModel clientRole = client.getRole(clientRoleName);
                if (clientRole != null) {
                    LOG.tracef(
                            "Resolved client role '%s' for client '%s' in realm '%s'",
                            clientRoleName, clientId, realm.getName()
                    );
                    return clientRole;
                }
            }
            index = roleName.indexOf('.', index + 1);
        }

        LOG.warnf("Could not resolve client role from name '%s' in realm '%s'", roleName, realm.getName());
        return null;
    }

    record MapperConfig(
            Set<String> allowedDomains,
            @Nullable RoleModel matchedRole,
            @Nullable RoleModel fallbackRole
    ) {
    }
}
