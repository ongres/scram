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


import com.ongres.scram.common.SCRAMAttributeValue;
import com.ongres.scram.common.SCRAMAttributes;
import com.ongres.scram.common.SCRAMStringFormatting;
import com.ongres.scram.common.gssapi.GS2CbindFlag;
import com.ongres.scram.common.gssapi.GS2Header;
import com.ongres.scram.common.util.CharAttributeValue;
import com.ongres.scram.common.util.CryptoUtil;
import com.ongres.scram.common.util.StringWritable;
import com.ongres.scram.common.util.StringWritableCSV;

import java.util.Optional;

import static com.ongres.scram.common.util.Preconditions.checkNotNull;
import static com.ongres.scram.common.util.Preconditions.gt0;


/**
 * Constructs and parses client-first-messages.
 * Message contains a {@link GS2Header}, a username and a nonce. Formal syntax is:
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
    private final GS2Header gs2Header;
    private final CharAttributeValue user;
    private final CharAttributeValue nonce;

    private ClientFirstMessage(GS2Header gs2Header, CharAttributeValue user, CharAttributeValue nonce) {
        this.gs2Header = gs2Header;
        this.user = user;
        this.nonce = nonce;
    }

    @FunctionalInterface
    protected interface NonceProvider {
        String getNonce(int length);
    }

    protected ClientFirstMessage(
            GS2CbindFlag gs2CbindFlag, String cbind, String authzid, String user, int nonceLength,
            NonceProvider nonceProvider
    ) throws IllegalArgumentException {
        checkNotNull(gs2CbindFlag, "gs2CbindFlag");
        checkNotNull(user, "user");
        gt0(nonceLength, "nonceLength");

        gs2Header = new GS2Header(gs2CbindFlag, cbind, authzid);
        this.user = new SCRAMAttributeValue(SCRAMAttributes.USERNAME, SCRAMStringFormatting.toSaslName(user));
        nonce = new SCRAMAttributeValue(SCRAMAttributes.NONCE, nonceProvider.getNonce(nonceLength));
    }

    /**
     * Constructs a message with the specified channel binding flag;
     * a channel binding name and/or authizd if either is specified (not null); the username and the nonce length.
     * @param gs2CbindFlag The channel binding flag
     * @param cbind The channel binding name, if the channel binding flag was set to required
     * @param authzid An optional authzid (alternate authorization id)
     * @param user The username
     * @param nonceLength Desired length of the nonce
     * @throws IllegalArgumentException If channel binding flag, user or nonceLength are null;
     *                                  nonceLength is not positive;
     *                                  or if channel binding is required and no channel binding name is provided
     */
    public ClientFirstMessage(
            GS2CbindFlag gs2CbindFlag, String cbind, String authzid, String user, int nonceLength
    ) throws IllegalArgumentException {
        this(gs2CbindFlag, cbind, authzid, user, nonceLength, CryptoUtil::nonce);
    }

    /**
     * Constructs a message with the specified username and the nonce length,
     * and the indicated non-channel binding mode: either client supported or not.
     * @param user The username
     * @param nonceLength Desired length of the nonce
     * @throws IllegalArgumentException If user or nonceLength are null or nonceLength is not positive
     */
    public ClientFirstMessage(
            boolean clientChannelBinding, String user, int nonceLength
    ) throws IllegalArgumentException {
        this(
                clientChannelBinding ? GS2CbindFlag.CLIENT_YES_SERVER_NOT : GS2CbindFlag.CLIENT_NOT,
                null,
                null,
                user,
                nonceLength
        );
    }

    /**
     * Constructs a message with the specified username and the nonce length,
     * with no support (both client and server) for channel binding.
     * @param user The username
     * @param nonceLength Desired length of the nonce
     * @throws IllegalArgumentException If user or nonceLength are null or nonceLength is not positive
     */
    public ClientFirstMessage(String user, int nonceLength) throws IllegalArgumentException {
        this(false, user, nonceLength);
    }

    public GS2CbindFlag getChannelBindingFlag() {
        return gs2Header.getChannelBindingFlag();
    }

    public boolean isChannelBinding() {
        return gs2Header.getChannelBindingFlag() == GS2CbindFlag.CHANNEL_BINDING_REQUIRED;
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
        return StringWritableCSV.writeTo(sb, gs2Header, user, nonce);
    }

    public static ClientFirstMessage parseFrom(String clientFirstMessage) {
        checkNotNull(clientFirstMessage, "clientFirstMessage");

        GS2Header gs2Header = GS2Header.parseFrom(clientFirstMessage);  // Takes first two fields
        String[] userNonceString = StringWritableCSV.parseFrom(clientFirstMessage, 2, 2);

        SCRAMAttributeValue user = SCRAMAttributeValue.parse(userNonceString[0]);
        if(null == user || SCRAMAttributes.USERNAME.getChar() != user.getChar()) {
            throw new IllegalArgumentException("user must be the 3rd element of the client-first-message");
        }

        SCRAMAttributeValue nonce = SCRAMAttributeValue.parse(userNonceString[1]);
        if(null == nonce || SCRAMAttributes.NONCE.getChar() != nonce.getChar()) {
            throw new IllegalArgumentException("nonce must be the 4th element of the client-first-message");
        }

        return new ClientFirstMessage(gs2Header, user, nonce);
    }
}
