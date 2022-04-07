/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
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