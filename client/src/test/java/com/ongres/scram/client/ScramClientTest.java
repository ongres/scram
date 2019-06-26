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


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.junit.Test;

import com.ongres.scram.common.ScramMechanisms;
import com.ongres.scram.common.stringprep.StringPreparations;
import com.ongres.scram.common.util.CryptoUtil;


public class ScramClientTest {
    @Test
    public void getValid() {
        ScramClient client1 = ScramClient
                .channelBinding(ScramClient.ChannelBinding.NO)
                .stringPreparation(StringPreparations.NO_PREPARATION)
                .selectMechanismBasedOnServerAdvertised("SCRAM-SHA-1")
                .setup();
        ScramClient client2 = ScramClient
                .channelBinding(ScramClient.ChannelBinding.YES)
                .stringPreparation(StringPreparations.NO_PREPARATION)
                .selectMechanismBasedOnServerAdvertised("SCRAM-SHA-1", "SCRAM-SHA-256-PLUS")
                .nonceLength(64)
                .setup();
        ScramClient client3 = ScramClient
                .channelBinding(ScramClient.ChannelBinding.IF_SERVER_SUPPORTS_IT)
                .stringPreparation(StringPreparations.NO_PREPARATION)
                .selectMechanismBasedOnServerAdvertised("SCRAM-SHA-1", "SCRAM-SHA-1-PLUS")
                .nonceSupplier
                (new NonceSupplier() {
                    @Override
                    public String get() {
                        return CryptoUtil.nonce(36);
                    }
                })
                .setup();
        ScramClient client4 = ScramClient
                .channelBinding(ScramClient.ChannelBinding.IF_SERVER_SUPPORTS_IT)
                .stringPreparation(StringPreparations.NO_PREPARATION)
                .selectMechanismBasedOnServerAdvertisedCsv("SCRAM-SHA-1,SCRAM-SHA-256-PLUS")
                .secureRandomAlgorithmProvider("SHA1PRNG", "SUN")
                .nonceLength(64)
                .setup();
        ScramClient client5 = ScramClient
                .channelBinding(ScramClient.ChannelBinding.IF_SERVER_SUPPORTS_IT)
                .stringPreparation(StringPreparations.NO_PREPARATION)
                .selectMechanismBasedOnServerAdvertisedCsv("SCRAM-SHA-1,SCRAM-SHA-256-PLUS")
                .secureRandomAlgorithmProvider("SHA1PRNG", null)
                .nonceLength(64)
                .setup();
        ScramClient client6 = ScramClient
                .channelBinding(ScramClient.ChannelBinding.NO)
                .stringPreparation(StringPreparations.NO_PREPARATION)
                .selectClientMechanism(ScramMechanisms.SCRAM_SHA_1)
                .setup();
        ScramClient client7 = ScramClient
                .channelBinding(ScramClient.ChannelBinding.YES)
                .stringPreparation(StringPreparations.NO_PREPARATION)
                .selectClientMechanism(ScramMechanisms.SCRAM_SHA_256_PLUS)
                .setup();

        for (ScramClient client : new ScramClient[] {
                client1, client2, client3, client4, client5, client6, client7
            }) {
                assertNotNull(client);
            }
    }

    @Test
    public void getInvalid() {
        int n = 0;

        try {
            assertNotNull(ScramClient
                    .channelBinding(ScramClient.ChannelBinding.NO)
                    .stringPreparation(StringPreparations.NO_PREPARATION)
                    .selectMechanismBasedOnServerAdvertised("SCRAM-SHA-1-PLUS")
                    .setup()
            );
        } catch (IllegalArgumentException e) { n++; }
        try {
            assertNotNull(ScramClient
                    .channelBinding(ScramClient.ChannelBinding.YES)
                    .stringPreparation(StringPreparations.NO_PREPARATION)
                    .selectMechanismBasedOnServerAdvertised("SCRAM-SHA-1-PLUS,SCRAM-SAH-256-PLUS")
                    .setup()
            );
        } catch (IllegalArgumentException e) { n++; }
        try {
            assertNotNull(ScramClient
                    .channelBinding(ScramClient.ChannelBinding.IF_SERVER_SUPPORTS_IT)
                    .stringPreparation(StringPreparations.NO_PREPARATION)
                    .selectMechanismBasedOnServerAdvertised("INVALID-SCRAM-MECHANISM")
                    .setup()
            );
        } catch (IllegalArgumentException e) { n++; }
        try {
            assertNotNull(ScramClient
                    .channelBinding(ScramClient.ChannelBinding.IF_SERVER_SUPPORTS_IT)
                    .stringPreparation(StringPreparations.NO_PREPARATION)
                    .selectMechanismBasedOnServerAdvertised("SCRAM-SHA-1", "SCRAM-SHA-1-PLUS")
                    .nonceSupplier(null)
                    .setup()
            );
        } catch (IllegalArgumentException e) { n++; }
        try {
            assertNotNull(ScramClient
                    .channelBinding(ScramClient.ChannelBinding.IF_SERVER_SUPPORTS_IT)
                    .stringPreparation(StringPreparations.NO_PREPARATION)
                    .selectMechanismBasedOnServerAdvertised("SCRAM-SHA-1", "SCRAM-SHA-1-PLUS")
                    .nonceLength(0)
                    .setup()
            );
        } catch (IllegalArgumentException e) { n++; }
        try {
            assertNotNull(ScramClient
                    .channelBinding(ScramClient.ChannelBinding.IF_SERVER_SUPPORTS_IT)
                    .stringPreparation(StringPreparations.NO_PREPARATION)
                    .selectMechanismBasedOnServerAdvertised("SCRAM-SHA-1", "SCRAM-SHA-1-PLUS")
                    .secureRandomAlgorithmProvider("Invalid algorithm", null)
                    .setup()
            );
        } catch (IllegalArgumentException e) { n++; }
        try {
            assertNotNull(ScramClient
                    .channelBinding(ScramClient.ChannelBinding.IF_SERVER_SUPPORTS_IT)
                    .stringPreparation(StringPreparations.NO_PREPARATION)
                    .selectMechanismBasedOnServerAdvertised("SCRAM-SHA-1", "SCRAM-SHA-1-PLUS")
                    .secureRandomAlgorithmProvider("SHA1PRNG", "Invalid provider")
                    .setup()
            );
        } catch (IllegalArgumentException e) { n++; }
        try {
            assertNotNull(ScramClient
                    .channelBinding(ScramClient.ChannelBinding.YES)
                    .stringPreparation(StringPreparations.NO_PREPARATION)
                    .selectClientMechanism(ScramMechanisms.SCRAM_SHA_1)
                    .setup()
            );
        } catch (IllegalArgumentException e) { n++; }
        try {
            assertNotNull(ScramClient
                    .channelBinding(ScramClient.ChannelBinding.NO)
                    .stringPreparation(StringPreparations.NO_PREPARATION)
                    .selectClientMechanism(ScramMechanisms.SCRAM_SHA_1_PLUS)
                    .setup()
            );
        } catch (IllegalArgumentException e) { n++; }
        try {
            assertNotNull(ScramClient
                    .channelBinding(ScramClient.ChannelBinding.IF_SERVER_SUPPORTS_IT)
                    .stringPreparation(StringPreparations.NO_PREPARATION)
                    .selectClientMechanism(ScramMechanisms.SCRAM_SHA_1)
                    .setup()
            );
        } catch (IllegalArgumentException e) { n++; }

        assertEquals(10, n);
    }

    @Test
    public void supportedMechanismsTestAll() {
        String[] expecteds = new String[] { "SCRAM-SHA-1", "SCRAM-SHA-1-PLUS", "SCRAM-SHA-256", "SCRAM-SHA-256-PLUS" };
        Arrays.sort(expecteds);
        String[] actuals = ScramClient.supportedMechanisms().toArray(new String[0]);
        Arrays.sort(actuals);
        assertArrayEquals(
                expecteds,
                actuals
        );
    }
}
