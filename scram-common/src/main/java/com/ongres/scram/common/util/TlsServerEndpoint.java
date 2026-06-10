/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.util;

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.spec.PSSParameterSpec;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jetbrains.annotations.NotNull;

/**
 * Utilitiy for extracting the {@code "tls-server-end-point"} channel binding data.
 *
 * @apiNote This is not part of the public API of the SCRAM library, it's provided as a helper to
 *          extract the channel-binding data and could be renamed or removed at any time.
 */
public final class TlsServerEndpoint {

  /**
   * The "tls-server-end-point" Channel Binding Type.
   */
  public static final String TLS_SERVER_END_POINT = "tls-server-end-point";

  /**
   * A static mapping of universally standard signature OIDs to their underlying digest algorithms.
   * This guarantees resolution even if the JCE provider fails to provide a friendly string name.
   */
  private static final Map<String, String> OID_TO_DIGEST;

  static {
    ConcurrentMap<String, String> map = new ConcurrentHashMap<>();
    // OIDs associated with RSA Signatures
    map.put("1.2.840.113549.1.1.4", "MD5"); // MD5withRSA
    map.put("1.2.840.113549.1.1.5", "SHA-1"); // SHA1withRSA
    map.put("1.2.840.113549.1.1.11", "SHA-256"); // SHA256withRSA
    map.put("1.2.840.113549.1.1.12", "SHA-384"); // SHA384withRSA
    map.put("1.2.840.113549.1.1.13", "SHA-512"); // SHA512withRSA
    map.put("1.2.840.113549.1.1.14", "SHA-224"); // SHA224withRSA
    map.put("1.2.840.113549.1.1.15", "SHA-512/224"); // SHA512/224withRSA
    map.put("1.2.840.113549.1.1.16", "SHA-512/256"); // SHA512/256withRSA
    map.put("2.16.840.1.101.3.4.3.13", "SHA3-224"); // SHA3-224withRSA
    map.put("2.16.840.1.101.3.4.3.14", "SHA3-256"); // SHA3-256withRSA
    map.put("2.16.840.1.101.3.4.3.15", "SHA3-384"); // SHA3-384withRSA
    map.put("2.16.840.1.101.3.4.3.16", "SHA3-512"); // SHA3-512withRSA
    // OIDs associated with ECDSA Signatures
    map.put("1.2.840.10045.4.1", "SHA-1"); // SHA1withECDSA
    map.put("1.2.840.10045.4.3.1", "SHA-224"); // SHA224withECDSA
    map.put("1.2.840.10045.4.3.2", "SHA-256"); // SHA256withECDSA
    map.put("1.2.840.10045.4.3.3", "SHA-384"); // SHA384withECDSA
    map.put("1.2.840.10045.4.3.4", "SHA-512"); // SHA512withECDSA
    map.put("2.16.840.1.101.3.4.3.9", "SHA3-224"); // SHA3-224withECDSA
    map.put("2.16.840.1.101.3.4.3.10", "SHA3-256"); // SHA3-256withECDSA
    map.put("2.16.840.1.101.3.4.3.11", "SHA3-384"); // SHA3-384withECDSA
    map.put("2.16.840.1.101.3.4.3.12", "SHA3-512"); // SHA3-512withECDSA
    // OIDs associated with DSA Signatures
    map.put("1.2.840.10040.4.3", "SHA-1"); // SHA1withDSA
    map.put("2.16.840.1.101.3.4.3.1", "SHA-224"); // SHA224withDSA
    map.put("2.16.840.1.101.3.4.3.2", "SHA-256"); // SHA256withDSA
    map.put("2.16.840.1.101.3.4.3.3", "SHA-384"); // SHA384withDSA
    map.put("2.16.840.1.101.3.4.3.4", "SHA-512"); // SHA512withDSA
    map.put("2.16.840.1.101.3.4.3.5", "SHA3-224"); // SHA3-224withDSA
    map.put("2.16.840.1.101.3.4.3.6", "SHA3-256"); // SHA3-256withDSA
    map.put("2.16.840.1.101.3.4.3.7", "SHA3-384"); // SHA3-384withDSA
    map.put("2.16.840.1.101.3.4.3.8", "SHA3-512"); // SHA3-512withDSA

    OID_TO_DIGEST = Collections.unmodifiableMap(map);
  }

  private TlsServerEndpoint() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Get the digest algorithm that would be used for a given signature algorithm name.
   *
   * <p>The TLS server's certificate bytes need to be hashed with SHA-256 if its signature algorithm
   * is MD5 or SHA-1 as per RFC 5929 (https://tools.ietf.org/html/rfc5929#section-4.1). If something
   * else is used, the same hash as the signature algorithm is used.
   *
   * @param serverCert the TLS server's peer certificate
   * @return the MessageDigest algorithm
   * @throws NoSuchAlgorithmException if the signature algorithm is unrecognized or unmapped
   * @see <a href="https://www.rfc-editor.org/rfc/rfc5929#section-4.1">The tls-server-end-point
   *      Channel Binding Type</a>
   */
  private static MessageDigest getDigestAlgorithm(final X509Certificate serverCert)
      throws NoSuchAlgorithmException {

    String sigAlgOID = serverCert.getSigAlgOID();
    String sigAlgName = serverCert.getSigAlgName();
    sigAlgName = sigAlgName != null ? sigAlgName.trim().toUpperCase(Locale.ROOT) : null;

    // Guard against Pure Signatures (Ed25519 / Ed448)
    if ("1.3.101.112".equals(sigAlgOID) || "1.3.101.113".equals(sigAlgOID)) { //NOPMD
      throw new NoSuchAlgorithmException(
          String.format(Locale.ROOT, "Could not determine server certificate signature algorithm. Name: %s, OID: %s",
              serverCert.getSigAlgName(), sigAlgOID));
    }

    String algorithmName = null;

    // Handle Parameterized Algorithms (Bury the hash in ASN.1 params)
    if ("1.2.840.113549.1.1.10".equals(sigAlgOID) || "RSASSA-PSS".equals(sigAlgName)) {
      byte[] sigAlgParams = serverCert.getSigAlgParams();
      if (sigAlgParams == null) {
        throw new NoSuchAlgorithmException("RSASSA-PSS signature parameters are missing");
      }
      try {
        AlgorithmParameters params = AlgorithmParameters.getInstance("RSASSA-PSS");
        params.init(sigAlgParams);
        PSSParameterSpec spec = params.getParameterSpec(PSSParameterSpec.class);
        algorithmName = spec.getDigestAlgorithm().trim().toUpperCase(Locale.ROOT);
      } catch (IOException | GeneralSecurityException e) {
        throw new NoSuchAlgorithmException("Could not extract hash from RSASSA-PSS parameters", e);
      }
    } else if (sigAlgOID != null && OID_TO_DIGEST.containsKey(sigAlgOID)) {
      // Fast-Pass Exact OID Mapping
      algorithmName = OID_TO_DIGEST.get(sigAlgOID);
    } else if (sigAlgName != null) {
      // Handle Traditional Algorithms (Hash is explicitly in the name, e.g., "SHA256withRSA")
      int index = sigAlgName.indexOf("WITH");
      if (index > 0) {
        // Extract hash from signature name e.g., "SHA256" or "SHA3-512"
        algorithmName = sigAlgName.substring(0, index).trim().toUpperCase(Locale.ROOT);
      }
    }

    if (algorithmName == null) {
      throw new NoSuchAlgorithmException(
          String.format(Locale.ROOT, "Could not determine server certificate signature algorithm. Name: %s, OID: %s",
              serverCert.getSigAlgName(), sigAlgOID));
    }

    // Normalize the hash name (Applies to both Traditional and RSASSA-PSS)
    switch (algorithmName) {
      // Enforce RFC 5929 Mandatory Upgrades to SHA-256
      case "MD5":
      case "SHA1":
      case "SHA-1":
      case "SHA256":
        algorithmName = "SHA-256";
        break;
      case "SHA224":
        algorithmName = "SHA-224";
        break;
      case "SHA384":
        algorithmName = "SHA-384";
        break;
      case "SHA512":
        algorithmName = "SHA-512";
        break;
      case "SHA512/224":
        algorithmName = "SHA-512/224";
        break;
      case "SHA512/256":
        algorithmName = "SHA-512/256";
        break;
      default:
        // Pass-through for valid modern/standardized names like:
        // "SHA-512", "SHA-384", "SHA3-256", "SHA3-512", etc.
        break;
    }

    return MessageDigest.getInstance(algorithmName);
  }

  /**
   * The hash of the TLS server's certificate [RFC5280] as it appears, octet for octet, in the
   * server's Certificate message.
   *
   * @param serverCert the TLS server's peer certificate
   * @return the hash of the TLS server's peer certificate
   * @throws CertificateEncodingException if an encoding error occurs.
   * @deprecated this method silently swallows {@link NoSuchAlgorithmException} and returns an
   *             empty array. It is replaced by {@link #getChannelBindingHash(X509Certificate)}
   *             and will be removed in a future release.
   */
  @Deprecated
  public static byte @NotNull [] getChannelBindingData(final @NotNull X509Certificate serverCert)
      throws CertificateEncodingException {
    try {
      return getChannelBindingHash(serverCert);
    } catch (NoSuchAlgorithmException e) {
      // Preserve the old (and dangerous) silent-failure behavior for backward compatibility
      return new byte[0];
    }
  }

  /**
   * Computes the hash of the TLS server's certificate [RFC5280] as it appears, octet for octet, in
   * the server's Certificate message, for use as {@code "tls-server-end-point"} channel binding.
   *
   * <p>The TLS server's certificate bytes need to be hashed with {@code SHA-256} if its signature
   * algorithm is {@code MD5} or {@code SHA-1} as per
   * <a href="https://tools.ietf.org/html/rfc5929#section-4.1">RFC 5929 Section 4.1</a>. If another
   * algorithm is used, the same hash function as the signature algorithm is applied. Unsupported
   * or unmapped signature structures throw a {@link NoSuchAlgorithmException}.
   *
   * @param serverCert the TLS server's peer certificate
   * @return the hash of the TLS server's peer certificate
   * @throws CertificateEncodingException if an encoding error occurs
   * @throws NoSuchAlgorithmException if the required digest algorithm cannot be determined or is
   *         unsupported by the underlying security provider
   */
  public static byte @NotNull [] getChannelBindingHash(final @NotNull X509Certificate serverCert)
      throws CertificateEncodingException, NoSuchAlgorithmException {
    MessageDigest digestAlgorithm = getDigestAlgorithm(serverCert);
    return digestAlgorithm.digest(serverCert.getEncoded());
  }

}
