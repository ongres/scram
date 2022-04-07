/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.message;

import com.ongres.scram.common.exception.ScramParseException;
import org.junit.Test;

import static com.ongres.scram.common.RfcExampleSha1.CLIENT_NONCE;
import static com.ongres.scram.common.RfcExampleSha1.SERVER_FIRST_MESSAGE;
import static org.junit.Assert.assertEquals;

public class ServerFirstMessageTest {
    @Test
    public void validConstructor() {
        ServerFirstMessage serverFirstMessage = new ServerFirstMessage(
                CLIENT_NONCE,
                "3rfcNHYJY1ZVvWVs7j",
                "QSXCR+Q6sek8bf92",
                4096
        );

        assertEquals(SERVER_FIRST_MESSAGE, serverFirstMessage.toString());
    }

    @Test
    public void validParseFrom() throws ScramParseException {
        ServerFirstMessage serverFirstMessage = ServerFirstMessage.parseFrom(SERVER_FIRST_MESSAGE, CLIENT_NONCE);

        assertEquals(SERVER_FIRST_MESSAGE, serverFirstMessage.toString());
    }
}
