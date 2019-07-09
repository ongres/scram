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

package com.ongres.scram.common.stringprep;

import com.ongres.saslprep.SaslPrep;
import com.ongres.stringprep.StringPrep;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class SaslPrepTest {

    @Test
    public void rfc4013Examples() throws IOException {
        // Taken from https://tools.ietf.org/html/rfc4013#section-3
        Assert.assertEquals("IX", SaslPrep.saslPrep("I\u00ADX", true));
        Assert.assertEquals("user", SaslPrep.saslPrep("user", true));
        Assert.assertEquals("USER", SaslPrep.saslPrep("USER", true));
        Assert.assertEquals("a", SaslPrep.saslPrep("\u00AA", true));
        Assert.assertEquals("IX", SaslPrep.saslPrep("\u2168", true));
        try {
            SaslPrep.saslPrep("\u0007", true);
            Assert.fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Prohibited character ", e.getMessage());
        }
        try {
            SaslPrep.saslPrep("\u0627\u0031", true);
            Assert.fail("Should thow IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("The string contains any RandALCat character but a RandALCat character "
                    + "is not the first and the last characters", e.getMessage());
        }
    }

    @Test
    public void unassigned() throws IOException {
        int unassignedCodepoint;
        for (unassignedCodepoint = Character.MAX_CODE_POINT;
             unassignedCodepoint >= Character.MIN_CODE_POINT;
             unassignedCodepoint--) {
            if (!Character.isDefined(unassignedCodepoint) && 
                    !StringPrep.prohibitionAsciiControl(unassignedCodepoint) &&
                    !StringPrep.prohibitionAsciiSpace(unassignedCodepoint) &&
                    !StringPrep.prohibitionChangeDisplayProperties(unassignedCodepoint) &&
                    !StringPrep.prohibitionInappropriateCanonicalRepresentation(unassignedCodepoint) &&
                    !StringPrep.prohibitionInappropriatePlainText(unassignedCodepoint) &&
                    !StringPrep.prohibitionNonAsciiControl(unassignedCodepoint) &&
                    !StringPrep.prohibitionNonAsciiSpace(unassignedCodepoint) &&
                    !StringPrep.prohibitionNonCharacterCodePoints(unassignedCodepoint) &&
                    !StringPrep.prohibitionPrivateUse(unassignedCodepoint) &&
                    !StringPrep.prohibitionSurrogateCodes(unassignedCodepoint) &&
                    !StringPrep.prohibitionTaggingCharacters(unassignedCodepoint)) {
                break;
            }
        }
        String withUnassignedChar = "abc"+new String(Character.toChars(unassignedCodepoint));
        //Assert.assertEquals(withUnassignedChar, saslPrepQuery(withUnassignedChar));
        try {
            SaslPrep.saslPrep(withUnassignedChar, true);
            Assert.fail("Should thow IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Prohibited character 󯿽", e.getMessage());
        }
    }
}