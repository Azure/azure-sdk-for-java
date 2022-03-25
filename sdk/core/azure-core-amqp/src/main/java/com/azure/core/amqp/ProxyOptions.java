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
     * Attempts to load a proxy from the configuration or {@code null} if no proxy was found in the configuration.
     *
     * @param configuration The {@link Configuration} that is used to load proxy configurations from the environment. If
     * {@code null} is passed then {@link Configuration#getGlobalConfiguration()} will be used. If {@link
     * Configuration#NONE} is passed {@link IllegalArgumentException} will be thrown.
     * @return A {@link ProxyOptions} reflecting a proxy loaded from the environment, if no proxy is found {@code null}
     * will be returned.
     * @throws IllegalArgumentException If {@code configuration} is {@link Configuration#NONE} or configuration is invalid.
     */
    public static ProxyOptions fromConfiguration(Configuration configuration) {
        if (configuration == Configuration.NONE) {
            throw LOGGER.logExceptionAsWarning(new IllegalArgumentException("'configuration' cannot be 'Configuration.NONE'."));
        }

        Configuration proxyConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration()
            : configuration;

        String host = proxyConfiguration.get(Properties.PROXY_HOST);

        // No proxy configuration setup.
        if (CoreUtils.isNullOrEmpty(host)) {
            return null;
        }

        int port = proxyConfiguration.get(Properties.PROXY_PORT);
        InetSocketAddress socketAddress = new InetSocketAddress(host, port);

        Proxy.Type proxyType = proxyConfiguration.get(Properties.PROXY_TYPE);
        ProxyAuthenticationType authType = proxyConfiguration.get(Properties.AUTHENTICATION_TYPE);
        String username = proxyConfiguration.get(Properties.PROXY_USER);
        String password = proxyConfiguration.get(Properties.PROXY_PASSWORD);

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

    private static class Properties {
        public static final ConfigurationProperty<ProxyAuthenticationType> AUTHENTICATION_TYPE =
            new ConfigurationPropertyBuilder<>("amqp.proxy.authentication-type", s -> ProxyAuthenticationType.valueOf(s))
                .shared(true)
                .canLogValue(true)
                .defaultValue(ProxyAuthenticationType.NONE)
                .build();

        public static final ConfigurationProperty<Proxy.Type> PROXY_TYPE =
            new ConfigurationPropertyBuilder<>("amqp.proxy.type", s -> Proxy.Type.valueOf(s))
                .shared(true)
                .canLogValue(true)
                .defaultValue(Proxy.Type.DIRECT)
                .build();

        public static final ConfigurationProperty<String> PROXY_HOST = ConfigurationProperty.stringPropertyBuilder("amqp.proxy.hostname")
            .shared(true)
            .canLogValue(true)
            .build();

        public static final ConfigurationProperty<Integer> PROXY_PORT = ConfigurationProperty.integerPropertyBuilder("amqp.proxy.port")
            .shared(true)
            .required(true)
            .build();

        public static final ConfigurationProperty<String> PROXY_USER = ConfigurationProperty.stringPropertyBuilder("amqp.proxy.username")
            .shared(true)
            .canLogValue(true)
            .build();

        public static final ConfigurationProperty<String> PROXY_PASSWORD = ConfigurationProperty.stringPropertyBuilder("amqp.proxy.password")
            .shared(true)
            .build();

    }
}
