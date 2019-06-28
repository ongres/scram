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
 * Generator for PBE derived keys and ivs as defined by PKCS 5 V2.0 Scheme 2.
 * This generator uses a SHA-1 HMac as the calculation function.
 * <p>
 * The document this implementation is based on can be found at
 * <a href=http://www.rsasecurity.com/rsalabs/pkcs/pkcs-5/index.html>
 * RSA's PKCS5 Page</a>
 */
public class PKCS5S2ParametersGenerator
    extends PBEParametersGenerator
{
    private Mac hMac;
    private byte[] state;

    public PKCS5S2ParametersGenerator(Digest digest)
    {
        hMac = new HMac(digest);
        state = new byte[hMac.getMacSize()];
    }

    private void F(
        byte[]  S,
        int     c,
        byte[]  iBuf,
        byte[]  out,
        int     outOff)
    {
        if (c == 0)
        {
            throw new IllegalArgumentException("iteration count must be at least 1.");
        }

        if (S != null)
        {
            hMac.update(S, 0, S.length);
        }

        hMac.update(iBuf, 0, iBuf.length);
        hMac.doFinal(state, 0);

        System.arraycopy(state, 0, out, outOff, state.length);

        for (int count = 1; count < c; count++)
        {
            hMac.update(state, 0, state.length);
            hMac.doFinal(state, 0);

            for (int j = 0; j != state.length; j++)
            {
                out[outOff + j] ^= state[j];
            }
        }
    }

    private byte[] generateDerivedKey(
        int dkLen)
    {
        int     hLen = hMac.getMacSize();
        int     l = (dkLen + hLen - 1) / hLen;
        byte[]  iBuf = new byte[4];
        byte[]  outBytes = new byte[l * hLen];
        int     outPos = 0;

        CipherParameters param = new KeyParameter(password);

        hMac.init(param);

        for (int i = 1; i <= l; i++)
        {
            // Increment the value in 'iBuf'
            int pos = 3;
            while (++iBuf[pos] == 0)
            {
                --pos;
            }

            F(salt, iterationCount, iBuf, outBytes, outPos);
            outPos += hLen;
        }

        return outBytes;
    }

    /**
     * Generate a key parameter derived from the password, salt, and iteration
     * count we are currently initialised with.
     *
     * @param keySize the size of the key we want (in bits)
     * @return a KeyParameter object.
     */
    public CipherParameters generateDerivedParameters(
        int keySize)
    {
        keySize = keySize / 8;

        byte[]  dKey = Arrays.copyOfRange(generateDerivedKey(keySize), 0, keySize);

        return new KeyParameter(dKey, 0, keySize);
    }
}
