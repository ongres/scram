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


import com.ongres.scram.common.stringprep.StringPreparations;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;


public class ScramFunctionsTest {
    private static final String AUTH_MESSAGE = "n=user,r=fyko+d2lbbFgONRv9qkxdawL" + ","
            + "r=fyko+d2lbbFgONRv9qkxdawL3rfcNHYJY1ZVvWVs7j,s=QSXCR+Q6sek8bf92,i=4096" + ","
            + "c=biws,r=fyko+d2lbbFgONRv9qkxdawL3rfcNHYJY1ZVvWVs7j";
    private static final Base64.Decoder BASE_64_DECODER = Base64.getDecoder();

    private void assertBytesEqualsBase64(String expected, byte[] actual) {
        assertArrayEquals(BASE_64_DECODER.decode(expected), actual);
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
                BASE_64_DECODER.decode("QSXCR+Q6sek8bf92"), 4096
        );
    }

    @Test
    public void saltedPassword() {
        assertBytesEqualsBase64("HZbuOlKbWl+eR8AfIposuKbhX30=", generateSaltedPassword());
    }

    private byte[] generateClientKey() {
        return ScramFunctions.clientKey(ScramMechanisms.SCRAM_SHA_1, generateSaltedPassword());
    }

    @Test
    public void clientKey() {
        assertBytesEqualsBase64("4jTEe/bDZpbdbYUrmaqiuiZVVyg=", generateClientKey());
    }

    private byte[] generateStoredKey() {
        return ScramFunctions.storedKey(ScramMechanisms.SCRAM_SHA_1, generateSaltedPassword());
    }

    @Test
    public void storedKey() {
        assertBytesEqualsBase64("6dlGYMOdZcOPutkcNY8U2g7vK9Y=", generateStoredKey());
    }

    private byte[] generateServerKey() {
        return ScramFunctions.serverKey(ScramMechanisms.SCRAM_SHA_1, generateSaltedPassword());
    }

    @Test
    public void serverKey() {
        assertBytesEqualsBase64("D+CSWLOshSulAsxiupA+qs2/fTE=", generateServerKey());
    }

    private byte[] generateClientSignature() {
        return ScramFunctions.clientSignature(ScramMechanisms.SCRAM_SHA_1, generateStoredKey(), AUTH_MESSAGE);
    }

    @Test
    public void clientSignature() {
        assertBytesEqualsBase64("XXE4xIawv6vfSePi2ovW5cedthM=", generateClientSignature());
    }

    private byte[] generateClientProof() {
        return ScramFunctions.clientProof(generateClientKey(), generateClientSignature());
    }

    @Test
    public void clientProof() {
        assertBytesEqualsBase64("v0X8v3Bz2T0CJGbJQyF0X+HI4Ts=", generateClientProof());
    }

    private byte[] generateServerSignature() {
        return ScramFunctions.serverSignature(ScramMechanisms.SCRAM_SHA_1, generateServerKey(), AUTH_MESSAGE);
    }

    @Test
    public void serverSignature() {
        assertBytesEqualsBase64("rmF9pqV8S7suAoZWja4dJRkFsKQ=", generateServerSignature());
    }

    @Test
    public void verifyClientProof() {
        assertTrue(
                ScramFunctions.verifyClientProof(
                        ScramMechanisms.SCRAM_SHA_1, generateClientProof(), generateStoredKey(), AUTH_MESSAGE
                )
        );
    }

    @Test
    public void verifyServerSignature() {
        assertTrue(
                ScramFunctions.verifyServerSignature(
                        ScramMechanisms.SCRAM_SHA_1, generateServerKey(), AUTH_MESSAGE, generateServerSignature()
                )
        );
    }
}
