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
import java.util.regex.Pattern;

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

    private static final ConfigurationProperty<ProxyAuthenticationType> AUTH_TYPE_PROPERTY
        = new ConfigurationPropertyBuilder<>(ConfigurationProperties.AMQP_PROXY_AUTHENTICATION_TYPE,
            s -> ProxyAuthenticationType.valueOf(s)).shared(true)
                .logValue(true)
                .defaultValue(ProxyAuthenticationType.NONE)
                .build();

    private static final ConfigurationProperty<Proxy.Type> TYPE_PROPERTY
        = new ConfigurationPropertyBuilder<>(ConfigurationProperties.AMQP_PROXY_TYPE, s -> Proxy.Type.valueOf(s))
            .shared(true)
            .logValue(true)
            .defaultValue(Proxy.Type.HTTP)
            .build();

    private static final ConfigurationProperty<String> HOST_PROPERTY
        = ConfigurationPropertyBuilder.ofString(ConfigurationProperties.AMQP_PROXY_HOST)
            .shared(true)
            .logValue(true)
            .build();

    private static final ConfigurationProperty<Integer> PORT_PROPERTY
        = ConfigurationPropertyBuilder.ofInteger(ConfigurationProperties.AMQP_PROXY_PORT)
            .shared(true)
            .required(true)
            .build();

    private static final ConfigurationProperty<String> USER_PROPERTY
        = ConfigurationPropertyBuilder.ofString(ConfigurationProperties.AMQP_PROXY_USER)
            .shared(true)
            .logValue(true)
            .build();

    private static final ConfigurationProperty<String> PASSWORD_PROPERTY
        = ConfigurationPropertyBuilder.ofString(ConfigurationProperties.AMQP_PROXY_PASSWORD).shared(true).build();

    private static final ClientLogger LOGGER = new ClientLogger(ProxyOptions.class);
    private static final Pattern HOST_PORT_PATTERN = Pattern.compile("^[^:]+:\\d+");

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
            return SYSTEM_DEFAULTS;
        }

        configuration = (configuration == null) ? Configuration.getGlobalConfiguration() : configuration;

        String host = configuration.get(HOST_PROPERTY);
        if (CoreUtils.isNullOrEmpty(host)) {
            return loadFromEnvironmentConfiguration(configuration);
        }

        int port = configuration.get(PORT_PROPERTY);
        InetSocketAddress socketAddress = new InetSocketAddress(host, port);
        Proxy.Type proxyType = configuration.get(TYPE_PROPERTY);
        ProxyAuthenticationType authType = configuration.get(AUTH_TYPE_PROPERTY);
        String username = configuration.get(USER_PROPERTY);
        String password = configuration.get(PASSWORD_PROPERTY);

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

    private static ProxyOptions loadFromEnvironmentConfiguration(Configuration configuration) {
        final String proxyAddress = configuration.get(Configuration.PROPERTY_HTTP_PROXY);

        if (CoreUtils.isNullOrEmpty(proxyAddress)) {
            return ProxyOptions.SYSTEM_DEFAULTS;
        }

        final String authTypeStr = configuration.get(PROXY_AUTHENTICATION_TYPE);
        final ProxyAuthenticationType authentication
            = authTypeStr != null ? ProxyAuthenticationType.valueOf(authTypeStr) : ProxyAuthenticationType.NONE;

        if (HOST_PORT_PATTERN.matcher(proxyAddress.trim()).find()) {
            final String[] hostPort = proxyAddress.split(":");
            final String host = hostPort[0];
            final int port = Integer.parseInt(hostPort[1]);
            final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
            final String username = configuration.get(ProxyOptions.PROXY_USERNAME);
            final String password = configuration.get(ProxyOptions.PROXY_PASSWORD);
            return new ProxyOptions(authentication, proxy, username, password);
        } else if (Boolean.parseBoolean(configuration.get("java.net.useSystemProxies"))) {
            // java.net.useSystemProxies needs to be set to true in this scenario.
            // If it is set to false 'ProxyOptions' in azure-core will return null.
            com.azure.core.http.ProxyOptions httpProxyOptions
                = com.azure.core.http.ProxyOptions.fromConfiguration(configuration);
            if (httpProxyOptions != null) {
                return new ProxyOptions(authentication,
                    new Proxy(httpProxyOptions.getType().toProxyType(), httpProxyOptions.getAddress()),
                    httpProxyOptions.getUsername(), httpProxyOptions.getPassword());
            }
        }
        LOGGER.verbose(
            "'HTTP_PROXY' was configured but ignored as 'java.net.useSystemProxies' wasn't " + "set or was false.");
        return ProxyOptions.SYSTEM_DEFAULTS;
    }

    /**
     * Lists available configuration property names for AMQP {@link ProxyOptions}.
     */
    private static class ConfigurationProperties {
        /**
         * The AMQP proxy server authentication type that match {@link ProxyAuthenticationType} enum.
         * Supported values are {@code NONE} (no authentication), {@code BASIC} or {@code DIGEST}.
         * If unsupported value is provided, {@link IllegalArgumentException} will be thrown at retrieval time in
         * {@link ProxyOptions#fromConfiguration(Configuration)}
         * <p>
         * Default value is {@code NONE}.
         */
        public static final String AMQP_PROXY_AUTHENTICATION_TYPE = "amqp.proxy.authentication-type";

        /**
         * The AMQP proxy server type that match {@link Proxy.Type} enum.
         * Supported values are {@code HTTP} or {@code SOCKS}.
         * If unsupported value is provided, {@link IllegalArgumentException} will be thrown at retrieval time in
         * {@link ProxyOptions#fromConfiguration(Configuration)}
         * <p>
         * Default value is {@code DIRECT} (no proxy).
         */
        public static final String AMQP_PROXY_TYPE = "amqp.proxy.type";

        /**
         * The AMQP host name of the proxy server.
         * Default value is {@code null}.
         */
        public static final String AMQP_PROXY_HOST = "amqp.proxy.hostname";

        /**
         * The port number of the AMQP proxy server.
         * Required if {@code amqp.proxy.hostname} is set.
         */
        public static final String AMQP_PROXY_PORT = "amqp.proxy.port";

        /**
         * The AMQP proxy server user.
         * Default value is {@code null}.
         */
        public static final String AMQP_PROXY_USER = "amqp.proxy.username";

        /**
         * The AMQP proxy server password.
         * Default value is {@code null}.
         */
        public static final String AMQP_PROXY_PASSWORD = "amqp.proxy.password";
    }
}
