/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;

import org.junit.jupiter.api.Test;

class ScramFunctionsTest {
  private void assertBytesEqualsBase64(String expected, byte[] actual) {
    assertArrayEquals(ScramStringFormatting.base64Decode(expected), actual);
  }

  @Test
  void hmac() {
    String message = "The quick brown fox jumps over the lazy dog";
    byte[] key = generateSaltedPasswordSha256();
    assertBytesEqualsBase64(
        "1zw4SuJ+BRmn6corI3Y1+eGQwGQ=",
        ScramFunctions.hmac(ScramMechanism.SCRAM_SHA_1,
            key, message.getBytes(StandardCharsets.US_ASCII)));
    assertBytesEqualsBase64(
        "+Q4a/8FjMG6MoLE/8y8LWyEBJmjpVLbuXtU8rnLd/5E=",
        ScramFunctions.hmac(ScramMechanism.SCRAM_SHA_256,
            key, message.getBytes(StandardCharsets.US_ASCII)));
    assertBytesEqualsBase64(
        "uu/n7DW8kGuWWANfaHT/rwEU/EBufylMLTWOCtLmvxp"
            + "2Zmx01UpZO4nauZfkaSWob8jt7no0+xWIVXv/d7LvkQ==",
        ScramFunctions.hmac(ScramMechanism.SCRAM_SHA_512,
            key, message.getBytes(StandardCharsets.US_ASCII)));
  }

  private byte[] generateSaltedPassword() {
    return ScramFunctions.saltedPassword(
        ScramMechanism.SCRAM_SHA_1, StringPreparation.SASL_PREPARATION, "pencil".toCharArray(),
        Base64.getDecoder().decode("QSXCR+Q6sek8bf92".getBytes(StandardCharsets.UTF_8)), 4096);
  }

  private byte[] generateSaltedPasswordSha256() {
    byte[] salt = Base64.getDecoder().decode("Fgh8JU2AlRjBHUsIU/GgtQ==");
    byte[] saltedPassword = ScramFunctions.saltedPassword(
        ScramMechanism.SCRAM_SHA_256, StringPreparation.SASL_PREPARATION, "test".toCharArray(),
        salt, 4096);
    byte[] clientKey = ScramFunctions.clientKey(ScramMechanism.SCRAM_SHA_256, saltedPassword);
    byte[] storedKey = ScramFunctions.storedKey(ScramMechanism.SCRAM_SHA_256, clientKey);
    byte[] serverKey = ScramFunctions.serverKey(ScramMechanism.SCRAM_SHA_256, saltedPassword);
    String encodeToStringSalt = Base64.getEncoder().encodeToString(salt);
    String encodeToStringClient = Base64.getEncoder().encodeToString(storedKey);
    String encodeToStringServer = Base64.getEncoder().encodeToString(serverKey);
    assertEquals("XiT346dvVvPmnmTWeW0djrcMYBGuiQDh8QYbBJaBm/I=", encodeToStringClient);
    assertEquals("CY9vUvDF8v6FIR8Zwircvd82YV58J5AwWiMWwfssuwg=", encodeToStringServer);
    String pw =
        String.format(Locale.ROOT, "%S$%d:%s$%s:%s", ScramMechanism.SCRAM_SHA_256.getName(), 4096,
            encodeToStringSalt, encodeToStringClient, encodeToStringServer);

    assertEquals(
        "SCRAM-SHA-256$4096:Fgh8JU2AlRjBHUsIU/GgtQ==$XiT346dvVvPmnmTWeW0djrcMYBGuiQDh8QYbBJaBm/I=:"
            + "CY9vUvDF8v6FIR8Zwircvd82YV58J5AwWiMWwfssuwg=",
        pw);

    return ScramFunctions.saltedPassword(
        ScramMechanism.SCRAM_SHA_256, StringPreparation.SASL_PREPARATION, "pencil".toCharArray(),
        Base64.getDecoder().decode("W22ZaJ0SNY7soEsUEjb6gQ=="), 4096);
  }

  @Test
  void saltedPassword() {
    assertBytesEqualsBase64("HZbuOlKbWl+eR8AfIposuKbhX30=", generateSaltedPassword());
  }

  @Test
  void saltedPasswordWithSaslPrep() {
    assertBytesEqualsBase64("YniLes+b8WFMvBhtSACZyyvxeCc=", ScramFunctions.saltedPassword(
        ScramMechanism.SCRAM_SHA_1, StringPreparation.SASL_PREPARATION,
        "\u2168\u3000a\u0300".toCharArray(),
        Base64.getDecoder().decode("0BojBCBE6P2/N4bQ"), 6400));
    assertBytesEqualsBase64("YniLes+b8WFMvBhtSACZyyvxeCc=", ScramFunctions.saltedPassword(
        ScramMechanism.SCRAM_SHA_1, StringPreparation.SASL_PREPARATION,
        "\u00ADIX \u00E0".toCharArray(),
        Base64.getDecoder().decode("0BojBCBE6P2/N4bQ"), 6400));
    assertBytesEqualsBase64("YniLes+b8WFMvBhtSACZyyvxeCc=", ScramFunctions.saltedPassword(
        ScramMechanism.SCRAM_SHA_1, StringPreparation.SASL_PREPARATION, "IX \u00E0".toCharArray(),
        Base64.getDecoder().decode("0BojBCBE6P2/N4bQ"), 6400));
    assertBytesEqualsBase64("HZbuOlKbWl+eR8AfIposuKbhX30=", ScramFunctions.saltedPassword(
        ScramMechanism.SCRAM_SHA_1, StringPreparation.SASL_PREPARATION,
        "\u0070enc\u1806il".toCharArray(),
        Base64.getDecoder().decode("QSXCR+Q6sek8bf92"), 4096));

    IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
        () -> ScramFunctions.saltedPassword(
            ScramMechanism.SCRAM_SHA_1, StringPreparation.SASL_PREPARATION,
            "\u2168\u3000a\u0300\u0007".toCharArray(),
            Base64.getDecoder().decode("QSXCR+Q6sek8bf92"), 6400));
    assertEquals("Prohibited ASCII control \"0x0007\"", e.getMessage());

    assertBytesEqualsBase64("MFB9tXSMpUK2frvCND2TWRGdiVY=",
        ScramFunctions.saltedPassword(
            ScramMechanism.SCRAM_SHA_1_PLUS, StringPreparation.SASL_PREPARATION,
            "\u0070enc\u1806il \u00B4\u00BD".toCharArray(),
            Base64.getDecoder().decode("QSXCR+Q6sek8bf92"), 4096));
  }

  @Test
  void saltedPasswordSha256() {
    assertBytesEqualsBase64("xKSVEDI6tPlSysH6mUQZOeeOp01r6B3fcJbodRPcYV0=",
        generateSaltedPasswordSha256());
  }

  private byte[] generateClientKey() {
    return ScramFunctions.clientKey(ScramMechanism.SCRAM_SHA_1, generateSaltedPassword());
  }

  private byte[] generateClientKeySha256() {
    return ScramFunctions.clientKey(ScramMechanism.SCRAM_SHA_256, generateSaltedPasswordSha256());
  }

  @Test
  void clientKey() {
    assertBytesEqualsBase64("4jTEe/bDZpbdbYUrmaqiuiZVVyg=", generateClientKey());
  }

  @Test
  void clientKeySha256() {
    assertBytesEqualsBase64("pg/JI9Z+hkSpLRa5btpe9GVrDHJcSEN0viVTVXaZbos=",
        generateClientKeySha256());
  }

  private byte[] generateStoredKey() {
    return ScramFunctions.storedKey(ScramMechanism.SCRAM_SHA_1, generateClientKey());
  }

  private byte[] generateStoredKeySha256() {
    return ScramFunctions.storedKey(ScramMechanism.SCRAM_SHA_256, generateClientKeySha256());
  }

  @Test
  void storedKey() {
    assertBytesEqualsBase64("6dlGYMOdZcOPutkcNY8U2g7vK9Y=", generateStoredKey());
  }

  @Test
  void storedKeySha256() {
    assertBytesEqualsBase64("WG5d8oPm3OtcPnkdi4Uo7BkeZkBFzpcXkuLmtbsT4qY=",
        generateStoredKeySha256());
  }

  private byte[] generateServerKey() {
    return ScramFunctions.serverKey(ScramMechanism.SCRAM_SHA_1, generateSaltedPassword());
  }

  private byte[] generateServerKeySha256() {
    return ScramFunctions.serverKey(ScramMechanism.SCRAM_SHA_256, generateSaltedPasswordSha256());
  }

  @Test
  void serverKey() {
    assertBytesEqualsBase64("D+CSWLOshSulAsxiupA+qs2/fTE=", generateServerKey());
  }

  @Test
  void serverKeySha256() {
    assertBytesEqualsBase64("wfPLwcE6nTWhTAmQ7tl2KeoiWGPlZqQxSrmfPwDl2dU=",
        generateServerKeySha256());
  }

  private byte[] generateClientSignature() {
    return ScramFunctions.clientSignature(ScramMechanism.SCRAM_SHA_1, generateStoredKey(),
        com.ongres.scram.common.RfcExampleSha1.AUTH_MESSAGE);
  }

  private byte[] generateClientSignatureSha256() {
    return ScramFunctions.clientSignature(ScramMechanism.SCRAM_SHA_256, generateStoredKeySha256(),
        com.ongres.scram.common.RfcExampleSha256.AUTH_MESSAGE);
  }

  @Test
  void clientSignature() {
    assertBytesEqualsBase64("XXE4xIawv6vfSePi2ovW5cedthM=", generateClientSignature());
  }

  @Test
  void clientSignatureSha256() {
    assertBytesEqualsBase64("0nMSRnwopAqKfwXHPA3jPrPL+0qDeDtYFEzxmsa+G98=",
        generateClientSignatureSha256());
  }

  private byte[] generateClientProof() {
    return ScramFunctions.clientProof(generateClientKey(), generateClientSignature());
  }

  private byte[] generateClientProofSha256() {
    return ScramFunctions.clientProof(generateClientKeySha256(), generateClientSignatureSha256());
  }

  @Test
  void clientProof() {
    assertBytesEqualsBase64("v0X8v3Bz2T0CJGbJQyF0X+HI4Ts=", generateClientProof());
  }

  @Test
  void clientProofSha256() {
    assertBytesEqualsBase64("dHzbZapWIk4jUhN+Ute9ytag9zjfMHgsqmmiz7AndVQ=",
        generateClientProofSha256());
  }

  private byte[] generateServerSignature() {
    return ScramFunctions.serverSignature(ScramMechanism.SCRAM_SHA_1, generateServerKey(),
        com.ongres.scram.common.RfcExampleSha1.AUTH_MESSAGE);
  }

  private byte[] generateServerSignatureSha256() {
    return ScramFunctions.serverSignature(ScramMechanism.SCRAM_SHA_256, generateServerKeySha256(),
        com.ongres.scram.common.RfcExampleSha256.AUTH_MESSAGE);
  }

  @Test
  void serverSignature() {
    assertBytesEqualsBase64("rmF9pqV8S7suAoZWja4dJRkFsKQ=", generateServerSignature());
  }

  @Test
  void serverSignatureSha256() {
    assertBytesEqualsBase64("6rriTRBi23WpRR/wtup+mMhUZUn/dB5nLTJRsjl95G4=",
        generateServerSignatureSha256());
  }

  @Test
  void verifyClientProof() {
    assertTrue(
        ScramFunctions.verifyClientProof(
            ScramMechanism.SCRAM_SHA_1, generateClientProof(), generateStoredKey(),
            com.ongres.scram.common.RfcExampleSha1.AUTH_MESSAGE));
  }

  @Test
  void verifyClientProofSha256() {
    assertTrue(
        ScramFunctions.verifyClientProof(
            ScramMechanism.SCRAM_SHA_256, generateClientProofSha256(), generateStoredKeySha256(),
            com.ongres.scram.common.RfcExampleSha256.AUTH_MESSAGE));
  }

  @Test
  void verifyServerSignature() {
    assertTrue(
        ScramFunctions.verifyServerSignature(
            ScramMechanism.SCRAM_SHA_1, generateServerKey(),
            com.ongres.scram.common.RfcExampleSha1.AUTH_MESSAGE, generateServerSignature()));
  }

  @Test
  void verifyServerSignatureSha256() {
    assertTrue(
        ScramFunctions.verifyServerSignature(
            ScramMechanism.SCRAM_SHA_256, generateServerKeySha256(),
            com.ongres.scram.common.RfcExampleSha256.AUTH_MESSAGE,
            generateServerSignatureSha256()));
  }
}
