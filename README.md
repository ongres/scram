# SCRAM Java Implementation


## Overview

SCRAM (Salted Challenge Response Authentication Mechanism) is part of the family
of Simple Authentication and Security Layer
([SASL, RFC 4422](https://tools.ietf.org/html/rfc4422)) authentication
mechanisms.

It is described as part of [RFC 5802](https://tools.ietf.org/html/rfc5802).

This project will serve for the basis of
[PostgreSQL's](https://www.postgresql.org) [JDBC](https://jdbc.postgresql.org/)
driver SCRAM support (due in PostgreSQL 10).

The code is BSD "Simplified 2 Clause" licensed (see [LICENSE](LICENSE)).


## Goals

This project aims to provide a complete clean-room implementation of SCRAM. It
is written in Java and provided in a modular, re-usable way, independent of
other software or programs.

Initial goals are:

* Provide both client and server APIs.
* Implement SHA-256
* No initial support for SASLPrep
* No initial support for channel-binding
* Well tested
* The fewer external dependencies, the better
