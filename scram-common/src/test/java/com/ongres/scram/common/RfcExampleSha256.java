/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

/**
 * Constants for examples of the RFC for SHA-256 tests.
 */
public class RfcExampleSha256 {
  public static final String USER = "user";
  public static final String PASSWORD = "pencil";
  public static final String CLIENT_NONCE = "rOprNGfwEbeRWgbNEkqO";
  public static final String CLIENT_FIRST_MESSAGE_WITHOUT_GS2_HEADER =
      "n=" + USER + ",r=" + CLIENT_NONCE;
  public static final String CLIENT_FIRST_MESSAGE =
      "n," + "," + CLIENT_FIRST_MESSAGE_WITHOUT_GS2_HEADER;
  public static final String SERVER_SALT = "W22ZaJ0SNY7soEsUEjb6gQ==";
  public static final int SERVER_ITERATIONS = 4096;
  public static final String SERVER_NONCE = "%hvYDpWUa2RaTCAfuxFIlj)hNlF$k0";
  public static final String FULL_NONCE = CLIENT_NONCE + SERVER_NONCE;
  public static final String SERVER_FIRST_MESSAGE = "r=" + FULL_NONCE + ",s=" + SERVER_SALT
      + ",i=" + SERVER_ITERATIONS;
  public static final String GS2_HEADER_BASE64 = "biws";
  public static final String CLIENT_FINAL_MESSAGE_WITHOUT_PROOF = "c=" + GS2_HEADER_BASE64
      + ",r=" + FULL_NONCE;
  public static final String AUTH_MESSAGE = CLIENT_FIRST_MESSAGE_WITHOUT_GS2_HEADER + ","
      + SERVER_FIRST_MESSAGE + ","
      + CLIENT_FINAL_MESSAGE_WITHOUT_PROOF;
  public static final String CLIENT_FINAL_MESSAGE_PROOF =
      "dHzbZapWIk4jUhN+Ute9ytag9zjfMHgsqmmiz7AndVQ=";
  public static final String CLIENT_FINAL_MESSAGE = CLIENT_FINAL_MESSAGE_WITHOUT_PROOF
      + ",p=" + CLIENT_FINAL_MESSAGE_PROOF;
  public static final String SERVER_FINAL_MESSAGE =
      "v=6rriTRBi23WpRR/wtup+mMhUZUn/dB5nLTJRsjl95G4=";
}
