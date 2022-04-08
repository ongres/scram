/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.client;

import static com.ongres.scram.common.RfcExampleSha1.CLIENT_FINAL_MESSAGE;
import static com.ongres.scram.common.RfcExampleSha1.CLIENT_FIRST_MESSAGE;
import static com.ongres.scram.common.RfcExampleSha1.CLIENT_NONCE;
import static com.ongres.scram.common.RfcExampleSha1.PASSWORD;
import static com.ongres.scram.common.RfcExampleSha1.SERVER_FINAL_MESSAGE;
import static com.ongres.scram.common.RfcExampleSha1.SERVER_FIRST_MESSAGE;
import static com.ongres.scram.common.RfcExampleSha1.SERVER_ITERATIONS;
import static com.ongres.scram.common.RfcExampleSha1.SERVER_SALT;
import static com.ongres.scram.common.RfcExampleSha1.USER;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ongres.scram.common.exception.ScramInvalidServerSignatureException;
import com.ongres.scram.common.exception.ScramParseException;
import com.ongres.scram.common.exception.ScramServerErrorException;
import com.ongres.scram.common.stringprep.StringPreparations;
import org.junit.jupiter.api.Test;

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
