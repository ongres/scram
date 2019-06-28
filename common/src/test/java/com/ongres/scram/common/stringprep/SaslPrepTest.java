package com.ongres.scram.common.stringprep;

import org.junit.Assert;
import org.junit.Test;

public class SaslPrepTest {

    @Test
    public void rfc4013Examples() {
        // Taken from https://tools.ietf.org/html/rfc4013#section-3
        Assert.assertEquals("IX", SaslPrep.saslPrep("I\u00ADX"));
        Assert.assertEquals("user", SaslPrep.saslPrep("user"));
        Assert.assertEquals("USER", SaslPrep.saslPrep("USER"));
        Assert.assertEquals("a", SaslPrep.saslPrep("\u00AA"));
        Assert.assertEquals("IX", SaslPrep.saslPrep("\u2168"));
        try {
            SaslPrep.saslPrep("\u0007");
            Assert.fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue("Expected error message to start with"
                + " \"Prohibited codepoint 7 at position 0 \" but was \""
                + e.getMessage() + "\"", e.getMessage().startsWith("Prohibited codepoint 7 at position 0 "));
        }
        try {
            SaslPrep.saslPrep("\u0627\u0031");
            Assert.fail("Should thow IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("First character is RandALCat, but last character is not", e.getMessage());
        }
    }

    @Test
    public void mappedToSpace() {
        Assert.assertEquals("A B", SaslPrep.saslPrep("A\u00A0B"));
    }

    @Test
    public void bidi2() {
        // RandALCat character first *and* last is OK
        Assert.assertEquals("\u0627\u0031\u0627", SaslPrep.saslPrep("\u0627\u0031\u0627"));
        // Both RandALCat character and LCat is not allowed
        try {
            SaslPrep.saslPrep("\u0627\u0041\u0627");
            Assert.fail("Should thow IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Contains both RandALCat characters and LCat characters", e.getMessage());
        }
    }

    @Test
    public void unassigned() {
        int unassignedCodepoint;
        for (unassignedCodepoint = Character.MAX_CODE_POINT;
             unassignedCodepoint >= Character.MIN_CODE_POINT;
             unassignedCodepoint--) {
            if (!Character.isDefined(unassignedCodepoint)
                    && !SaslPrep.prohibited(unassignedCodepoint)) {
                break;
            }
        }
        String withUnassignedChar = "abc"+new String(Character.toChars(unassignedCodepoint));
        //Assert.assertEquals(withUnassignedChar, saslPrepQuery(withUnassignedChar));
        try {
            SaslPrep.saslPrep(withUnassignedChar);
            Assert.fail("Should thow IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Character at position 3 is unassigned", e.getMessage());
        }
    }
}