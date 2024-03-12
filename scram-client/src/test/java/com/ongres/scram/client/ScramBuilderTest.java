/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Base64;

import com.ongres.scram.common.ClientFinalMessage;
import com.ongres.scram.common.ScramFunctions;
import com.ongres.scram.common.ScramMechanism;
import com.ongres.scram.common.StringPreparation;
import com.ongres.scram.common.exception.ScramParseException;
import org.junit.jupiter.api.Test;

class ScramBuilderTest {
  @Test
  void getValid() throws ScramParseException {
    ScramClient client1 = ScramClient.builder()
        .advertisedMechanisms(Arrays.asList("SCRAM-SHA-1"))
        .username("*")
        .password("*".toCharArray())
        .stringPreparation(StringPreparation.POSTGRESQL_PREPARATION)
        .build();
    assertNotNull(client1);
    assertNotNull(client1.clientFirstMessage());
    ScramClient client2 = ScramClient.builder()
        .advertisedMechanisms(Arrays.asList("SCRAM-SHA-1", "SCRAM-SHA-256-PLUS"))
        .username("*")
        .password("*".toCharArray())
        .stringPreparation(StringPreparation.SASL_PREPARATION)
        .nonceLength(12)
        .channelBinding("tls-server-end-point", new byte[0])
        .build();
    assertNotNull(client2);
    assertNotNull(client2.clientFirstMessage());
    ScramClient client3 = ScramClient.builder()
        .advertisedMechanisms(Arrays.asList("SCRAM-SHA-1", "SCRAM-SHA-1-PLUS"))
        .username("*")
        .password("*".toCharArray())
        .stringPreparation(StringPreparation.NO_PREPARATION)
        .nonceLength(36)
        .build();
    assertNotNull(client3);
    assertNotNull(client3.clientFirstMessage());
    ScramClient client4 = ScramClient.builder()
        .advertisedMechanisms(Arrays.asList("SCRAM-SHA-1", "SCRAM-SHA-256-PLUS"))
        .username("*")
        .password("*".toCharArray())
        .stringPreparation(StringPreparation.NO_PREPARATION)
        .nonceLength(64)
        .build();
    assertNotNull(client4);
    assertNotNull(client4.clientFirstMessage());
    ScramClient client5 = ScramClient.builder()
        .advertisedMechanisms(Arrays.asList("SCRAM-SHA-1", "SCRAM-SHA-256-PLUS"))
        .username("*")
        .password("*".toCharArray())
        .stringPreparation(StringPreparation.NO_PREPARATION)
        // .secureRandomAlgorithmProvider("PKCS11", null)
        .nonceLength(64)
        .build();
    assertNotNull(client5);
    assertNotNull(client5.clientFirstMessage());
    ScramClient client6 = ScramClient.builder()
        .advertisedMechanisms(Arrays.asList("SCRAM-SHA-1"))
        .username("*")
        .password("*".toCharArray())
        .build();
    assertNotNull(client6);
    assertNotNull(client6.clientFirstMessage());
    ScramClient client7 = ScramClient.builder()
        .advertisedMechanisms(Arrays.asList("SCRAM-SHA-256-PLUS"))
        .username("*")
        .password("*".toCharArray())
        .channelBinding("tls-server-end-point", new byte[10])
        .stringPreparation(StringPreparation.NO_PREPARATION)
        .build();
    assertNotNull(client7);
    assertNotNull(client7.clientFirstMessage());

    byte[] saltedPassword = ScramFunctions.saltedPassword(ScramMechanism.SCRAM_SHA_1,
        StringPreparation.SASL_PREPARATION,
        RfcExampleSha1.PASSWORD.toCharArray(),
        Base64.getDecoder().decode(RfcExampleSha1.SERVER_SALT), 4096);
    ScramClient client8 = ScramClient.builder()
        .advertisedMechanisms(Arrays.asList("SCRAM-SHA-1", "SCRAM-SHA-1-PLUS"))
        .username(RfcExampleSha1.USER)
        .saltedPassword(saltedPassword)
        .nonceSupplier(() -> RfcExampleSha1.CLIENT_NONCE)
        .build();
    assertNotNull(client8);
    assertEquals("SCRAM-SHA-1", client8.getScramMechanism().getName());
    assertNotNull(client8.clientFirstMessage());
    assertThrows(IllegalStateException.class, () -> client8.clientFinalMessage());
    assertDoesNotThrow(() -> client8.serverFirstMessage(RfcExampleSha1.SERVER_FIRST_MESSAGE));
    assertThrows(IllegalStateException.class, () -> client8.clientFirstMessage());
    assertThrows(IllegalStateException.class,
        () -> client8.serverFinalMessage(RfcExampleSha1.SERVER_FINAL_MESSAGE));
    ClientFinalMessage clientFinalMessage = client8.clientFinalMessage();
    assertEquals(RfcExampleSha1.CLIENT_FINAL_MESSAGE, clientFinalMessage.toString());
    assertThrows(IllegalStateException.class,
        () -> client8.serverFirstMessage(RfcExampleSha1.SERVER_FIRST_MESSAGE));
    assertDoesNotThrow(
        () -> client8.serverFinalMessage(RfcExampleSha1.SERVER_FINAL_MESSAGE));
  }

  @Test
  void getInvalid() {
    assertThrows(IllegalArgumentException.class, () -> ScramClient.builder()
        .advertisedMechanisms(Arrays.asList("SCRAM-SHA-1-PLUS"))
        .username("*")
        .password("*".toCharArray())
        .stringPreparation(StringPreparation.NO_PREPARATION)
        .build());
  }

}
