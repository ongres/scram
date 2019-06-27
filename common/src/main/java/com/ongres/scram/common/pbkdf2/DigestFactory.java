package com.ongres.scram.common.pbkdf2;

/**
 * Basic factory class for message digests.
 */
public final class DigestFactory
{
    public static Digest createSHA256()
    {
        return new SHA256Digest();
    }
}
