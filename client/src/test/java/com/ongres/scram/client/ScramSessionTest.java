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


import com.ongres.scram.common.exception.ScramInvalidServerSignatureException;
import com.ongres.scram.common.exception.ScramParseException;
import com.ongres.scram.common.exception.ScramServerErrorException;
import com.ongres.scram.common.stringprep.StringPreparations;
import org.junit.Test;

import static com.ongres.scram.common.RfcExampleSha1.*;
import static org.junit.Assert.*;

public class ScramSessionTest {
    private final ScramClient scramClient = ScramClient
            .channelBinding(ScramClient.ChannelBinding.NO)
            .stringPreparation(StringPreparations.NO_PREPARATION)
            .selectMechanismBasedOnServerAdvertised("SCRAM-SHA-1")
            .nonceSupplier
            (new NonceSupplier() {
                @Override
                public String get() {
                    return CLIENT_NONCE;
                }
            })
            .setup();

    @Test
    public void completeTest()
    throws ScramParseException, ScramInvalidServerSignatureException, ScramServerErrorException {
        ScramSession scramSession = scramClient.scramSession(USER);
        assertEquals(CLIENT_FIRST_MESSAGE, scramSession.clientFirstMessage());

        ScramSession.ServerFirstProcessor serverFirstProcessor = scramSession.receiveServerFirstMessage(
                SERVER_FIRST_MESSAGE
        );
        assertEquals(SERVER_SALT, serverFirstProcessor.getSalt());
        assertEquals(SERVER_ITERATIONS, serverFirstProcessor.getIteration());

        ScramSession.ClientFinalProcessor clientFinalProcessor = serverFirstProcessor.clientFinalProcessor(PASSWORD);
        assertEquals(CLIENT_FINAL_MESSAGE, clientFinalProcessor.clientFinalMessage());

        clientFinalProcessor.receiveServerFinalMessage(SERVER_FINAL_MESSAGE);
    }
}
