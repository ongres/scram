/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.example;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import com.ongres.scram.client.ScramClient;
import com.ongres.scram.common.ClientFinalMessage;
import com.ongres.scram.common.util.TlsServerEndpoint;
import org.junit.jupiter.api.Test;

class ScramClientTest {

  @Test
  void completeTest()
      throws CertificateException, IOException {
    final X509Certificate cert = getCert();
    final byte[] channelBindingData = TlsServerEndpoint.getChannelBindingData(cert);

    ScramClient scramSession = ScramClient.builder()
        .advertisedMechanisms(Arrays.asList("SCRAM-SHA-256", "SCRAM-SHA-256-PLUS"))
        .username("user")
        .password("pencil".toCharArray())
        .channelBinding(TlsServerEndpoint.TLS_SERVER_END_POINT, channelBindingData)
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

  private X509Certificate getCert() throws CertificateException, IOException {
    String pemFilePath = "/SHA512withRSA.pem";
    // Create a CertificateFactory object for X.509
    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
    final X509Certificate cert;
    try (InputStream inputStream = getClass().getResourceAsStream(pemFilePath)) {
      // Check if the file exists
      assertNotNull(inputStream, "Certificate file not found");
      // Load the PEM file as an X.509 certificate
      cert = (X509Certificate) certFactory.generateCertificate(inputStream);
      // Perform your assertions or further tests here
      assertNotNull(cert);
      assertEquals("SHA512withRSA", cert.getSigAlgName());
    }
    return cert;
  }

  @Test
  void iterationTest()
      throws CertificateException, IOException {
    final X509Certificate cert = getCert();
    final byte[] channelBindingData = TlsServerEndpoint.getChannelBindingData(cert);

    ScramClient scramSession = ScramClient.builder()
        .advertisedMechanisms(Arrays.asList("SCRAM-SHA-256"))
        .username("postgres")
        .password("pencil".toCharArray())
        .channelBinding(TlsServerEndpoint.TLS_SERVER_END_POINT, channelBindingData)
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
}
