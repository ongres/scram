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
import org.junit.Test;

import static org.junit.Assert.*;


public class ScramClientTest {
    private static final String USERNAME = "user";
    private static final String PASSWORD = "pencil";
    private static final String CLIENT_NONCE = "fyko+d2lbbFgONRv9qkxdawL";
    private static final String CLIENT_FIRST_MESSAGE = "n,,n=" + USERNAME + ",r=" + CLIENT_NONCE;
    private static final String SERVER_SALT = "QSXCR+Q6sek8bf92";
    private static final int SERVER_ITERATIONS = 4096;
    private static final String SERVER_NONCE = "3rfcNHYJY1ZVvWVs7j";
    private static final String SERVER_FIRST_MESSAGE = "r=" + CLIENT_NONCE + SERVER_NONCE
                                                            + ",s=" + SERVER_SALT + ",i=" + SERVER_ITERATIONS;
    private static final String CLIENT_FINAL_MESSAGE = "c=biws,r=" + CLIENT_NONCE + SERVER_NONCE
            + ",p=v0X8v3Bz2T0CJGbJQyF0X+HI4Ts=";
    private static final String SERVER_FINAL_MESSAGE = "v=rmF9pqV8S7suAoZWja4dJRkFsKQ=";

    private final ScramClientGenerator scramClientGenerator = ScramClientGenerator
            .channelBinding(ScramClientGenerator.ChannelBinding.NO)
            .stringPreparation(StringPreparations.NO_PREPARATION)
            .serverMechanisms("SCRAM-SHA-1")
            .nonceSupplier(() -> CLIENT_NONCE)
            .setup();

    @Test
    public void completeTest() {
        ScramClient scramClient = scramClientGenerator.scramClient(USERNAME);
        assertEquals(CLIENT_FIRST_MESSAGE, scramClient.clientFirstMessage());

        ScramClient.ServerFirstProcessor serverFirstProcessor = scramClient.receiveServerFirstMessage(
                SERVER_FIRST_MESSAGE
        );
        assertEquals(SERVER_SALT, serverFirstProcessor.getSalt());
        assertEquals(SERVER_ITERATIONS, serverFirstProcessor.getIteration());

        ScramClient.ClientFinalProcessor clientFinalProcessor = serverFirstProcessor.finalMessagesHandler(PASSWORD);
        assertEquals(CLIENT_FINAL_MESSAGE, clientFinalProcessor.clientFinalMessage());

        ScramClient.ServerFinalProcessor serverFinalProcessor
                = clientFinalProcessor.receiveServerFinalMessage(SERVER_FINAL_MESSAGE);
        assertFalse(serverFinalProcessor.isError());
        assertTrue(serverFinalProcessor.verifyServerSignature());
    }
}
