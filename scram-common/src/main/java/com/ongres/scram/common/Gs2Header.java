/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import static com.ongres.scram.common.util.Preconditions.castNonNull;
import static com.ongres.scram.common.util.Preconditions.checkNotEmpty;
import static com.ongres.scram.common.util.Preconditions.checkNotNull;

import com.ongres.scram.common.util.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * GS2 header for SCRAM.
 *
 * <table>
 * <caption>Formal Syntax:</caption>
 * <tr>
 * <td>gs2-cbind-flag</td>
 * <td>("p=" cb-name) / "n" / "y"<br>
 * ;; "n" -> client doesn't support channel binding.<br>
 * ;; "y" -> client does support channel binding<br>
 * ;; but thinks the server does not.<br>
 * ;; "p" -> client requires channel binding.<br>
 * ;; The selected channel binding follows "p=".</td>
 * <tr>
 * <td>gs2-header</td>
 * <td>gs2-cbind-flag "," [ authzid ] ","<br>
 * ;; GS2 header for SCRAM<br>
 * ;; (the actual GS2 header includes an optional<br>
 * ;; flag to indicate that the GSS mechanism is not<br>
 * ;; "standard", but since SCRAM is "standard", we<br>
 * ;; don't include that flag).</td>
 * </tr>
 * <tr>
 * <td>authzid</td>
 * <td>"a=" saslname</td>
 * </tr>
 * </table>
 *
 * @see <a href="https://tools.ietf.org/html/rfc5802#section-7">[RFC5802] Formal Syntax</a>
 */
public final class Gs2Header extends StringWritable {

  private final @NotNull Gs2AttributeValue gs2CbindFlag;
  private final @Nullable Gs2AttributeValue authzid;

  /**
   * Construct and validates a Gs2Header. Only provide the channel binding name if the channel
   * binding flag is set to required.
   *
   * @param cbindFlag The channel binding flag
   * @param cbName The channel-binding name. Should be not null if channel binding is required
   * @param authzid The optional SASL authorization identity
   * @throws IllegalArgumentException If the channel binding flag and argument are invalid
   */
  public Gs2Header(@NotNull Gs2CbindFlag cbindFlag, @Nullable String cbName,
      @Nullable String authzid) {
    checkChannelBinding(cbindFlag, cbName);

    this.gs2CbindFlag = new Gs2AttributeValue(Gs2Attributes.byGs2CbindFlag(cbindFlag), cbName);
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
  public Gs2Header(@NotNull Gs2CbindFlag cbindFlag, @Nullable String cbName) {
    this(cbindFlag, cbName, null);
  }

  /**
   * Construct and validates a Gs2Header with no authzid nor channel binding.
   *
   * @param cbindFlag The channel binding flag
   * @throws IllegalArgumentException If the channel binding is supported (no cbname can be provided
   *           here)
   */
  public Gs2Header(@NotNull Gs2CbindFlag cbindFlag) {
    this(cbindFlag, null, null);
  }

  /**
   * Return the channel binding flag.
   *
   * @return the {@code gs2-cbind-flag}
   */
  public @NotNull Gs2CbindFlag getChannelBindingFlag() {
    return Gs2CbindFlag.byChar(gs2CbindFlag.getChar());
  }

  /**
   * Return the channel binding type.
   *
   * @return the {@code cb-name}
   */
  public @Nullable String getChannelBindingName() {
    return gs2CbindFlag.getValue();
  }

  /**
   * Return the authzid.
   *
   * @return the {@code "a=" saslname}
   */
  public @Nullable String getAuthzid() {
    return authzid != null ? castNonNull(authzid).getValue() : null;
  }

  @Override
  StringBuilder writeTo(StringBuilder sb) {
    return StringWritableCsv.writeTo(sb, gs2CbindFlag, authzid);
  }

  /**
   * Read a Gs2Header from a String. String may contain trailing fields that will be ignored.
   *
   * @param message The String containing the Gs2Header
   * @return The parsed Gs2Header object
   * @throws IllegalArgumentException If the format/values of the String do not conform to a
   *           Gs2Header
   */
  public static @NotNull Gs2Header parseFrom(@NotNull String message) {
    checkNotNull(message, "Null message");

    @NotNull
    String[] gs2HeaderSplit = StringWritableCsv.parseFrom(message, 2);
    if (gs2HeaderSplit.length == 0) {
      throw new IllegalArgumentException("Invalid number of fields for the GS2 Header");
    }

    Gs2AttributeValue gs2cbind = Gs2AttributeValue.parse(castNonNull(gs2HeaderSplit[0]));
    String authzId = Preconditions.isNullOrEmpty(gs2HeaderSplit[1])
        ? null
        : castNonNull(Gs2AttributeValue.parse(gs2HeaderSplit[1])).getValue();

    return new Gs2Header(Gs2CbindFlag.byChar(gs2cbind.getChar()), gs2cbind.getValue(), authzId);
  }

  private static void checkChannelBinding(@NotNull Gs2CbindFlag cbindFlag,
      @Nullable String cbName) {
    checkNotNull(cbindFlag, "cbindFlag");
    if (cbindFlag == Gs2CbindFlag.CHANNEL_BINDING_REQUIRED ^ cbName != null) {
      throw new IllegalArgumentException(
          "Specify required channel binding flag and type together, or none");
    }
    if (cbindFlag == Gs2CbindFlag.CHANNEL_BINDING_REQUIRED) {
      validateChannelBindingType(castNonNull(cbName));
    }
  }

  /**
   * Checks that the channel binding name is valid.
   *
   * <pre>{@code
   * cb-name = 1*(ALPHA / DIGIT / "." / "-")
   *           ;; See RFC 5056, Section 7.
   * }</pre>
   *
   * @see <a
   *      href="https://www.iana.org/assignments/channel-binding-types/channel-binding-types.xhtml">IANA
   *      Channel-Binding Types</a>
   * @param cbname Channel Binding Name
   * @throws IllegalArgumentException If the name is not a valid channel binding type.
   */
  private static void validateChannelBindingType(@NotNull String cbname) {
    checkNotEmpty(cbname, "cbname");
    switch (cbname) {
      // IANA Registered Types
      case "tls-server-end-point":
      case "tls-unique":
      case "tls-exporter":
        break;
      default:
        // https://datatracker.ietf.org/doc/html/rfc5056#section-7
        for (int i = 0; i < cbname.length(); i++) {
          char ch = cbname.charAt(i);
          if (!(ch >= 'A' && ch <= 'Z') && !(ch >= 'a' && ch <= 'z')
              && !(ch >= '0' && ch <= '9') && !(ch >= '-' && ch <= '.')) {
            throw new IllegalArgumentException(
                "Invalid Channel Binding Type name '" + cbname + "'");
          }
        }
        break;
    }
  }

}
