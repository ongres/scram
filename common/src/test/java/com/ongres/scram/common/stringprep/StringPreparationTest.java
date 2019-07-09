/*
 * Copyright 2017, OnGres.
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


import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class StringPreparationTest {
    private static final String[] ONLY_NON_PRINTABLE_STRINGS = new String[] { " ", (char) 13 + "", (char) 13 + "   " };

    @Test
    public void doNormalizeNullEmpty() {
        String[] nullEmpty = new String[] { null, "" };
        int n = 0;
        for(StringPreparation stringPreparation : StringPreparations.values()) {
            for(String s : nullEmpty) {
                try {
                    stringPreparation.normalize(s);
                } catch (IllegalArgumentException e) {
                    n++;
                }
            }
        }

        assertTrue(
                "IllegalArgumentException not thrown for either null or empty input",
                n == nullEmpty.length * StringPreparations.values().length
        );
    }

    @Test
    public void doNormalizeValidAsciiCases() {
        // 200 usernames from http://jimpix.co.uk/words/random-username-list.asp
        String[] validAsciiUsernames = new String[] {
            "toastingxenotime", "infecttolerant", "cobblerjack", "zekedigital", "freshscarisdale", "lamwaylon",
            "lagopodousmonkeys", "fanfarecheesy", "willowfinnegan", "canoeamoeba", "stinkeroddball", "terracecomet",
            "cakebrazos", "headersidesaddle", "cloudultracrepidarian", "grimegastropub", "stallchilli",
            "shawnapentagon", "chapeltarp", "rydbergninja", "differencegym", "europiummuscle", "swilledonce",
            "defensivesyntaxis", "desktopredundant", "stakingsky", "goofywaiting", "boundsemm", "pipermonstrous",
            "faintfrog", "riskinsist", "constantjunkie", "rejectbroth", "ceilbeau", "ponyjaialai", "burnishselfies",
            "unamusedglenmore", "parmesanporcupine", "suteconcerto", "ribstony", "sassytwelve", "coursesnasturtium",
            "singlecinders", "kinkben", "chiefpussface", "unknownivery", "robterra", "wearycubes", "bearcontent",
            "aquifertrip", "insulinlick", "batterypeace", "rubigloo", "fixessnizort", "coalorecheesy", "logodarthvader",
            "equipmentbizarre", "charitycolne", "gradecomputer", "incrediblegases", "ingotflyingfish", "abaftmounting",
            "kissingfluke", "chesterdinky", "anthropicdip", "portalcairo", "purebredhighjump", "jamaicansteeping",
            "skaterscoins", "chondrulelocust", "modespretty", "otisnadrid", "lagoonone", "arrivepayday", "lawfulpatsy",
            "customersdeleted", "superiorarod", "abackwarped", "footballcyclic", "sawtshortstop", "waskerleysanidine",
            "polythenehead", "carpacciosierra", "gnashgabcheviot", "plunkarnisdale", "surfacebased", "wickedpark",
            "capitalistivan", "kinglassmuse", "adultsceiriog", "medrones", "climaxshops", "archeangolfer", "tomfront",
            "kobeshift", "nettleaugustus", "bitesizedlion", "crickedbunting", "englishrichard", "dangerousdelmonico",
            "sparklemicrosoft", "kneepadsfold", "enunciatesunglasses", "parchmentsteak", "meigpiton", "puttingcitrusy",
            "eyehash", "newtonatomiser", "witchesburberry", "positionwobbly", "clipboardamber", "ricolobster",
            "calendarpetal", "shinywound", "dealemral", "moonrakerfinnish", "banditliberated", "whippedfanatical",
            "jargongreasy", "yumlayla", "dwarfismtransition", "doleriteduce", "sikickball",
            "columngymnastics", "draybowmont", "jupitersnorkling", "siderealmolding", "dowdyrosary", "novaskeeter",
            "whickerpulley", "rutlandsliders", "categoryflossed", "coiltiedogfish", "brandwaren", "altairlatigo",
            "acruxyouthscape", "harmonicdash", "jasperserver", "slicedaggie", "gravityfern", "bitsstorm",
            "readymadehobby", "surfeitgrape", "pantheonslabs", "ammandecent", "skicrackers", "speyfashions",
            "languagedeeno", "pettyconfit", "minutesshimmering", "thinhopeangellist", "sleevelesscadmium", "controlarc",
            "robinvolvox", "postboxskylark", "tortepleasing", "lutzdillinger", "amnioteperl", "burntmaximize",
            "gamblingearn", "bumsouch", "coronagraphdown", "bodgeelearning", "hackingscraper", "hartterbium",
            "mindyurgonian", "leidlebalki", "labelthumbs", "lincolncrisps", "pearhamster", "termsfiona",
            "tickingsomber", "hatellynfi", "northumberlandgrotesque", "harpistcaramel", "gentryswiss", "illusionnooks",
            "easilyrows", "highgluten", "backedallegiance", "laelsitesearch", "methodfix", "teethminstral",
            "chemicalchildish", "likablepace", "alikealeph", "nalasincere", "investbaroque", "conditionenvelope",
            "splintsmccue", "carnonprompt", "resultharvey", "acceptsheba", "redditmonsoon", "multiplepostbox",
            "invitationchurch", "drinksgaliath", "ordersvivid", "mugsgit", "clumpingfreak"
        };

        for(StringPreparation stringPreparation : StringPreparations.values()) {
            for(String s : validAsciiUsernames) {
                assertEquals(s, stringPreparation.normalize(s));
            }
        }
    }

    /*
     * Some simple random testing won't hurt. If a test would fail, create new test with the generated word.
     */
    @Test
    public void doNormalizeValidAsciiRandom() {
        int n = 10 * 1000;
        int maxLenght = 64;
        Random random = new Random();
        String[] values = new String[n];
        for(int i = 0; i < n; i++) {
            char[] charValue = new char[random.nextInt(maxLenght) + 1];
            for(int j = 0; j < charValue.length; j++) {
                charValue[j] = (char) (random.nextInt(127 - 33) + 33);
            }
            values[i] = new String(charValue);
        }

        for(StringPreparation stringPreparation : StringPreparations.values()) {
            for(String s : values) {
                assertEquals(
                        "'" + s + "' is a printable ASCII string, should not be changed by normalize()",
                        s,
                        stringPreparation.normalize(s)
                );
            }
        }
    }

    @Test
    public void doNormalizeNoPreparationEmptyAfterNormalization() {
        int n = 0;
        for(String s : ONLY_NON_PRINTABLE_STRINGS) {
            try {
                StringPreparations.NO_PREPARATION.normalize(s);
            } catch (IllegalArgumentException e) {
                n++;
            }
        }

        assertTrue(
                "IllegalArgumentException not thrown for either null or empty output after normalization",
                n == ONLY_NON_PRINTABLE_STRINGS.length
        );
    }

    @Test
    public void doNormalizeNoPreparationNonEmptyAfterNormalization() {
        // No exception should be thrown
        for(String s : ONLY_NON_PRINTABLE_STRINGS) {
            StringPreparations.NO_PREPARATION.normalize(s + "a");
        }
    }
}
