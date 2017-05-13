/*
 * Copyright 2017, OnGres.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */


package com.ongres.scram.client;


import com.ongres.scram.common.ScramFunctions;
import com.ongres.scram.common.ScramMechanism;
import com.ongres.scram.common.gssapi.Gs2CbindFlag;
import com.ongres.scram.common.message.ClientFirstMessage;
import com.ongres.scram.common.message.ClientLastMessage;
import com.ongres.scram.common.message.ServerFirstMessage;
import com.ongres.scram.common.stringprep.StringPreparation;

import java.util.Base64;
import java.util.Optional;

import static com.ongres.scram.common.util.Preconditions.checkNotEmpty;
import static com.ongres.scram.common.util.Preconditions.checkNotNull;


/**
 * A class that represents a SCRAM client. Use this class to perform a SCRAM negotiation with a SCRAM server.
 * This class performs an authentication execution for a given user, and has state related to it.
 * Thus, it cannot be shared across users or authentication executions.
 */
public class ScramClient {
    private final ScramMechanism scramMechanism;
    private final StringPreparation stringPreparation;
    private final String user;
    private final String nonce;
    private ClientFirstMessage clientFirstMessage;
    private String serverFirstMessageString;

    /**
     * Constructs a SCRAM client, to perform an authentication for a given user.
     * This class can be instantiated directly,
     * but it is recommended that a {@link ScramClientGenerator} is used instead.
     * @param scramMechanism The SCRAM mechanism that will be using this client
     * @param stringPreparation
     * @param user
     * @param nonce
     */
    public ScramClient(ScramMechanism scramMechanism, StringPreparation stringPreparation, String user, String nonce) {
        this.scramMechanism = checkNotNull(scramMechanism, "scramMechanism");
        this.stringPreparation = checkNotNull(stringPreparation, "stringPreparation");
        this.user = checkNotEmpty(user, "user");
        this.nonce = checkNotEmpty(nonce, "nonce");
    }

    private String setAndReturnClientFirstMessage(ClientFirstMessage clientFirstMessage) {
        this.clientFirstMessage = clientFirstMessage;

        return clientFirstMessage.toString();
    }

    /**
     * Returns the text representation of a SCRAM client-first-message, with the GSS-API header values indicated.
     * @param gs2CbindFlag The channel binding flag
     * @param cbindName The channel binding algorithm name, if channel binding is supported, or null
     * @param authzid The optional
     * @return The message
     */
    public String clientFirstMessage(Gs2CbindFlag gs2CbindFlag, String cbindName, String authzid) {
        return setAndReturnClientFirstMessage(new ClientFirstMessage(gs2CbindFlag, authzid, cbindName, user, nonce));
    }

    /**
     * Returns the text representation of a SCRAM client-first-message, with no channel binding nor authzid.
     * @return The message
     */
    public String clientFirstMessage() {
        return setAndReturnClientFirstMessage(new ClientFirstMessage(user, nonce));
    }

    /**
     * Handles a received server-first-message.
     * Generate by calling {@link #receiveServerFirstMessage(String)}.
     */
    public class ServerFirstHandler {
        private final ServerFirstMessage serverFirstMessage;

        private ServerFirstHandler(String receivedServerFirstMessage) throws IllegalArgumentException {
            serverFirstMessageString = checkNotEmpty(receivedServerFirstMessage, "receivedServerFirstMessage");
            serverFirstMessage = ServerFirstMessage.parseFrom(receivedServerFirstMessage, nonce);
        }

        public String getSalt() {
            return serverFirstMessage.getSalt();
        }

        public int getIteration() {
            return serverFirstMessage.getIteration();
        }

        /**
         * Generates a {@link FinalMessagesHandler}, that allows to generate the client-last-message and also
         * receive and parse the server-first-message. It is based on the user's password.
         * @param password The user's password
         * @return The handler
         * @throws IllegalArgumentException If the message is null or empty
         */
        public FinalMessagesHandler finalMessagesHandler(String password) throws IllegalArgumentException {
            return new FinalMessagesHandler(
                    serverFirstMessage.getNonce(),
                    checkNotEmpty(password, "password"),
                    getSalt(),
                    getIteration()
            );
        }

        /**
         * Generates a {@link FinalMessagesHandler}, that allows to generate the client-last-message and also
         * receive and parse the server-first-message. It is based on the clientKey and storedKey,
         * which, if available, provide an optimized path versus providing the original user's password.
         * @param clientKey The client key, as per the SCRAM algorithm.
         *                  It can be generated with:
         *                  {@link ScramFunctions#clientKey(ScramMechanism, StringPreparation, String, byte[], int)}
         * @param storedKey The stored key, as per the SCRAM algorithm.
         *                  It can be generated from the client key with:
         *                  {@link ScramFunctions#storedKey(ScramMechanism, byte[])}
         * @return The handler
         * @throws IllegalArgumentException If the message is null or empty
         */
        public FinalMessagesHandler finalMessagesHandler(byte[] clientKey, byte[] storedKey)
        throws IllegalArgumentException {
            return new FinalMessagesHandler(
                    serverFirstMessage.getNonce(),
                    checkNotNull(clientKey, "clientKey"),
                    checkNotNull(storedKey, "storedKey")
            );
        }
    }

    /**
     * Handler that allows to generate the client-last-message,
     * as well as process the server-last-message and verify server's signature.
     * Generate the handler by calling either {@link ServerFirstHandler#finalMessagesHandler(String)}
     * or {@link ServerFirstHandler#finalMessagesHandler(byte[], byte[])}.
     */
    public class FinalMessagesHandler {
        private final String nonce;
        private final byte[] clientKey;
        private final byte[] storedKey;

        private FinalMessagesHandler(String nonce, byte[] clientKey, byte[] storedKey) throws IllegalArgumentException {
            this.nonce = nonce;
            this.clientKey = checkNotNull(clientKey, "clientKey");
            this.storedKey = checkNotNull(storedKey, "storedKey");
        }

        private FinalMessagesHandler(String nonce, byte[] clientKey) {
            this(nonce, clientKey, ScramFunctions.storedKey(scramMechanism, clientKey));
        }

        private FinalMessagesHandler(String nonce, String password, String salt, int iteration) {
            this(
                    nonce,
                    ScramFunctions.clientKey(
                        scramMechanism, stringPreparation, password, Base64.getDecoder().decode(salt), iteration
                    )
            );
        }

        private String clientLastMessage(Optional<byte[]> cbindData) {
            String authMessage = clientFirstMessage.writeToWithoutGs2Header(new StringBuffer())
                    .append(",")
                    .append(serverFirstMessageString)
                    .append(",")
                    .append(ClientLastMessage.writeToWithoutProof(clientFirstMessage.getGs2Header(), cbindData, nonce))
                    .toString();

            ClientLastMessage clientLastMessage = new ClientLastMessage(
                    clientFirstMessage.getGs2Header(),
                    cbindData,
                    nonce,
                    ScramFunctions.clientProof(
                            clientKey,
                            ScramFunctions.clientSignature(scramMechanism, storedKey, authMessage)
                    )
            );

            return clientLastMessage.toString();
        }

        /**
         * Generates the SCRAM representation of the client-last-message, including the given channel-binding data.
         * @param cbindData The bytes of the channel-binding data
         * @return The message
         * @throws IllegalArgumentException If the channel binding data is null
         */
        public String clientLastMessage(byte[] cbindData) throws IllegalArgumentException {
            return clientLastMessage(Optional.of(checkNotNull(cbindData, "cbindData")));
        }

        /**
         * Generates the SCRAM representation of the client-last-message.
         * @return The message
         */
        public String clientLastMessage() {
            return clientLastMessage(Optional.empty());
        }

        /**
         * Parses the server-last-message and verifies the signature.
         * @param serverLastMessage The received message
         * @return True if the signature is correct, false otherwise
         * @throws IllegalArgumentException If the message is null or empty
         */
        public boolean verifyServerSignature(String serverLastMessage) {
            checkNotEmpty(serverLastMessage, "serverLastMessage");

            // TODO: implement when the server-last-message object is implemented in scram-common

            return false;
        }

        /**
         * Returns the server-generate error message if there was any error in the SCRAM process.
         * @return An optionally filled-in error message String.
         */
        public Optional<String> serverErrorMessage() {
            // TODO: implement when the server-last-message object is implemented in scram-common

            return Optional.empty();
        }
    }

    /**
     * Constructs a handler for the server-first-message, from its String representation.
     * @param serverFirstMessage The message
     * @return The handler
     * @throws IllegalArgumentException If the message is null or empty
     */
    public ServerFirstHandler receiveServerFirstMessage(String serverFirstMessage) throws IllegalArgumentException {
        return new ServerFirstHandler(checkNotEmpty(serverFirstMessage, "serverFirstMessage"));
    }
}
