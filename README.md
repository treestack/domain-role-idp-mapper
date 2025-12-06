# Domain Role IdP Mapper

Assign Keycloak realm roles to brokered users based on their email domain.

This Identity Provider mapper grants a configured role when a user's email domain matches a list of allowed domains. If the email domain does not match, it can optionally grant a fallback role.

## Features
- Space‑separated list of allowed domains (case‑insensitive)
- Grants a role on match; optional fallback role otherwise
- Safe: only grants roles the user does not already have
- Works with any Identity Provider (federated/brokered login)

## Compatibility
- Built and tested against Keycloak 22.x
- Java 17

## Installation
1. Build the project:
   ```sh
   mvn -q -DskipTests package
   ```
2. Copy the resulting JAR from `target/` into your Keycloak `providers/` directory.
3. Restart Keycloak.

> Note: The POM no longer uses an absolute `outputDirectory`. This is intentional to keep the project portable. Use your own deployment process to move the JAR.

## Usage
1. In the realm admin console, go to Identity Providers and select your IdP.
2. Open the Mappers tab → Create mapper.
3. Choose Mapper Type: `Email Domain → Role Mapper`.
4. Configure properties:
   - `Allowed E‑Mail Domain(s)`: Space‑separated list (e.g. `example.com example.org`).
   - `Role for Matching Domains`: Role to grant when the user's email domain is in the allowed list.
   - `Fallback Role` (optional): Role to grant when the domain does not match.

## Configuration Details
- Matching is case‑insensitive and trims whitespace.
- If the user email is missing or invalid (no `@`), no role is granted.
- If a configured role name cannot be resolved in the realm, no role is granted for that branch.

## Example
Allowed domains: `example.com corp.local`

- `alice@example.com` → grant `matchedRole`
- `bob@other.net` → grant `fallbackRole` (if configured)

## Development
Requirements: JDK 17, Maven 3.9+

Run tests:
```sh
mvn -q -DskipITs test
```

Build a classifier targeted JAR (default profile is `keycloak-22`):
```sh
mvn -q -DskipTests package
```

## License
MIT — see [LICENSE](LICENSE).