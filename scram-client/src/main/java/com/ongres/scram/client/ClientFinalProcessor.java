/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.client;

import static com.ongres.scram.common.util.Preconditions.checkNotEmpty;
import static com.ongres.scram.common.util.Preconditions.checkNotNull;

import java.util.Arrays;

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

  /**
   * Primary constructor utilizing pre-computed cryptographic keys.
   */
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

  /**
   * Constructs a processor using pre-computed Client and Server keys.
   * The Stored Key is automatically derived.
   *
   * @param scramMechanism The SCRAM mechanism.
   * @param clientKey The pre-computed client key material.
   * @param serverKey The pre-computed server key material.
   * @param clientFirstMessage The client-first-message contextual state.
   * @param serverFirstMessage The server-first-message contextual state.
   */
  ClientFinalProcessor(ScramMechanism scramMechanism, byte[] clientKey, byte[] serverKey,
      ClientFirstMessage clientFirstMessage, ServerFirstMessage serverFirstMessage) {
    this(scramMechanism, clientKey.clone(), ScramFunctions.storedKey(scramMechanism, clientKey),
        serverKey.clone(), clientFirstMessage, serverFirstMessage);
  }

  /**
   * Constructs a processor using a pre-computed salted password.
   * Client, Server, and Stored keys are derived directly from the salted password.
   *
   * @param scramMechanism The SCRAM mechanism.
   * @param saltedPassword The pre-computed salted password material.
   * @param clientFirstMessage The client-first-message contextual state.
   * @param serverFirstMessage The server-first-message contextual state.
   */
  ClientFinalProcessor(ScramMechanism scramMechanism, byte[] saltedPassword,
      ClientFirstMessage clientFirstMessage, ServerFirstMessage serverFirstMessage) {
    this(
        scramMechanism,
        ScramFunctions.clientKey(scramMechanism, saltedPassword),
        ScramFunctions.serverKey(scramMechanism, saltedPassword),
        clientFirstMessage, serverFirstMessage);
  }

  /**
   * Constructs a processor from raw credentials, performing PBKDF2 salt derivation.
   *
   * @param scramMechanism The SCRAM mechanism.
   * @param stringPreparation The SASLprep normalization configuration rules.
   * @param password The cleartext password array.
   * @param salt The salt bytes received from the server.
   * @param clientFirstMessage The client-first-message contextual state.
   * @param serverFirstMessage The server-first-message contextual state.
   */
  ClientFinalProcessor(ScramMechanism scramMechanism, StringPreparation stringPreparation,
      char[] password, byte[] salt, ClientFirstMessage clientFirstMessage,
      ServerFirstMessage serverFirstMessage) {
    this.scramMechanism = scramMechanism;
    this.clientFirstMessage = clientFirstMessage;
    this.serverFirstMessage = serverFirstMessage;

    byte[] saltedPassword = ScramFunctions.saltedPassword(
        scramMechanism, stringPreparation, password, salt, serverFirstMessage.getIterationCount());
    try {
      this.clientKey = ScramFunctions.clientKey(scramMechanism, saltedPassword);
      this.serverKey = ScramFunctions.serverKey(scramMechanism, saltedPassword);
      this.storedKey = ScramFunctions.storedKey(scramMechanism, this.clientKey);
    } finally {
      // Wipe saltedPassword immediately
      Arrays.fill(saltedPassword, (byte) 0);
    }
  }

  /**
   * Generates and caches the authMessage metadata string required for signing logic if not already present.
   *
   * @param cbindData The channel binding payload data bytes.
   */
  private void generateAndCacheAuthMessage(byte[] cbindData) {
    if (null == this.authMessage) {
      this.authMessage = ScramFunctions.authMessage(clientFirstMessage, serverFirstMessage, cbindData);
    }
  }

  /**
   * Generates the SCRAM representation of the client-final-message, including the given
   * channel-binding data.
   *
   * @param cbindData The bytes of the channel-binding data, or null if no channel binding is utilized.
   * @return The constructed ClientFinalMessage object.
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
   * Receive and process the server-final-message. The server's signature is explicitly verified.
   *
   * @param serverFinalMessage The received raw server-final-message text line string.
   * @return The parsed and validated ServerFinalMessage instance representation.
   * @throws ScramParseException If the string composition fails structure parsing rules.
   * @throws ScramServerErrorException If the server-final-message contains an error attribute message.
   * @throws ScramInvalidServerSignatureException If the verified computed signature fails validation matches.
   * @throws IllegalArgumentException If the input parameter message value evaluates null or empty.
   */
  @NotNull
  ServerFinalMessage receiveServerFinalMessage(@NotNull String serverFinalMessage)
      throws ScramParseException, ScramServerErrorException, ScramInvalidServerSignatureException {
    checkNotEmpty(serverFinalMessage, "serverFinalMessage");

    try {
      ServerFinalMessage message = ServerFinalMessage.parseFrom(serverFinalMessage);
      if (message.isError()) {
        throw new ScramServerErrorException(message.getServerError());
      }
      if (!ScramFunctions.verifyServerSignature(
          scramMechanism, serverKey, authMessage, message.getVerifier())) {
        throw new ScramInvalidServerSignatureException("Invalid SCRAM server signature");
      }
      return message;
    } finally {
      // Wipe the sensitive data, even if an exception was thrown above
      Arrays.fill(clientKey, (byte) 0);
      Arrays.fill(storedKey, (byte) 0);
      Arrays.fill(serverKey, (byte) 0);
    }
  }

}
