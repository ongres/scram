/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.gssapi;

import static com.ongres.scram.common.util.Preconditions.checkNotNull;

import com.ongres.scram.common.ScramStringFormatting;
import com.ongres.scram.common.util.AbstractStringWritable;
import com.ongres.scram.common.util.StringWritableCsv;

/**
 * GSS Header. Format:
 *
 * {@code
 * gs2-header      = gs2-cbind-flag "," [ authzid ] ","
 * gs2-cbind-flag  = ("p=" cb-name) / "n" / "y"
 * authzid         = "a=" saslname
 * }
 *
 * <p>Current implementation does not support channel binding. If p is used as the cbind flag, the
 * cb-name value is not validated.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5802#section-7">[RFC5802] Formal Syntax</a>
 */
public class Gs2Header extends AbstractStringWritable {
  private final Gs2AttributeValue cbind;
  private final Gs2AttributeValue authzid;

  /**
   * Construct and validates a Gs2Header. Only provide the channel binding name if the channel
   * binding flag is set to required.
   *
   * @param cbindFlag The channel binding flag
   * @param cbName The channel-binding name. Should be not null iif channel binding is required
   * @param authzid The optional SASL authorization identity
   * @throws IllegalArgumentException If the channel binding flag and argument are invalid
   */
  public Gs2Header(Gs2CbindFlag cbindFlag, String cbName, String authzid)
      throws IllegalArgumentException {
    checkNotNull(cbindFlag, "cbindFlag");
    if (cbindFlag == Gs2CbindFlag.CHANNEL_BINDING_REQUIRED ^ cbName != null) {
      throw new IllegalArgumentException(
          "Specify channel binding flag and value together, or none");
    }
    // TODO: cbName is not being properly validated
    cbind = new Gs2AttributeValue(Gs2Attributes.byGS2CbindFlag(cbindFlag), cbName);

    this.authzid = authzid == null ? null
        : new Gs2AttributeValue(Gs2Attributes.AUTHZID, ScramStringFormatting.toSaslName(authzid));
  }

  /**
   * Construct and validates a Gs2Header with no authzid. Only provide the channel binding name if
   * the channel binding flag is set to required.
   *
   * @param cbindFlag The channel binding flag
   * @param cbName The channel-binding name. Should be not null iif channel binding is required
   * @throws IllegalArgumentException If the channel binding flag and argument are invalid
   */
  public Gs2Header(Gs2CbindFlag cbindFlag, String cbName) throws IllegalArgumentException {
    this(cbindFlag, cbName, null);
  }

  /**
   * Construct and validates a Gs2Header with no authzid nor channel binding.
   *
   * @param cbindFlag The channel binding flag
   * @throws IllegalArgumentException If the channel binding is supported (no cbname can be provided
   *         here)
   */
  public Gs2Header(Gs2CbindFlag cbindFlag) {
    this(cbindFlag, null, null);
  }

  public Gs2CbindFlag getChannelBindingFlag() {
    return Gs2CbindFlag.byChar(cbind.getChar());
  }

  public String getChannelBindingName() {
    return cbind.getValue();
  }

  public String getAuthzid() {
    return authzid != null ? authzid.getValue() : null;
  }

  @Override
  public final StringBuffer writeTo(StringBuffer sb) {
    return StringWritableCsv.writeTo(sb, cbind, authzid);
  }

  /**
   * Read a Gs2Header from a String. String may contain trailing fields that will be ignored.
   *
   * @param message The String containing the Gs2Header
   * @return The parsed Gs2Header object
   * @throws IllegalArgumentException If the format/values of the String do not conform to a
   *         Gs2Header
   */
  public static Gs2Header parseFrom(String message) throws IllegalArgumentException {
    checkNotNull(message, "Null message");

    String[] gs2HeaderSplit = StringWritableCsv.parseFrom(message, 2);
    if (gs2HeaderSplit.length == 0) {
      throw new IllegalArgumentException("Invalid number of fields for the GS2 Header");
    }

    Gs2AttributeValue gs2cbind = Gs2AttributeValue.parse(gs2HeaderSplit[0]);
    return new Gs2Header(
        Gs2CbindFlag.byChar(gs2cbind.getChar()),
        gs2cbind.getValue(),
        gs2HeaderSplit[1] == null || gs2HeaderSplit[1].isEmpty() ? null
            : Gs2AttributeValue.parse(gs2HeaderSplit[1]).getValue());
  }
}
