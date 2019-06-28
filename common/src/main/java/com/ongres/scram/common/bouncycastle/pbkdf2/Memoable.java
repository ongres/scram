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
 * Interface for Memoable objects. Memoable objects allow the taking of a snapshot of their internal state
 * via the copy() method and then reseting the object back to that state later using the reset() method.
 */
public interface Memoable
{
    /**
     * Produce a copy of this object with its configuration and in its current state.
     * <p>
     * The returned object may be used simply to store the state, or may be used as a similar object
     * starting from the copied state.
     */
    Memoable copy();

    /**
     * Restore a copied object state into this object.
     * <p>
     * Implementations of this method <em>should</em> try to avoid or minimise memory allocation to perform the reset.
     *
     * @param other an object originally {@link #copy() copied} from an object of the same type as this instance.
     * @throws ClassCastException if the provided object is not of the correct type.
     */
    void reset(Memoable other);
}
