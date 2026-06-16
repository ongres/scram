/*
 * Copyright (c) 2026 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.client;

import java.util.Locale;

import org.jetbrains.annotations.NotNull;

/**
 * Defines the client-side security policies for negotiating channel binding during a SCRAM
 * authentication execution.
 *
 * <p>Channel binding protects the authentication flow against active Machine-in-the-Middle (MITM)
 * and session hijacking attacks by cryptographically binding the outer security layer (such as TLS)
 * to the inner SASL/SCRAM authentication layer.
 *
 * <p>This policy dictates how the {@link ScramClient} evaluates available channel binding data
 * against the server's advertised mechanisms (e.g., {@code SCRAM-SHA-256} vs {@code SCRAM-SHA-256-PLUS})
 * during the mechanism negotiation phase, ultimately determining the runtime wire-format
 * {@code gs2-cbind-flag} ('n', 'y', or 'p').
 *
 * @see <a href="https://tools.ietf.org/html/rfc5802#section-6">RFC 5802 Section 6: Channels and Channel Binding</a>
 *
 * @since 3.3
 */
public enum ChannelBindingPolicy {

  /**
   * Disables channel binding entirely.
   *
   * <p>The client will strictly use standard, non-channel-bound SCRAM mechanisms (e.g.,
   * {@code SCRAM-SHA-256}), even if valid channel binding data or server-supported
   * {@code -PLUS} mechanisms are available.
   *
   * <p>This policy forces the internal protocol engine to output the {@code 'n'}
   * (Client does not support channel binding) GS2 flag. Use this policy primarily
   * for debugging, unencrypted connections, or working around proxy servers or middleboxes
   * that strip channel data.
   */
  DISABLE,

  /**
   * Permits channel binding if supported by the server, but allows a secure fallback if it is not.
   *
   * <p>This is the default recommended behavior for general-purpose applications:
   * <ul>
   * <li>If the server advertises a {@code -PLUS} mechanism and channel binding data is provided,
   * the negotiation upgrades to channel binding and uses the {@code 'p'} GS2 flag.</li>
   * <li>If the server lacks {@code -PLUS} support, the client gracefully downgrades to standard
   * SCRAM. If channel binding data was provided, the {@code 'y'} GS2 flag is transmitted to
   * explicitly declare that the client possesses channel binding capabilities, allowing the server
   * to catch and terminate malicious downgrade attacks mid-flight. If no binding data was
   * configured, {@code 'n'} is used instead.</li>
   * </ul>
   */
  ALLOW,

  /**
   * Enforces strict, non-negotiable channel binding validation.
   *
   * <p>The authentication execution will fail immediately during the client initialization
   * phase (throwing an {@link ChannelBindingException}) if any of the following boundaries
   * are violated:
   * <ul>
   * <li>The server does not explicitly advertise a channel-bound {@code -PLUS} mechanism.</li>
   * <li>The client was built without required channel binding type or data (e.g., missing
   * the binding token).</li>
   * </ul>
   *
   * <p>This policy forces the use of the {@code 'p'} GS2 flag and is intended for high-security
   * environments where channel verification is a mandatory operational requirement (e.g.,
   * applications requiring strict compliance matching database configurations like PostgreSQL's
   * {@code channel_binding=require}).
   */
  REQUIRE;

  /**
   * Returns the {@link ChannelBindingPolicy} constant for the given string value
   * (case-insensitive).
   *
   * <p>Accepted values:
   * <ul>
   * <li>{@code "disable"} → {@link #DISABLE}</li>
   * <li>{@code "allow"} → {@link #ALLOW}</li>
   * <li>{@code "prefer"} → {@link #ALLOW} (alias for PostgreSQL {@code channel_binding=prefer}
   * compatibility)</li>
   * <li>{@code "require"} → {@link #REQUIRE}</li>
   * </ul>
   *
   * @param value the string representation of the policy
   * @return the matching {@link ChannelBindingPolicy}
   * @throws IllegalArgumentException if {@code value} does not match any known policy
   */
  public static ChannelBindingPolicy of(@NotNull String value) {
    String lowerValue = value.toLowerCase(Locale.ROOT);
    switch (lowerValue) {
      case "disable":
        return DISABLE;
      case "allow":
      case "prefer":
        return ALLOW;
      case "require":
        return REQUIRE;
      default:
        throw new IllegalArgumentException("Invalid channel binding value: " + value);
    }
  }
}
