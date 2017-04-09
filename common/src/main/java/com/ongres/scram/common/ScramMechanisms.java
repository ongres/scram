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
import java.util.Optional;

import static com.ongres.scram.common.util.Preconditions.checkNotNull;
import static com.ongres.scram.common.util.Preconditions.gt0;


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
 *
 * @see <a href="https://www.iana.org/assignments/sasl-mechanisms/sasl-mechanisms.xhtml#scram">
 *      SASL SCRAM Family Mechanisms</a>
 */
public enum ScramMechanisms implements ScramMechanism {
    SCRAM_SHA_1         (   "SHA-1",    "SHA-1",    "HmacSHA1",     false,  1   ),
    SCRAM_SHA_1_PLUS    (   "SHA-1",    "SHA-1",    "HmacSHA1",     true,   1   ),
    SCRAM_SHA_256       (   "SHA-256",  "SHA-256",  "HmacSHA256",   false,  10  ),
    SCRAM_SHA_256_PLUS  (   "SHA-256",  "SHA-256",  "HmacSHA256",   true,   10  )
    ;

    private static final String SCRAM_MECHANISM_NAME_PREFIX = "SCRAM-";
    private static final String CHANNEL_BINDING_SUFFIX = "-PLUS";

    private final String mechanismName;
    private final String hashAlgorithmName;
    private final String hmacAlgorithmName;
    private final boolean channelBinding;
    private final int priority;

    ScramMechanisms(
            String name, String hashAlgorithmName, String hmacAlgorithmName, boolean channelBinding, int priority
    ) {
        this.mechanismName = SCRAM_MECHANISM_NAME_PREFIX
                + checkNotNull(name, "name")
                + (channelBinding ? CHANNEL_BINDING_SUFFIX : "")
        ;
        this.hashAlgorithmName = checkNotNull(hashAlgorithmName, "hashAlgorithmName");
        this.hmacAlgorithmName = checkNotNull(hmacAlgorithmName, "hmacAlgorithmName");
        this.channelBinding = channelBinding;
        this.priority = gt0(priority, "priority");
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

    @Override
    public String getName() {
        return mechanismName;
    }

    @Override
    public boolean supportsChannelBinding() {
        return channelBinding;
    }

    @Override
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

    @Override
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

    /**
     * Gets a SCRAM mechanism, given its standard IANA name.
     * @param name The standard IANA full name of the mechanism.
     * @return An Optional instance that contains the ScramMechanism if it was found, or empty otherwise.
     */
    public static Optional<ScramMechanisms> byName(String name) {
        return Optional.empty();
    }

    /**
     * This class classifies SCRAM mechanisms by two properties: whether they support channel binding;
     * and a priority, which is higher for safer algorithms (like SHA-256 vs SHA-1).
     *
     * Given a list of SCRAM mechanisms supported by the peer, pick one that matches the channel binding requirements
     * and has the highest priority.
     *
     * @param channelBinding The type of matching mechanism searched for
     * @param peerMechanisms
     * @return The selected mechanism, or null if no mechanism matched
     */
    public static ScramMechanisms selectMatchingMechanism(boolean channelBinding, String... peerMechanisms) {
        return null;
    }
}
