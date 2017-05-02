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


public class ScramClientGeneratorTest {
    @Test
    public void getValid() {
        ScramClientGenerator generator1 = ScramClientGenerator
                .channelBinding(ScramClientGenerator.ChannelBinding.NO)
                .stringPreparation(StringPreparations.NO_PREPARATION)
                .serverMechanisms("SCRAM-SHA-1")
                .setup();
        ScramClientGenerator generator2 = ScramClientGenerator
                .channelBinding(ScramClientGenerator.ChannelBinding.YES)
                .stringPreparation(StringPreparations.NO_PREPARATION)
                .serverMechanisms("SCRAM-SHA-1", "SCRAM-SHA-256-PLUS")
                .nonceLength(64)
                .setup();
        ScramClientGenerator generator3 = ScramClientGenerator
                .channelBinding(ScramClientGenerator.ChannelBinding.IF_SERVER_SUPPORTS_IT)
                .stringPreparation(StringPreparations.NO_PREPARATION)
                .serverMechanisms("SCRAM-SHA-1", "SCRAM-SHA-1-PLUS")
                .nonceSupplier(() -> CryptoUtil.nonce(36))
                .setup();
        ScramClientGenerator generator4 = ScramClientGenerator
                .channelBinding(ScramClientGenerator.ChannelBinding.IF_SERVER_SUPPORTS_IT)
                .stringPreparation(StringPreparations.NO_PREPARATION)
                .serverMechanismsCsv("SCRAM-SHA-1,SCRAM-SHA-256-PLUS")
                .secureRandomAlgorithmProvider("SHA1PRNG", "SUN")
                .nonceLength(64)
                .setup();
        ScramClientGenerator generator5 = ScramClientGenerator
                .channelBinding(ScramClientGenerator.ChannelBinding.IF_SERVER_SUPPORTS_IT)
                .stringPreparation(StringPreparations.NO_PREPARATION)
                .serverMechanismsCsv("SCRAM-SHA-1,SCRAM-SHA-256-PLUS")
                .secureRandomAlgorithmProvider("SHA1PRNG", null)
                .nonceLength(64)
                .setup();

        Stream.of(generator1, generator2, generator3, generator4, generator5).forEach(c -> assertNotNull(c));
    }

    @Test
    public void getInvalid() {
        int n = 0;

        try {
            assertNotNull(ScramClientGenerator
                    .channelBinding(ScramClientGenerator.ChannelBinding.NO)
                    .stringPreparation(StringPreparations.NO_PREPARATION)
                    .serverMechanisms("SCRAM-SHA-1-PLUS")
                    .setup()
            );
        } catch (IllegalArgumentException e) { n++; }
        try {
            assertNotNull(ScramClientGenerator
                    .channelBinding(ScramClientGenerator.ChannelBinding.YES)
                    .stringPreparation(StringPreparations.NO_PREPARATION)
                    .serverMechanisms("SCRAM-SHA-1-PLUS,SCRAM-SAH-256-PLUS")
                    .setup()
            );
        } catch (IllegalArgumentException e) { n++; }
        try {
            assertNotNull(ScramClientGenerator
                    .channelBinding(ScramClientGenerator.ChannelBinding.IF_SERVER_SUPPORTS_IT)
                    .stringPreparation(StringPreparations.NO_PREPARATION)
                    .serverMechanisms("INVALID-SCRAM-MECHANISM")
                    .setup()
            );
        } catch (IllegalArgumentException e) { n++; }
        try {
            assertNotNull(ScramClientGenerator
                    .channelBinding(ScramClientGenerator.ChannelBinding.IF_SERVER_SUPPORTS_IT)
                    .stringPreparation(StringPreparations.NO_PREPARATION)
                    .serverMechanisms("SCRAM-SHA-1", "SCRAM-SHA-1-PLUS")
                    .nonceSupplier(null)
                    .setup()
            );
        } catch (IllegalArgumentException e) { n++; }
        try {
            assertNotNull(ScramClientGenerator
                    .channelBinding(ScramClientGenerator.ChannelBinding.IF_SERVER_SUPPORTS_IT)
                    .stringPreparation(StringPreparations.NO_PREPARATION)
                    .serverMechanisms("SCRAM-SHA-1", "SCRAM-SHA-1-PLUS")
                    .nonceLength(0)
                    .setup()
            );
        } catch (IllegalArgumentException e) { n++; }
        try {
            assertNotNull(ScramClientGenerator
                    .channelBinding(ScramClientGenerator.ChannelBinding.IF_SERVER_SUPPORTS_IT)
                    .stringPreparation(StringPreparations.NO_PREPARATION)
                    .serverMechanisms("SCRAM-SHA-1", "SCRAM-SHA-1-PLUS")
                    .secureRandomAlgorithmProvider("Invalid algorithm", null)
                    .setup()
            );
        } catch (IllegalArgumentException e) { n++; }
        try {
            assertNotNull(ScramClientGenerator
                    .channelBinding(ScramClientGenerator.ChannelBinding.IF_SERVER_SUPPORTS_IT)
                    .stringPreparation(StringPreparations.NO_PREPARATION)
                    .serverMechanisms("SCRAM-SHA-1", "SCRAM-SHA-1-PLUS")
                    .secureRandomAlgorithmProvider("SHA1PRNG", "Invalid provider")
                    .setup()
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
                ScramClientGenerator.supportedMechanisms().stream().sorted().toArray()
        );
    }
}
