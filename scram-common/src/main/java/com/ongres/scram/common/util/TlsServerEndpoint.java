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
import java.util.Locale;

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
    String sigAlgName = serverCert.getSigAlgName();
    if (sigAlgName == null) {
      throw new NoSuchAlgorithmException("Certificate signature algorithm is null");
    }
    // Normalize name to upper case
    sigAlgName = sigAlgName.toUpperCase(Locale.ROOT);

    String algorithmName;

    // Handle Parameterized Algorithms (Bury the hash in ASN.1 params)
    if ("RSASSA-PSS".equals(sigAlgName)) {
      byte[] sigAlgParams = serverCert.getSigAlgParams();
      if (sigAlgParams == null) {
        throw new NoSuchAlgorithmException("RSASSA-PSS signature parameters are missing");
      }
      try {
        AlgorithmParameters params = AlgorithmParameters.getInstance("RSASSA-PSS");
        params.init(sigAlgParams);
        PSSParameterSpec spec = params.getParameterSpec(PSSParameterSpec.class);
        algorithmName = spec.getDigestAlgorithm().toUpperCase(Locale.ROOT);
      } catch (IOException | GeneralSecurityException e) {
        throw new NoSuchAlgorithmException("Could not extract hash from RSASSA-PSS parameters", e);
      }
    } else {
      // Handle Traditional Algorithms (Hash is explicitly in the name, e.g., "SHA256withRSA")
      int index = sigAlgName.indexOf("WITH");
      if (index > 0) {
        algorithmName = sigAlgName.substring(0, index); // e.g., "SHA256" or "SHA3-512"
      } else {
        // Correctly mirrors PostgreSQL/OpenSSL behavior: Ed25519/Ed448 yield NID_undef.
        // We must fail here because the Postgres server will also fail.
        throw new NoSuchAlgorithmException(
            "Could not determine server certificate signature algorithm: "
                + serverCert.getSigAlgName());
      }
    }

    // Normalize the hash name (Applies to both Traditional and RSASSA-PSS)
    switch (algorithmName) {
      // Enforce RFC 5929 Mandatory Upgrades (MD5 or SHA-1 must become SHA-256)
      case "MD5":
      case "SHA1":
      case "SHA-1":
      case "SHA256":
      case "SHA-256":
        algorithmName = "SHA-256";
        break;
      case "SHA224":
      case "SHA-224":
        algorithmName = "SHA-224";
        break;
      case "SHA384":
      case "SHA-384":
        algorithmName = "SHA-384";
        break;
      case "SHA512":
      case "SHA-512":
        algorithmName = "SHA-512";
        break;
      default:
        // Pass-through for valid modern algorithms like SHA3-512
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
   *           unsupported by the underlying security provider
   */
  public static byte @NotNull [] getChannelBindingHash(final @NotNull X509Certificate serverCert)
      throws CertificateEncodingException, NoSuchAlgorithmException {
    MessageDigest digestAlgorithm = getDigestAlgorithm(serverCert);
    return digestAlgorithm.digest(serverCert.getEncoded());
  }

}
