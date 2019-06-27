/*
 * Copyright 2019, OnGres.
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

package com.ongres.scram.common.bouncycastle.pbkdf2;

/**
 * super class for all Password Based Encryption (PBE) parameter generator classes.
 */
public abstract class PBEParametersGenerator
{
    protected byte[]  password;
    protected byte[]  salt;
    protected int     iterationCount;

    /**
     * base constructor.
     */
    protected PBEParametersGenerator()
    {
    }

    /**
     * initialise the PBE generator.
     *
     * @param password the password converted into bytes (see below).
     * @param salt the salt to be mixed with the password.
     * @param iterationCount the number of iterations the "mixing" function
     * is to be applied for.
     */
    public void init(
        byte[]  password,
        byte[]  salt,
        int     iterationCount)
    {
        this.password = password;
        this.salt = salt;
        this.iterationCount = iterationCount;
    }

    /**
     * return the password byte array.
     *
     * @return the password byte array.
     */
    public byte[] getPassword()
    {
        return password;
    }

    /**
     * return the salt byte array.
     *
     * @return the salt byte array.
     */
    public byte[] getSalt()
    {
        return salt;
    }

    /**
     * return the iteration count.
     *
     * @return the iteration count.
     */
    public int getIterationCount()
    {
        return iterationCount;
    }

    /**
     * generate derived parameters for a key of length keySize.
     *
     * @param keySize the length, in bits, of the key required.
     * @return a parameters object representing a key.
     */
    public abstract CipherParameters generateDerivedParameters(int keySize);

    /**
     * converts a password to a byte array according to the scheme in
     * PKCS5 (UTF-8, no padding)
     *
     * @param password a character array representing the password.
     * @return a byte array representing the password.
     */
    public static byte[] PKCS5PasswordToUTF8Bytes(
        char[]  password)
    {
        if (password != null)
        {
            return Strings.toUTF8ByteArray(password);
        }
        else
        {
            return new byte[0];
        }
    }
}
