# Changelog

## [1.2.1] - 2026-01-07

### Added
- Release signing with cosign for supply chain security
- Integration smoke test

### Fixed
- Slightly stricter email validation logic
- Added slf4j-simple test dependency to prevent warnings during tests

## [1.2.0] - 2025-12-08

### Added
- **Selectable matching mode**: Choose between Exact, Wildcard, or Regex domain matching
- SonarQube integration for code quality analysis
- Comprehensive test suite
- CI caching for faster builds

### Fixed
- Wildcards now replace path segments only (security hardening)

### Changed
- Switched from Codecov to SonarQube for coverage reporting
- Reduced cognitive complexity in codebase
- Clarified sync mode documentation

### Documentation
- Added SonarQube quality badges
- Clarified sync mode override options
- Improved Javadoc comments

## [1.1.0] - 2025-12-07

### Added
- **Client role support**: Map to client-specific roles in addition to realm roles
- CI/CD workflow with GitHub Actions
- Release automation workflow
- Code coverage reporting
- Security implications documentation

### Fixed
- Handle dots in client or role names correctly

### Documentation
- Added configuration screenshot
- Added security implications section with recommendations
- Added compatibility information (Keycloak 22.x, 26.x)

## [1.0.0] - 2025-12-07

### Added
- Initial release
- Email domain to role mapping for Keycloak Identity Provider
- Space-separated list of allowed domains (case-insensitive)
- Configurable match role for allowed domains
- Optional fallback role for non-matching domains
- Only grants roles the user does not already have
- Java 17+ support
- Keycloak 22.x compatibility

---

[1.2.1]: https://github.com/treestack/domain-role-idp-mapper/compare/v1.2.0...v1.2.1
[1.2.0]: https://github.com/treestack/domain-role-idp-mapper/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/treestack/domain-role-idp-mapper/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/treestack/domain-role-idp-mapper/releases/tag/v1.0.0