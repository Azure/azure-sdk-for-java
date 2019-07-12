// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.implementation.annotation.Immutable;

import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.Arrays;
import java.util.Objects;

/**
 * Properties for configuring proxies with Event Hubs.
 */
@Immutable
public class ProxyConfiguration implements AutoCloseable {
    /**
     * The configuration key for containing the username who authenticates with the proxy.
     */
    public static final String PROXY_USERNAME = "PROXY_USERNAME";
    /**
     * The configuration key for containing the password for the username who authenticates with the proxy.
     */
    public static final String PROXY_PASSWORD = "PROXY_PASSWORD";

    private final ClientLogger logger = new ClientLogger(ProxyConfiguration.class);
    private final PasswordAuthentication credentials;
    private final Proxy proxyAddress;
    private final ProxyAuthenticationType authentication;

    /**
     * Gets the system defaults for proxy configuration and authentication.
     */
    public static final ProxyConfiguration SYSTEM_DEFAULTS = new ProxyConfiguration();

    private ProxyConfiguration() {
        this.credentials = null;
        this.proxyAddress = null;
        this.authentication = null;
    }

    /**
     * Creates a proxy configuration that uses the {@code proxyAddress} and authenticates with provided
     * {@code username}, {@code password} and {@code authentication}.
     *
     * @param authentication Authentication method to preemptively use with proxy.
     * @param proxyAddress Proxy to use. If {@code null} is passed in, then the system configured {@link java.net.Proxy}
     * is used.
     * @param username Optional. Username used to authenticate with proxy. If not specified, the system-wide
     * {@link java.net.Authenticator} is used to fetch credentials.
     * @param password Optional. Password used to authenticate with proxy.
     *
     * @throws NullPointerException if {@code authentication} is {@code null}.
     * @throws IllegalArgumentException if {@code authentication} is {@link ProxyAuthenticationType#BASIC} or
     * {@link ProxyAuthenticationType#DIGEST} and {@code username} or {@code password} are {@code null}.
     */
    public ProxyConfiguration(ProxyAuthenticationType authentication, Proxy proxyAddress, String username, String password) {
        Objects.requireNonNull(authentication);
        this.authentication = authentication;
        this.proxyAddress = proxyAddress;

        if (username != null && password != null) {
            this.credentials = new PasswordAuthentication(username, password.toCharArray());
        } else {
            logger.info("Username or password is null. Using system-wide authentication.");
            this.credentials = null;
        }
    }

    /**
     * Gets the proxy authentication type.
     *
     * @return the proxy authentication type to use. Returns {@code null} if no authentication type was set.
     * This occurs when user uses {@link ProxyConfiguration#SYSTEM_DEFAULTS}.
     */
    public ProxyAuthenticationType authentication() {
        return this.authentication;
    }

    /**
     * Gets the proxy address.
     *
     * @return the proxy address. Return {@code null} if no proxy address was set
     * This occurs when user uses {@link ProxyConfiguration#SYSTEM_DEFAULTS}.
     */
    public Proxy proxyAddress() {
        return this.proxyAddress;
    }

    /**
     * Gets the credentials user provided for authentication of proxy server.
     *
     * @return the username and password to use. Return {@code null} if no credential was set.
     * This occurs when user uses {@link ProxyConfiguration#SYSTEM_DEFAULTS}.
     */
    public PasswordAuthentication credential() {
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
