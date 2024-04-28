/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.client;

import static com.ongres.scram.common.util.Preconditions.checkNotEmpty;
import static com.ongres.scram.common.util.Preconditions.checkNotNull;

import com.ongres.scram.common.ClientFinalMessage;
import com.ongres.scram.common.ClientFirstMessage;
import com.ongres.scram.common.ScramFunctions;
import com.ongres.scram.common.ScramMechanism;
import com.ongres.scram.common.ServerFinalMessage;
import com.ongres.scram.common.ServerFirstMessage;
import com.ongres.scram.common.StringPreparation;
import com.ongres.scram.common.exception.ScramInvalidServerSignatureException;
import com.ongres.scram.common.exception.ScramParseException;
import com.ongres.scram.common.exception.ScramServerErrorException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Processor that allows to generate the client-final-message, as well as process the
 * server-final-message and verify server's signature. Generate the processor by calling either
 * {@code ServerFirstProcessor#clientFinalProcessor(char[])} or
 * {@code ServerFirstProcessor#clientFinalProcessor(byte[], byte[])}.
 */
final class ClientFinalProcessor {
  private final byte[] clientKey;
  private final byte[] storedKey;
  private final byte[] serverKey;

  private final ScramMechanism scramMechanism;

  private final ClientFirstMessage clientFirstMessage;

  private final ServerFirstMessage serverFirstMessage;
  private String authMessage;

  private ClientFinalProcessor(ScramMechanism scramMechanism, byte[] clientKey,
      byte[] storedKey, byte[] serverKey, ClientFirstMessage clientFirstMessage,
      ServerFirstMessage serverFirstMessage) {
    this.scramMechanism = scramMechanism;
    this.clientKey = checkNotNull(clientKey, "clientKey");
    this.storedKey = checkNotNull(storedKey, "storedKey");
    this.serverKey = checkNotNull(serverKey, "serverKey");
    this.clientFirstMessage = clientFirstMessage;
    this.serverFirstMessage = serverFirstMessage;
  }

  ClientFinalProcessor(ScramMechanism scramMechanism, byte[] clientKey, byte[] serverKey,
      ClientFirstMessage clientFirstMessage, ServerFirstMessage serverFirstMessage) {
    this(scramMechanism, clientKey, ScramFunctions.storedKey(scramMechanism, clientKey),
        serverKey, clientFirstMessage, serverFirstMessage);
  }

  ClientFinalProcessor(ScramMechanism scramMechanism, byte[] saltedPassword,
      ClientFirstMessage clientFirstMessage, ServerFirstMessage serverFirstMessage) {
    this(
        scramMechanism,
        ScramFunctions.clientKey(scramMechanism, saltedPassword),
        ScramFunctions.serverKey(scramMechanism, saltedPassword),
        clientFirstMessage, serverFirstMessage);
  }

  ClientFinalProcessor(ScramMechanism scramMechanism, StringPreparation stringPreparation,
      char[] password, byte[] salt, ClientFirstMessage clientFirstMessage,
      ServerFirstMessage serverFirstMessage) {
    this(scramMechanism,
        ScramFunctions.saltedPassword(scramMechanism, stringPreparation, password, salt,
            serverFirstMessage.getIterationCount()),
        clientFirstMessage, serverFirstMessage);
  }

  private void generateAndCacheAuthMessage(byte[] cbindData) {
    if (null == this.authMessage) {
      this.authMessage = ScramFunctions.authMessage(clientFirstMessage, serverFirstMessage, cbindData);
    }
  }

  /**
   * Generates the SCRAM representation of the client-final-message, including the given
   * channel-binding data.
   *
   * @param cbindData The bytes of the channel-binding data
   * @return The message
   */
  @NotNull
  ClientFinalMessage clientFinalMessage(byte @Nullable [] cbindData) {
    generateAndCacheAuthMessage(cbindData);

    return new ClientFinalMessage(
        clientFirstMessage.getGs2Header(),
        cbindData,
        serverFirstMessage.getNonce(),
        ScramFunctions.clientProof(
            clientKey,
            ScramFunctions.clientSignature(scramMechanism, storedKey, authMessage)));
  }

  /**
   * Receive and process the server-final-message. Server SCRAM signatures is verified.
   *
   * @param serverFinalMessage The received server-final-message
   * @throws ScramParseException If the message is not a valid server-final-message
   * @throws ScramServerErrorException If the server-final-message contained an error
   * @throws ScramInvalidServerSignatureException If the server signature is invalid
   * @throws IllegalArgumentException If the message is null or empty
   */
  @NotNull
  ServerFinalMessage receiveServerFinalMessage(@NotNull String serverFinalMessage)
      throws ScramParseException, ScramServerErrorException, ScramInvalidServerSignatureException {
    checkNotEmpty(serverFinalMessage, "serverFinalMessage");

    ServerFinalMessage message = ServerFinalMessage.parseFrom(serverFinalMessage);
    if (message.isError()) {
      throw new ScramServerErrorException(message.getServerError());
    }
    if (!ScramFunctions.verifyServerSignature(
        scramMechanism, serverKey, authMessage, message.getVerifier())) {
      throw new ScramInvalidServerSignatureException("Invalid SCRAM server signature");
    }
    return message;
  }
}
