/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import com.ongres.scram.common.stringprep.StringPreparation;

/**
 * Definition of the functionality to be provided by every ScramMechanism.
 *
 * Every ScramMechanism implemented must provide implementations of their respective digest and hmac
 * function that will not throw a RuntimeException on any JVM, to guarantee true portability of this library.
 */
public interface ScramMechanism {
    /**
     * The name of the mechanism, which must be a value registered under IANA:
     * <a href="https://www.iana.org/assignments/sasl-mechanisms/sasl-mechanisms.xhtml#scram">
     *      SASL SCRAM Family Mechanisms</a>
     * @return The mechanism name
     */
    String getName();

    /**
     * Calculate a message digest, according to the algorithm of the SCRAM mechanism.
     * @param message the message
     * @return The calculated message digest
     * @throws RuntimeException If the algorithm is not provided by current JVM or any included implementations
     */
    byte[] digest(byte[] message) throws RuntimeException;

    /**
     * Calculate the hmac of a key and a message, according to the algorithm of the SCRAM mechanism.
     * @param key the key
     * @param message the message
     * @return The calculated message hmac instance
     * @throws RuntimeException If the algorithm is not provided by current JVM or any included implementations
     */
    byte[] hmac(byte[] key, byte[] message) throws RuntimeException;

    /**
     * Returns the length of the key length  of the algorithm.
     * @return The length (in bits)
     */
    int algorithmKeyLength();

    /**
     * Whether this mechanism supports channel binding
     * @return True if it supports channel binding, false otherwise
     */
    boolean supportsChannelBinding();

    /**
     * Compute the salted password
     * @return The salted password
     */
    byte[] saltedPassword(StringPreparation stringPreparation, String password,
            byte[] salt, int iteration);
}
