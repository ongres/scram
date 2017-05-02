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


import com.ongres.scram.common.stringprep.StringPreparation;


/**
 * Utility functions (e.g. crypto) for SCRAM.
 */
public class ScramFunctions {
    /**
     * Compute the salted password, based on the given SCRAM mechanism, the String preparation algorithm,
     * the provided salt and the number of iterations.
     *
     * {@code
     *      SaltedPassword  := Hi(Normalize(password), salt, i)
     * }
     *
     * @param scramMechanism The SCRAM mechanism
     * @param stringPreparation The String preparation
     * @param password The non-salted password
     * @param salt The bytes representing the salt
     * @param iteration The number of iterations
     * @return The salted password
     */
    public static byte[] saltedPassword(
            ScramMechanism scramMechanism, StringPreparation stringPreparation, String password, byte[] salt,
            int iteration
    ) {
        return null;
    }

    /**
     * Computes the HMAC of the message and key, using the given SCRAM mechanism.
     * @param scramMechanism The SCRAM mechanism
     * @param message The message to compute the HMAC
     * @param key The key used to initialize the MAC
     * @return The computed HMAC
     */
    public static byte[] hmac(ScramMechanism scramMechanism, byte[] message, byte[] key) {
        return null;
    }

    /**
     * Generates a client key, from the salted password.
     *
     * {@code
     *      ClientKey       := HMAC(SaltedPassword, "Client Key")
     * }
     *
     * @param scramMechanism The SCRAM mechanism
     * @param saltedPassword The salted password
     * @return The client key
     */
    public static byte[] clientKey(ScramMechanism scramMechanism, byte[] saltedPassword) {
        return null;
    }

    /**
     * Generates a client key from the password and salt.
     *
     * {@code
     *      SaltedPassword  := Hi(Normalize(password), salt, i)
     *      ClientKey       := HMAC(SaltedPassword, "Client Key")
     * }
     *
     * @param scramMechanism The SCRAM mechanism
     * @param stringPreparation The String preparation
     * @param password The non-salted password
     * @param salt The bytes representing the salt
     * @param iteration The number of iterations
     * @return The client key
     */
    public static byte[] clientKey(
            ScramMechanism scramMechanism, StringPreparation stringPreparation, String password, byte[] salt,
            int iteration
    ) {
        return null;
    }

    /**
     * Generates a server key, from the salted password.
     *
     * {@code
     *      ServerKey       := HMAC(SaltedPassword, "Server Key")
     * }
     *
     * @param scramMechanism The SCRAM mechanism
     * @param saltedPassword The salted password
     * @return The server key
     */
    public static byte[] serverKey(ScramMechanism scramMechanism, byte[] saltedPassword) {
        return null;
    }

    /**
     * Generates a server key from the password and salt.
     *
     * {@code
     *      SaltedPassword  := Hi(Normalize(password), salt, i)
     *      ServerKey       := HMAC(SaltedPassword, "Server Key")
     * }
     *
     * @param scramMechanism The SCRAM mechanism
     * @param stringPreparation The String preparation
     * @param password The non-salted password
     * @param salt The bytes representing the salt
     * @param iteration The number of iterations
     * @return The server key
     */
    public static byte[] serverKey(
            ScramMechanism scramMechanism, StringPreparation stringPreparation, String password, byte[] salt,
            int iteration
    ) {
        return null;
    }

    /**
     * Computes the hash function of a given value, based on the SCRAM mechanism hash function.
     * @param scramMechanism The SCRAM mechanism
     * @param value The value to hash
     * @return The hashed value
     */
    public static byte[] hash(ScramMechanism scramMechanism, byte[] value) {
        return null;
    }

    /**
     * Generates a stored key, from the salted password.
     *
     * {@code
     *      StoredKey       := H(ClientKey)
     * }
     *
     * @param scramMechanism The SCRAM mechanism
     * @param saltedPassword The salted password
     * @return The stored key
     */
    public static byte[] storedKey(ScramMechanism scramMechanism, byte[] saltedPassword) {
        return null;
    }

    /**
     * Generates a stored key from the password and salt.
     *
     * {@code
     *      SaltedPassword  := Hi(Normalize(password), salt, i)
     *      ClientKey       := HMAC(SaltedPassword, "Client Key")
     *      StoredKey       := H(ClientKey)
     * }
     *
     * @param scramMechanism The SCRAM mechanism
     * @param stringPreparation The String preparation
     * @param password The non-salted password
     * @param salt The bytes representing the salt
     * @param iteration The number of iterations
     * @return The stored key
     */
    public static byte[] storedKey(
            ScramMechanism scramMechanism, StringPreparation stringPreparation, String password, byte[] salt,
            int iteration
    ) {
        return null;
    }

    /**
     * Computes the SCRAM client signature.
     *
     * {@code
     *      ClientSignature := HMAC(StoredKey, AuthMessage)
     * }
     *
     * @param scramMechanism The SCRAM mechanism
     * @param storedKey The stored key
     * @param authMessage The auth message
     * @return The client signature
     */
    public static byte[] clientSignature(ScramMechanism scramMechanism, byte[] storedKey, String authMessage) {
        return null;
    }

    /**
     * Computes the SCRAM client proof to be sent to the server on the client-last-message.
     *
     * {@code
     *      ClientProof     := ClientKey XOR ClientSignature
     * }
     *
     * @param clientKey The client key
     * @param clientSignature The client signature
     * @return The client proof
     */
    public static byte[] clientProof(byte[] clientKey, byte[] clientSignature) {
        return null;
    }

    /**
     * Compute the SCRAM server signature.
     *
     * {@code
     *      ServerSignature := HMAC(ServerKey, AuthMessage)
     * }
     *
     * @param scramMechanism The SCRAM mechanism
     * @param serverKey The server key
     * @param authMessage The auth message
     * @return The server signature
     */
    public static byte[] serverSignature(ScramMechanism scramMechanism, byte[] serverKey, String authMessage) {
        return null;
    }

    /**
     * Verifies that a provided client proof is correct.
     * @param scramMechanism The SCRAM mechanism
     * @param clientProof The provided client proof
     * @param storedKey The stored key
     * @param authMessage The auth message
     * @return True if the client proof is correct
     */
    public static boolean verifyClientProof(
            ScramMechanism scramMechanism, byte[] clientProof, byte[] storedKey, String authMessage
    ) {
        return false;
    }

    /**
     * Verifies that a provided server proof is correct.
     * @param scramMechanism The SCRAM mechanism
     * @param serverKey The server key
     * @param authMessage The auth message
     * @param serverSignature The provided server signature
     * @return True if the server signature is correct
     */
    public static boolean verifyServerSignature(
            ScramMechanism scramMechanism, byte[] serverKey, String authMessage, byte[] serverSignature
    ) {
        return false;
    }
}
