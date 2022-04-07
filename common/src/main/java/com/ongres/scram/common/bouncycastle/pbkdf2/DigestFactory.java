/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.bouncycastle.pbkdf2;

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
