/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.client;

import static com.ongres.scram.common.util.Preconditions.checkNotEmpty;
import static com.ongres.scram.common.util.Preconditions.checkNotNull;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Base64;

import com.ongres.scram.common.ClientFirstMessage;
import com.ongres.scram.common.ScramFunctions;
import com.ongres.scram.common.ScramMechanism;
import com.ongres.scram.common.ServerFirstMessage;
import com.ongres.scram.common.StringPreparation;
import com.ongres.scram.common.exception.ScramParseException;
import org.jetbrains.annotations.NotNull;

/**
 * Process a received server-first-message. Generate by calling
 * {@link ScramClient#receiveServerFirstMessage(String)}.
 */
final class ServerFirstProcessor {
  private final ScramMechanism scramMechanism;
  private final StringPreparation stringPreparation;
  private final ClientFirstMessage clientFirstMessage;
  private final ServerFirstMessage serverFirstMessage;

  ServerFirstProcessor(ScramMechanism scramMechanism, StringPreparation stringPreparation,
      @NotNull String receivedServerFirstMessage, @NotNull String nonce,
      @NotNull ClientFirstMessage clientFirstMessage) throws ScramParseException {
    this.scramMechanism = scramMechanism;
    this.stringPreparation = stringPreparation;
    this.serverFirstMessage = ServerFirstMessage.parseFrom(receivedServerFirstMessage, nonce);
    this.clientFirstMessage = clientFirstMessage;
  }

  @NotNull
  ServerFirstMessage getServerFirstMessage() {
    return serverFirstMessage;
  }

  /**
   * Generates a {@code ClientFinalProcessor}, that allows to generate the client-final-message and
   * also receive and parse the server-first-message. It is based on the user's password.
   *
   * @param password The user's password
   * @return The handler
   * @throws IllegalArgumentException If the message is null or empty
   */
  ClientFinalProcessor clientFinalProcessor(char[] password) {
    return new ClientFinalProcessor(
        scramMechanism,
        stringPreparation,
        checkNotEmpty(password, "password"),
        Base64.getDecoder().decode(serverFirstMessage.getSalt().getBytes(UTF_8)),
        clientFirstMessage,
        serverFirstMessage);
  }

  /**
   * Generates a {@code ClientFinalProcessor}, that allows to generate the client-final-message and
   * also receive and parse the server-first-message. It is based on the clientKey and serverKey,
   * which, if available, provide an optimized path versus providing the original user's password.
   *
   * @param clientKey The client key, as per the SCRAM algorithm. It can be generated with:
   *          {@link ScramFunctions#clientKey(ScramMechanism, byte[])}
   * @param serverKey The server key, as per the SCRAM algorithm. It can be generated with:
   *          {@link ScramFunctions#serverKey(ScramMechanism, byte[])}
   * @return The handler
   * @throws IllegalArgumentException If the clientKey/serverKey is null
   */
  ClientFinalProcessor clientFinalProcessor(byte[] clientKey, byte[] serverKey) {
    return new ClientFinalProcessor(
        scramMechanism,
        checkNotNull(clientKey, "clientKey"),
        checkNotNull(serverKey, "serverKey"),
        clientFirstMessage,
        serverFirstMessage);
  }

  /**
   * Generates a {@code ClientFinalProcessor}, that allows to generate the client-final-message and
   * also receive and parse the server-first-message. It is based on the saltedPassword,
   * which, if available, provide an optimized path versus providing the original user's password.
   *
   * @param saltedPassword The salted password, as per the SCRAM algorithm. It can be generated
   *          with:
   *          {@link ScramFunctions#saltedPassword(ScramMechanism, StringPreparation, char[], byte[], int)}
   * @return The handler
   * @throws IllegalArgumentException If the saltedPassword is null
   */
  ClientFinalProcessor clientFinalProcessor(byte[] saltedPassword) {
    return new ClientFinalProcessor(
        scramMechanism,
        checkNotNull(saltedPassword, "saltedPassword"),
        clientFirstMessage,
        serverFirstMessage);
  }
}
