/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

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
   * @param signatureAlgorithm the signature algorithm name for the certificate signature algorithm
   * @return the MessageDigest algorithm, or {@code null} if the name is not recognized
   * @see <a href="https://www.rfc-editor.org/rfc/rfc5929#section-4.1">The tls-server-end-point
   *      Channel Binding Type</a>
   */
  private static MessageDigest getDigestAlgorithm(String signatureAlgorithm) {
    int index = signatureAlgorithm.indexOf("with");
    signatureAlgorithm = index > 0 ? signatureAlgorithm.substring(0, index) : "SHA-256";
    // if the certificate's signatureAlgorithm uses a single hash
    // function and that hash function neither MD5 nor SHA-1, then use
    // the hash function associated with the certificate's signatureAlgorithm.
    if (!signatureAlgorithm.startsWith("SHA3-")) {
      signatureAlgorithm = signatureAlgorithm.replace("SHA", "SHA-");
    }
    // if the certificate's signatureAlgorithm uses a single hash
    // function, and that hash function is either MD5 [RFC1321] or SHA-1
    // [RFC3174], then use SHA-256 [FIPS-180-3]
    if ("MD5".equals(signatureAlgorithm) || "SHA-1".equals(signatureAlgorithm)) {
      signatureAlgorithm = "SHA-256";
    }

    try {
      return MessageDigest.getInstance(signatureAlgorithm);
    } catch (NoSuchAlgorithmException e) {
      return null;
    }
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
   * @param serverCert the TLS server's peer certificate
   * @return the hash of the TLS server's peer certificate
   * @throws CertificateEncodingException if an encoding error occurs.
   */
  public static byte @NotNull [] getChannelBindingData(final @NotNull X509Certificate serverCert)
      throws CertificateEncodingException {
    MessageDigest digestAlgorithm = getDigestAlgorithm(serverCert.getSigAlgName());
    if (digestAlgorithm == null) {
      return new byte[0];
    }
    return digestAlgorithm.digest(serverCert.getEncoded());
  }

}
