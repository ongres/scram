/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import static com.ongres.scram.common.util.Preconditions.castNonNull;
import static com.ongres.scram.common.util.Preconditions.checkNotEmpty;
import static com.ongres.scram.common.util.Preconditions.checkNotNull;

import com.ongres.scram.common.exception.ScramParseException;
import com.ongres.scram.common.exception.ServerErrorValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Constructs and parses {@code server-final-messages}.
 *
 * <table class="formal-syntax">
 * <caption>Formal Syntax:</caption>
 * <tr>
 * <td>server-error</td>
 * <td>"e=" server-error-value</td>
 * </tr>
 * <tr>
 * <td>server-error-value</td>
 * <td>"invalid-encoding" /<br>
 * "extensions-not-supported" / ; unrecognized 'm' value<br>
 * "invalid-proof" /<br>
 * "channel-bindings-dont-match" /<br>
 * "server-does-support-channel-binding" /<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;; server does not support channel binding<br>
 * "channel-binding-not-supported" /<br>
 * "unsupported-channel-binding-type" /<br>
 * "unknown-user" /<br>
 * "invalid-username-encoding" /<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;; invalid username encoding (invalid UTF-8 or<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;; SASLprep failed)<br>
 * "no-resources" /<br>
 * "other-error"<br><br>
 * ; Unrecognized errors should be treated as "other-error".<br>
 * ; In order to prevent information disclosure, the server<br>
 * ; may substitute the real reason with "other-error".</td>
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
 * @implNote {@code extensions} are not supported.
 * @see <a href="https://tools.ietf.org/html/rfc5802#section-7">[RFC5802] Section 7</a>
 */
public final class ServerFinalMessage extends AbstractScramMessage {

  private final byte @Nullable [] verifier;
  private final @Nullable String serverError;

  /**
   * Constructs a server-final-message with no errors, and the provided server verifier.
   *
   * @param verifier The bytes of the computed signature
   * @throws IllegalArgumentException If the verifier is null
   */
  public ServerFinalMessage(byte @NotNull [] verifier) {
    this.verifier = checkNotNull(verifier, "verifier");
    this.serverError = null;
  }

  /**
   * Constructs a server-final-message which represents a SCRAM error.
   *
   * @param serverError The error message
   * @throws IllegalArgumentException If the error is null
   */
  public ServerFinalMessage(@NotNull String serverError) {
    this.serverError = validateServerErrorType(serverError);
    this.verifier = null;
  }

  /**
   * Whether this server-final-message contains an error.
   *
   * @return True if it contains an error, false if it contains a verifier
   */
  public boolean isError() {
    return null != serverError;
  }

  /**
   * Get the verifier value from the "v=" server-final-message.
   *
   * @return the {@code verifier}
   */
  public byte @Nullable [] getVerifier() {
    return verifier != null ? checkNotNull(verifier, "verifier").clone() : null;
  }

  /**
   * Get the server-error-value from the "e=" server-final-message.
   *
   * @return the {@code server-error-value}
   */
  public @Nullable String getServerError() {
    return serverError;
  }

  /**
   * Parses a server-final-message from a String.
   *
   * @param serverFinalMessage The message
   * @return A constructed server-final-message instance
   * @throws ScramParseException If the argument is not a valid server-final-message
   * @throws IllegalArgumentException If the message is null or empty
   */
  public static @NotNull ServerFinalMessage parseFrom(@NotNull String serverFinalMessage)
      throws ScramParseException {
    checkNotEmpty(serverFinalMessage, "serverFinalMessage");

    @NotNull
    String @NotNull [] attributeValues = StringWritableCsv.parseFrom(serverFinalMessage, 1, 0);
    if (attributeValues.length != 1) {
      throw new ScramParseException("Invalid server-final-message");
    }

    ScramAttributeValue attributeValue = ScramAttributeValue.parse(attributeValues[0]);
    if (ScramAttributes.SERVER_SIGNATURE.getChar() == attributeValue.getChar()) {
      byte[] verifier = ScramStringFormatting.base64Decode(attributeValue.getValue());
      return new ServerFinalMessage(verifier);
    } else if (ScramAttributes.ERROR.getChar() == attributeValue.getChar()) {
      return new ServerFinalMessage(attributeValue.getValue());
    } else {
      throw new ScramParseException(
          "Invalid server-final-message: it must contain either a verifier or an error attribute");
    }
  }

  @Override
  StringBuilder writeTo(StringBuilder sb) {
    return StringWritableCsv.writeTo(
        sb,
        isError()
            ? new ScramAttributeValue(ScramAttributes.ERROR, castNonNull(serverError))
            : new ScramAttributeValue(ScramAttributes.SERVER_SIGNATURE,
                ScramStringFormatting.base64Encode(castNonNull(verifier))));
  }

  private static String validateServerErrorType(@NotNull String serverError) {
    checkNotNull(serverError, "serverError");
    if (ServerErrorValue.getErrorMessage(serverError) == null) {
      throw new IllegalArgumentException(
          "Invalid server-error-value '" + serverError + "'");
    }
    return serverError;
  }
}
