/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import static com.ongres.scram.common.util.Preconditions.castNonNull;
import static com.ongres.scram.common.util.Preconditions.checkNotEmpty;
import static com.ongres.scram.common.util.Preconditions.checkNotNull;
import static com.ongres.scram.common.util.Preconditions.gt0;

import com.ongres.scram.common.exception.ScramParseException;
import org.jetbrains.annotations.NotNull;

/**
 * Constructs and parses {@code server-first-messages}.
 *
 * <table>
 * <caption>Formal Syntax:</caption>
 * <tr>
 * <td>nonce</td>
 * <td>"r=" c-nonce [s-nonce]<br>
 * ;; Second part provided by server.</td>
 * </tr>
 * <tr>
 * <td>salt</td>
 * <td>"s=" base64</td>
 * </tr>
 * <tr>
 * <td>server-first-message</td>
 * <td>[reserved-mext ","] nonce "," salt ",<br>
 * "iteration-count ["," extensions]</td>
 * </tr>
 * </table>
 *
 * @implNote {@code extensions} are not supported.
 * @see <a href="https://tools.ietf.org/html/rfc5802#section-7">[RFC5802] Section 7</a>
 */
public final class ServerFirstMessage extends AbstractScramMessage {

  private final @NotNull String clientNonce;
  private final @NotNull String serverNonce;
  private final @NotNull String salt;
  private final int iterationCount;

  /**
   * Constructs a server-first-message from a client-first-message and the additional required data.
   *
   * <table>
   * <caption>Formal Syntax:</caption>
   * <tr>
   * <td>server-error</td>
   * <td>"e=" server-error-value</td>
   * </tr>
   * <tr>
   * <td>verifier</td>
   * <td>"v=" base64<br>
   * ;; base-64 encoded ServerSignature.</td>
   * </tr>
   * <tr>
   * <td>server-final-message</td>
   * <td>(server-error / verifier)<br>
   * ["," extensions]</td>
   * </tr>
   * </table>
   *
   * @param clientNonce The c-nonce used in client-first-message
   * @param serverNonce The s-nonce returned by the server
   * @param salt The salt
   * @param iterationCount The iteration count (must be positive)
   * @throws IllegalArgumentException If clientFirstMessage, serverNonce or salt are null or empty,
   *           or iteration &lt; 1
   */
  public ServerFirstMessage(@NotNull String clientNonce, @NotNull String serverNonce,
      @NotNull String salt, int iterationCount) {
    this.clientNonce = checkNotEmpty(clientNonce, "clientNonce");
    this.serverNonce = checkNotEmpty(serverNonce, "serverNonce");
    this.salt = checkNotNull(salt, "salt");
    this.iterationCount = gt0(iterationCount, "iterationCount");
  }

  /**
   * The client nonce.
   *
   * @return The client nonce
   */
  public @NotNull String getClientNonce() {
    return clientNonce;
  }

  /**
   * The server nonce.
   *
   * @return The server nonce
   */
  public @NotNull String getServerNonce() {
    return serverNonce;
  }

  /**
   * The concatenation of the client nonce and the server nonce: {@code c-nonce [s-nonce]}.
   *
   * @return The nonce
   */
  public @NotNull String getNonce() {
    return clientNonce + serverNonce;
  }

  /**
   * The salt in base64.
   *
   * @return The salt in base64.
   */
  public String getSalt() {
    return salt;
  }

  /**
   * The number of iterations.
   *
   * @return The number of iterations.
   */
  public int getIterationCount() {
    return iterationCount;
  }

  /**
   * Parses a server-first-message from a String.
   *
   * @param serverFirstMessage The string representing the server-first-message
   * @param clientNonce The clientNonce that is present in the client-first-message
   * @return The parsed instance
   * @throws ScramParseException If the argument is not a valid server-first-message
   * @throws IllegalArgumentException If either argument is empty or serverFirstMessage is not a
   *           valid message
   */
  public static @NotNull ServerFirstMessage parseFrom(@NotNull String serverFirstMessage,
      @NotNull String clientNonce) throws ScramParseException {
    checkNotEmpty(serverFirstMessage, "serverFirstMessage");
    checkNotEmpty(clientNonce, "clientNonce");

    String[] attributeValues = StringWritableCsv.parseFrom(serverFirstMessage, 3, 0);
    if (attributeValues.length != 3) {
      throw new ScramParseException("Invalid server-first-message");
    }

    ScramAttributeValue nonce = ScramAttributeValue.parse(castNonNull(attributeValues[0]));
    if (ScramAttributes.NONCE.getChar() != nonce.getChar()) {
      throw new ScramParseException(
          "nonce must be the 1st element of the server-first-message");
    }
    if (!nonce.getValue().startsWith(clientNonce)) {
      throw new ScramParseException("parsed nonce does not start with client nonce");
    }

    ScramAttributeValue salt = ScramAttributeValue.parse(castNonNull(attributeValues[1]));
    if (ScramAttributes.SALT.getChar() != salt.getChar()) {
      throw new ScramParseException("salt must be the 2nd element of the server-first-message");
    }

    ScramAttributeValue iteration = ScramAttributeValue.parse(castNonNull(attributeValues[2]));
    if (ScramAttributes.ITERATION.getChar() != iteration.getChar()) {
      throw new ScramParseException(
          "iteration must be the 3rd element of the server-first-message");
    }

    int iterationInt;
    try {
      iterationInt = Integer.parseInt(iteration.getValue());
    } catch (NumberFormatException ex) {
      throw new ScramParseException("invalid iteration", ex);
    }

    return new ServerFirstMessage(clientNonce, nonce.getValue().substring(clientNonce.length()),
        salt.getValue(), iterationInt);
  }

  @Override
  StringBuilder writeTo(StringBuilder sb) {
    return StringWritableCsv.writeTo(
        sb,
        new ScramAttributeValue(ScramAttributes.NONCE, getNonce()),
        new ScramAttributeValue(ScramAttributes.SALT, salt),
        new ScramAttributeValue(ScramAttributes.ITERATION, Integer.toString(iterationCount)));
  }

}
