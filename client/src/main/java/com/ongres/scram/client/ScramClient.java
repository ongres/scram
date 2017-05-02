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

    public String clientFirstMessage(Gs2CbindFlag gs2CbindFlag, String cbindName, String authzid) {
        return setAndReturnClientFirstMessage(new ClientFirstMessage(gs2CbindFlag, authzid, cbindName, user, nonce));
    }

    public String clientFirstMessage() {
        return setAndReturnClientFirstMessage(new ClientFirstMessage(user, nonce));
    }

    public class ServerFirstHandler {
        private ServerFirstMessage serverFirstMessage;

        public ServerFirstHandler(String receivedServerFirstMessage) {
            serverFirstMessageString = receivedServerFirstMessage;
            serverFirstMessage = ServerFirstMessage.parseFrom(receivedServerFirstMessage, nonce);
        }

        public String getSalt() {
            return serverFirstMessage.getSalt();
        }

        public int getIteration() {
            return serverFirstMessage.getIteration();
        }

        public FinalMessagesHandler finalMessagesHandler(String password) {
            return new FinalMessagesHandler(serverFirstMessage.getNonce(), password, getSalt(), getIteration());
        }

        public FinalMessagesHandler finalMessagesHandler(byte[] clientKey, byte[] storedKey) {
            return new FinalMessagesHandler(serverFirstMessage.getNonce(), clientKey, storedKey);
        }
    }

    public class FinalMessagesHandler {
        private final String nonce;
        private final byte[] clientKey;
        private final byte[] storedKey;

        public FinalMessagesHandler(String nonce, byte[] clientKey, byte[] storedKey) {
            this.nonce = nonce;
            this.clientKey = clientKey;
            this.storedKey = storedKey;
        }

        public FinalMessagesHandler(String nonce, byte[] clientKey) {
            this(nonce, clientKey, ScramFunctions.storedKey(scramMechanism, clientKey));
        }

        public FinalMessagesHandler(String nonce, String password, String salt, int iteration) {
            this(
                    nonce,
                    ScramFunctions.clientKey(
                        scramMechanism, stringPreparation, password, Base64.getDecoder().decode(salt), iteration
                    )
            );
        }

        public String clientFinalMessage(Optional<String> cbindData) {
            ClientLastMessage clientLastMessage = new ClientLastMessage(
                    clientFirstMessage.getGs2Header(), cbindData, nonce
            );

            String authMessage = clientFirstMessage.writeToWithoutGs2Header(new StringBuffer())
                    .append(",")
                    .append(serverFirstMessageString)
                    .append(",")
                    .append(clientLastMessage.writeToWithoutProof(new StringBuffer()))
                    .toString();

            clientLastMessage.setProof(
                    ScramFunctions.clientProof(
                            clientKey,
                            ScramFunctions.clientSignature(scramMechanism, storedKey, authMessage)
                    )
            );

            return clientLastMessage.toString();
        }

        public String clientFinalMessage(String cbindData) {
            return clientFinalMessage(Optional.of(checkNotNull(cbindData, "cbindData")));
        }

        public String clientFinalMessage() {
            return clientFinalMessage(Optional.empty());
        }

        public boolean verifyServerSignature(String serverFinalMessage) {
            return false;
        }

        public Optional<String> serverErrorMessage() {
            return Optional.empty();
        }
    }

    public ServerFirstHandler receiveServerFirstMessage(String serverFirstMessage) {
        return new ServerFirstHandler(serverFirstMessage);
    }
}
