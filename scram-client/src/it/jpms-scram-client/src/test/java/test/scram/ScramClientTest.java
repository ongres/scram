/*
 * Copyright (c) 2024 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package test.scram;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.Arrays;

import com.ongres.scram.client.ScramClient;
import com.ongres.scram.common.ScramFunctions;
import org.junit.jupiter.api.Test;

class ScramClientTest {

  @Test
  void accessPublic() {
    assertEquals("com.ongres.scram.client", ScramClient.class.getModule().getName());
    assertEquals("com.ongres.scram.common", ScramFunctions.class.getModule().getName());
  }

  @Test
  void testBuildClient() {
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
                + "s=W22ZaJ0SNY7soEsUEjb6gQ==,i=4096"));
    assertEquals(
        "c=biws,r=rOprNGfwEbeRWgbNEkqO%hvYDpWUa2RaTCAfuxFIlj)hNlF$k0,"
            + "p=dHzbZapWIk4jUhN+Ute9ytag9zjfMHgsqmmiz7AndVQ=",
        scramSession.clientFinalMessage().toString());
    assertDoesNotThrow(
        () -> scramSession.serverFinalMessage("v=6rriTRBi23WpRR/wtup+mMhUZUn/dB5nLTJRsjl95G4="));
  }

}
