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
import com.ongres.scram.common.gssapi.Gs2Header;
import com.ongres.scram.common.util.StringWritable;
import com.ongres.scram.common.util.StringWritableCsv;

import java.util.Optional;

import static com.ongres.scram.common.util.Preconditions.checkNotEmpty;
import static com.ongres.scram.common.util.Preconditions.checkNotNull;


/**
 * Constructs and parses client-last-messages. Formal syntax is:
 *
 * {@code
 * client-final-message-without-proof = channel-binding "," nonce ["," extensions]
 * client-final-message = client-final-message-without-proof "," proof
 * }
 *
 * Note that extensions are not supported.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5802#section-7">[RFC5802] Section 7</a>
 */
public class ClientLastMessage implements StringWritable {
    private final String cbind;
    private final String nonce;
    private byte[] proof;

    public ClientLastMessage(Gs2Header gs2Header, Optional<String> cbindData, String nonce) {
        checkNotNull(gs2Header, "gs2Header");
        checkNotEmpty(nonce, "nonce");

        this.cbind = gs2Header.writeTo(new StringBuffer()).append(",").append(cbindData.orElse("")).toString();
        this.nonce = nonce;
    }

    public void setProof(byte[] proof) {
        this.proof = checkNotNull(proof, "proof");
    }

    public StringBuffer writeToWithoutProof(StringBuffer sb) {
        return StringWritableCsv.writeTo(
                sb,
                new ScramAttributeValue(ScramAttributes.CHANNEL_BINDING, ScramStringFormatting.base64Encode(cbind)),
                new ScramAttributeValue(ScramAttributes.NONCE, nonce)
        );
    }

    @Override
    public StringBuffer writeTo(StringBuffer sb) {
        if(null == proof) {
            throw new IllegalStateException("Set the proof before calling this method");
        }

        writeToWithoutProof(sb);

        return StringWritableCsv.writeTo(
                sb,
                null,   // This marks the position of writeToWithoutProof, required for the ","
                new ScramAttributeValue(ScramAttributes.CLIENT_PROOF, ScramStringFormatting.base64Encode(proof))
        );
    }

    @Override
    public String toString() {
        return writeTo(new StringBuffer()).toString();
    }
}
