/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import static com.ongres.scram.common.util.Preconditions.checkNotNull;

import com.ongres.scram.common.exception.ScramParseException;

/**
 * SCRAM Attributes as defined in <a href="https://tools.ietf.org/html/rfc5802#section-5.1">Section
 * 5.1 of the RFC</a>.
 *
 * <p>Not all the available attributes may be available in this implementation.
 */
enum ScramAttributes implements CharSupplier {

  /**
   * This attribute specifies the name of the user whose password is used for authentication (a.k.a.
   * "authentication identity" [<a href="https://tools.ietf.org/html/rfc4422">RFC4422</a>]). If the
   * "a" attribute is not specified (which would normally be the case), this username is also the
   * identity that will be associated with the connection subsequent to authentication and
   * authorization.
   *
   * <p>The client SHOULD prepare the username using the "SASLprep" profile [<a
   * href="https://tools.ietf.org/html/rfc4013">RFC4013</a>] of the "stringprep" algorithm [<a
   * href="https://tools.ietf.org/html/rfc3454">RFC3454</a>] treating it as a query string (i.e.,
   * unassigned Unicode code points are allowed).
   *
   * <p>The characters ',' or '=' in usernames are sent as '=2C' and '=3D' respectively.
   */
  USERNAME('n'),

  /**
   * This is an optional attribute, and is part of the GS2 [<a
   * href="https://tools.ietf.org/html/rfc5801">RFC5801</a>] bridge between the GSS-API and SASL.
   * This attribute specifies an authorization identity. A client may include it in its first
   * message to the server if it wants to authenticate as one user, but subsequently act as a
   * different user. This is typically used by an administrator to perform some management task on
   * behalf of another user, or by a proxy in some situations.
   *
   * <p>If this attribute is omitted (as it normally would be), the authorization identity is
   * assumed to be derived from the username specified with the (required) "n" attribute.
   *
   * <p>The server always authenticates the user specified by the "n" attribute. If the "a"
   * attribute specifies a different user, the server associates that identity with the connection
   * after successful authentication and authorization checks.
   *
   * <p>The syntax of this field is the same as that of the "n" field with respect to quoting of '='
   * and ','.
   */
  AUTHZID('a'),

  /**
   * This attribute specifies a sequence of random printable ASCII characters excluding ',' (which
   * forms the nonce used as input to the hash function). No quoting is applied to this string.
   */
  NONCE('r'),

  /**
   * This REQUIRED attribute specifies the base64-encoded GS2 header and channel binding data. The
   * attribute data consist of: <ul> <li> the GS2 header from the client's first message (recall
   * that the GS2 header contains a channel binding flag and an optional authzid). This header is
   * going to include channel binding type prefix (see [<a
   * href="https://tools.ietf.org/html/rfc5056">RFC5056</a>]), if and only if the client is using
   * channel binding; </li> <li> followed by the external channel's channel binding data, if and
   * only if the client is using channel binding. </li> </ul>
   */
  CHANNEL_BINDING('c'),

  /**
   * This attribute specifies the base64-encoded salt used by the server for this user.
   */
  SALT('s'),

  /**
   * This attribute specifies an iteration count for the selected hash function and user.
   */
  ITERATION('i'),

  /**
   * This attribute specifies a base64-encoded ClientProof.
   */
  CLIENT_PROOF('p'),

  /**
   * This attribute specifies a base64-encoded ServerSignature.
   */
  SERVER_SIGNATURE('v'),

  /**
   * This attribute specifies an error that occurred during authentication exchange. Can help
   * diagnose the reason for the authentication exchange failure.
   */
  ERROR('e');

  private final char attributeChar;

  ScramAttributes(char attributeChar) {
    this.attributeChar = checkNotNull(attributeChar, "attributeChar");
  }

  @Override
  public char getChar() {
    return attributeChar;
  }

  /**
   * Find a SCRAMAttribute by its character.
   *
   * @param c The character.
   * @return The SCRAMAttribute that has that character.
   * @throws ScramParseException If no SCRAMAttribute has this character.
   */
  public static ScramAttributes byChar(char c) throws ScramParseException {
    switch (c) {
      case 'n':
        return USERNAME;
      case 'a':
        return AUTHZID;
      case 'r':
        return NONCE;
      case 'c':
        return CHANNEL_BINDING;
      case 's':
        return SALT;
      case 'i':
        return ITERATION;
      case 'p':
        return CLIENT_PROOF;
      case 'v':
        return SERVER_SIGNATURE;
      case 'e':
        return ERROR;
      default:
        throw new ScramParseException("Attribute with char '" + c + "' does not exist");
    }
  }
}
