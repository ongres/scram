/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import static com.ongres.scram.common.util.Preconditions.checkNotEmpty;
import static com.ongres.scram.common.util.Preconditions.checkNotNull;

import com.ongres.scram.common.exception.ScramParseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Constructs and parses client-first-messages. Message contains a {@code gs2-header}, a username
 * and a
 * nonce.
 *
 * <table>
 * <caption>Formal Syntax:</caption>
 * <tr>
 * <td>client-first-message-bare</td>
 * <td>[reserved-mext ","] username "," nonce ["," extensions]</td>
 * </tr>
 * <tr>
 * <td>client-first-message</td>
 * <td>gs2-header client-first-message-bare</td>
 * </tr>
 * </table>
 *
 * @implNote {@code extensions} are not supported.
 * @see <a href="https://tools.ietf.org/html/rfc5802#section-7">[RFC5802] Section 7</a>
 */
public final class ClientFirstMessage extends AbstractScramMessage {

  /**
   * gs2-header = gs2-cbind-flag "," [ authzid ] ",".
   */
  private final @NotNull Gs2Header gs2Header;

  /**
   * username = "n=" saslname.
   */
  private final @NotNull String username;

  /**
   * nonce= "r=" c-nonce [s-nonce].
   */
  private final @NotNull String clientNonce;

  /**
   * Constructs a client-first-message for the given user, nonce and gs2Header. This constructor is
   * intended to be instantiated by a scram client, and not directly. The client should be providing
   * the header, and nonce (and probably the user too).
   *
   * @param gs2Header The GSS-API header
   * @param username The SCRAM username
   * @param clientNonce The nonce for this session
   * @throws IllegalArgumentException If any of the arguments is null or empty
   */
  public ClientFirstMessage(@NotNull Gs2Header gs2Header, @NotNull String username,
      @NotNull String clientNonce) {
    this.gs2Header = checkNotNull(gs2Header, "gs2Header");
    this.username = ScramStringFormatting.toSaslName(checkNotEmpty(username, "username"));
    this.clientNonce = checkNotEmpty(clientNonce, "clientNonce");
  }

  /**
   * Constructs a client-first-message for the given parameters. Under normal operation, this
   * constructor is intended to be instantiated by a scram client, and not directly. However, this
   * constructor is more user- or test-friendly, as the arguments are easier to provide without
   * building other indirect object parameters.
   *
   * @param gs2CbindFlag The channel-binding flag
   * @param authzid The optional authzid
   * @param cbindName The optional channel binding name
   * @param username The SCRAM user
   * @param clientNonce The nonce for this session
   * @throws IllegalArgumentException If the flag, user or nonce are null or empty
   */
  public ClientFirstMessage(@NotNull Gs2CbindFlag gs2CbindFlag, @Nullable String cbindName,
      @Nullable String authzid, @NotNull String username, @NotNull String clientNonce) {
    this(new Gs2Header(gs2CbindFlag, cbindName, authzid), username, clientNonce);
  }

  /**
   * Constructs a client-first-message for the given parameters, with no channel binding nor
   * authzid. Under normal operation, this constructor is intended to be instantiated by a scram
   * client, and not directly. However, this constructor is more user- or test-friendly, as the
   * arguments are easier to provide without building other indirect object parameters.
   *
   * @param username The SCRAM user
   * @param clientNonce The nonce for this session
   * @throws IllegalArgumentException If the user or nonce are null or empty
   */
  public ClientFirstMessage(@NotNull String username, @NotNull String clientNonce) {
    this(new Gs2Header(Gs2CbindFlag.CLIENT_NOT), username, clientNonce);
  }

  /**
   * Check to probe if gs2-cbind-flag is set to "p=".
   *
   * @return true if the message requires channel binding
   */
  public boolean isChannelBindingRequired() {
    return gs2Header.getChannelBindingFlag() == Gs2CbindFlag.CHANNEL_BINDING_REQUIRED;
  }

  /**
   * Return the Gs2Header.
   *
   * @return the {@code gs2-header}
   */
  public @NotNull Gs2Header getGs2Header() {
    return gs2Header;
  }

  /**
   * Return the username.
   *
   * @return the {@code "n=" saslname}
   */
  public @NotNull String getUsername() {
    return username;
  }

  /**
   * Return the client nonce.
   *
   * @return the {@code c-nonce}
   */
  public @NotNull String getClientNonce() {
    return clientNonce;
  }

  /**
   * Limited version of the StringWritableCsv method, that doesn't write the GS2 header. This method
   * is useful to construct the auth message used as part of the SCRAM algorithm.
   *
   * @param sb A StringBuffer where to write the data to.
   * @return The same StringBuffer
   */
  @NotNull
  StringBuilder clientFirstMessageBare(@NotNull StringBuilder sb) {
    return StringWritableCsv.writeTo(
        sb,
        new ScramAttributeValue(ScramAttributes.USERNAME, username),
        new ScramAttributeValue(ScramAttributes.NONCE, clientNonce));
  }

  /**
   * Construct a {@link ClientFirstMessage} instance from a message (String).
   *
   * @param clientFirstMessage The String representing the client-first-message
   * @return The instance
   * @throws ScramParseException If the message is not a valid client-first-message
   * @throws IllegalArgumentException If the message is null or empty
   */
  @NotNull
  public static ClientFirstMessage parseFrom(@NotNull String clientFirstMessage)
      throws ScramParseException {
    checkNotEmpty(clientFirstMessage, "clientFirstMessage");

    @NotNull
    String @NotNull [] userNonceString;
    try {
      userNonceString = StringWritableCsv.parseFrom(clientFirstMessage, 2, 2);
    } catch (IllegalArgumentException e) {
      throw new ScramParseException("Illegal series of attributes in client-first-message", e);
    }

    ScramAttributeValue user = ScramAttributeValue.parse(userNonceString[0]);
    if (ScramAttributes.USERNAME.getChar() != user.getChar()) {
      throw new ScramParseException("user must be the 3rd element of the client-first-message");
    }

    ScramAttributeValue nonce = ScramAttributeValue.parse(userNonceString[1]);
    if (ScramAttributes.NONCE.getChar() != nonce.getChar()) {
      throw new ScramParseException("nonce must be the 4th element of the client-first-message");
    }

    Gs2Header gs2Header = Gs2Header.parseFrom(clientFirstMessage); // Takes first two fields
    return new ClientFirstMessage(gs2Header, user.getValue(), nonce.getValue());
  }

  @Override
  StringBuilder writeTo(StringBuilder sb) {
    StringWritableCsv.writeTo(
        sb,
        gs2Header,
        null // This marks the position of the rest of the elements, required for the ","
    );

    return clientFirstMessageBare(sb);
  }
}
