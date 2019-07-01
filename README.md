# SCRAM Java Implementation

## Overview

SCRAM (Salted Challenge Response Authentication Mechanism) is part of the family
of Simple Authentication and Security Layer
([SASL, RFC 4422](https://tools.ietf.org/html/rfc4422)) authentication
mechanisms.

It is described as part of [RFC 5802](https://tools.ietf.org/html/rfc5802) and
[RFC7677](https://tools.ietf.org/html/rfc7677).

This project will serve for the basis of
[PostgreSQL's](https://www.postgresql.org) [JDBC](https://jdbc.postgresql.org/)
driver SCRAM support (coming in PostgreSQL 10).

The code is licensed under the BSD "Simplified 2 Clause" license (see [LICENSE](LICENSE)).


## Goals

This project aims to provide a complete clean-room implementation of SCRAM. It
is written in Java and provided in a modular, re-usable way, independent of
other software or programs.

Current functionality includes:

* [Common infrastructure](common) for building both client and server SCRAM implementations.
* A [Client API](client) for using SCRAM as a client.
* Support for both SHA-1 and SHA-256.
* Basic support for channel binding.
* No runtime external dependencies.
* Well tested (+75 tests).


Current limitations:

* Server API and integration tests will be added soon.


## How to use the client API

Please read [Client's README.md](client).

Javadoc: [![Javadocs](http://javadoc.io/badge/com.ongres.scram/client.svg?label=client)](http://javadoc.io/doc/com.ongres.scram/client)



## Common API

'common' is the module that contains code common to both client and server SCRAM projects.
If you with to develop either a client or server API, you may very well build on top of this
API. Import maven dependency:

    <dependency>
        <groupId>com.ongres.scram</groupId>
        <artifactId>common</artifactId>
    </dependency>
    
and check the Javadoc: [![Javadocs](http://javadoc.io/badge/com.ongres.scram/common.svg)](http://javadoc.io/doc/com.ongres.scram/common)


## Contributing

Please submit [Merge Requests](https://gitlab.com/ongresinc/scram) for code contributions.
Make sure to compile with `mvn -Psafer` before submitting a MR.

Releases (on the master branch only) must be verified with:

    mvn -Psafer -Pmaster-branch
