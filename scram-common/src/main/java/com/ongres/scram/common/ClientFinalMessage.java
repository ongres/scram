/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import static com.ongres.scram.common.util.Preconditions.checkNotEmpty;
import static com.ongres.scram.common.util.Preconditions.checkNotNull;

import java.nio.charset.StandardCharsets;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Constructs and parses client-final-messages.
 *
 *
 * <table>
 * <caption>Formal Syntax:</caption>
 * <tr>
 * <td>cbind-input</td>
 * <td>gs2-header [ cbind-data ]<br>
 * ;; cbind-data MUST be present for<br>
 * ;; gs2-cbind-flag of "p" and MUST be absent<br>
 * ;; for "y" or "n".</td>
 * <tr>
 * <td>channel-binding</td>
 * <td>"c=" base64<br>
 * ;; base64 encoding of cbind-input.</td>
 * </tr>
 * <tr>
 * <td>client-final-message-without-proof</td>
 * <td>channel-binding "," nonce ["," extensions]</td>
 * </tr>
 * <tr>
 * <td>client-final-message</td>
 * <td>client-final-message-without-proof "," proof</td>
 * </tr>
 * </table>
 *
 * @implNote {@code extensions} are not supported.
 * @see <a href="https://tools.ietf.org/html/rfc5802#section-7">[RFC5802] Section 7</a>
 */
public final class ClientFinalMessage extends AbstractScramMessage {

  /**
   * channel-binding = "c=" base64 encoding of cbind-input.
   */
  private final String cbindInput;

  /**
   * nonce = "r=" c-nonce [s-nonce]. Second part provided by server.
   */
  private final String nonce;

  /**
   * proof = "p=" base64.
   */
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
    this.cbindInput = generateCBindInput(gs2Header, cbindData);
    this.nonce = checkNotEmpty(nonce, "nonce");
    this.proof = checkNotNull(proof, "proof").clone();
  }

  /**
   * Return the channel-binding "c=" base64 encoding of cbind-input.
   *
   * @return the {@code channel-binding}
   */
  public String getCbindInput() {
    return cbindInput;
  }

  /**
   * Return the nonce.
   *
   * @return the {@code nonce}
   */
  public String getNonce() {
    return nonce;
  }

  /**
   * Return the proof.
   *
   * @return the {@code proof}
   */
  public byte[] getProof() {
    return proof.clone();
  }

  private static void checkChannelBinding(Gs2Header gs2Header, byte[] cbindData) {
    final Gs2CbindFlag channelBindingFlag = gs2Header.getChannelBindingFlag();
    if (channelBindingFlag == Gs2CbindFlag.CHANNEL_BINDING_REQUIRED
        && null == cbindData) {
      throw new IllegalArgumentException("Channel binding data is required");
    }
    if (channelBindingFlag != Gs2CbindFlag.CHANNEL_BINDING_REQUIRED
        && null != cbindData) {
      throw new IllegalArgumentException("Channel binding data should not be present");
    }
  }

  private static @NotNull String generateCBindInput(@NotNull Gs2Header gs2Header,
      byte @Nullable [] cbindData) {
    checkNotNull(gs2Header, "gs2Header");
    checkChannelBinding(gs2Header, cbindData);

    byte[] cbindInput = gs2Header.writeTo(new StringBuilder(32))
        .append(',').toString().getBytes(StandardCharsets.UTF_8);

    if (null != cbindData && cbindData.length != 0) {
      byte[] cbindInputNew = new byte[cbindInput.length + cbindData.length];
      System.arraycopy(cbindInput, 0, cbindInputNew, 0, cbindInput.length);
      System.arraycopy(cbindData, 0, cbindInputNew, cbindInput.length, cbindData.length);
      cbindInput = cbindInputNew;
    }

    return ScramStringFormatting.base64Encode(cbindInput);
  }

  private StringBuilder writeToWithoutProof(@NotNull StringBuilder sb) {
    return StringWritableCsv.writeTo(sb,
        new ScramAttributeValue(ScramAttributes.CHANNEL_BINDING, cbindInput),
        new ScramAttributeValue(ScramAttributes.NONCE, nonce));
  }

  static StringBuilder withoutProof(StringBuilder sb, Gs2Header gs2Header, byte[] cbindData,
      String nonce) {
    return StringWritableCsv.writeTo(sb,
        new ScramAttributeValue(ScramAttributes.CHANNEL_BINDING,
            generateCBindInput(gs2Header, cbindData)),
        new ScramAttributeValue(ScramAttributes.NONCE, nonce));
  }

  @Override
  StringBuilder writeTo(StringBuilder sb) {
    writeToWithoutProof(sb);

    return StringWritableCsv.writeTo(
        sb,
        null, // This marks the position of writeToWithoutProof, required for the ","
        new ScramAttributeValue(ScramAttributes.CLIENT_PROOF,
            ScramStringFormatting.base64Encode(proof)));
  }

}
