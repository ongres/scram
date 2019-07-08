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


import com.ongres.scram.common.bouncycastle.base64.Base64;
import com.ongres.scram.common.stringprep.StringPreparations;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class ScramFunctionsTest {
    private void assertBytesEqualsBase64(String expected, byte[] actual) {
        assertArrayEquals(Base64.decode(expected), actual);
    }

    @Test
    public void hmac() throws UnsupportedEncodingException {
        String message = "The quick brown fox jumps over the lazy dog";
        byte[] key = "key".getBytes(StandardCharsets.UTF_8);

        assertBytesEqualsBase64(
                "3nybhbi3iqa8ino29wqQcBydtNk=",
                ScramFunctions.hmac(ScramMechanisms.SCRAM_SHA_1, message.getBytes(StandardCharsets.US_ASCII), key)
        );
        assertBytesEqualsBase64(
                "97yD9DBThCSxMpjmqm+xQ+9NWaFJRhdZl0edvC0aPNg=",
                ScramFunctions.hmac(ScramMechanisms.SCRAM_SHA_256, message.getBytes(StandardCharsets.US_ASCII), key)
        );
    }

    private byte[] generateSaltedPassword() {
        return ScramFunctions.saltedPassword(
                ScramMechanisms.SCRAM_SHA_1, StringPreparations.NO_PREPARATION, "pencil",
                Base64.decode("QSXCR+Q6sek8bf92"), 4096
        );
    }
    
    private byte[] generateSaltedPasswordSha256() {
        return ScramFunctions.saltedPassword(
                ScramMechanisms.SCRAM_SHA_256, StringPreparations.NO_PREPARATION, "pencil",
                Base64.decode("W22ZaJ0SNY7soEsUEjb6gQ=="), 4096
        );
    }
    
    @Test
    public void saltedPassword() {
        assertBytesEqualsBase64("HZbuOlKbWl+eR8AfIposuKbhX30=", generateSaltedPassword());
    }
    
    @Test
    public void saltedPasswordWithSaslPrep() {
        assertBytesEqualsBase64("YniLes+b8WFMvBhtSACZyyvxeCc=", ScramFunctions.saltedPassword(
                ScramMechanisms.SCRAM_SHA_1, StringPreparations.SASL_PREPARATION, "\u2168\u3000a\u0300",
                Base64.decode("0BojBCBE6P2/N4bQ"), 6400
        ));
        assertBytesEqualsBase64("YniLes+b8WFMvBhtSACZyyvxeCc=", ScramFunctions.saltedPassword(
                ScramMechanisms.SCRAM_SHA_1, StringPreparations.SASL_PREPARATION, "\u00ADIX \u00E0",
                Base64.decode("0BojBCBE6P2/N4bQ"), 6400
        ));
        assertBytesEqualsBase64("YniLes+b8WFMvBhtSACZyyvxeCc=", ScramFunctions.saltedPassword(
                ScramMechanisms.SCRAM_SHA_1, StringPreparations.SASL_PREPARATION, "IX \u00E0",
                Base64.decode("0BojBCBE6P2/N4bQ"), 6400
        ));
        assertBytesEqualsBase64("HZbuOlKbWl+eR8AfIposuKbhX30=", ScramFunctions.saltedPassword(
                ScramMechanisms.SCRAM_SHA_1, StringPreparations.SASL_PREPARATION, "\u0070enc\u1806il",
                Base64.decode("QSXCR+Q6sek8bf92"), 4096
        ));
        try {
            ScramFunctions.saltedPassword(
                ScramMechanisms.SCRAM_SHA_1, StringPreparations.SASL_PREPARATION, "\u2168\u3000a\u0300\u0007",
                Base64.decode("QSXCR+Q6sek8bf92"), 6400);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Prohibited character \u0007", e.getMessage());
        }
    }
    
    @Test
    public void saltedPasswordSha256() {
        assertBytesEqualsBase64("xKSVEDI6tPlSysH6mUQZOeeOp01r6B3fcJbodRPcYV0=", generateSaltedPasswordSha256());
    }
    
    private byte[] generateClientKey() {
        return ScramFunctions.clientKey(ScramMechanisms.SCRAM_SHA_1, generateSaltedPassword());
    }
    
    private byte[] generateClientKeySha256() {
        return ScramFunctions.clientKey(ScramMechanisms.SCRAM_SHA_256, generateSaltedPasswordSha256());
    }
    
    @Test
    public void clientKey() {
        assertBytesEqualsBase64("4jTEe/bDZpbdbYUrmaqiuiZVVyg=", generateClientKey());
    }
    
    @Test
    public void clientKeySha256() {
        assertBytesEqualsBase64("pg/JI9Z+hkSpLRa5btpe9GVrDHJcSEN0viVTVXaZbos=", generateClientKeySha256());
    }
    
    private byte[] generateStoredKey() {
        return ScramFunctions.storedKey(ScramMechanisms.SCRAM_SHA_1, generateClientKey());
    }
    
    private byte[] generateStoredKeySha256() {
        return ScramFunctions.storedKey(ScramMechanisms.SCRAM_SHA_256, generateClientKeySha256());
    }
    
    @Test
    public void storedKey() {
        assertBytesEqualsBase64("6dlGYMOdZcOPutkcNY8U2g7vK9Y=", generateStoredKey());
    }
    
    @Test
    public void storedKeySha256() {
        assertBytesEqualsBase64("WG5d8oPm3OtcPnkdi4Uo7BkeZkBFzpcXkuLmtbsT4qY=", generateStoredKeySha256());
    }
    
    private byte[] generateServerKey() {
        return ScramFunctions.serverKey(ScramMechanisms.SCRAM_SHA_1, generateSaltedPassword());
    }
    
    private byte[] generateServerKeySha256() {
        return ScramFunctions.serverKey(ScramMechanisms.SCRAM_SHA_256, generateSaltedPasswordSha256());
    }
    
    @Test
    public void serverKey() {
        assertBytesEqualsBase64("D+CSWLOshSulAsxiupA+qs2/fTE=", generateServerKey());
    }
    
    @Test
    public void serverKeySha256() {
        assertBytesEqualsBase64("wfPLwcE6nTWhTAmQ7tl2KeoiWGPlZqQxSrmfPwDl2dU=", generateServerKeySha256());
    }
    
    private byte[] generateClientSignature() {
        return ScramFunctions.clientSignature(ScramMechanisms.SCRAM_SHA_1, generateStoredKey(), com.ongres.scram.common.RfcExampleSha1.AUTH_MESSAGE);
    }
    
    private byte[] generateClientSignatureSha256() {
        return ScramFunctions.clientSignature(ScramMechanisms.SCRAM_SHA_256, generateStoredKeySha256(), com.ongres.scram.common.RfcExampleSha256.AUTH_MESSAGE);
    }
    
    @Test
    public void clientSignature() {
        assertBytesEqualsBase64("XXE4xIawv6vfSePi2ovW5cedthM=", generateClientSignature());
    }
    
    @Test
    public void clientSignatureSha256() {
        assertBytesEqualsBase64("0nMSRnwopAqKfwXHPA3jPrPL+0qDeDtYFEzxmsa+G98=", generateClientSignatureSha256());
    }
    
    private byte[] generateClientProof() {
        return ScramFunctions.clientProof(generateClientKey(), generateClientSignature());
    }
    
    private byte[] generateClientProofSha256() {
        return ScramFunctions.clientProof(generateClientKeySha256(), generateClientSignatureSha256());
    }
    
    @Test
    public void clientProof() {
        assertBytesEqualsBase64("v0X8v3Bz2T0CJGbJQyF0X+HI4Ts=", generateClientProof());
    }
    
    @Test
    public void clientProofSha256() {
        assertBytesEqualsBase64("dHzbZapWIk4jUhN+Ute9ytag9zjfMHgsqmmiz7AndVQ=", generateClientProofSha256());
    }
    
    private byte[] generateServerSignature() {
        return ScramFunctions.serverSignature(ScramMechanisms.SCRAM_SHA_1, generateServerKey(), com.ongres.scram.common.RfcExampleSha1.AUTH_MESSAGE);
    }
    
    private byte[] generateServerSignatureSha256() {
        return ScramFunctions.serverSignature(ScramMechanisms.SCRAM_SHA_256, generateServerKeySha256(), com.ongres.scram.common.RfcExampleSha256.AUTH_MESSAGE);
    }
    
    @Test
    public void serverSignature() {
        assertBytesEqualsBase64("rmF9pqV8S7suAoZWja4dJRkFsKQ=", generateServerSignature());
    }
    
    @Test
    public void serverSignatureSha256() {
        assertBytesEqualsBase64("6rriTRBi23WpRR/wtup+mMhUZUn/dB5nLTJRsjl95G4=", generateServerSignatureSha256());
    }
    
    @Test
    public void verifyClientProof() {
        assertTrue(
                ScramFunctions.verifyClientProof(
                        ScramMechanisms.SCRAM_SHA_1, generateClientProof(), generateStoredKey(), com.ongres.scram.common.RfcExampleSha1.AUTH_MESSAGE
                )
        );
    }
    
    @Test
    public void verifyClientProofSha256() {
        assertTrue(
                ScramFunctions.verifyClientProof(
                        ScramMechanisms.SCRAM_SHA_256, generateClientProofSha256(), generateStoredKeySha256(), com.ongres.scram.common.RfcExampleSha256.AUTH_MESSAGE
                )
        );
    }
    
    @Test
    public void verifyServerSignature() {
        assertTrue(
                ScramFunctions.verifyServerSignature(
                        ScramMechanisms.SCRAM_SHA_1, generateServerKey(), com.ongres.scram.common.RfcExampleSha1.AUTH_MESSAGE, generateServerSignature()
                )
        );
    }
    
    @Test
    public void verifyServerSignatureSha256() {
        assertTrue(
                ScramFunctions.verifyServerSignature(
                        ScramMechanisms.SCRAM_SHA_256, generateServerKeySha256(), com.ongres.scram.common.RfcExampleSha256.AUTH_MESSAGE, generateServerSignatureSha256()
                )
        );
    }
}
