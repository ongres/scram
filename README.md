# SCRAM Java Implementation

![Maven Central Version](https://img.shields.io/maven-central/v/com.ongres.scram/scram-aggregator)
[![Reproducible Builds](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/jvm-repo-rebuild/reproducible-central/master/content/com/ongres/scram/badge.json)](https://github.com/jvm-repo-rebuild/reproducible-central/blob/master/content/com/ongres/scram/README.md)
![GitHub License](https://img.shields.io/github/license/ongres/scram)

> Salted Challenge Response Authentication Mechanism (SCRAM)

## Overview

SCRAM (Salted Challenge Response Authentication Mechanism) is part of the family of
Simple Authentication and Security Layer
([SASL, RFC 4422](https://datatracker.ietf.org/doc/html/rfc4422)) authentication mechanisms. It is described as part of [RFC 5802](https://datatracker.ietf.org/doc/html/rfc5802) and
[RFC 7677](https://datatracker.ietf.org/doc/html/rfc7677).

This project provides a robust and well-tested implementation of the Salted Challenge
Response Authentication Mechanism (SCRAM) in Java. It adheres to the specifications
outlined in RFC 5802 and RFC 7677, ensuring secure user authentication.

This SCRAM Java implementation can be used for [PostgreSQL](https://www.postgresql.org) (which supports [SASL authentication](https://www.postgresql.org/docs/current/sasl-authentication.html) since PostgreSQL 10) through the [PostgreSQL JDBC Driver](https://jdbc.postgresql.org/) and others projects that connect from Java.

The code is licensed under the BSD "Simplified 2 Clause" license (see [LICENSE](LICENSE)).

## Key Features

* Clean-room Implementation: The code is written from scratch, offering a reliable and independent solution.
* Modular Structure: The library is designed for modularity, promoting reusability and maintainability.
* Client-Server Support: The implementation caters to both client and server-side SCRAM usage in the `scram-common` module. For the moment only the `scram-client` module is implemented.
* Multiple Hashing Algorithms: It supports `SHA-1` and `SHA-256` as described in the official RFC 5802 and RFC 7677 respectively, and also provides `SHA-224`, `SHA-384` and `SHA-512` for flexible security strength selection.
* Channel Binding support: The library supports client mechanism negotiation with support of channel binding data provided externally.
* Extensive Testing: The codebase is thoroughly tested to guarantee its functionality and correctness.
* Minimal Dependencies: The library operates with a single dependency based on the implementation of the [SASLprep](https://github.com/ongres/stringprep) required by the RFC 5802.

## How to use the SCRAM Client API

[![Maven Central](https://img.shields.io/badge/maven--central-scram_client-informational?style=for-the-badge&logo=apache-maven&logoColor=red)](https://maven-badges.herokuapp.com/maven-central/com.ongres.scram/scram-client)

Javadoc: [![Javadocs](http://javadoc.io/badge/com.ongres.scram/scram-client.svg?label=scram-client)](http://javadoc.io/doc/com.ongres.scram/scram-client)

### Example of use:
```java
byte[] cbindData = ...
ScramClient scramClient = ScramClient.builder()
    .advertisedMechanisms(Arrays.asList("SCRAM-SHA-256", "SCRAM-SHA-256-PLUS"))
    .username("user")
    .password("pencil".toCharArray())
    .channelBinding("tls-server-end-point", cbindData) // client supports channel binding
    .build();
  // The build() call negotiates the SCRAM mechanism to be used. In this example,
  // since the server advertise support for the SCRAM-SHA-256-PLUS mechanism,
  // and the builder is set with the channel binding type and data, the constructed
  // scramClient will use the "SCRAM-SHA-256-PLUS" mechanism for authentication.

// FE-> Send the client-first-message ("p=...,,n=...,r=...")
ClientFirstMessage clientFirstMsg = scramClient.clientFirstMessage();
...
// <-BE Receive the server-first-message
ServerFirstMessage serverFirstMsg = scramClient.serverFirstMessage("r=...,s=...,i=...");
...
// FE-> Send the client-final-message ("c=...,r=...,p=...")
ClientFinalMessage clientFinalMsg = scramClient.clientFinalMessage();
...
// <-BE Receive the server-final-message, throw an ScramException on error or invalid signature
ServerFinalMessage serverFinalMsg = scramClient.serverFinalMessage("v=...");
```

## Contributing

We welcome contributions to this project! Feel free to submit pull requests that improve the codebase, add features, or fix bugs. Please make sure your contributions adhere to coding style guidelines and include thorough testing.
Make sure to compile with `./mvnw verify -Pchecks` before submitting a PR.

By making a contribution to this project, you certify that you adhere to requirements of the [DCO](https://developercertificate.org/) by signing-off your commits (`git commit -s`).:
