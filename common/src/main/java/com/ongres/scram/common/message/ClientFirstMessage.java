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


package com.ongres.scram.common.message;


import com.ongres.scram.common.ScramAttributeValue;
import com.ongres.scram.common.ScramAttributes;
import com.ongres.scram.common.ScramStringFormatting;
import com.ongres.scram.common.gssapi.Gs2CbindFlag;
import com.ongres.scram.common.gssapi.Gs2Header;
import com.ongres.scram.common.util.CharAttributeValue;
import com.ongres.scram.common.util.StringWritable;
import com.ongres.scram.common.util.StringWritableCsv;

import java.util.Optional;

import static com.ongres.scram.common.util.Preconditions.checkNotEmpty;
import static com.ongres.scram.common.util.Preconditions.checkNotNull;


/**
 * Constructs and parses client-first-messages.
 * Message contains a {@link Gs2Header}, a username and a nonce. Formal syntax is:
 *
 * {@code
 * client-first-message-bare = [reserved-mext ","] username "," nonce ["," extensions]
   client-first-message = gs2-header client-first-message-bare
 * }
 *
 * Note that extensions are not supported.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5802#section-7">[RFC5802] Section 7</a>
 */
public class ClientFirstMessage implements StringWritable {
    private final Gs2Header gs2Header;
    private final CharAttributeValue user;
    private final CharAttributeValue nonce;

    private ClientFirstMessage(Gs2Header gs2Header, CharAttributeValue user, CharAttributeValue nonce) {
        this.gs2Header = gs2Header;
        this.user = user;
        this.nonce = nonce;
    }

    /**
     * Constructs the {@link ClientFirstMessage} with the provided options.
     */
    public static class Builder {
        private final Gs2CbindFlag gs2CbindFlag;
        private final String user;
        private final String nonce;

        private String authzid;
        private String cbindName;

        /**
         * Sets up the {@link ClientFirstMessage.Builder}
         * @param gs2CbindFlag Channel binding flag
         * @param user Username
         * @param nonce Nonce to be used for the client-first-message
         * @throws IllegalArgumentException If either argument is null or empty
         */
        public Builder(Gs2CbindFlag gs2CbindFlag, String user, String nonce) throws IllegalArgumentException {
            this.gs2CbindFlag = checkNotNull(gs2CbindFlag, "gs2CbindFlag");
            this.user = checkNotEmpty(user, "user");
            this.nonce = checkNotEmpty(nonce, "nonce");
        }

        /**
         * Adds authzid (alternate role) information to the message
         * @param authzid The authzid
         * @throws IllegalArgumentException If authzid is null or empty
         */
        public Builder authzid(String authzid) throws IllegalArgumentException {
            this.authzid = checkNotEmpty(authzid, "authzid");

            return this;
        }

        /**
         * Adds channel binding data to the message
         * @param cbindName The channel binding name
         * @throws IllegalArgumentException If the channel binding name is null or empty
         */
        public Builder channelBindingData(String cbindName) throws IllegalArgumentException {
            this.cbindName = checkNotEmpty(cbindName, "cbindName");

            return this;
        }

        /**
         * Returns the constructed instance
         * @return The client-first-message
         */
        public ClientFirstMessage get() {
            if(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED == gs2CbindFlag && null == cbindName) {
                throw new IllegalArgumentException("Channel binding name is required if channel binding is specified");
            }

            return new ClientFirstMessage(
                    new Gs2Header(gs2CbindFlag, cbindName, authzid),
                    new ScramAttributeValue(ScramAttributes.USERNAME, ScramStringFormatting.toSaslName(user)),
                    new ScramAttributeValue(ScramAttributes.NONCE, nonce)
            );
        }
    }

    public Gs2CbindFlag getChannelBindingFlag() {
        return gs2Header.getChannelBindingFlag();
    }

    public boolean isChannelBinding() {
        return gs2Header.getChannelBindingFlag() == Gs2CbindFlag.CHANNEL_BINDING_REQUIRED;
    }

    public Optional<String> getChannelBindingName() {
        return gs2Header.getChannelBindingName();
    }

    public Optional<String> getAuthzid() {
        return gs2Header.getAuthzid();
    }

    public String getUser() {
        return user.getValue();
    }

    public String getNonce() {
        return nonce.getValue();
    }

    @Override
    public StringBuffer writeTo(StringBuffer sb) {
        return StringWritableCsv.writeTo(sb, gs2Header, user, nonce);
    }

    /**
     * Construct a {@link ClientFirstMessage} instance from a message (String)
     * @param clientFirstMessage The String representing the client-first-message
     * @return The instance
     * @throws IllegalArgumentException If the message is null or empty
     */
    public static ClientFirstMessage parseFrom(String clientFirstMessage) throws IllegalArgumentException {
        checkNotEmpty(clientFirstMessage, "clientFirstMessage");

        Gs2Header gs2Header = Gs2Header.parseFrom(clientFirstMessage);  // Takes first two fields
        String[] userNonceString = StringWritableCsv.parseFrom(clientFirstMessage, 2, 2);

        ScramAttributeValue user = ScramAttributeValue.parse(userNonceString[0]);
        if(null == user || ScramAttributes.USERNAME.getChar() != user.getChar()) {
            throw new IllegalArgumentException("user must be the 3rd element of the client-first-message");
        }

        ScramAttributeValue nonce = ScramAttributeValue.parse(userNonceString[1]);
        if(null == nonce || ScramAttributes.NONCE.getChar() != nonce.getChar()) {
            throw new IllegalArgumentException("nonce must be the 4th element of the client-first-message");
        }

        return new ClientFirstMessage(gs2Header, user, nonce);
    }
}
