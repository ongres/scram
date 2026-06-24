/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.example;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

import com.ongres.scram.client.ChannelBindingException;
import com.ongres.scram.client.ChannelBindingPolicy;
import com.ongres.scram.client.MechanismNegotiationException;
import com.ongres.scram.client.ScramClient;
import com.ongres.scram.common.ClientFinalMessage;
import com.ongres.scram.common.ClientFirstMessage;
import com.ongres.scram.common.Gs2CbindFlag;
import com.ongres.scram.common.Gs2Header;
import com.ongres.scram.common.ServerFinalMessage;
import com.ongres.scram.common.ServerFirstMessage;
import com.ongres.scram.common.util.TlsServerEndpoint;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
    @DisplayName("REQUIRE: Successfully establishes bound session with certificate")
    void policyRequire_ValidCertificate_EstablishesPFlag() {
      ScramClient client = createBaseBuilder(BARE_AND_PLUS)
          .channelBindingPolicy(ChannelBindingPolicy.REQUIRE)
          .channelBinding(loadCertificate("/SHA256withECDSA.pem"))
          .build();

      assertEquals("SCRAM-SHA-256-PLUS", client.getScramMechanism().getName());
      assertTrue(client.getScramMechanism().isPlus());

      // Validate ClientFirstMessage
      ClientFirstMessage clientFirstMessage = client.clientFirstMessage();
      Gs2Header gs2Header = clientFirstMessage.getGs2Header();
      assertEquals(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED, gs2Header.getChannelBindingFlag());
      assertEquals(TlsServerEndpoint.TLS_SERVER_END_POINT, gs2Header.getChannelBindingName());
      assertEquals(TEST_NONCE, clientFirstMessage.getClientNonce());
      // Validate ServerFirstMessage
      ServerFirstMessage serverFirstMessage = assertDoesNotThrow(
          () -> client.serverFirstMessage(
              "r=rOprNGfwEbeRWgbNEkqO1q^MGrWUi{etW+H7(#k431kB,"
                  + "s=Fgh8JU2AlRjBHUsIU/GgtQ==,"
                  + "i=600000"));
      assertEquals("rOprNGfwEbeRWgbNEkqO", serverFirstMessage.getClientNonce());
      assertEquals("1q^MGrWUi{etW+H7(#k431kB", serverFirstMessage.getServerNonce());
      assertEquals("Fgh8JU2AlRjBHUsIU/GgtQ==", serverFirstMessage.getSalt());
      assertEquals(600000, serverFirstMessage.getIterationCount());
      // Validate ClientFinalMessage
      ClientFinalMessage clientFinalMessage = client.clientFinalMessage();
      assertEquals("cD10bHMtc2VydmVyLWVuZC1wb2ludCwsaTF/YqwjDjfDcMRe33RXDzQJqzxBgI+/HTBO7gLIOCc=",
          clientFinalMessage.getCbindInput());
      assertEquals("rOprNGfwEbeRWgbNEkqO1q^MGrWUi{etW+H7(#k431kB", clientFinalMessage.getNonce());
      assertEquals("wy5BxhT8TbbKv0gpnaoIisolhxk4uhQPEAJDY8unCW8=",
          new String(Base64.getEncoder().encode(clientFinalMessage.getProof()), StandardCharsets.ISO_8859_1));
      // Validate ServerFinalMessage
      ServerFinalMessage serverFinalMessage = assertDoesNotThrow(
          () -> client.serverFinalMessage("v=RE2+e+A0hGuE+BnaNo5L7A2ZtwY2KXQayVKi+rthmw8="));
      assertFalse(serverFinalMessage.isError());
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
    @DisplayName("REQUIRE: Ed25519 not supported")
    void policyRequire_Ed25519_ThrowsException() {
      ChannelBindingException ex = assertThrows(ChannelBindingException.class,
          () -> createBaseBuilder(BARE_AND_PLUS)
              .channelBindingPolicy(ChannelBindingPolicy.REQUIRE)
              .channelBinding(loadCertificate("/Ed25519.pem"))
              .build());
      assertEquals("Channel binding is required, but no channel binding data or type was provided",
          ex.getMessage());
      Throwable cause = ex.getCause();
      assertTrue(cause instanceof NoSuchAlgorithmException);
      assertEquals("Could not determine server certificate signature algorithm. "
          + "Name: Ed25519, OID: 1.3.101.112",
          cause.getMessage());
    }

    @Test
    @DisplayName("Aborts if the channelBinding called both methods")
    void policyRequire_DualSet_ThrowsException() {
      IllegalStateException ex = assertThrows(IllegalStateException.class,
          () -> createBaseBuilder(BARE_AND_PLUS)
              .channelBinding(loadCertificate("/SHA512withRSA.pem"))
              .channelBinding(TlsServerEndpoint.TLS_SERVER_END_POINT, VALID_CBIND_DATA)
              .build());
      assertEquals("channelBinding(String, byte[]) called but channel binding "
          + "was already configured via channelBinding(X509Certificate)", ex.getMessage());

      IllegalStateException ex2 = assertThrows(IllegalStateException.class,
          () -> createBaseBuilder(BARE_AND_PLUS)
              .channelBinding(TlsServerEndpoint.TLS_SERVER_END_POINT, VALID_CBIND_DATA)
              .channelBinding(loadCertificate("/SHA512withRSA.pem"))
              .build());
      assertEquals("channelBinding(X509Certificate) called but channel binding "
          + "was already configured via channelBinding(String, byte[])", ex2.getMessage());
    }

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

  @ParameterizedTest
  @MethodSource("provideValidCertificates")
  void testValidCertificateChannelBinding(X509Certificate cert) {
    ScramClient client = createBaseBuilder(BARE_AND_PLUS)
        .channelBindingPolicy(ChannelBindingPolicy.REQUIRE)
        .channelBinding(cert)
        .build();

    assertEquals("SCRAM-SHA-256-PLUS", client.getScramMechanism().getName());
    assertTrue(client.getScramMechanism().isPlus());
    Gs2Header gs2Header = getGs2Header(client);
    assertEquals(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED, gs2Header.getChannelBindingFlag());
    assertEquals(TlsServerEndpoint.TLS_SERVER_END_POINT, gs2Header.getChannelBindingName());
  }

  /**
   * Provides valid certificates that SHOULD succeed in generating a channel binding hash.
   * Format: Arguments.of(Certificate, Expected Signature Algorithm, Expected Hash Algorithm)
   */
  private static Stream<Arguments> provideValidCertificates() {
    return Stream.of(
        // RFC 5929 Mandatory Upgrades (MD5 -> SHA-256)
        Arguments.of(loadCertificate("/MD5withRSA.pem"),
            "MD5withRSA", "SHA-256"),

        // Standard explicit algorithms
        Arguments.of(loadCertificate("/SHA224withRSA.pem"),
            "SHA224withRSA", "SHA-224"),
        Arguments.of(loadCertificate("/SHA256withECDSA.pem"),
            "SHA256withECDSA", "SHA-256"),
        Arguments.of(loadCertificate("/SHA512withRSA.pem"),
            "SHA512withRSA", "SHA-512"),
        Arguments.of(loadCertificate("/SHA512_224withRSA.pem"),
            "SHA512/224withRSA", "SHA-512/224"),
        Arguments.of(loadCertificate("/SHA512_256withRSA.pem"),
            "SHA512/256withRSA", "SHA-512/256"),

        // Modern algorithms (Might be skipped on Java 8 if provider is missing)
        Arguments.of(loadCertificate("/SHA3-512withECDSA.pem"),
            "SHA3-512withECDSA", "SHA3-512"),

        // Parameterized RSASSA-PSS with SHA-384 digest
        Arguments.of(loadCertificate("/RSASSA-PSS.pem"),
            "RSASSA-PSS", "SHA-384"),

        // RSASSA-PSS with SHA-1 digest: RFC 5929 mandatory upgrade SHA-1 -> SHA-256
        Arguments.of(loadCertificate("/RSASSA-PSS-SHA1.pem"),
            "RSASSA-PSS", "SHA-256"));
  }

  /**
   * Helper method to load a certificate from the classpath.
   */
  private static X509Certificate loadCertificate(String pemFilePath) {
    try (InputStream inputStream = ChannelBindingNegotiationTest.class.getResourceAsStream(pemFilePath)) {
      assertNotNull(inputStream);

      CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
      X509Certificate cert = (X509Certificate) certFactory.generateCertificate(inputStream);

      assertNotNull(cert);
      return cert;
    } catch (CertificateException | IOException e) {
      throw new RuntimeException("Failed to load test certificate: " + pemFilePath, e);
    }
  }

}
