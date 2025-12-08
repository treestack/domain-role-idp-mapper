[![CI](https://github.com/treestack/domain-role-idp-mapper/actions/workflows/ci.yml/badge.svg)](https://github.com/treestack/domain-role-idp-mapper/actions/workflows/ci.yml)
[![GitHub Release](https://img.shields.io/github/release/treestack/domain-role-idp-mapper.svg?style=flat)]() 
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Donate to MSF](https://img.shields.io/badge/Donate-MSF-red)](https://www.msf.org/donate)

# Domain Role IdP Mapper

Assign Keycloak roles to brokered users based on their email domain.

This Identity Provider mapper grants a configured role when a user's email domain matches a list of allowed domains. If the email domain does not match, it can optionally grant a fallback role.

## Security implications

> [!WARNING]  
> This mapper assigns Keycloak roles based solely on the email domain provided by the external Identity Provider (e.g. Microsoft Entra ID / MS365).
> This has direct security implications for both authorization and operations.
> 
> **It's obvious that this is not intended for use in high-security environments!**

The core rule implemented is:
> If the user’s email domain matches one of the configured domains, assign the matched role; otherwise assign the fallback role.

This means that full trust is placed in the Identity Provider’s email claim. If users can self-edit their primary email, this becomes a privilege-escalation vector. Also make sure that guest or external accounts must not be able to receive internal domains. 

Account takeover at the IdP equals privilege escalation in Keycloak: A compromised MS365 account immediately receives the mapped Keycloak role.
Authorization security therefore directly depends on the IdP's security.

Recommendations:
- Restrict the IdP to trusted tenants and issuers only.
- Prevent guest users from receiving internal domains.
- Keep domain-matched roles strictly scoped.[^1]
- Add additional security measures if applicable. For MS365, prefer adding a tenant ID (tid) check in addition to domain matching, enforce tenant restrictions.

The fallback role is granted to all users whose email does not match any configured domain. If the fallback role is too powerful, all unmatched users gain unintended access.

[^1]: The roles assigned via email-domain matching must be strictly limited in scope. It should only grant the minimum permissions required for users authenticated via that domain. Broad system, administrative, or cross-tenant privileges must never be bound solely to a domain-based rule.

Recommendations:
- Keep the fallback role minimally privileged.
- Consider using no fallback role at all if users should have zero access by default.

## Features
- Space‑separated list of allowed domains (case‑insensitive)
- Grants a role on match; optional fallback role otherwise
- Only grants roles the user does not already have

## Compatibility
- Built and tested against Keycloak 22.x
- Tested with Keycloak 26.x
- Java 17+

## Installation
1. Copy the latest release into your Keycloak `providers/` directory.
2. Restart Keycloak.

## Usage

![](/doc/idp-mapper.png?raw=true "Identity Provider Mapper Configuration")

1. Select your IdP in the Identity Providers configuration.
2. Open the Mappers tab → Create mapper.
3. Choose *Mapper Type*: `Email Domain → Role Mapper`.
4. Select an appropriate *Sync mode override*, see Keycloak's [Server Administration Guide](https://www.keycloak.org/docs/latest/server_admin) for details.
5. Configure properties:
   - `Allowed E‑Mail Domain(s)`: Space‑separated list (e.g. `example.com example.org`).
   - `Role for Matching Domains`: Role to grant when the user's email domain is in the allowed list.
   - `Fallback Role` (optional): Role to grant when the domain does not match.

## Configuration Details
- If the user email is missing or invalid (no `@`), no role is granted.
- If a configured role name cannot be resolved in the realm, no role is granted for that branch.

## Development
Requirements: JDK 17, Maven 3.9+

Run tests:
```sh
mvn -q test
```

Build a classifier targeted JAR (default profile is `keycloak-22`):
```sh
mvn -q -DskipTests package
```

## License
MIT — see [LICENSE](LICENSE).

## Support

This project does not accept donations. If you find it useful, please consider supporting **Médecins Sans Frontières (Doctors Without Borders)** instead:
https://www.msf.org/donate
