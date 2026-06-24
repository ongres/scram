/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.client;

import static com.ongres.scram.common.util.Preconditions.checkArgument;
import static com.ongres.scram.common.util.Preconditions.checkNotEmpty;
import static com.ongres.scram.common.util.Preconditions.checkNotNull;
import static com.ongres.scram.common.util.Preconditions.gt0;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

import com.ongres.scram.common.ClientFinalMessage;
import com.ongres.scram.common.ClientFirstMessage;
import com.ongres.scram.common.Gs2CbindFlag;
import com.ongres.scram.common.ScramFunctions;
import com.ongres.scram.common.ScramMechanism;
import com.ongres.scram.common.ServerFinalMessage;
import com.ongres.scram.common.ServerFirstMessage;
import com.ongres.scram.common.StringPreparation;
import com.ongres.scram.common.exception.ScramInvalidServerSignatureException;
import com.ongres.scram.common.exception.ScramParseException;
import com.ongres.scram.common.exception.ScramServerErrorException;
import com.ongres.scram.common.util.TlsServerEndpoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class that represents a SCRAM client. Use this class to perform a SCRAM negotiation with a
 * SCRAM server. This class performs an authentication execution for a given user, and has state
 * related to it. Thus, it cannot be shared across users or authentication executions.
 *
 * <p>Example of usage:
 *
 * <pre>{@code
 * ScramClient scramClient = ScramClient.builder()
 *     .advertisedMechanisms(Arrays.asList("SCRAM-SHA-256", "SCRAM-SHA-256-PLUS"))
 *     .username("user")
 *     .password("pencil".toCharArray())
 *     .channelBindingPolicy(ChannelBindingPolicy.REQUIRE) // client requires channel binding
 *     .channelBinding("tls-server-end-point", channelBindingData)
 *     .build();
 *
 *   // The build() call negotiates the SCRAM mechanism to be used. In this example,
 *   // since the server advertise support for the SCRAM-SHA-256-PLUS mechanism,
 *   // and the builder is set with the channel binding type and data, the constructed
 *   // scramClient will use the "SCRAM-SHA-256-PLUS" mechanism for authentication.
 *
 * // Send the client-first-message ("p=...,,n=...,r=...")
 * ClientFirstMessage clientFirstMsg = scramClient.clientFirstMessage();
 * ...
 * // Receive the server-first-message
 * ServerFirstMessage serverFirstMsg = scramClient.serverFirstMessage("r=...,s=...,i=...");
 * ...
 * // Send the client-final-message ("c=...,r=...,p=...")
 * ClientFinalMessage clientFinalMsg = scramClient.clientFinalMessage();
 * ...
 * // Receive the server-final-message, throw an ScramException on error
 * ServerFinalMessage serverFinalMsg = scramClient.serverFinalMessage("v=...");
 * }</pre>
 *
 * <p>Commonly, a protocol will specify that the server advertises supported and available
 * mechanisms to the client via some facility provided by the protocol, and the client will then
 * select the "best" mechanism from this list that it supports and finds suitable.
 *
 * <p>When building the ScramClient, it provides mechanism negotiation based on parameters, if
 * channel binding is missing the client will use {@code "n"} as gs2-cbind-flag, if the channel
 * binding is set, but the mechanisms send by the server do not advertise the {@code -PLUS}
 * version, it will use {@code "y"} as gs2-cbind-flag, when both client and server support channel
 * binding, it will use {@code "p=" cb-name} as gs2-cbind-flag.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc5802.html">RFC-5802: Salted Challenge Response
 *      Authentication Mechanism (SCRAM) SASL and GSS-API Mechanisms</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7677.html">RFC-7677: SCRAM-SHA-256 and
 *      SCRAM-SHA-256-PLUS Simple Authentication and Security Layer (SASL) Mechanisms</a>
 */
public final class ScramClient implements MessageFlow {

  private final ScramMechanism scramMechanism;
  private final Gs2CbindFlag channelBinding;
  private final StringPreparation stringPreparation;
  private final String username;
  private final char[] password;
  private final byte[] saltedPassword;
  private final byte[] clientKey;
  private final byte[] serverKey;
  private final String cbindType;
  private final byte[] cbindData;
  private final String authzid;
  private final String nonce;

  private Stage currentState = Stage.NONE;
  private ClientFirstMessage clientFirstMessage;

  private ServerFirstProcessor serverFirstProcessor;

  private ClientFinalProcessor clientFinalProcessor;

  /**
   * Constructs a SCRAM client, to perform an authentication for a given user. This class can not be
   * instantiated directly, use a {@link #builder()} is used instead.
   *
   * @param builder The Builder used to initialize this client
   */
  private ScramClient(@NotNull Builder builder) {
    this.channelBinding = builder.channelBinding;
    this.scramMechanism = builder.selectedScramMechanism;
    this.stringPreparation = builder.stringPreparation;
    this.username = builder.username;
    this.password = builder.password != null ? builder.password.clone() : null;
    this.saltedPassword = builder.saltedPassword != null ? builder.saltedPassword.clone() : null;
    this.clientKey = builder.clientKey != null ? builder.clientKey.clone() : null;
    this.serverKey = builder.serverKey != null ? builder.serverKey.clone() : null;
    this.nonce = builder.nonce;
    this.cbindType = builder.cbindType;
    this.cbindData = builder.cbindData;
    this.authzid = builder.authzid;
  }

  /**
   * Returns the scram mechanism negotiated by this SASL client.
   *
   * @return the SCRAM mechanims selected during the negotiation
   */
  public ScramMechanism getScramMechanism() {
    return scramMechanism;
  }

  /**
   * Returns the text representation of a SCRAM {@code client-first-message}.
   *
   * @apiNote should be the initial call and can be called only once
   * @return The {@code client-first-message}
   */
  @Override
  public ClientFirstMessage clientFirstMessage() {
    if (currentState != Stage.NONE) {
      throw new IllegalStateException("Invalid state for processing client first message");
    }
    this.clientFirstMessage = new ClientFirstMessage(channelBinding, cbindType, authzid, username, nonce);
    this.currentState = Stage.CLIENT_FIRST;
    return clientFirstMessage;
  }

  /**
   * Process the {@code server-first-message}, from its String representation.
   *
   * @apiNote should be called after {@link #clientFirstMessage()} and can be called only once
   * @param serverFirstMessage The {@code server-first-message}
   * @throws ScramParseException If the message is not a valid server-first-message
   * @throws IllegalArgumentException If the message is null or empty
   */
  @Override
  public ServerFirstMessage serverFirstMessage(String serverFirstMessage)
      throws ScramParseException {
    if (currentState != Stage.CLIENT_FIRST) {
      throw new IllegalStateException("Invalid state for processing server first message");
    }
    checkNotEmpty(serverFirstMessage, "serverFirstMessage");
    this.serverFirstProcessor = new ServerFirstProcessor(scramMechanism, stringPreparation, serverFirstMessage, nonce,
        clientFirstMessage);
    this.currentState = Stage.SERVER_FIRST;
    return serverFirstProcessor.getServerFirstMessage();
  }

  /**
   * Returns the text representation of a SCRAM {@code client-final-message}.
   *
   * @apiNote should be called after {@link #serverFirstMessage(String)} and can be called only once
   * @return The {@code client-final-message}
   */
  @Override
  public ClientFinalMessage clientFinalMessage() {
    if (currentState != Stage.SERVER_FIRST || serverFirstProcessor == null) {
      throw new IllegalStateException("Invalid state for processing client final message");
    }
    try {
      if (password != null) {
        this.clientFinalProcessor = serverFirstProcessor.clientFinalProcessor(password);
      } else if (saltedPassword != null) {
        this.clientFinalProcessor = serverFirstProcessor.clientFinalProcessor(saltedPassword);
      } else if (clientKey != null && serverKey != null) {
        this.clientFinalProcessor = serverFirstProcessor.clientFinalProcessor(clientKey, serverKey);
      }
    } finally {
      // Wipe the sensitive data, even if an exception was thrown above
      if (password != null) {
        Arrays.fill(password, (char) 0);
      }
      if (saltedPassword != null) {
        Arrays.fill(saltedPassword, (byte) 0);
      }
      if (clientKey != null) {
        Arrays.fill(clientKey, (byte) 0);
      }
      if (serverKey != null) {
        Arrays.fill(serverKey, (byte) 0);
      }
    }
    ClientFinalMessage clientFinalMessage = clientFinalProcessor.clientFinalMessage(cbindData);
    this.currentState = Stage.CLIENT_FINAL;
    return clientFinalMessage;
  }

  /**
   * Process and verify the {@code server-final-message}, from its String representation.
   *
   * @apiNote should be called after {@link #clientFinalMessage()} and can be called only once
   * @param serverFinalMessage The {@code server-final-message}
   * @throws ScramParseException If the message is not a valid
   * @throws ScramServerErrorException If the message is an error
   * @throws ScramInvalidServerSignatureException If the verification fails
   * @throws IllegalArgumentException If the message is null or empty
   */
  @Override
  public ServerFinalMessage serverFinalMessage(String serverFinalMessage)
      throws ScramParseException, ScramServerErrorException, ScramInvalidServerSignatureException {
    if (currentState != Stage.CLIENT_FINAL || clientFinalProcessor == null) {
      throw new IllegalStateException("Invalid state for processing server final message");
    }
    ServerFinalMessage receiveServerFinalMessage = clientFinalProcessor.receiveServerFinalMessage(serverFinalMessage);
    this.currentState = Stage.SERVER_FINAL;
    return receiveServerFinalMessage;
  }

  /**
   * Creates a builder for {@link ScramClient ScramClient} instances.
   *
   * @return Builder instance to contruct a {@link ScramClient ScramClient}
   */
  public static MechanismsBuildStage builder() {
    return new Builder();
  }

  /**
   * Builder stage for the advertised mechanisms.
   */
  public interface MechanismsBuildStage {

    /**
     * List of the advertised mechanisms that will be negotiated between the server and the client.
     *
     * @param scramMechanisms list with the IANA-registered mechanism name of this SASL client
     * @return {@code this} builder for use in a chained invocation
     */
    UsernameBuildStage advertisedMechanisms(@NotNull Collection<@NotNull String> scramMechanisms);
  }

  /**
   * Builder stage for the required username.
   */
  public interface UsernameBuildStage {

    /**
     * Sets the username.
     *
     * @param username the required username
     * @return {@code this} builder for use in a chained invocation
     */
    PasswordBuildStage username(@NotNull String username);
  }

  /**
   * Builder stage for the password (or a ClientKey/ServerKey, or SaltedPassword).
   */
  public interface PasswordBuildStage {

    /**
     * Sets the password.
     *
     * @param password the required password
     * @return {@code this} builder for use in a chained invocation
     */
    FinalBuildStage password(char @NotNull [] password);

    /**
     * Sets the SaltedPassword.
     *
     * @param saltedPassword the required SaltedPassword
     * @return {@code this} builder for use in a chained invocation
     */
    FinalBuildStage saltedPassword(byte @NotNull [] saltedPassword);

    /**
     * Sets the ClientKey/ServerKey.
     *
     * @param clientKey the required ClientKey
     * @param serverKey the required ServerKey
     * @return {@code this} builder for use in a chained invocation
     */
    FinalBuildStage clientAndServerKey(byte @NotNull [] clientKey, byte @NotNull [] serverKey);
  }

  /**
   * Builder stage for the optional atributes and the final build() call.
   */
  public interface FinalBuildStage {

    /**
     * Sets the channel binding policy for this client. Defaults to {@link ChannelBindingPolicy#ALLOW}.
     *
     * <p>The policy controls the gs2-cbind-flag sent in the SCRAM exchange and how the client
     * reacts when the server does or does not advertise a {@code -PLUS} mechanism:
     * <ul>
     * <li>{@link ChannelBindingPolicy#DISABLE}: never use channel binding (gs2-cbind-flag {@code "n"}).</li>
     * <li>{@link ChannelBindingPolicy#ALLOW}: use channel binding when both sides support it,
     * otherwise proceed without it.</li>
     * <li>{@link ChannelBindingPolicy#REQUIRE}: fail at {@link #build()} if channel binding
     * cannot be established.</li>
     * </ul>
     *
     * @param policy the channel binding policy; must not be {@code null}
     * @return {@code this} builder for use in a chained invocation
     * @see ChannelBindingPolicy
     */
    FinalBuildStage channelBindingPolicy(@NotNull ChannelBindingPolicy policy);

    /**
     * Sets the channel binding type and data for this client.
     *
     * <p>If either argument is {@code null} or empty, no channel binding data is configured and the
     * gs2-cbind-flag is determined solely by the {@link ChannelBindingPolicy}: {@code "n"} for
     * {@link ChannelBindingPolicy#DISABLE} or {@link ChannelBindingPolicy#ALLOW} without server
     * support, {@code "y"} for {@code ALLOW} when the server advertises {@code -PLUS} but no data
     * is available.
     *
     * @apiNote Prefer {@link #channelBinding(X509Certificate)} for {@code tls-server-end-point}
     *          bindings; this overload is intended for binding types the builder does not compute
     *          itself, such as {@code tls-exporter}. Calling both overloads throws
     *          {@link IllegalStateException}.
     *
     * @param cbindType the channel binding type name (e.g. {@code "tls-server-end-point"}),
     *        or {@code null} to configure no binding
     * @param cbindData the channel binding data, or {@code null} to configure no binding
     * @return {@code this} builder for use in a chained invocation
     * @see #channelBinding(X509Certificate)
     * @see ChannelBindingPolicy
     */
    FinalBuildStage channelBinding(@Nullable String cbindType, byte @Nullable [] cbindData);

    /**
     * Configures {@code tls-server-end-point} channel binding (RFC 5929) from the given TLS server
     * certificate.
     *
     * <p>This is a convenience over {@link #channelBinding(String, byte[])}: the binding type is fixed
     * to {@code "tls-server-end-point"} and the binding data is derived from {@code serverCertificate}
     * as the hash of its DER encoding, computed with the digest mandated by RFC 5929 for the
     * certificate's signature algorithm. The argument must be the server's end-entity (leaf)
     * certificate &mdash; for example, the first element of
     * {@link javax.net.ssl.SSLSession#getPeerCertificates()} &mdash; not an issuer or CA certificate.
     *
     * <p>The certificate is only recorded by this call. The binding data is computed during
     * {@link #build()}, where it is reconciled with the configured {@link ChannelBindingPolicy} and the
     * mechanisms advertised by the server.
     *
     * <p>Binding data cannot be derived from every certificate. When the certificate's signature
     * algorithm has no channel binding defined by RFC 5929 &mdash; for example EdDSA certificates,
     * whose signatures use no separate hash &mdash; no data is produced: under {@code ALLOW} the client
     * silently continues without channel binding, and under {@code REQUIRE} {@link #build()} fails. This
     * method itself never fails for such reasons; the outcome is always decided by the policy at build
     * time.
     *
     * @apiNote This method and {@link #channelBinding(String, byte[])} are two ways of configuring the
     *          same binding and are mutually exclusive; configuring channel binding more than once
     *          throws {@link IllegalStateException}. Use {@link #channelBinding(String, byte[])} to
     *          supply a binding type or data this builder does not compute itself, such as
     *          {@code tls-exporter} from a non-JDK TLS stack.
     *
     * @param serverCertificate the server's end-entity (leaf) TLS certificate to bind to,
     *        or {@code null} to configure no binding
     * @return {@code this} builder for use in a chained invocation
     * @see #channelBinding(String, byte[])
     * @see ChannelBindingPolicy
     */
    FinalBuildStage channelBinding(@Nullable X509Certificate serverCertificate);

    /**
     * Sets the StringPreparation, is recommended to leave the default SASL_PREPARATION.
     *
     * @param stringPreparation type of string preparation normalization
     * @return {@code this} builder for use in a chained invocation
     */
    FinalBuildStage stringPreparation(@NotNull StringPreparation stringPreparation);

    /**
     * Sets the authzid.
     *
     * @param authzid the optional authorization id
     * @return {@code this} builder for use in a chained invocation
     */
    FinalBuildStage authzid(@NotNull String authzid);

    /**
     * Sets a non-default length for the nonce generation.
     *
     * <p>The default value is 24. This call overwrites the length used for the client nonce.
     *
     * @param length The length of the nonce. Must be positive and greater than 0
     * @return {@code this} builder for use in a chained invocation
     * @throws IllegalArgumentException If length is less than 1
     */
    FinalBuildStage nonceLength(int length);

    /**
     * The client will use a default nonce generator, unless an external one is provided by this
     * method.
     *
     * @apiNote you should rely on the default randomly generated nonce instead of this, this call
     *          exists mostly for testing with a predefined nonce
     * @param nonceSupplier A supplier of valid nonce Strings. Please note that according to the <a
     *        href="https://tools.ietf.org/html/rfc5802#section-7">SCRAM RFC</a> only ASCII
     *        printable characters (except the comma, ',') are permitted on a nonce. Length is not
     *        limited.
     * @return {@code this} builder for use in a chained invocation
     * @throws IllegalArgumentException If nonceSupplier is null
     */
    FinalBuildStage nonceSupplier(@NotNull Supplier<@NotNull String> nonceSupplier);

    /**
     * Selects a non-default SecureRandom instance, based on the given algorithm and optionally
     * provider. This SecureRandom instance will be used to generate secure random values, like the
     * ones required to generate the nonce. Algorithm and provider names are those supported by the
     * {@link SecureRandom} class.
     *
     * @param algorithm The name of the algorithm to use
     * @param provider The name of the provider of SecureRandom. Might be null
     * @return {@code this} builder for use in a chained invocation
     * @throws IllegalArgumentException If algorithm is null, or either the algorithm or provider
     *         are not supported
     */
    FinalBuildStage secureRandomAlgorithmProvider(@NotNull String algorithm,
        @Nullable String provider);

    /**
     * Returns the fully constructed {@link ScramClient} ready to start the message flow
     * with the server.
     *
     * @return a ScramClient instance configured with the specified parameters
     * @throws IllegalArgumentException if a parameter is null or empty
     * @throws MechanismNegotiationException if the local mechanism configuration is incompatible
     *         with the client state engine or missing core fallback options
     * @throws ChannelBindingException if a channel binding policy mismatch or cryptographic
     *         negotiation failure occurs
     */
    ScramClient build();
  }

  /**
   * Builds instances of type {@link ScramClient}. Initialize attributes and then invoke
   * the {@link #build()} method to create an instance.
   *
   * @apiNote {@code Builder} is not thread-safe and generally should not be stored in a field or
   *          collection, but instead used immediately to create instances.
   */
  static final class Builder
      implements MechanismsBuildStage, UsernameBuildStage, PasswordBuildStage, FinalBuildStage {

    ScramMechanism selectedScramMechanism;
    Collection<String> scramMechanisms;
    Gs2CbindFlag channelBinding = Gs2CbindFlag.CLIENT_NOT;
    ChannelBindingPolicy bindingPolicy = ChannelBindingPolicy.ALLOW;
    StringPreparation stringPreparation = StringPreparation.SASL_PREPARATION;
    X509Certificate serverCertificate;
    int nonceLength = 24;
    String nonce;
    SecureRandom secureRandom;
    String username;
    char[] password;
    byte[] saltedPassword;
    byte[] clientKey;
    byte[] serverKey;
    String cbindType;
    byte[] cbindData;
    String authzid;
    Supplier<String> nonceSupplier;
    private boolean cbindConfigured;

    private Builder() {
      // called from ScramClient.builder()
    }

    @Override
    public FinalBuildStage stringPreparation(@NotNull StringPreparation stringPreparation) {
      this.stringPreparation = checkNotNull(stringPreparation, "stringPreparation");
      return this;
    }

    @Override
    public FinalBuildStage channelBindingPolicy(@NotNull ChannelBindingPolicy policy) {
      this.bindingPolicy = checkNotNull(policy, "policy");
      return this;
    }

    @Override
    public FinalBuildStage channelBinding(@Nullable X509Certificate serverCertificate) {
      if (serverCertificate == null) {
        return this;
      }
      if (cbindConfigured) {
        throw new IllegalStateException(
            "channelBinding(X509Certificate) called but channel binding was already configured "
                + "via channelBinding(String, byte[])");
      }
      cbindConfigured = true;
      this.serverCertificate = serverCertificate;
      return this;
    }

    @Override
    public FinalBuildStage channelBinding(@Nullable String cbindType, byte @Nullable [] cbindData) {
      if (cbindConfigured) {
        throw new IllegalStateException(
            "channelBinding(String, byte[]) called but channel binding was already configured "
                + "via channelBinding(X509Certificate)");
      }
      cbindConfigured = true;
      this.cbindType = cbindType;
      this.cbindData = cbindData != null ? cbindData.clone() : null;
      return this;
    }

    @Override
    public FinalBuildStage authzid(@NotNull String authzid) {
      this.authzid = checkNotEmpty(authzid, "authzid");
      return this;
    }

    @Override
    public PasswordBuildStage username(@NotNull String username) {
      this.username = checkNotEmpty(username, "username");
      return this;
    }

    @Override
    public FinalBuildStage password(char @NotNull [] password) {
      this.password = checkNotEmpty(password, "password");
      return this;
    }

    @Override
    public FinalBuildStage saltedPassword(byte @NotNull [] saltedPassword) {
      this.saltedPassword = checkNotNull(saltedPassword, "saltedPassword");
      return this;
    }

    @Override
    public FinalBuildStage clientAndServerKey(byte @NotNull [] clientKey,
        byte @NotNull [] serverKey) {
      this.clientKey = checkNotNull(clientKey, "clientKey");
      this.serverKey = checkNotNull(serverKey, "serverKey");
      return this;
    }

    @Override
    public UsernameBuildStage advertisedMechanisms(
        @NotNull Collection<@NotNull String> scramMechanisms) {
      checkNotNull(scramMechanisms, "scramMechanisms");
      checkArgument(!scramMechanisms.isEmpty(), "scramMechanisms");
      this.scramMechanisms = new ArrayList<>(scramMechanisms);
      return this;
    }

    @Override
    public FinalBuildStage nonceLength(int length) {
      this.nonceLength = gt0(length, "length");
      return this;
    }

    @Override
    public FinalBuildStage nonceSupplier(@NotNull Supplier<@NotNull String> nonceSupplier) {
      this.nonceSupplier = checkNotNull(nonceSupplier, "nonceSupplier");
      return this;
    }

    @Override
    public FinalBuildStage secureRandomAlgorithmProvider(@NotNull String algorithm,
        @Nullable String provider) {
      try {
        this.secureRandom = null == provider
            ? SecureRandom.getInstance(algorithm)
            : SecureRandom.getInstance(algorithm, provider);
      } catch (NoSuchAlgorithmException | NoSuchProviderException ex) {
        throw new IllegalArgumentException("Invalid algorithm or provider", ex);
      }
      return this;
    }

    @Override
    public ScramClient build() {
      final SecureRandom random = secureRandom != null ? secureRandom : new SecureRandom();
      this.nonce = nonceSupplier != null
          ? nonceSupplier.get()
          : ScramFunctions.nonce(nonceLength, random);
      this.selectedScramMechanism = mechanismNegotiation();
      return new ScramClient(this);
    }

    private ScramMechanism mechanismNegotiation() {
      final ScramMechanism cbind = selectMechanism(scramMechanisms, true);
      final ScramMechanism noncbind = selectMechanism(scramMechanisms, false);

      ScramMechanism mechanismNegotiation = cbind != null ? cbind : noncbind;
      if (mechanismNegotiation == null) {
        throw new MechanismNegotiationException("Either a bare or -PLUS mechanism must be present");
      }

      // If explicitly DISABLED, strip any passed data immediately to enforce standard SCRAM
      if (bindingPolicy == ChannelBindingPolicy.DISABLE) {
        this.cbindType = null;
        this.cbindData = null;
      }

      Exception cbindFailure = null;
      // Extract the TLS_SERVER_END_POINT from the serverCertificate
      if (bindingPolicy != ChannelBindingPolicy.DISABLE && serverCertificate != null) {
        try {
          this.cbindType = TlsServerEndpoint.TLS_SERVER_END_POINT;
          this.cbindData = TlsServerEndpoint.getChannelBindingHash(serverCertificate);
        } catch (NoSuchAlgorithmException | CertificateEncodingException e) {
          this.cbindType = null;
          this.cbindData = null; // e.g. Ed25519: can't bind → "no data"
          cbindFailure = e; // keep the reason for REQUIRE
        }
      }

      // Check client capability constraints
      boolean serverSupportsPlus = cbind != null;
      boolean clientHasData = cbindType != null && cbindData != null
          && !cbindType.isEmpty() && cbindData.length > 0;

      // Strict Enforcement Policy
      if (bindingPolicy == ChannelBindingPolicy.REQUIRE) {
        if (!serverSupportsPlus) {
          throw new ChannelBindingException(
              "Channel binding is required, but the server does not support -PLUS mechanisms");
        }
        if (!clientHasData) {
          throw new ChannelBindingException(
              "Channel binding is required, but no channel binding data or type was provided", cbindFailure);
        }
        this.channelBinding = Gs2CbindFlag.CHANNEL_BINDING_REQUIRED;
        mechanismNegotiation = cbind;
      } else if (bindingPolicy == ChannelBindingPolicy.ALLOW && serverSupportsPlus && clientHasData) {
        // Flexible Upgrade Policy
        this.channelBinding = Gs2CbindFlag.CHANNEL_BINDING_REQUIRED;
        mechanismNegotiation = cbind;
      } else {
        // Safe Downgrade
        if (noncbind == null) {
          throw new MechanismNegotiationException("A non-PLUS mechanism was not advertised by the server");
        }

        // RFC 5802 Protection: If the client possesses data but is forced to fallback
        // because the server lacks -PLUS, it MUST emit 'y' to intercept mid-flight downgrade attacks.
        this.channelBinding = clientHasData ? Gs2CbindFlag.CLIENT_YES_SERVER_NOT : Gs2CbindFlag.CLIENT_NOT;
        this.cbindType = null;
        this.cbindData = null;
        mechanismNegotiation = noncbind;
      }

      return mechanismNegotiation;
    }

    /**
     * This method classifies SCRAM mechanisms by two properties: whether they support channel
     * binding; and a priority, which is higher for safer algorithms (like SHA-256 vs SHA-1).
     *
     * @param scramMechanisms The mechanisms supported by the other peer
     * @param channelBinding True to select {@code -PLUS} mechanisms.
     * @return The selected mechanism, or null if no mechanism matched
     */
    private static @Nullable ScramMechanism selectMechanism(
        @NotNull Collection<@NotNull String> scramMechanisms,
        boolean channelBinding) {
      ScramMechanism selectedMechanism = null;
      for (String mechanism : scramMechanisms) {
        ScramMechanism candidateMechanism = ScramMechanism.byName(mechanism);
        if (candidateMechanism != null && candidateMechanism.isPlus() == channelBinding
            && (selectedMechanism == null
                || candidateMechanism.ordinal() > selectedMechanism.ordinal())) {
          selectedMechanism = candidateMechanism;
        }
      }
      return selectedMechanism;
    }

  }

}
