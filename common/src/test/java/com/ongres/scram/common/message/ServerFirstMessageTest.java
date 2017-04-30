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


package com.ongres.scram.common.message;


import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class ServerFirstMessageTest {
    private static final String CLIENT_NONCE = "fyko+d2lbbFgONRv9qkxdawL";
    private static final String SERVER_FIRST_MESSAGE =
            "r=fyko+d2lbbFgONRv9qkxdawL3rfcNHYJY1ZVvWVs7j,s=QSXCR+Q6sek8bf92,i=4096";

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
    public void validParseFrom() {
        ServerFirstMessage serverFirstMessage = ServerFirstMessage.parseFrom(SERVER_FIRST_MESSAGE, CLIENT_NONCE);

        assertEquals(SERVER_FIRST_MESSAGE, serverFirstMessage.toString());
    }
}
