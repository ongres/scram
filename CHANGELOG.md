# Changelog
All notable changes to this project will be documented in this file.

## [Unreleased]

## 3.0 - 2024-04-03
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

# 2.1

* Updated saslprep to version 1.1 to remove a build dependency coming from stringprep module

# 2.0

* Out of beta testing

# 2.0-beta3

* Fixed licenses issues

# 2.0-beta2

* Added saslprep tests

# 2.0-beta1

* Add new dependency StringPrep

# 1.9-beta1

* API change to be compatible with Java 7
* Added standard SASLPrep
* Failover to bouncy castle implementation of PBKDF2WithHmacSHA256 to support Oracle JDK 7

# 1.0.0-beta.2

* Fix maven issue and javadoc

# 1.0.0-beta.1

* First version

