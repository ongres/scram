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


import com.ongres.scram.common.stringprep.StringPreparations;
import com.ongres.scram.common.util.CryptoUtil;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class ScramClientTest {
    @Test
    public void getValid() {
        ScramClient scramClient1 = ScramClient
                .channelBinding(ScramClient.ChannelBinding.NO)
                .stringPreparation(StringPreparations.NO_PREPARATION)
                .serverMechanisms("SCRAM-SHA-1")
                .get();
        ScramClient scramClient2 = ScramClient
                .channelBinding(ScramClient.ChannelBinding.YES)
                .stringPreparation(StringPreparations.NO_PREPARATION)
                .serverMechanisms("SCRAM-SHA-1", "SCRAM-SHA-256-PLUS")
                .nonceLength(64)
                .get();
        ScramClient scramClient3 = ScramClient
                .channelBinding(ScramClient.ChannelBinding.IF_SERVER_SUPPORTS_IT)
                .stringPreparation(StringPreparations.NO_PREPARATION)
                .serverMechanisms("SCRAM-SHA-1", "SCRAM-SHA-1-PLUS")
                .nonceSupplier(() -> CryptoUtil.nonce(36))
                .get();
        ScramClient scramClient4 = ScramClient
                .channelBinding(ScramClient.ChannelBinding.IF_SERVER_SUPPORTS_IT)
                .stringPreparation(StringPreparations.NO_PREPARATION)
                .serverMechanismsCsv("SCRAM-SHA-1,SCRAM-SHA-256-PLUS")
                .secureRandomAlgorithmProvider("SHA1PRNG", "SUN")
                .nonceLength(64)
                .get();
        ScramClient scramClient5 = ScramClient
                .channelBinding(ScramClient.ChannelBinding.IF_SERVER_SUPPORTS_IT)
                .stringPreparation(StringPreparations.NO_PREPARATION)
                .serverMechanismsCsv("SCRAM-SHA-1,SCRAM-SHA-256-PLUS")
                .secureRandomAlgorithmProvider("SHA1PRNG", null)
                .nonceLength(64)
                .get();

        Stream.of(scramClient1, scramClient2, scramClient3, scramClient4, scramClient5).forEach(c -> assertNotNull(c));
    }

    @Test
    public void getInvalid() {
        int n = 0;

        try {
            assertNotNull(ScramClient
                    .channelBinding(ScramClient.ChannelBinding.NO)
                    .stringPreparation(StringPreparations.NO_PREPARATION)
                    .serverMechanisms("SCRAM-SHA-1-PLUS")
                    .get()
            );
        } catch (IllegalArgumentException e) { n++; }
        try {
            assertNotNull(ScramClient
                    .channelBinding(ScramClient.ChannelBinding.YES)
                    .stringPreparation(StringPreparations.NO_PREPARATION)
                    .serverMechanisms("SCRAM-SHA-1-PLUS,SCRAM-SAH-256-PLUS")
                    .get()
            );
        } catch (IllegalArgumentException e) { n++; }
        try {
            assertNotNull(ScramClient
                    .channelBinding(ScramClient.ChannelBinding.IF_SERVER_SUPPORTS_IT)
                    .stringPreparation(StringPreparations.NO_PREPARATION)
                    .serverMechanisms("INVALID-SCRAM-MECHANISM")
                    .get()
            );
        } catch (IllegalArgumentException e) { n++; }
        try {
            assertNotNull(ScramClient
                    .channelBinding(ScramClient.ChannelBinding.IF_SERVER_SUPPORTS_IT)
                    .stringPreparation(StringPreparations.NO_PREPARATION)
                    .serverMechanisms("SCRAM-SHA-1", "SCRAM-SHA-1-PLUS")
                    .nonceSupplier(null)
                    .get()
            );
        } catch (IllegalArgumentException e) { n++; }
        try {
            assertNotNull(ScramClient
                    .channelBinding(ScramClient.ChannelBinding.IF_SERVER_SUPPORTS_IT)
                    .stringPreparation(StringPreparations.NO_PREPARATION)
                    .serverMechanisms("SCRAM-SHA-1", "SCRAM-SHA-1-PLUS")
                    .nonceLength(0)
                    .get()
            );
        } catch (IllegalArgumentException e) { n++; }
        try {
            assertNotNull(ScramClient
                    .channelBinding(ScramClient.ChannelBinding.IF_SERVER_SUPPORTS_IT)
                    .stringPreparation(StringPreparations.NO_PREPARATION)
                    .serverMechanisms("SCRAM-SHA-1", "SCRAM-SHA-1-PLUS")
                    .secureRandomAlgorithmProvider("Invalid algorithm", null)
                    .get()
            );
        } catch (IllegalArgumentException e) { n++; }
        try {
            assertNotNull(ScramClient
                    .channelBinding(ScramClient.ChannelBinding.IF_SERVER_SUPPORTS_IT)
                    .stringPreparation(StringPreparations.NO_PREPARATION)
                    .serverMechanisms("SCRAM-SHA-1", "SCRAM-SHA-1-PLUS")
                    .secureRandomAlgorithmProvider("SHA1PRNG", "Invalid provider")
                    .get()
            );
        } catch (IllegalArgumentException e) { n++; }

        assertEquals(7, n);
    }

    @Test
    public void supportedMechanismsTestAll() {
        assertArrayEquals(
                Arrays.stream(
                        new String[] { "SCRAM-SHA-1", "SCRAM-SHA-1-PLUS", "SCRAM-SHA-256", "SCRAM-SHA-256-PLUS" }
                ).sorted().toArray(),
                ScramClient.supportedMechanisms().stream().sorted().toArray()
        );
    }
}
