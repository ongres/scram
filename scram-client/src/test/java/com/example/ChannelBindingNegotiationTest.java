/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Base64;

import com.ongres.scram.client.ScramClient;
import com.ongres.scram.common.ClientFirstMessage;
import com.ongres.scram.common.Gs2CbindFlag;
import com.ongres.scram.common.exception.ScramParseException;
import com.ongres.scram.common.util.TlsServerEndpoint;
import org.junit.jupiter.api.Test;

/**
 * Servers that support the use of channel binding SHOULD advertise
 * both the non-PLUS (SCRAM-{@code hash-function}) and PLUS-variant (SCRAM-
 * {@code hash-function}-PLUS) mechanism name. If the server cannot
 * support channel binding, it SHOULD advertise only the non-PLUS-
 * variant. If the server would never succeed in the authentication
 * of the non-PLUS-variant due to policy reasons, it MUST advertise
 * only the PLUS-variant.
 */
class ChannelBindingNegotiationTest {

  private static final byte[] CBIND_DATA = Base64.getDecoder().decode("Dv4abLuK1TiHcq3tJXrHODILGF"
      + "QuC1M4kfP4w7dyRvjadaqGq8D/Po1XeJZpzUqal+mAKXNGytneo5KPOsJnYA==");

  /**
   * If the client supports channel binding and the server does not
   * appear to (i.e., the client did not see the -PLUS name advertised
   * by the server), then the client MUST NOT use an "n" gs2-cbind-
   * flag.
   */
  @Test
  void clientYesServerNo() throws ScramParseException {
    ScramClient scramSession = ScramClient.builder()
        .advertisedMechanisms(Arrays.asList("SCRAM-SHA-256"))
        .username("user")
        .password("pencil".toCharArray())
        .channelBinding(TlsServerEndpoint.TLS_SERVER_END_POINT, CBIND_DATA)
        .nonceSupplier(() -> "rOprNGfwEbeRWgbNEkqO")
        .build();
    assertEquals("SCRAM-SHA-256", scramSession.getScramMechanism().getName());
    ClientFirstMessage msg = scramSession.clientFirstMessage();
    assertEquals(Gs2CbindFlag.CLIENT_YES_SERVER_NOT, msg.getGs2Header().getChannelBindingFlag());
  }

  /**
   * Clients that support mechanism negotiation and channel binding
   * MUST use a "p" gs2-cbind-flag when the server offers the PLUS-
   * variant of the desired GS2 mechanism.
   */
  @Test
  void channelBindingRequired() throws ScramParseException {
    ScramClient scramSession = ScramClient.builder()
        .advertisedMechanisms(Arrays.asList("SCRAM-SHA-256", "SCRAM-SHA-256-PLUS"))
        .username("user")
        .password("pencil".toCharArray())
        .channelBinding(TlsServerEndpoint.TLS_SERVER_END_POINT, CBIND_DATA)
        .nonceSupplier(() -> "rOprNGfwEbeRWgbNEkqO")
        .build();
    assertEquals("SCRAM-SHA-256-PLUS", scramSession.getScramMechanism().getName());
    ClientFirstMessage msg = scramSession.clientFirstMessage();
    assertEquals(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED, msg.getGs2Header().getChannelBindingFlag());
  }

  /**
   * If the client does not support channel binding, then it MUST use
   * an "n" gs2-cbind-flag. Conversely, if the client requires the use
   * of channel binding then it MUST use a "p" gs2-cbind-flag. Clients
   * that do not support mechanism negotiation never use a "y" gs2-
   * cbind-flag, they use either "p" or "n" according to whether they
   * require and support the use of channel binding or whether they do
   * not, respectively.
   */
  @Test
  void clientNo() throws ScramParseException {
    ScramClient scramSession = ScramClient.builder()
        .advertisedMechanisms(Arrays.asList("SCRAM-SHA-256", "SCRAM-SHA-256-PLUS"))
        .username("user")
        .password("pencil".toCharArray())
        .nonceSupplier(() -> "rOprNGfwEbeRWgbNEkqO")
        .build();
    assertEquals("SCRAM-SHA-256", scramSession.getScramMechanism().getName());
    ClientFirstMessage msg = scramSession.clientFirstMessage();
    assertEquals(Gs2CbindFlag.CLIENT_NOT, msg.getGs2Header().getChannelBindingFlag());
  }

}
