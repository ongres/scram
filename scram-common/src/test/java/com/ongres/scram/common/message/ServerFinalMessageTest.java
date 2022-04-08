/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.message;

import static com.ongres.scram.common.RfcExampleSha1.AUTH_MESSAGE;
import static com.ongres.scram.common.RfcExampleSha1.PASSWORD;
import static com.ongres.scram.common.RfcExampleSha1.SERVER_FINAL_MESSAGE;
import static com.ongres.scram.common.RfcExampleSha1.SERVER_ITERATIONS;
import static com.ongres.scram.common.RfcExampleSha1.SERVER_SALT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ongres.scram.common.ScramAttributes;
import com.ongres.scram.common.ScramFunctions;
import com.ongres.scram.common.ScramMechanisms;
import com.ongres.scram.common.bouncycastle.base64.Base64;
import com.ongres.scram.common.exception.ScramParseException;
import com.ongres.scram.common.stringprep.StringPreparations;
import org.junit.jupiter.api.Test;

class ServerFinalMessageTest {
  @Test
  void validConstructor() {
    byte[] serverKey = ScramFunctions.serverKey(
        ScramMechanisms.SCRAM_SHA_1,
        StringPreparations.NO_PREPARATION,
        PASSWORD,
        Base64.decode(SERVER_SALT),
        SERVER_ITERATIONS);
    ServerFinalMessage serverFinalMessage1 = new ServerFinalMessage(
        ScramFunctions.serverSignature(ScramMechanisms.SCRAM_SHA_1, serverKey, AUTH_MESSAGE));
    assertEquals(SERVER_FINAL_MESSAGE, serverFinalMessage1.toString());
    assertFalse(serverFinalMessage1.isError());

    ServerFinalMessage serverFinalMessage2 =
        new ServerFinalMessage(ServerFinalMessage.Error.UNKNOWN_USER);
    assertEquals(ScramAttributes.ERROR.getChar() + "=" + "unknown-user",
        serverFinalMessage2.toString());
    assertTrue(serverFinalMessage2.isError());
  }

  @Test
  void validParseFrom() throws ScramParseException {
    ServerFinalMessage serverFinalMessage1 = ServerFinalMessage.parseFrom(SERVER_FINAL_MESSAGE);
    assertEquals(SERVER_FINAL_MESSAGE, serverFinalMessage1.toString());
    assertFalse(serverFinalMessage1.isError());

    ServerFinalMessage serverFinalMessage2 =
        ServerFinalMessage.parseFrom("e=channel-binding-not-supported");
    assertEquals("e=channel-binding-not-supported", serverFinalMessage2.toString());
    assertTrue(serverFinalMessage2.isError());
    assertTrue(
        serverFinalMessage2.getError() == ServerFinalMessage.Error.CHANNEL_BINDING_NOT_SUPPORTED);
  }
}
