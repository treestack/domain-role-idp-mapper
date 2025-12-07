[![Release JAR](https://github.com/treestack/domain-role-idp-mapper/actions/workflows/release.yml/badge.svg)](https://github.com/treestack/domain-role-idp-mapper/actions/workflows/release.yml)

# Domain Role IdP Mapper

Assign Keycloak roles to brokered users based on their email domain.

This Identity Provider mapper grants a configured role when a user's email domain matches a list of allowed domains. If the email domain does not match, it can optionally grant a fallback role.

## Features
- Space‑separated list of allowed domains (case‑insensitive)
- Grants a role on match; optional fallback role otherwise
- Only grants roles the user does not already have

## Compatibility
- Built and tested against Keycloak 22.x
- Java 17+

## Installation
1. Copy the latest release into your Keycloak `providers/` directory.
2. Restart Keycloak.

## Usage

![](/doc/idp-mapper.png?raw=true "Identity Provider Mapper Configuration")

1. Select your IdP in the Identity Providers configuration.
2. Open the Mappers tab → Create mapper.
3. Choose Mapper Type: `Email Domain → Role Mapper`.
4. Configure properties:
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
