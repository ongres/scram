/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
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
   * @throws NoSuchAlgorithmException if no digest can be derived from the signature algorithm, or
   *         the digest is not supported by the installed security providers
   * @see <a href="https://www.rfc-editor.org/rfc/rfc5929#section-4.1">The tls-server-end-point
   *      Channel Binding Type</a>
   */
  private static MessageDigest getDigestAlgorithm(final X509Certificate serverCert)
      throws NoSuchAlgorithmException {
    final String signatureAlgorithm = serverCert.getSigAlgName();
    int index = signatureAlgorithm.indexOf("with");
    if (index <= 0) {
      // Signature algorithms without a single associated hash function (e.g. Ed25519, Ed448,
      // RSASSA-PSS, or post-quantum algorithms) have no defined tls-server-end-point hash.
      throw new NoSuchAlgorithmException(String.format(Locale.ROOT,
          "Could not determine server certificate signature algorithm. Name: %s, OID: %s",
          signatureAlgorithm, serverCert.getSigAlgOID()));
    }
    String algorithm = signatureAlgorithm.substring(0, index);
    // if the certificate's signatureAlgorithm uses a single hash
    // function and that hash function neither MD5 nor SHA-1, then use
    // the hash function associated with the certificate's signatureAlgorithm.
    if (!algorithm.startsWith("SHA3-")) {
      algorithm = algorithm.replace("SHA", "SHA-");
    }
    // if the certificate's signatureAlgorithm uses a single hash
    // function, and that hash function is either MD5 [RFC1321] or SHA-1
    // [RFC3174], then use SHA-256 [FIPS-180-3]
    if ("MD5".equals(algorithm) || "SHA-1".equals(algorithm)) {
      algorithm = "SHA-256";
    }

    return MessageDigest.getInstance(algorithm);
  }

  /**
   * The hash of the TLS server's certificate [RFC5280] as it appears, octet for octet, in the
   * server's Certificate message. Note that the Certificate message contains a certificate_list, in
   * which the first element is the server's certificate.
   *
   * <p>The TLS server's certificate bytes need to be hashed with SHA-256 if its signature algorithm
   * is MD5 or SHA-1 as per RFC 5929 (https://tools.ietf.org/html/rfc5929#section-4.1). If something
   * else is used, the same hash as the signature algorithm is used.
   *
   * <p><strong>Security note:</strong> if no hash can be derived for the certificate's signature
   * algorithm (for example Ed25519, Ed448 or post-quantum algorithms), this method returns an empty
   * array, and passing it to the {@code ScramClient} builder makes it negotiate a mechanism without
   * channel binding. Clients that require channel binding should use
   * {@link #getChannelBindingHash(X509Certificate)}, which throws instead.
   *
   * @param serverCert the TLS server's peer certificate
   * @return the hash of the TLS server's peer certificate, or an empty array if it can't be derived
   * @throws CertificateEncodingException if an encoding error occurs.
   * @see <a href="https://github.com/ongres/scram/security/advisories/GHSA-p9jg-fcr6-3mhf">
   *      GHSA-p9jg-fcr6-3mhf</a>
   */
  public static byte @NotNull [] getChannelBindingData(final @NotNull X509Certificate serverCert)
      throws CertificateEncodingException {
    try {
      return getChannelBindingHash(serverCert);
    } catch (NoSuchAlgorithmException e) {
      // Kept for backward compatibility with 3.2, see getChannelBindingHash.
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
   * algorithm is used, the same hash function as the signature algorithm is applied. Unlike
   * {@link #getChannelBindingData(X509Certificate)}, unsupported signature algorithms throw a
   * {@link NoSuchAlgorithmException} so that channel binding can't be silently downgraded.
   *
   * @param serverCert the TLS server's peer certificate
   * @return the hash of the TLS server's peer certificate
   * @throws CertificateEncodingException if an encoding error occurs
   * @throws NoSuchAlgorithmException if the required digest algorithm cannot be determined or is
   *         unsupported by the underlying security provider
   * @since 3.2.1
   */
  public static byte @NotNull [] getChannelBindingHash(final @NotNull X509Certificate serverCert)
      throws CertificateEncodingException, NoSuchAlgorithmException {
    MessageDigest digestAlgorithm = getDigestAlgorithm(serverCert);
    return digestAlgorithm.digest(serverCert.getEncoded());
  }

}
