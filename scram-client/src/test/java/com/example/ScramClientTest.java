/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.example;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Base64;

import com.ongres.scram.client.ScramClient;
import com.ongres.scram.common.ClientFinalMessage;
import com.ongres.scram.common.ScramFunctions;
import com.ongres.scram.common.ScramMechanism;
import com.ongres.scram.common.StringPreparation;
import com.ongres.scram.common.exception.ScramInvalidServerSignatureException;
import com.ongres.scram.common.exception.ScramServerErrorException;
import com.ongres.scram.common.util.TlsServerEndpoint;
import org.junit.jupiter.api.Test;

class ScramClientTest {

  private static final byte[] CBIND_DATA = Base64.getDecoder().decode("Dv4abLuK1TiHcq3tJXrHODILGF"
      + "QuC1M4kfP4w7dyRvjadaqGq8D/Po1XeJZpzUqal+mAKXNGytneo5KPOsJnYA==");

  @Test
  void completeTest() {
    ScramClient scramSession = ScramClient.builder()
        .advertisedMechanisms(Arrays.asList("SCRAM-SHA-256", "SCRAM-SHA-256-PLUS"))
        .username("user")
        .password("pencil".toCharArray())
        .channelBinding(TlsServerEndpoint.TLS_SERVER_END_POINT, CBIND_DATA)
        .nonceSupplier(() -> "rOprNGfwEbeRWgbNEkqO")
        .build();
    assertEquals("SCRAM-SHA-256-PLUS", scramSession.getScramMechanism().getName());
    assertEquals("p=tls-server-end-point,,n=user,r=rOprNGfwEbeRWgbNEkqO",
        scramSession.clientFirstMessage().toString());

    assertDoesNotThrow(
        () -> scramSession.serverFirstMessage(
            "r=rOprNGfwEbeRWgbNEkqO%hvYDpWUa2RaTCAfuxFIlj)hNlF$k0,"
                + "s=W22ZaJ0SNY7soEsUEjb6gQ==,"
                + "i=4096"));

    ClientFinalMessage clientFinalMessage = scramSession.clientFinalMessage();
    assertEquals(
        "c=cD10bHMtc2VydmVyLWVuZC1wb2ludCwsDv4abLuK1TiHcq3tJ"
            + "XrHODILGFQuC1M4kfP4w7dyRvjadaqGq8D/Po1XeJZpzUqal+mAKXNGytneo5KPOsJnYA=="
            + ",r=rOprNGfwEbeRWgbNEkqO%hvYDpWUa2RaTCAfuxFIlj)hNlF$k0"
            + ",p=WIBtRXGH4I4R2CU1/tHa2YREwrJjLFa3/pKJQH/0Ofo=",
        clientFinalMessage.toString());

    assertDoesNotThrow(
        () -> scramSession.serverFinalMessage("v=9k31qsYXd74d6BnbFf9jE+r9un6a8ou85FYeNxDAdqc="));
  }

  @Test
  void completeTestWithoutChannelBinding() {
    ScramClient scramSession = ScramClient.builder()
        .advertisedMechanisms(Arrays.asList("SCRAM-SHA-256", "SCRAM-SHA-256-PLUS"))
        .username("user")
        .password("pencil".toCharArray())
        .nonceSupplier(() -> "rOprNGfwEbeRWgbNEkqO")
        .build();
    assertEquals("SCRAM-SHA-256", scramSession.getScramMechanism().getName());
    assertEquals("n,,n=user,r=rOprNGfwEbeRWgbNEkqO",
        scramSession.clientFirstMessage().toString());

    assertDoesNotThrow(
        () -> scramSession.serverFirstMessage(
            "r=rOprNGfwEbeRWgbNEkqO%hvYDpWUa2RaTCAfuxFIlj)hNlF$k0,"
                + "s=W22ZaJ0SNY7soEsUEjb6gQ==,"
                + "i=4096"));

    ClientFinalMessage clientFinalMessage = scramSession.clientFinalMessage();
    assertEquals(
        "c=biws,r=rOprNGfwEbeRWgbNEkqO%hvYDpWUa2RaTCAfuxFIlj)hNlF$k0"
            + ",p=dHzbZapWIk4jUhN+Ute9ytag9zjfMHgsqmmiz7AndVQ=",
        clientFinalMessage.toString());

    assertDoesNotThrow(
        () -> scramSession.serverFinalMessage("v=6rriTRBi23WpRR/wtup+mMhUZUn/dB5nLTJRsjl95G4="));
  }

  @Test
  void iterationTest() {
    ScramClient scramSession = ScramClient.builder()
        .advertisedMechanisms(Arrays.asList("SCRAM-SHA-256"))
        .username("postgres")
        .password("pencil".toCharArray())
        .channelBinding(TlsServerEndpoint.TLS_SERVER_END_POINT, CBIND_DATA)
        .nonceSupplier(() -> "1q^MGrWUi{etW+H7(#k431kB")
        .build();
    assertEquals("SCRAM-SHA-256", scramSession.getScramMechanism().getName());
    assertEquals("y,,n=postgres,r=1q^MGrWUi{etW+H7(#k431kB",
        scramSession.clientFirstMessage().toString());

    assertDoesNotThrow(
        () -> scramSession.serverFirstMessage(
            "r=1q^MGrWUi{etW+H7(#k431kBdAr3CWX7B6houDP4f7Z2XEpZ,"
                + "s=Fgh8JU2AlRjBHUsIU/GgtQ==,"
                + "i=1000000"));

    ClientFinalMessage clientFinalMessage = scramSession.clientFinalMessage();
    assertEquals(
        "c=eSws,"
            + "r=1q^MGrWUi{etW+H7(#k431kBdAr3CWX7B6houDP4f7Z2XEpZ,"
            + "p=vQ3IyYl3LvjWOlK2c0IP5QAi6XB7Dm0Axo0V51DcHZA=",
        clientFinalMessage.toString());

    assertDoesNotThrow(
        () -> scramSession.serverFinalMessage("v=sz/isCwVSUn/TBWeYABz6WaoZIcfsui9NPaJCoxxAjY="));
  }

  @Test
  void preComputedKeysTest() throws Exception {
    ScramMechanism mechanism = ScramMechanism.SCRAM_SHA_256;
    byte[] salt = Base64.getDecoder().decode("W22ZaJ0SNY7soEsUEjb6gQ==");
    int iterationCount = 4096;

    // Pre-computed clientKey/serverKey
    byte[] saltedPassword = ScramFunctions.saltedPassword(
        mechanism, StringPreparation.SASL_PREPARATION, "pencil".toCharArray(), salt, iterationCount
    );
    byte[] clientKey = ScramFunctions.clientKey(mechanism, saltedPassword);
    byte[] serverKey = ScramFunctions.serverKey(mechanism, saltedPassword);

    // Initialize the client utilizing the optimized pre-computed keys tracking option
    ScramClient scramSession = ScramClient.builder()
        .advertisedMechanisms(Arrays.asList("SCRAM-SHA-256"))
        .username("user")
        .clientAndServerKey(clientKey, serverKey)
        .nonceSupplier(() -> "rOprNGfwEbeRWgbNEkqO")
        .build();

    // Verify step processing yields identical proofs and matches execution vectors exactly
    assertEquals("SCRAM-SHA-256", scramSession.getScramMechanism().getName());
    assertEquals("n,,n=user,r=rOprNGfwEbeRWgbNEkqO", scramSession.clientFirstMessage().toString());

    assertDoesNotThrow(
        () -> scramSession.serverFirstMessage(
            "r=rOprNGfwEbeRWgbNEkqO%hvYDpWUa2RaTCAfuxFIlj)hNlF$k0,"
                + "s=W22ZaJ0SNY7soEsUEjb6gQ==,"
                + "i=4096"));

    ClientFinalMessage clientFinalMessage = scramSession.clientFinalMessage();
    assertEquals(
        "c=biws,r=rOprNGfwEbeRWgbNEkqO%hvYDpWUa2RaTCAfuxFIlj)hNlF$k0"
            + ",p=dHzbZapWIk4jUhN+Ute9ytag9zjfMHgsqmmiz7AndVQ=",
        clientFinalMessage.toString());

    assertDoesNotThrow(
        () -> scramSession.serverFinalMessage("v=6rriTRBi23WpRR/wtup+mMhUZUn/dB5nLTJRsjl95G4="));
  }

  @Test
  void throwScramInvalidServerSignatureException() {
    ScramClient scramSession = ScramClient.builder()
        .advertisedMechanisms(Arrays.asList("SCRAM-SHA-256"))
        .username("postgres")
        .password("pencil".toCharArray())
        .channelBinding(TlsServerEndpoint.TLS_SERVER_END_POINT, CBIND_DATA)
        .nonceSupplier(() -> "1q^MGrWUi{etW+H7(#k431kB")
        .build();
    scramSession.clientFirstMessage();

    assertDoesNotThrow(
        () -> scramSession.serverFirstMessage(
            "r=1q^MGrWUi{etW+H7(#k431kBdAr3CWX7B6houDP4f7Z2XEpZ,"
                + "s=Fgh8JU2AlRjBHUsIU/GgtQ==,"
                + "i=10"));
    scramSession.clientFinalMessage();

    // The server returns an ivalid signature
    assertThrows(ScramInvalidServerSignatureException.class,
        () -> scramSession.serverFinalMessage("v=Fgh8JU2AlRjBHUsIU/GgtQ=="));
  }

  @Test
  void throwScramServerErrorException() {
    ScramClient scramSession = ScramClient.builder()
        .advertisedMechanisms(Arrays.asList("SCRAM-SHA-256", "SCRAM-SHA-512"))
        .username("postgres")
        .password("pencil".toCharArray())
        .channelBinding(TlsServerEndpoint.TLS_SERVER_END_POINT, CBIND_DATA)
        .nonceSupplier(() -> "1q^MGrWUi{etW+H7(#k431kB")
        .build();

    scramSession.clientFirstMessage();
    assertDoesNotThrow(
        () -> scramSession.serverFirstMessage(
            "r=1q^MGrWUi{etW+H7(#k431kBdAr3CWX7B6houDP4f7Z2XEpZ,"
                + "s=Fgh8JU2AlRjBHUsIU/GgtQ==,"
                + "i=10"));
    scramSession.clientFinalMessage();

    // Simulate that the server returns an error
    assertThrows(ScramServerErrorException.class,
        () -> scramSession.serverFinalMessage("e=invalid-proof"));
  }

}
