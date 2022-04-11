/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.message;

import static com.ongres.scram.common.util.Preconditions.checkNotEmpty;
import static com.ongres.scram.common.util.Preconditions.checkNotNull;

import com.ongres.scram.common.ScramAttributeValue;
import com.ongres.scram.common.ScramAttributes;
import com.ongres.scram.common.ScramStringFormatting;
import com.ongres.scram.common.gssapi.Gs2Header;
import com.ongres.scram.common.util.StringWritable;
import com.ongres.scram.common.util.StringWritableCsv;

/**
 * Constructs and parses client-final-messages.
 *
 * <p>Formal syntax is: {@code
 * client-final-message-without-proof = channel-binding "," nonce ["," extensions]
 * client-final-message = client-final-message-without-proof "," proof
 * }
 *
 * <p>Note that extensions are not supported.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5802#section-7">[RFC5802] Section 7</a>
 */
public class ClientFinalMessage implements StringWritable {

  private final String cbind;
  private final String nonce;
  private final byte[] proof;

  /**
   * Constructus a client-final-message with the provided gs2Header (the same one used in the
   * client-first-message), optionally the channel binding data, and the nonce. This method is
   * intended to be used by SCRAM clients, and not to be constructed directly.
   *
   * @param gs2Header The GSS-API header
   * @param cbindData If using channel binding, the channel binding data
   * @param nonce The nonce
   * @param proof The bytes representing the computed client proof
   */
  public ClientFinalMessage(Gs2Header gs2Header, byte[] cbindData, String nonce, byte[] proof) {
    this.cbind = generateCBind(
        checkNotNull(gs2Header, "gs2Header"),
        cbindData);
    this.nonce = checkNotEmpty(nonce, "nonce");
    this.proof = checkNotNull(proof, "proof");
  }

  private static String generateCBind(Gs2Header gs2Header, byte[] cbindData) {
    StringBuffer sb = new StringBuffer();
    gs2Header.writeTo(sb).append(',');

    if (null != cbindData) {
      new ScramAttributeValue(ScramAttributes.CHANNEL_BINDING,
          ScramStringFormatting.base64Encode(cbindData)).writeTo(sb);
    }

    return sb.toString();
  }

  private static StringBuffer writeToWithoutProof(StringBuffer sb, String cbind, String nonce) {
    return StringWritableCsv.writeTo(
        sb,
        new ScramAttributeValue(ScramAttributes.CHANNEL_BINDING,
            ScramStringFormatting.base64Encode(cbind)),
        new ScramAttributeValue(ScramAttributes.NONCE, nonce));
  }

  private static StringBuffer writeToWithoutProof(
      StringBuffer sb, Gs2Header gs2Header, byte[] cbindData, String nonce) {
    return writeToWithoutProof(
        sb,
        generateCBind(
            checkNotNull(gs2Header, "gs2Header"),
            cbindData),
        nonce);
  }

  /**
   * Returns a StringBuffer filled in with the formatted output of a client-first-message without
   * the proof value. This is useful for computing the auth-message, used in turn to compute the
   * proof.
   *
   * @param gs2Header The GSS-API header
   * @param cbindData The optional channel binding data
   * @param nonce The nonce
   * @return The String representation of the part of the message that excludes the proof
   */
  public static StringBuffer writeToWithoutProof(Gs2Header gs2Header, byte[] cbindData,
      String nonce) {
    return writeToWithoutProof(new StringBuffer(), gs2Header, cbindData, nonce);
  }

  @Override
  public final StringBuffer writeTo(StringBuffer sb) {
    writeToWithoutProof(sb, cbind, nonce);

    return StringWritableCsv.writeTo(
        sb,
        null, // This marks the position of writeToWithoutProof, required for the ","
        new ScramAttributeValue(ScramAttributes.CLIENT_PROOF,
            ScramStringFormatting.base64Encode(proof)));
  }

  @Override
  public String toString() {
    return writeTo(new StringBuffer()).toString();
  }
}
