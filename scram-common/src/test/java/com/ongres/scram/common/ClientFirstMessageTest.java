/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import static com.ongres.scram.common.RfcExampleSha1.CLIENT_NONCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ongres.scram.common.exception.ScramParseException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ClientFirstMessageTest {

  @ParameterizedTest
  @CsvSource(value = {"CLIENT_NOT,cbind,,user,nonce",
      "CHANNEL_BINDING_REQUIRED,,,user,nonce",
      "CHANNEL_BINDING_REQUIRED,tls-*,,user,nonce",
      "CLIENT_YES_SERVER_NOT,,,,nonce",
      "CLIENT_YES_SERVER_NOT,,,'',nonce",
      "CHANNEL_BINDING_REQUIRED,tls-export,,u,",
      "CHANNEL_BINDING_REQUIRED,tls-export,,u,''"})
  void constructorTestInvalid(@NotNull Gs2CbindFlag flag, String cbName, String authzid,
      @NotNull String user, @NotNull String nonce) {
    assertThrows(IllegalArgumentException.class,
        () -> new ClientFirstMessage(flag, cbName, authzid, user, nonce));
  }

  @Test
  void writeToValidValues() {
    assertEquals("n,,n=user,r=fyko",
        new ClientFirstMessage("user", "fyko").toString());
    assertEquals("y,,n=user,r=fyko",
        new ClientFirstMessage(Gs2CbindFlag.CLIENT_YES_SERVER_NOT, null, null, "user", "fyko")
            .toString());
    assertEquals("p=tls-server-end-point,,n=user,r=fyko",
        new ClientFirstMessage(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED, "tls-server-end-point", null,
            "user", "fyko").toString());
    assertEquals("p=tls-server-end-point,a=authzid,n=user,r=fyko",
        new ClientFirstMessage(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED, "tls-server-end-point",
            "authzid", "user", "fyko").toString());
  }

  @Test
  void parseToValidValues() throws ScramParseException {
    assertEquals(ClientFirstMessage.parseFrom("n,,n=user,r=fyko").toString(),
        new ClientFirstMessage("user", "fyko").toString());
    assertEquals(ClientFirstMessage.parseFrom("y,,n=user,r=fyko").toString(),
        new ClientFirstMessage(Gs2CbindFlag.CLIENT_YES_SERVER_NOT, null, null, "user", "fyko")
            .toString());
    assertEquals(ClientFirstMessage.parseFrom("p=tls-server-end-point,,n=user,r=fyko").toString(),
        new ClientFirstMessage(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED, "tls-server-end-point", null,
            "user", "fyko").toString());
    assertEquals(
        ClientFirstMessage.parseFrom("p=tls-server-end-point,a=authzid,n=user,r=fyko").toString(),
        new ClientFirstMessage(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED, "tls-server-end-point",
            "authzid", "user", "fyko").toString());
  }

  @Test
  void parseFromValidValues() throws ScramParseException {
    ClientFirstMessage m1 = ClientFirstMessage.parseFrom("n,,n=user,r=" + CLIENT_NONCE);
    assertFalse(m1.isChannelBindingRequired());
    assertSame(Gs2CbindFlag.CLIENT_NOT, m1.getGs2Header().getChannelBindingFlag());
    assertNull(m1.getGs2Header().getChannelBindingName());
    assertNull(m1.getGs2Header().getAuthzid());
    assertEquals("user", m1.getUsername());
    assertEquals(CLIENT_NONCE, m1.getClientNonce());

    ClientFirstMessage m2 = ClientFirstMessage.parseFrom("y,,n=user,r=" + CLIENT_NONCE);
    assertTrue(
        !m2.isChannelBindingRequired()
            && m2.getGs2Header().getChannelBindingFlag() == Gs2CbindFlag.CLIENT_YES_SERVER_NOT
            && null == m2.getGs2Header().getAuthzid() && "user".equals(m2.getUsername())
            && CLIENT_NONCE.equals(m2.getClientNonce()));

    ClientFirstMessage m3 = ClientFirstMessage.parseFrom("y,a=user2,n=user,r=" + CLIENT_NONCE);
    assertTrue(
        !m3.isChannelBindingRequired()
            && m3.getGs2Header().getChannelBindingFlag() == Gs2CbindFlag.CLIENT_YES_SERVER_NOT
            && null != m3.getGs2Header().getAuthzid()
            && "user2".equals(m3.getGs2Header().getAuthzid())
            && "user".equals(m3.getUsername()) && CLIENT_NONCE.equals(m3.getClientNonce()));

    ClientFirstMessage m4 =
        ClientFirstMessage.parseFrom("p=tls-unique,a=user2,n=user,r=" + CLIENT_NONCE);
    assertTrue(m4.isChannelBindingRequired());
    assertSame(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED, m4.getGs2Header().getChannelBindingFlag());
    assertNotNull(m4.getGs2Header().getChannelBindingName());
    assertEquals("tls-unique", m4.getGs2Header().getChannelBindingName());
    assertNotNull(m4.getGs2Header().getAuthzid());
    assertEquals("user2", m4.getGs2Header().getAuthzid());
    assertEquals("user", m4.getUsername());
    assertEquals(CLIENT_NONCE, m4.getClientNonce());
  }

  @Test
  void parseFromInvalidValues() {
    String[] invalidValues = new String[] {
        "n,,r=user,r=" + CLIENT_NONCE, "n,,z=user,r=" + CLIENT_NONCE, "n,,n=user", "n,", "n,,",
        "n,,n=user,r", "n,,n=user,r="
    };

    int n = 0;
    for (String s : invalidValues) {
      try {
        assertNotNull(ClientFirstMessage.parseFrom(s));
      } catch (ScramParseException e) {
        n++;
      }
    }

    assertEquals(invalidValues.length, n);
  }
}
