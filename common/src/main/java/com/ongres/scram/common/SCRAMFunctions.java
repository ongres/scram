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


/**
 * Class with static methods that provide the building blocks for generating the elements of SCRAM messages.
 */
public class SCRAMFunctions {
    /**
     * Given a value-safe-char (normalized UTF-8 String),
     * return one where characters ',' and '=' are represented by '=2C' or '=3D', respectively.
     * @param value The value to convert so saslName
     * @return The saslName, with caracter escaped (if any)
     * @see <a href="https://tools.ietf.org/html/rfc5802#section-7">[RFC5802] Section 7: Formal Syntax</a>
     */
    public static String toSaslName(String value) {
        return null;
    }

    /**
     * Given a saslName, return a non-escaped String.
     * @param value The saslName
     * @return The saslName, unescaped
     * @throws IllegalArgumentException If a ',' character is present, or a '=' not followed by either '2C' or '3C'
     * @see <a href="https://tools.ietf.org/html/rfc5802#section-7">[RFC5802] Section 7: Formal Syntax</a>
     */
    public static String fromSaslName(String value) throws IllegalArgumentException {
        return null;
    }
}
