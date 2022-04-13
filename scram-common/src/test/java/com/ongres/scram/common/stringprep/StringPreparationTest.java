/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.stringprep;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.SecureRandom;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class StringPreparationTest {
  private static final String[] ONLY_NON_PRINTABLE_STRINGS =
      new String[] {(char) 13 + "", (char) 13 + "\n\n"};

  @ParameterizedTest
  @NullAndEmptySource
  void doNormalizeNullEmpty(char[] value) {
    for (StringPreparation stringPreparation : StringPreparations.values()) {
      assertThrows(IllegalArgumentException.class, () -> stringPreparation.normalize(value));
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "toastingxenotime", "infecttolerant", "cobblerjack", "zekedigital", "freshscarisdale",
      "lamwaylon",
      "lagopodousmonkeys", "fanfarecheesy", "willowfinnegan", "canoeamoeba", "stinkeroddball",
      "terracecomet",
      "cakebrazos", "headersidesaddle", "cloudultracrepidarian", "grimegastropub", "stallchilli",
      "shawnapentagon", "chapeltarp", "rydbergninja", "differencegym", "europiummuscle",
      "swilledonce",
      "defensivesyntaxis", "desktopredundant", "stakingsky", "goofywaiting", "boundsemm",
      "pipermonstrous",
      "faintfrog", "riskinsist", "constantjunkie", "rejectbroth", "ceilbeau", "ponyjaialai",
      "burnishselfies",
      "unamusedglenmore", "parmesanporcupine", "suteconcerto", "ribstony", "sassytwelve",
      "coursesnasturtium",
      "singlecinders", "kinkben", "chiefpussface", "unknownivery", "robterra", "wearycubes",
      "bearcontent",
      "aquifertrip", "insulinlick", "batterypeace", "rubigloo", "fixessnizort", "coalorecheesy",
      "logodarthvader",
      "equipmentbizarre", "charitycolne", "gradecomputer", "incrediblegases", "ingotflyingfish",
      "abaftmounting",
      "kissingfluke", "chesterdinky", "anthropicdip", "portalcairo", "purebredhighjump",
      "jamaicansteeping",
      "skaterscoins", "chondrulelocust", "modespretty", "otisnadrid", "lagoonone", "arrivepayday",
      "lawfulpatsy",
      "customersdeleted", "superiorarod", "abackwarped", "footballcyclic", "sawtshortstop",
      "waskerleysanidine",
      "polythenehead", "carpacciosierra", "gnashgabcheviot", "plunkarnisdale", "surfacebased",
      "wickedpark",
      "capitalistivan", "kinglassmuse", "adultsceiriog", "medrones", "climaxshops",
      "archeangolfer", "tomfront",
      "kobeshift", "nettleaugustus", "bitesizedlion", "crickedbunting", "englishrichard",
      "dangerousdelmonico",
      "sparklemicrosoft", "kneepadsfold", "enunciatesunglasses", "parchmentsteak", "meigpiton",
      "puttingcitrusy",
      "eyehash", "newtonatomiser", "witchesburberry", "positionwobbly", "clipboardamber",
      "ricolobster",
      "calendarpetal", "shinywound", "dealemral", "moonrakerfinnish", "banditliberated",
      "whippedfanatical",
      "jargongreasy", "yumlayla", "dwarfismtransition", "doleriteduce", "sikickball",
      "columngymnastics", "draybowmont", "jupitersnorkling", "siderealmolding", "dowdyrosary",
      "novaskeeter",
      "whickerpulley", "rutlandsliders", "categoryflossed", "coiltiedogfish", "brandwaren",
      "altairlatigo",
      "acruxyouthscape", "harmonicdash", "jasperserver", "slicedaggie", "gravityfern",
      "bitsstorm",
      "readymadehobby", "surfeitgrape", "pantheonslabs", "ammandecent", "skicrackers",
      "speyfashions",
      "languagedeeno", "pettyconfit", "minutesshimmering", "thinhopeangellist",
      "sleevelesscadmium", "controlarc",
      "robinvolvox", "postboxskylark", "tortepleasing", "lutzdillinger", "amnioteperl",
      "burntmaximize",
      "gamblingearn", "bumsouch", "coronagraphdown", "bodgeelearning", "hackingscraper",
      "hartterbium",
      "mindyurgonian", "leidlebalki", "labelthumbs", "lincolncrisps", "pearhamster", "termsfiona",
      "tickingsomber", "hatellynfi", "northumberlandgrotesque", "harpistcaramel", "gentryswiss",
      "illusionnooks",
      "easilyrows", "highgluten", "backedallegiance", "laelsitesearch", "methodfix",
      "teethminstral",
      "chemicalchildish", "likablepace", "alikealeph", "nalasincere", "investbaroque",
      "conditionenvelope",
      "splintsmccue", "carnonprompt", "resultharvey", "acceptsheba", "redditmonsoon",
      "multiplepostbox",
      "invitationchurch", "drinksgaliath", "ordersvivid", "mugsgit", "clumpingfreak"
  })
  void doNormalizeValidAsciiCases(String username) {
    char[] validAsciiUsername = username.toCharArray();
    for (StringPreparation stringPreparation : StringPreparations.values()) {
      assertArrayEquals(validAsciiUsername, stringPreparation.normalize(validAsciiUsername));
    }
  }

  private static Stream<String> provideRandomStrings() {
    final SecureRandom srand = new SecureRandom();
    return IntStream.iterate(0, i -> i)
        .limit(1000)
        .mapToObj(c -> srand.ints(32, 127)
            .filter(i -> (i >= 32) && (i <= 127))
            .limit(128)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString());
  }

  /*
   * Some simple random testing won't hurt. If a test would fail, create new test with the generated
   * word.
   */
  @ParameterizedTest
  @MethodSource("provideRandomStrings")
  void doNormalizeValidAsciiRandom(String random) {
    for (StringPreparation stringPreparation : StringPreparations.values()) {
      assertArrayEquals(random.toCharArray(), stringPreparation.normalize(random.toCharArray()),
          "'" + random + "' is a printable ASCII string, should not be changed by normalize()");
    }
  }

  @Test
  void doNormalizeNoPreparationEmptyAfterNormalization() {
    for (String s : ONLY_NON_PRINTABLE_STRINGS) {
      char[] charArray = s.toCharArray();
      assertThrows(IllegalArgumentException.class,
          () -> StringPreparations.NO_PREPARATION.normalize(charArray));
    }
  }

  @Test
  void doNormalizeNoPreparationNonEmptyAfterNormalization() {
    // No exception should be thrown
    for (String s : ONLY_NON_PRINTABLE_STRINGS) {
      StringPreparations.NO_PREPARATION.normalize((s + "a").toCharArray());
    }
  }
}
