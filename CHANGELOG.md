# Changelog
All notable changes to this project will be documented in this file.

## [Unreleased]
### :lock: Security
- Prevent silent downgrade attacks during channel binding negotiation via unsupported certificate algorithms. Add `TlsServerEndpoint.getChannelBindingHash`, which throws `NoSuchAlgorithmException` where `getChannelBindingData` returns an empty array, and `.channelBindingPolicy()` on the client builder with `DISABLE`, `ALLOW` (default), and `REQUIRE`. A client that keeps passing the result of `getChannelBindingData` under the default `ALLOW` policy still falls back to SCRAM without channel binding when the certificate signature algorithm yields no digest: to prevent the downgrade, use `getChannelBindingHash` and stop on its exception, or set `REQUIRE`. Backported from 3.3; `build()` failures that 3.2 reported as `IllegalArgumentException` keep that type. [GHSA-p9jg-fcr6-3mhf](https://github.com/ongres/scram/security/advisories/GHSA-p9jg-fcr6-3mhf)

### :bug: Bug Fixes
- Compute `tls-server-end-point` channel binding data for `RSASSA-PSS` server certificates, for which `getChannelBindingData` returned an empty array.
- Compute `tls-server-end-point` channel binding data when the security provider spells the signature algorithm name in upper case, as BouncyCastle does (`SHA256WITHRSA`), or gives no name the client recognizes but a known OID. `getChannelBindingData` returned an empty array for such certificates.

## [3.2] - 2025-09-16
### :lock: Security
- Fix Timing Attack Vulnerability in SCRAM Authentication

### :ghost: Maintenance
- Updated dependencies and maven plugins.
- Use `central-publishing-maven-plugin` to deploy to Maven Central.

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
[3.2]: https://github.com/ongres/scram/compare/3.1...3.2
[Unreleased]: https://github.com/ongres/scram/compare/3.2...main
