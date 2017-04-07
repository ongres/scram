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


import javax.crypto.Mac;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.ongres.scram.common.util.Preconditions.checkNotNull;


/**
 * SCRAM Mechanisms supported by this library.
 * At least, SCRAM-SHA-1 and SCRAM-SHA-256 are provided, since both the hash and the HMAC implementations
 * are provided by the Java JDK version 6 or greater.
 *
 * {@link java.security.MessageDigest}: "Every implementation of the Java platform is required to support the
 * following standard MessageDigest algorithms: MD5, SHA-1, SHA-256".
 *
 * {@link javax.crypto.Mac}: "Every implementation of the Java platform is required to support the following
 * standard Mac algorithms: HmacMD5, HmacSHA1, HmacSHA256".
 */
public enum ScramMechanisms implements ScramMechanism {
    SCRAM_SHA_1     (   "SHA-1",    "SHA-1",    "HmacSHA1"      ),
    SCRAM_SHA_256   (   "SHA-256",  "SHA-256",  "HmacSHA256"    )
    ;

    private static final String SCRAM_MECHANISM_NAME_PREFIX = "SCRAM-";

    private final String mechanismName;
    private final String hashAlgorithmName;
    private final String hmacAlgorithmName;

    ScramMechanisms(String localMechanismName, String hashAlgorithmName, String hmacAlgorithmName) {
        this.mechanismName = SCRAM_MECHANISM_NAME_PREFIX + checkNotNull(localMechanismName, "localMechanismName");
        this.hashAlgorithmName = checkNotNull(hashAlgorithmName, "hashAlgorithmName");
        this.hmacAlgorithmName = checkNotNull(hmacAlgorithmName, "hmacAlgorithmName");
    }

    public String getName() {
        return mechanismName;
    }

    /**
     * Method that returns the name of the hash algorithm.
     * It is protected since should be of no interest for direct users.
     * The instance is supposed to provide abstractions over the algorithm names,
     * and are not meant to be directly exposed.
     * @return The name of the hash algorithm
     */
    protected String getHashAlgorithmName() {
        return hashAlgorithmName;
    }

    /**
     * Method that returns the name of the HMAC algorithm.
     * It is protected since should be of no interest for direct users.
     * The instance is supposed to provide abstractions over the algorithm names,
     * and are not meant to be directly exposed.
     * @return The name of the HMAC algorithm
     */
    protected String getHmacAlgorithmName() {
        return hmacAlgorithmName;
    }

    public MessageDigest getMessageDigestInstance() {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(hashAlgorithmName);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithm " + hashAlgorithmName + " not present in current JVM");
        }

        assert messageDigest != null;

        return messageDigest;
    }

    public Mac getMacInstance() {
        Mac mac;
        try {
            mac = Mac.getInstance(hmacAlgorithmName);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MAC Algorithm " + hmacAlgorithmName + " not present in current JVM");
        }

        assert mac != null;

        return mac;
    }
}
