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
 * Utility methods for converting byte arrays into ints and longs, and back again.
 */
public abstract class Pack
{
    public static int bigEndianToInt(byte[] bs, int off)
    {
        int n = bs[  off] << 24;
        n |= (bs[++off] & 0xff) << 16;
        n |= (bs[++off] & 0xff) << 8;
        n |= (bs[++off] & 0xff);
        return n;
    }

    public static long bigEndianToLong(byte[] bs, int off)
    {
        int hi = bigEndianToInt(bs, off);
        int lo = bigEndianToInt(bs, off + 4);
        return ((long)(hi & 0xffffffffL) << 32) | (long)(lo & 0xffffffffL);
    }

    public static void longToBigEndian(long n, byte[] bs, int off)
    {
        intToBigEndian((int)(n >>> 32), bs, off);
        intToBigEndian((int)(n & 0xffffffffL), bs, off + 4);
    }

    public static byte[] longToBigEndian(long[] ns)
    {
        byte[] bs = new byte[8 * ns.length];
        longToBigEndian(ns, bs, 0);
        return bs;
    }

    public static void longToBigEndian(long[] ns, byte[] bs, int off)
    {
        for (int i = 0; i < ns.length; ++i)
        {
            longToBigEndian(ns[i], bs, off);
            off += 8;
        }
    }

    public static short littleEndianToShort(byte[] bs, int off)
    {
        int n = bs[  off] & 0xff;
        n |= (bs[++off] & 0xff) << 8;
        return (short)n;
    }

    public static void intToBigEndian(int n, byte[] bs, int off)
    {
        bs[  off] = (byte)(n >>> 24);
        bs[++off] = (byte)(n >>> 16);
        bs[++off] = (byte)(n >>>  8);
        bs[++off] = (byte)(n       );
    }
}
