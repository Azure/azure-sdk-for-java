// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationProperty;
import com.azure.core.util.ConfigurationPropertyBuilder;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.Arrays;
import java.util.Objects;

/**
 * Properties for configuring proxies with Event Hubs.
 */
@Immutable
public class ProxyOptions implements AutoCloseable {
    /**
     * The configuration key for containing the username who authenticates with the proxy.
     */
    public static final String PROXY_USERNAME = "PROXY_USERNAME";
    /**
     * The configuration key for containing the password for the username who authenticates with the proxy.
     */
    public static final String PROXY_PASSWORD = "PROXY_PASSWORD";
    /**
     * The configuration key for containing the authentication type to be used by the proxy.
     * This can one of three values -
     *  - NONE
     *  - BASIC
     *  - DIGEST
     *  as defined in ProxyAuthenticationType
     */
    public static final String PROXY_AUTHENTICATION_TYPE = "PROXY_AUTHENTICATION_TYPE";

    /**
     * The AMQP proxy server authentication type that match {@link ProxyAuthenticationType} enum.
     * Supported values are {@code NONE} (no authentication), {@code BASIC} or {@code DIGEST}.
     * If unsupported value is provided, {@link IllegalArgumentException} will be thrown at retrieval time in
     * {@link ProxyOptions#fromConfiguration(Configuration)}
     *
     * Default value is {@code NONE}.
     */
    public static final String AMQP_PROXY_AUTHENTICATION_TYPE_PROPERTY = "amqp.proxy.authentication-type";

    /**
     * The AMQP proxy server type that match {@link Proxy.Type} enum.
     * Supported values are {@code HTTP} or {@code SOCKS}.
     * If unsupported value is provided, {@link IllegalArgumentException} will be thrown at retrieval time in
     * {@link ProxyOptions#fromConfiguration(Configuration)}
     *
     * Default value is {@code DIRECT} (no proxy).
     */
    public static final String AMQP_PROXY_TYPE_PROPERTY = "amqp.proxy.type";

    /**
     * The AMQP host name of the proxy server.
     * Default value is {@code null}.
     */
    public static final String AMQP_PROXY_HOST_PROPERTY = "amqp.proxy.hostname";

    /**
     * The port number of the AMQP proxy server.
     * Required if {@code amqp.proxy.hostname} is set.
     */
    public static final String AMQP_PROXY_PORT_PROPERTY = "amqp.proxy.port";

    /**
     * The AMQP proxy server user.
     * Default value is {@code null}.
     */
    public static final String AMQP_PROXY_USER_PROPERTY = "amqp.proxy.username";

    /**
     * The AMQP proxy server password.
     * Default value is {@code null}.
     */
    public static final String AMQP_PROXY_PASSWORD_PROPERTY = "amqp.proxy.password";

    private static final ConfigurationProperty<ProxyAuthenticationType> AUTH_TYPE_PROPERTY =
        new ConfigurationPropertyBuilder<>(AMQP_PROXY_AUTHENTICATION_TYPE_PROPERTY, s -> ProxyAuthenticationType.valueOf(s))
            .shared(true)
            .logValue(true)
            .defaultValue(ProxyAuthenticationType.NONE)
            .build();

    private static final ConfigurationProperty<Proxy.Type> TYPE_PROPERTY = new ConfigurationPropertyBuilder<>(AMQP_PROXY_TYPE_PROPERTY, s -> Proxy.Type.valueOf(s))
            .shared(true)
            .logValue(true)
            .defaultValue(Proxy.Type.DIRECT)
            .build();

    private static final ConfigurationProperty<String> HOST_PROPERTY = ConfigurationPropertyBuilder.ofString(AMQP_PROXY_HOST_PROPERTY)
        .shared(true)
        .logValue(true)
        .build();

    private static final ConfigurationProperty<Integer> PORT_PROPERTY = ConfigurationPropertyBuilder.ofInteger(AMQP_PROXY_PORT_PROPERTY)
        .shared(true)
        .required(true)
        .build();

    private static final ConfigurationProperty<String> USER_PROPERTY = ConfigurationPropertyBuilder.ofString(AMQP_PROXY_USER_PROPERTY)
        .shared(true)
        .logValue(true)
        .build();

    private static final ConfigurationProperty<String> PASSWORD_PROPERTY = ConfigurationPropertyBuilder.ofString(AMQP_PROXY_PASSWORD_PROPERTY)
        .shared(true)
        .build();

    private static final ClientLogger LOGGER = new ClientLogger(ProxyOptions.class);
    private final PasswordAuthentication credentials;
    private final Proxy proxyAddress;
    private final ProxyAuthenticationType authentication;

    /**
     * Gets the system defaults for proxy configuration and authentication.
     */
    public static final ProxyOptions SYSTEM_DEFAULTS = new ProxyOptions();

    private ProxyOptions() {
        this.credentials = null;
        this.proxyAddress = null;
        this.authentication = ProxyAuthenticationType.NONE;
    }

    /**
     * Creates a proxy configuration that uses the {@code proxyAddress} and authenticates with provided
     * {@code username}, {@code password} and {@code authentication}.
     *
     * @param authentication Authentication method to preemptively use with proxy.
     * @param proxyAddress Proxy to use. If {@code null} is passed in, then the system configured {@link java.net.Proxy}
     *     is used.
     * @param username Optional. Username used to authenticate with proxy. If not specified, the system-wide
     *     {@link java.net.Authenticator} is used to fetch credentials.
     * @param password Optional. Password used to authenticate with proxy.
     * @throws NullPointerException if {@code authentication} is {@code null}.
     * @throws IllegalArgumentException if {@code authentication} is {@link ProxyAuthenticationType#BASIC} or
     *     {@link ProxyAuthenticationType#DIGEST} and {@code username} or {@code password} are {@code null}.
     */
    public ProxyOptions(ProxyAuthenticationType authentication, Proxy proxyAddress, String username, String password) {
        this.authentication = Objects.requireNonNull(authentication, "'authentication' cannot be null.");
        this.proxyAddress = proxyAddress;

        if (username != null && password != null) {
            this.credentials = new PasswordAuthentication(username, password.toCharArray());
        } else {
            LOGGER.info("Username or password is null. Using system-wide authentication.");
            this.credentials = null;
        }
    }

    /**
     * Attempts to load a proxy from the configuration.
     *
     * @param configuration The {@link Configuration} that is used to load proxy configurations from the environment. If
     * {@code null} is passed then {@link Configuration#getGlobalConfiguration()} will be used.
     * @return A {@link ProxyOptions} reflecting a proxy loaded from the environment, if no proxy is found {@code null}
     * will be returned.
     *
     * @throws RuntimeException If passed {@link Configuration} contains invalid configuration options,
     *                          {@link RuntimeException} is thrown.
     */
    public static ProxyOptions fromConfiguration(Configuration configuration) {
        if (configuration == Configuration.NONE) {
            return null;
        }

        Configuration proxyConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration()
            : configuration;

        String host = proxyConfiguration.get(HOST_PROPERTY);

        // No proxy configuration setup.
        if (CoreUtils.isNullOrEmpty(host)) {
            return null;
        }

        int port = proxyConfiguration.get(PORT_PROPERTY);
        InetSocketAddress socketAddress = new InetSocketAddress(host, port);

        Proxy.Type proxyType = proxyConfiguration.get(TYPE_PROPERTY);
        ProxyAuthenticationType authType = proxyConfiguration.get(AUTH_TYPE_PROPERTY);
        String username = proxyConfiguration.get(USER_PROPERTY);
        String password = proxyConfiguration.get(PASSWORD_PROPERTY);

        return new ProxyOptions(authType, new Proxy(proxyType, socketAddress), username, password);
    }

    /**
     * Gets the proxy authentication type.
     *
     * @return the proxy authentication type to use. Returns {@code null} if no authentication type was set. This occurs
     *     when user uses {@link ProxyOptions#SYSTEM_DEFAULTS}.
     */
    public ProxyAuthenticationType getAuthentication() {
        return this.authentication;
    }

    /**
     * Gets the proxy address.
     *
     * @return the proxy address. Return {@code null} if no proxy address was set This occurs when user uses
     *     {@link ProxyOptions#SYSTEM_DEFAULTS}.
     */
    public Proxy getProxyAddress() {
        return this.proxyAddress;
    }

    /**
     * Gets the credentials user provided for authentication of proxy server.
     *
     * @return the username and password to use. Return {@code null} if no credential was set. This occurs when user
     *     uses {@link ProxyOptions#SYSTEM_DEFAULTS}.
     */
    public PasswordAuthentication getCredential() {
        return this.credentials;
    }

    /**
     * Gets whether the user has defined credentials.
     *
     * @return true if the user has defined the credentials to use, false otherwise.
     */
    public boolean hasUserDefinedCredentials() {
        return credentials != null;
    }

    /**
     * Gets whether the proxy address has been configured. Used to determine whether to use system-defined or
     * user-defined proxy.
     *
     * @return true if the proxy url has been set, and false otherwise.
     */
    public boolean isProxyAddressConfigured() {
        return proxyAddress != null && proxyAddress.address() != null;
    }

    /**
     * Disposes of the configuration along with potential credentials.
     */
    @Override
    public void close() {
        if (credentials != null) {
            Arrays.fill(credentials.getPassword(), '\0');
        }
    }
}
