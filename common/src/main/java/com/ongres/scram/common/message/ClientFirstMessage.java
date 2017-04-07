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
     * Constructs a message with the specified channel binding flag;
     * a channel binding name and/or authizd if either is specified (not null); the username and the nonce length.
     * @param gs2CbindFlag The channel binding flag
     * @param cbind The channel binding name, if the channel binding flag was set to required
     * @param authzid An optional authzid (alternate authorization id)
     * @param user The username
     * @param nonce The nonce
     * @throws IllegalArgumentException If channel binding flag, user or nonce are null;
     *                                  or if channel binding is required and no channel binding name is provided
     */
    public ClientFirstMessage(
            Gs2CbindFlag gs2CbindFlag, String cbind, String authzid, String user, String nonce
    ) throws IllegalArgumentException {
        checkNotNull(gs2CbindFlag, "gs2CbindFlag");
        checkNotNull(user, "user");
        checkNotNull(nonce, "nonce");

        gs2Header = new Gs2Header(gs2CbindFlag, cbind, authzid);
        this.user = new ScramAttributeValue(ScramAttributes.USERNAME, ScramStringFormatting.toSaslName(user));
        this.nonce = new ScramAttributeValue(ScramAttributes.NONCE, nonce);
    }

    /**
     * Constructs a message with the specified username and the nonce length,
     * and the indicated non-channel binding mode: either client supported or not.
     * @param user The username
     * @param nonce The nonce
     * @throws IllegalArgumentException If user or nonce are null
     */
    public ClientFirstMessage(
            boolean clientChannelBinding, String user, String nonce
    ) throws IllegalArgumentException {
        this(
                clientChannelBinding ? Gs2CbindFlag.CLIENT_YES_SERVER_NOT : Gs2CbindFlag.CLIENT_NOT,
                null,
                null,
                user,
                nonce
        );
    }

    /**
     * Constructs a message with the specified username and the nonce length,
     * with no support (both client and server) for channel binding.
     * @param user The username
     * @param nonce The nonce
     * @throws IllegalArgumentException If user or nonce are null
     */
    public ClientFirstMessage(String user, String nonce) throws IllegalArgumentException {
        this(false, user, nonce);
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

    public static ClientFirstMessage parseFrom(String clientFirstMessage) {
        checkNotNull(clientFirstMessage, "clientFirstMessage");

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
