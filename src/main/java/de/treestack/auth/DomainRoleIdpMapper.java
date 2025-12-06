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
    private static final String CFG_MATCHED_ROLE = "matchedRole";
    private static final String CFG_FALLBACK_ROLE = "fallbackRole";

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES;

    static {
        List<ProviderConfigProperty> props = new ArrayList<>();

        // Keycloak doesn't support MULTIVALUED_STRING_TYPE for IdP mappers
        // Maybe related to this? https://github.com/keycloak/keycloak/issues/30168
        ProviderConfigProperty domains = new ProviderConfigProperty();
        domains.setName(CFG_DOMAINS);
        domains.setLabel("Allowed E-Mail Domain(s)");
        domains.setHelpText("Multiple domains can be separated by space");
        domains.setType(ProviderConfigProperty.STRING_TYPE);

        ProviderConfigProperty matchedRole = new ProviderConfigProperty();
        matchedRole.setName(CFG_MATCHED_ROLE);
        matchedRole.setLabel("Role for Matching Domains");
        matchedRole.setType(ProviderConfigProperty.ROLE_TYPE);

        ProviderConfigProperty fallbackRole = new ProviderConfigProperty();
        fallbackRole.setName(CFG_FALLBACK_ROLE);
        fallbackRole.setLabel("Fallback Role");
        fallbackRole.setType(ProviderConfigProperty.ROLE_TYPE);

        props.add(domains);
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
        super.updateBrokeredUser(session, realm, user, mapperModel, context);
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
            return;
        }

        String domain = extractDomain(email);
        MapperConfig cfg = loadConfig(mapperModel);
        RoleModel matched = findRole(realm, cfg.matchedRoleName());
        RoleModel fallback = findRole(realm, cfg.fallbackRoleName());

        if (cfg.allowedDomains().contains(domain)) {
            if (matched != null && !user.hasRole(matched)) {
                LOG.infof("Granting role %s to user %s due to matching domain %s", matched, user.getUsername(), domain);
                user.grantRole(matched);
            }
        } else {
            if (fallback != null && !user.hasRole(fallback)) {
                LOG.infof("Granting fallback role %s to user %s", fallback, user.getUsername());
                user.grantRole(fallback);
            }
        }
    }

    /**
     * Load and normalize configuration values from the mapper model.
     */
    private static MapperConfig loadConfig(IdentityProviderMapperModel mapperModel) {
        Map<String, String> cfg = mapperModel.getConfig();
        return new MapperConfig(
                parseAllowedDomains(cfg.get(CFG_DOMAINS)),
                cfg.get(CFG_MATCHED_ROLE),
                cfg.get(CFG_FALLBACK_ROLE)
        );
    }

    private static boolean isValidEmail(@Nullable String email) {
        return email != null && email.contains("@");
    }

    private static String extractDomain(String email) {
        return email.substring(email.indexOf('@') + 1).toLowerCase();
    }

    private static Set<String> parseAllowedDomains(@Nullable String rawDomains) {
        return Optional.ofNullable(rawDomains)
                .map(s -> Arrays.asList(s.split(" ")))
                .orElse(List.of())
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    private static @Nullable RoleModel findRole(RealmModel realm, @Nullable String roleName) {
        return roleName != null ? realm.getRole(roleName) : null;
    }

    private record MapperConfig(
            Set<String> allowedDomains,
            @Nullable String matchedRoleName,
            @Nullable String fallbackRoleName
    )  {}

}
