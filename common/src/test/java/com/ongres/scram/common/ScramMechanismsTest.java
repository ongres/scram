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


import org.junit.Test;

import javax.crypto.Mac;
import java.security.MessageDigest;

import static org.junit.Assert.*;


public class ScramMechanismsTest {
    @Test
    public void TestHashSupportedByJVM() {
        MessageDigest messageDigest;
        for(ScramMechanisms scramMechanism : ScramMechanisms.values()) {
            try {
                messageDigest = scramMechanism.getMessageDigestInstance();
            } catch(RuntimeException ex) {
                fail(ex.getMessage());
                return;
            }
            assertNotNull("got a null MessageDigest", messageDigest);
            assertEquals(
                    "algorithm name and obtained algorithm name differ",
                    scramMechanism.getHashAlgorithmName(),
                    messageDigest.getAlgorithm()
            );
        }
    }

    @Test
    public void TestHMACSupportedByJVM() {
        Mac hmac;
        for(ScramMechanisms scramMechanism : ScramMechanisms.values()) {
            try {
                hmac = scramMechanism.getMacInstance();
            } catch(RuntimeException ex) {
                fail(ex.getMessage());
                return;
            }
            assertNotNull("got a null HMAC", hmac);
            assertEquals(
                    "algorithm name and obtained algorithm name differ",
                    scramMechanism.getHmacAlgorithmName(),
                    hmac.getAlgorithm()
            );
        }
    }
}
