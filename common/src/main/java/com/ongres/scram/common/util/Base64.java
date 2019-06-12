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

import java.io.IOException;

public class Base64 {

    private Base64() {}

    public static Encoder getEncoder() {
         return Encoder.RFC4648;
    }

    public static Decoder getDecoder() {
         return Decoder.RFC4648;
    }

    public static class Encoder {

        private static final Encoder RFC4648 = new Encoder();

        private Encoder() {
        }

        public String encodeToString(byte[] b) {
            try {
                return Base64Impl.encodeBytes(b);
            } catch(IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public static class Decoder {

        private static final Decoder RFC4648 = new Decoder();

        private Decoder() {
        }

        public byte[] decode(String s) {
            try {
                return Base64Impl.decode(s);
            } catch(IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}

