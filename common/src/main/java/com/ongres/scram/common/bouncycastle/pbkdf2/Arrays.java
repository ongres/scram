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
 * General array utilities.
 */
public final class Arrays
{
    private Arrays()
    {
        // static class, hide constructor
    }

    /**
     * Make a copy of a range of bytes from the passed in data array. The range can
     * extend beyond the end of the input array, in which case the return array will
     * be padded with zeroes.
     *
     * @param data the array from which the data is to be copied.
     * @param from the start index at which the copying should take place.
     * @param to the final index of the range (exclusive).
     *
     * @return a new byte array containing the range given.
     */
    public static byte[] copyOfRange(byte[] data, int from, int to)
    {
        int newLength = getLength(from, to);

        byte[] tmp = new byte[newLength];

        if (data.length - from < newLength)
        {
            System.arraycopy(data, from, tmp, 0, data.length - from);
        }
        else
        {
            System.arraycopy(data, from, tmp, 0, newLength);
        }

        return tmp;
    }

    private static int getLength(int from, int to)
    {
        int newLength = to - from;
        if (newLength < 0)
        {
            StringBuffer sb = new StringBuffer(from);
            sb.append(" > ").append(to);
            throw new IllegalArgumentException(sb.toString());
        }
        return newLength;
    }
}
