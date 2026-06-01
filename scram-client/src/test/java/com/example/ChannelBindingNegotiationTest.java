/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;
import java.util.List;

import com.ongres.scram.client.ChannelBindingException;
import com.ongres.scram.client.ChannelBindingPolicy;
import com.ongres.scram.client.MechanismNegotiationException;
import com.ongres.scram.client.ScramClient;
import com.ongres.scram.common.ClientFirstMessage;
import com.ongres.scram.common.Gs2CbindFlag;
import com.ongres.scram.common.Gs2Header;
import com.ongres.scram.common.exception.ScramRuntimeException;
import com.ongres.scram.common.util.TlsServerEndpoint;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

/**
 * Validates mechanism negotiation rules and the calculation of GS2 headers based on the
 * client's {@link ChannelBindingPolicy} configuration and server capabilities.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5802#section-6">RFC 5802: Channels and Channel Binding</a>
 * @see <a href="https://tools.ietf.org/html/rfc7677#section-3">RFC 7677: SCRAM-SHA-256-PLUS Selection</a>
 */
@DisplayName("SCRAM Channel Binding Negotiation Tests")
class ChannelBindingNegotiationTest {

  private static final byte[] VALID_CBIND_DATA = Base64.getDecoder().decode(
      "Dv4abLuK1TiHcq3tJXrHODILGFQuC1M4kfP4w7dyRvjadaqGq8D/Po1XeJZpzUqal+mAKXNGytneo5KPOsJnYA==");

  private static final String TEST_NONCE = "rOprNGfwEbeRWgbNEkqO";
  private static final List<String> BARE_AND_PLUS = List.of("SCRAM-SHA-256-PLUS", "SCRAM-SHA-256");
  private static final List<String> BARE_ONLY = List.of("SCRAM-SHA-256");
  private static final List<String> PLUS_ONLY = List.of("SCRAM-SHA-256-PLUS");

  @Nested
  @DisplayName("Successful Negotiation Profiles")
  class SuccessfulPathNegotiations {

    @Test
    @DisplayName("ALLOW: Active Downgrade Protection ('y' flag) when server lacks -PLUS support")
    void policyAllow_ServerNoPlus_EmitsYFlag() {
      ScramClient client = createBaseBuilder(BARE_ONLY)
          .channelBindingPolicy(ChannelBindingPolicy.ALLOW)
          .channelBinding(TlsServerEndpoint.TLS_SERVER_END_POINT, VALID_CBIND_DATA)
          .build();

      assertEquals("SCRAM-SHA-256", client.getScramMechanism().getName());
      assertFalse(client.getScramMechanism().isPlus());
      Gs2Header gs2Header = getGs2Header(client);
      assertEquals(Gs2CbindFlag.CLIENT_YES_SERVER_NOT, gs2Header.getChannelBindingFlag());
    }

    @Test
    @DisplayName("ALLOW: Seamless upgrade to Channel Bound ('p' flag) when both peers support it")
    void policyAllow_ServerSupportsPlus_UpgradesToPFlag() {
      ScramClient client = createBaseBuilder(BARE_AND_PLUS)
          .channelBindingPolicy(ChannelBindingPolicy.ALLOW)
          .channelBinding(TlsServerEndpoint.TLS_SERVER_END_POINT, VALID_CBIND_DATA)
          .build();

      assertEquals("SCRAM-SHA-256-PLUS", client.getScramMechanism().getName());
      assertTrue(client.getScramMechanism().isPlus());
      Gs2Header gs2Header = getGs2Header(client);
      assertEquals(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED, gs2Header.getChannelBindingFlag());
      assertEquals(TlsServerEndpoint.TLS_SERVER_END_POINT, gs2Header.getChannelBindingName());
    }

    @Test
    @DisplayName("REQUIRE: Successfully establishes bound session when prerequisites match")
    void policyRequire_ValidEnvironment_EstablishesPFlag() {
      ScramClient client = createBaseBuilder(BARE_AND_PLUS)
          .channelBindingPolicy(ChannelBindingPolicy.REQUIRE)
          .channelBinding(TlsServerEndpoint.TLS_SERVER_END_POINT, VALID_CBIND_DATA)
          .build();

      assertEquals("SCRAM-SHA-256-PLUS", client.getScramMechanism().getName());
      assertTrue(client.getScramMechanism().isPlus());
      Gs2Header gs2Header = getGs2Header(client);
      assertEquals(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED, gs2Header.getChannelBindingFlag());
      assertEquals(TlsServerEndpoint.TLS_SERVER_END_POINT, gs2Header.getChannelBindingName());
    }

    @Test
    @DisplayName("DISABLE: Rejects channel data and forces standard bare mechanism ('n' flag)")
    void policyDisable_WithChannelData_ForcesNFlag() {
      ScramClient client = createBaseBuilder(BARE_AND_PLUS)
          .channelBindingPolicy(ChannelBindingPolicy.DISABLE)
          .channelBinding(TlsServerEndpoint.TLS_SERVER_END_POINT, VALID_CBIND_DATA)
          .build();

      assertEquals("SCRAM-SHA-256", client.getScramMechanism().getName());
      assertFalse(client.getScramMechanism().isPlus());
      Gs2Header gs2Header = getGs2Header(client);
      assertEquals(Gs2CbindFlag.CLIENT_NOT, gs2Header.getChannelBindingFlag());
    }

    @Test
    @DisplayName("ALLOW: Falls back to standard bare mechanism ('n' flag) if client lacks channel data")
    void policyAllow_MissingClientData_FallsBackToNFlag() {
      ScramClient client = createBaseBuilder(BARE_AND_PLUS)
          .channelBindingPolicy(ChannelBindingPolicy.ALLOW)
          // No channelBinding() configuration provided
          .build();

      assertEquals("SCRAM-SHA-256", client.getScramMechanism().getName());
      assertFalse(client.getScramMechanism().isPlus());
      Gs2Header gs2Header = getGs2Header(client);
      assertEquals(Gs2CbindFlag.CLIENT_NOT, gs2Header.getChannelBindingFlag());
    }
  }

  @Nested
  @DisplayName("Malformed Parameter and Structural Fallbacks")
  class DataFallbackNegotiations {

    @ParameterizedTest(name = "ALLOW fallback to 'n' flag when type is: [{0}]")
    @NullAndEmptySource
    void policyAllow_InvalidBindingType_FallsBackToNFlag(String invalidType) {
      ScramClient client = createBaseBuilder(BARE_AND_PLUS)
          .channelBindingPolicy(ChannelBindingPolicy.ALLOW)
          .channelBinding(invalidType, VALID_CBIND_DATA)
          .build();

      assertEquals("SCRAM-SHA-256", client.getScramMechanism().getName());
      assertFalse(client.getScramMechanism().isPlus());
      Gs2Header gs2Header = getGs2Header(client);
      assertEquals(Gs2CbindFlag.CLIENT_NOT, gs2Header.getChannelBindingFlag());
    }

    @ParameterizedTest(name = "ALLOW fallback to 'n' flag when raw bytes are: {0}")
    @NullAndEmptySource
    void policyAllow_InvalidBindingBytes_FallsBackToNFlag(byte[] invalidData) {
      ScramClient client = createBaseBuilder(BARE_AND_PLUS)
          .channelBindingPolicy(ChannelBindingPolicy.ALLOW)
          .channelBinding(TlsServerEndpoint.TLS_SERVER_END_POINT, invalidData)
          .build();

      assertEquals("SCRAM-SHA-256", client.getScramMechanism().getName());
      assertFalse(client.getScramMechanism().isPlus());
      Gs2Header gs2Header = getGs2Header(client);
      assertEquals(Gs2CbindFlag.CLIENT_NOT, gs2Header.getChannelBindingFlag());
    }

  }

  @Nested
  @DisplayName("Strict Enforcement and Error Paths")
  class EnforcementFailureNegotiations {

    @Test
    @DisplayName("REQUIRE: Aborts initialization if the server fails to advertise a -PLUS mechanism")
    void policyRequire_ServerLacksPlus_ThrowsException() {
      ChannelBindingException ex = assertThrows(ChannelBindingException.class,
          () -> createBaseBuilder(BARE_ONLY)
              .channelBindingPolicy(ChannelBindingPolicy.REQUIRE)
              .channelBinding(TlsServerEndpoint.TLS_SERVER_END_POINT, VALID_CBIND_DATA)
              .build());
      assertEquals("Channel binding is required, but the server does not support -PLUS mechanisms",
          ex.getMessage());
    }

    @Test
    @DisplayName("REQUIRE: Aborts initialization if binding tokens/data are absent")
    void policyRequire_ClientLacksData_ThrowsException() {
      ChannelBindingException ex = assertThrows(ChannelBindingException.class,
          () -> createBaseBuilder(BARE_AND_PLUS)
              .channelBindingPolicy(ChannelBindingPolicy.REQUIRE)
              // Missing channel binding parameters
              .build());
      assertEquals("Channel binding is required, but no channel binding data or type was provided",
          ex.getMessage());
    }

    @Test
    @DisplayName("ALLOW: Aborts if server demands -PLUS exclusively but client possesses no channel data")
    void policyAllow_ServerMandatesPlus_ClientHasNoData_ThrowsException() {
      MechanismNegotiationException ex = assertThrows(MechanismNegotiationException.class,
          () -> createBaseBuilder(PLUS_ONLY)
              .channelBindingPolicy(ChannelBindingPolicy.ALLOW)
              // Missing channel data
              .build());
      assertEquals("A non-PLUS mechanism was not advertised by the server", ex.getMessage());
    }
  }

  private static ScramClient.FinalBuildStage createBaseBuilder(List<String> mechanisms) {
    return ScramClient.builder()
        .advertisedMechanisms(mechanisms)
        .username("user")
        .password("pencil".toCharArray())
        .nonceSupplier(() -> TEST_NONCE);
  }

  private static @NotNull Gs2Header getGs2Header(ScramClient client) {
    ClientFirstMessage msg = client.clientFirstMessage();
    return msg.getGs2Header();
  }
}
