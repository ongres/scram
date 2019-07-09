/*
 * Copyright 2017, OnGres.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */


package com.ongres.scram.common;


public class RfcExampleSha1 {
    public static final String USER = "user";
    public static final String PASSWORD = "pencil";
    public static final String CLIENT_NONCE = "fyko+d2lbbFgONRv9qkxdawL";
    public static final String CLIENT_FIRST_MESSAGE_WITHOUT_GS2_HEADER = "n=" + USER + ",r=" + CLIENT_NONCE;
    public static final String CLIENT_FIRST_MESSAGE = "n," + "," + CLIENT_FIRST_MESSAGE_WITHOUT_GS2_HEADER;
    public static final String SERVER_SALT = "QSXCR+Q6sek8bf92";
    public static final int SERVER_ITERATIONS = 4096;
    public static final String SERVER_NONCE = "3rfcNHYJY1ZVvWVs7j";
    public static final String FULL_NONCE = CLIENT_NONCE + SERVER_NONCE;
    public static final String SERVER_FIRST_MESSAGE = "r=" + FULL_NONCE + ",s=" + SERVER_SALT
            + ",i=" + SERVER_ITERATIONS;
    public static final String GS2_HEADER_BASE64 = "biws";
    public static final String CLIENT_FINAL_MESSAGE_WITHOUT_PROOF = "c=" + GS2_HEADER_BASE64
            + ",r=" + FULL_NONCE;
    public static final String AUTH_MESSAGE = CLIENT_FIRST_MESSAGE_WITHOUT_GS2_HEADER + ","
            + SERVER_FIRST_MESSAGE + ","
            + CLIENT_FINAL_MESSAGE_WITHOUT_PROOF;
    public static final String CLIENT_FINAL_MESSAGE_PROOF = "v0X8v3Bz2T0CJGbJQyF0X+HI4Ts=";
    public static final String CLIENT_FINAL_MESSAGE = CLIENT_FINAL_MESSAGE_WITHOUT_PROOF
            + ",p=" + CLIENT_FINAL_MESSAGE_PROOF;
    public static final String SERVER_FINAL_MESSAGE = "v=rmF9pqV8S7suAoZWja4dJRkFsKQ=";
}
