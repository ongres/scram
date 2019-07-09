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


import com.ongres.scram.common.ScramAttributes;
import com.ongres.scram.common.ScramFunctions;
import com.ongres.scram.common.ScramMechanisms;
import com.ongres.scram.common.bouncycastle.base64.Base64;
import com.ongres.scram.common.exception.ScramParseException;
import com.ongres.scram.common.stringprep.StringPreparations;
import org.junit.Test;

import static com.ongres.scram.common.RfcExampleSha1.*;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ServerFinalMessageTest {
    @Test
    public void validConstructor() {
        byte[] serverKey = ScramFunctions.serverKey(
                ScramMechanisms.SCRAM_SHA_1,
                StringPreparations.NO_PREPARATION,
                PASSWORD,
                Base64.decode(SERVER_SALT),
                SERVER_ITERATIONS
        );
        ServerFinalMessage serverFinalMessage1 = new ServerFinalMessage(
                ScramFunctions.serverSignature(ScramMechanisms.SCRAM_SHA_1, serverKey, AUTH_MESSAGE)
        );
        assertEquals(SERVER_FINAL_MESSAGE, serverFinalMessage1.toString());
        assertFalse(serverFinalMessage1.isError());

        ServerFinalMessage serverFinalMessage2 = new ServerFinalMessage(ServerFinalMessage.Error.UNKNOWN_USER);
        assertEquals(ScramAttributes.ERROR.getChar() + "=" + "unknown-user", serverFinalMessage2.toString());
        assertTrue(serverFinalMessage2.isError());
    }

    @Test
    public void validParseFrom() throws ScramParseException {
        ServerFinalMessage serverFinalMessage1 = ServerFinalMessage.parseFrom(SERVER_FINAL_MESSAGE);
        assertEquals(SERVER_FINAL_MESSAGE, serverFinalMessage1.toString());
        assertFalse(serverFinalMessage1.isError());

        ServerFinalMessage serverFinalMessage2 = ServerFinalMessage.parseFrom("e=channel-binding-not-supported");
        assertEquals("e=channel-binding-not-supported", serverFinalMessage2.toString());
        assertTrue(serverFinalMessage2.isError());
        assertTrue(serverFinalMessage2.getError() == ServerFinalMessage.Error.CHANNEL_BINDING_NOT_SUPPORTED);
    }
}
