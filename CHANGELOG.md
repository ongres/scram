# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

## [3.4] - 2026-06-10

### :bug: Bug Fixes

- Resolve regression when passing pre-computed keys `.clientAndServerKey(clientKey, serverKey)`.

### :rocket: New features

- Introduces a static OID-to-Digest mapping table for `TlsServerEndpoint` to guarantee resolution when friendly algorithm names are unavailable or provider-specific.

## [3.3] - 2026-06-04

### :lock: Security

- Prevent silent downgrade attacks during channel binding negotiation via unsupported certificate algorithms. [GHSA-p9jg-fcr6-3mhf](https://github.com/ongres/scram/security/advisories/GHSA-p9jg-fcr6-3mhf)
- Harden memory security by explicitly zeroing out highly sensitive cryptographic keys (`saltedPassword`, `clientKey`, and `serverKey`) immediately following the client final message exchange to prevent lingering material in heap memory.

### :rocket: New features

- Implement an interrupt-aware implementation of the PBKDF2 'hi' function introducing `ScramInterruptedException`, utilizing a stride-based check to allow long-running cryptographic operations to safely abort without blocking thread shutdown.
- Introduce `.channelBindingPolicy()` to the client builder to explicitly configure `DISABLE`, `ALLOW` (default), and `REQUIRE` enforcement modes.
- Introduce `MechanismNegotiationException` and `ChannelBindingException` runtime exception hierarchy to provide granular, precise failure types for driver integration loops instead of relying on generic `IllegalArgumentException` throws.
- Add support for `RSASSA-PSS` server certificate signature extraction to ensure modern cryptographic algorithms are supported during `tls-server-end-point` channel binding computations.
- Add support for `SCRAM-SHA3-512` and `SCRAM-SHA3-512-PLUS` SASL mechanisms to provide modern NIST SHA-3 hashing standards with higher cryptographic resilience against length-extension attacks (supported on modern JVMs only).

### :building_construction: Improvements

- Update the `saslprep` dependency to 2.4.
- Updated internal Maven plugins and project dependencies to their latest stable versions.

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
[3.3]: https://github.com/ongres/scram/compare/3.2...3.3
[3.4]: https://github.com/ongres/scram/compare/3.4...3.4
[Unreleased]: https://github.com/ongres/scram/compare/3.4...main
