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

        // Parameterized RSASSA-PSS (The generated PEM uses SHA-384)
        Arguments.of(loadCertificate("/RSASSA-PSS.pem"),
            "RSASSA-PSS", "SHA-384"));
  }

  /**
   * Provides certificates that MUST fail to maintain parity with PostgreSQL / OpenSSL behavior.
   */
  private static Stream<Arguments> provideUnsupportedCertificates() {
    return Stream.of(
        Arguments.of(loadCertificate("/Ed25519.pem"), "Ed25519"),
        Arguments.of(loadCertificate("/Ed448.pem"), "Ed448"));
  }

  @SuppressWarnings("deprecation")
  @ParameterizedTest(name = "Extracts correct binding hash for {1} -> expects {2}")
  @MethodSource("provideValidCertificates")
  void testValidCertificateChannelBinding(X509Certificate cert, String sigAlg,
      String expectedHashAlg) {
    // Ensure the generated PEM matches the test expectation
    assertEquals(sigAlg, cert.getSigAlgName(),
        "Loaded certificate does not match expected algorithm");

    // 1. Check if the current JDK/Provider supports the required hash (e.g., SHA3-512 on Java 8)
    MessageDigest expectedDigest = getDigestOrSkipTest(expectedHashAlg);

    // 2. Calculate the expected hash manually
    byte[] expectedHash = expectedDigest.digest(assertDoesNotThrow(cert::getEncoded));

    // 3. Test the primary method
    byte[] actualHash = assertDoesNotThrow(() -> TlsServerEndpoint.getChannelBindingHash(cert));
    assertArrayEquals(expectedHash, actualHash,
        "getChannelBindingHash did not return the expected digest bytes");

    // 4. Test the deprecated method
    byte[] actualDeprecatedData = assertDoesNotThrow(() -> TlsServerEndpoint.getChannelBindingData(cert));
    assertArrayEquals(expectedHash, actualDeprecatedData,
        "Deprecated getChannelBindingData did not match expected digest bytes");
  }

  @SuppressWarnings({ "deprecation" })
  @ParameterizedTest(name = "Rejects unsupported/pure algorithm {1} (Postgres parity)")
  @MethodSource("provideUnsupportedCertificates")
  void testUnsupportedCertificatesFail(X509Certificate cert, String sigAlg) {
    // Ensure the generated PEM matches the test expectation
    assertEquals(sigAlg, cert.getSigAlgName(),
        "Loaded certificate does not match expected algorithm");

    // 1. Ensure the modern method throws an exception
    NoSuchAlgorithmException exception = assertThrows(NoSuchAlgorithmException.class,
        () -> TlsServerEndpoint.getChannelBindingHash(cert));

    assertEquals(String.format(Locale.ROOT,
        "Could not determine server certificate signature algorithm. Name: %s, OID: %s",
        cert.getSigAlgName(), cert.getSigAlgOID()),
        exception.getMessage());

    // 2. Ensure the deprecated method swallows the exception and returns an empty array
    byte[] deprecatedResult = assertDoesNotThrow(() -> TlsServerEndpoint.getChannelBindingData(cert));
    assertArrayEquals(new byte[0], deprecatedResult,
        "Deprecated method must return byte[0] on unsupported algorithms");
  }

  /**
   * Helper method to load a certificate from the classpath.
   */
  private static X509Certificate loadCertificate(String pemFilePath) {
    try (InputStream inputStream = TlsServerEndpointTest.class.getResourceAsStream(pemFilePath)) {
      assertNotNull(inputStream);

      CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
      X509Certificate cert = (X509Certificate) certFactory.generateCertificate(inputStream);

      assertNotNull(cert);
      return cert;
    } catch (CertificateException | IOException e) {
      throw new RuntimeException("Failed to load test certificate: " + pemFilePath, e);
    }
  }

  /**
   * Attempts to load the MessageDigest. If the JVM does not support it, it skips the test.
   */
  private MessageDigest getDigestOrSkipTest(String digestAlgorithm) {
    try {
      return MessageDigest.getInstance(digestAlgorithm);
    } catch (NoSuchAlgorithmException e) {
      Assumptions.abort("Skipping test: Current JVM/Provider does not support " + digestAlgorithm);
      return null; // Unreachable
    }
  }
}
