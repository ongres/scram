# Changelog
All notable changes to this project will be documented in this file.

## [Unreleased]

## [3.1] - 2024-06-26
### :building_construction: Improvements
- Ensure the `LICENSE` file is included in the Jar file.
- Update of the `saslprep` dependency to 2.2.

### :ghost: Maintenance
- Added coverage report module.
- Updated dependencies and maven plugins.
- Remove `nexus-staging-maven-plugin`.

## [3.0] - 2024-04-03
### :boom: Breaking changes
- :warning: Full refactor of the `scram` java implementation, this release is compatible with Java 8+, but it's incompatible with previous releases :warning:

### :rocket: New features
- Fully rewrite the `ScramClient` allowing negotiation of channel-binding properly.
- Create Multi-release Modular JARs, the modules names are:
  - `com.ongres.scram.common` for the common scram messages.
  - `com.ongres.scram.client` for the scram client implementation.
- Add `StringPreparation.POSTGRESQL_PREPARATION`, for any error in SASL preparation, it falls back to return the raw string.
- Now the released jars are reproducible.
- Publish CycloneDX SBOM.
- Implementation of `tls-server-end-point` channel binding data extraction.

### :building_construction: Improvements
- Update of the `saslprep` dependency to 2.1.
- Now the password is passed as a `char[]`.
- Improve Javadoc documentation.

### :ghost: Maintenance
- Migrate the main repo back to GitHub.
- Remove the shaded Bouncy Castle pbkdf2 and base64 implementation used for Java 7 support.

[3.0]: https://github.com/ongres/scram/compare/2.1...3.0
[3.1]: https://github.com/ongres/scram/compare/3.0...3.1
[Unreleased]: https://github.com/ongres/scram/compare/3.1...main
