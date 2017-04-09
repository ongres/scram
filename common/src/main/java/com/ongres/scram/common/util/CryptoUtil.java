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


package com.ongres.scram.common.util;


import java.security.SecureRandom;


/**
 * Utility static methods for cryptography related tasks.
 */
public class CryptoUtil {
    private static final int MIN_ASCII_PRINTABLE_RANGE = 0x21;
    private static final int MAX_ASCII_PRINTABLE_RANGE = 0x7e;
    private static final int EXCLUDED_CHAR = (int) ','; // 0x2c

    private static class SecureRandomHolder {
        private static final SecureRandom INSTANCE = new SecureRandom();
    }

    /**
     * Generates a random string (called a 'nonce'), composed of ASCII printable characters, except comma (',').
     * @param size The length of the nonce, in characters/bytes
     * @param random The SecureRandom to use
     * @return The String representing the nonce
     */
    public static String nonce(int size, SecureRandom random) {
        if(size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }

        char[] chars = new char[size];
        int r;
        for(int i = 0; i < size;) {
            r = random.nextInt(MAX_ASCII_PRINTABLE_RANGE - MIN_ASCII_PRINTABLE_RANGE + 1) + MIN_ASCII_PRINTABLE_RANGE;
            if(r != EXCLUDED_CHAR) {
                chars[i++] = (char) r;
            }
        }

        return new String(chars);
    }

    /**
     * Generates a random string (called a 'nonce'), composed of ASCII printable characters, except comma (',').
     * It uses a default SecureRandom instance.
     * @param size The length of the nonce, in characters/bytes
     * @return The String representing the nonce
     */
    public static String nonce(int size) {
        return nonce(size, SecureRandomHolder.INSTANCE);
    }
}
