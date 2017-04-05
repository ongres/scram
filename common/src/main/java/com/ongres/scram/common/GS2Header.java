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


package com.ongres.scram.common;


import com.ongres.scram.common.util.AbstractStringWritable;
import com.ongres.scram.common.util.CharAttributeValueCSV;


/**
 * GSS Header. Format:
 *
 * {@code
 * gs2-header      = gs2-cbind-flag "," [ authzid ] ","
 * gs2-cbind-flag  = ("p=" cb-name) / "n" / "y"
 * authzid         = "a=" saslname
 * }
 *
 * Current implementation does not support channel binding.
 * If p is used as the cbind flag, the cb-name value is not validated.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5802#section-7">[RFC5802] Formal Syntax</a>
 */
public class GS2Header extends AbstractStringWritable {
    private final GS2AttributeValue cbind;
    private final GS2AttributeValue authzid;

    /**
     * Construct and validates a GS2Header.
     * Only provide the channel binding name if the channel binding flag is set to required.
     * @param cbindFlag The channel binding flag
     * @param cbName The channel-binding name. Should be not null iif channel binding is required
     * @param authzid The optional SASL authorization identity
     * @throws IllegalArgumentException If the channel binding flag and argument are invalid
     */
    public GS2Header(GS2CbindFlag cbindFlag, String cbName, String authzid) throws IllegalArgumentException {
        this.cbind = null;
        this.authzid = null;
    }

    /**
     * Construct and validates a GS2Header with no authzid.
     * Only provide the channel binding name if the channel binding flag is set to required.
     * @param cbindFlag The channel binding flag
     * @param cbName The channel-binding name. Should be not null iif channel binding is required
     * @throws IllegalArgumentException If the channel binding flag and argument are invalid
     */
    public GS2Header(GS2CbindFlag cbindFlag, String cbName) throws IllegalArgumentException {
        this(cbindFlag, cbName, null);
    }

    /**
     * Construct and validates a GS2Header with no authzid nor channel binding.
     * @param cbindFlag The channel binding flag
     * @throws IllegalArgumentException If the channel binding is supported (no cbname can be provided here)
     */
    public GS2Header(GS2CbindFlag cbindFlag) {
        this(cbindFlag, null, null);
    }

    @Override
    public StringBuffer writeTo(StringBuffer sb) {
        return CharAttributeValueCSV.writeTo(sb, cbind, authzid);
    }

    /**
     * Read a GS2Header from a String. String may contain trailing fields that will be ignored.
     * @param message The String containing the GS2Header
     * @return The parsed GS2Header object
     * @throws IllegalArgumentException If the format/values of the String do not conform to a GS2Header
     */
    public static GS2Header parseFrom(String message) throws IllegalArgumentException {
        return null;
    }
}
