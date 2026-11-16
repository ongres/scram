/*
 * Copyright (c) 2026 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Locale;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TlsServerEndpointTest {

  /**
   * Certificates that must produce a channel binding hash.
   * Format: Arguments.of(Certificate, Expected Signature Algorithm, Expected Hash Algorithm)
   */
  private static Stream<Arguments> provideValidCertificates() {
    return Stream.of(
        // RFC 5929 mandatory upgrade (MD5 -> SHA-256)
        Arguments.of(loadCertificate("/MD5withRSA.pem"), "MD5withRSA", "SHA-256"),
        Arguments.of(loadCertificate("/SHA224withRSA.pem"), "SHA224withRSA", "SHA-224"),
        Arguments.of(loadCertificate("/SHA256withECDSA.pem"), "SHA256withECDSA", "SHA-256"),
        Arguments.of(loadCertificate("/SHA512withRSA.pem"), "SHA512withRSA", "SHA-512"),
        Arguments.of(loadCertificate("/SHA512_224withRSA.pem"),
            "SHA512/224withRSA", "SHA-512/224"),
        Arguments.of(loadCertificate("/SHA512_256withRSA.pem"),
            "SHA512/256withRSA", "SHA-512/256"),
        // Skipped on JVMs without SHA3 support (e.g. Java 8)
        Arguments.of(loadCertificate("/SHA3-512withECDSA.pem"),
            "SHA3-512withECDSA", "SHA3-512"));
  }

  /**
   * Certificates whose signature algorithm has no single associated hash function, so no
   * {@code tls-server-end-point} hash can be derived (GHSA-p9jg-fcr6-3mhf).
   */
  private static Stream<Arguments> provideUnsupportedCertificates() {
    return Stream.of(
        Arguments.of(loadCertificate("/Ed25519.pem"), "Ed25519"),
        Arguments.of(loadCertificate("/Ed448.pem"), "Ed448"),
        Arguments.of(loadCertificate("/RSASSA-PSS.pem"), "RSASSA-PSS"));
  }

  @ParameterizedTest(name = "Extracts binding hash for {1} -> expects {2}")
  @MethodSource("provideValidCertificates")
  void validCertificateChannelBinding(X509Certificate cert, String sigAlg,
      String expectedHashAlg) {
    assertEquals(sigAlg, cert.getSigAlgName(),
        "Loaded certificate does not match expected algorithm");

    MessageDigest expectedDigest = getDigestOrSkipTest(expectedHashAlg);
    byte[] expectedHash = expectedDigest.digest(assertDoesNotThrow(cert::getEncoded));

    byte[] actualHash = assertDoesNotThrow(() -> TlsServerEndpoint.getChannelBindingHash(cert));
    assertArrayEquals(expectedHash, actualHash,
        "getChannelBindingHash did not return the expected digest bytes");

    byte[] actualData = assertDoesNotThrow(() -> TlsServerEndpoint.getChannelBindingData(cert));
    assertArrayEquals(expectedHash, actualData,
        "getChannelBindingData did not return the expected digest bytes");
  }

  @ParameterizedTest(name = "Rejects unsupported algorithm {1}")
  @MethodSource("provideUnsupportedCertificates")
  void unsupportedCertificateFailsClosed(X509Certificate cert, String sigAlg) {
    assertEquals(sigAlg, cert.getSigAlgName(),
        "Loaded certificate does not match expected algorithm");

    NoSuchAlgorithmException exception = assertThrows(NoSuchAlgorithmException.class,
        () -> TlsServerEndpoint.getChannelBindingHash(cert));
    assertEquals(String.format(Locale.ROOT,
        "Could not determine server certificate signature algorithm. Name: %s, OID: %s",
        cert.getSigAlgName(), cert.getSigAlgOID()),
        exception.getMessage());

    // Unchanged 3.2 behavior: the legacy method returns an empty array.
    byte[] legacyResult = assertDoesNotThrow(() -> TlsServerEndpoint.getChannelBindingData(cert));
    assertArrayEquals(new byte[0], legacyResult,
        "getChannelBindingData must return byte[0] on unsupported algorithms");
  }

  private static X509Certificate loadCertificate(String pemFilePath) {
    try (InputStream inputStream = TlsServerEndpointTest.class.getResourceAsStream(pemFilePath)) {
      assertNotNull(inputStream);
      CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
      X509Certificate cert = (X509Certificate) certFactory.generateCertificate(inputStream);
      assertNotNull(cert);
      return cert;
    } catch (CertificateException | IOException e) {
      throw new IllegalStateException("Failed to load test certificate: " + pemFilePath, e);
    }
  }

  private static MessageDigest getDigestOrSkipTest(String digestAlgorithm) {
    try {
      return MessageDigest.getInstance(digestAlgorithm);
    } catch (NoSuchAlgorithmException e) {
      Assumptions.abort("Skipping test: current JVM does not support " + digestAlgorithm);
      return null; // Unreachable
    }
  }
}
