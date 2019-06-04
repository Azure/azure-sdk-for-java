// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.implementation.logging.ServiceLogger;

import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.Arrays;
import java.util.Objects;

public class ProxyConfiguration implements AutoCloseable {
    private final ServiceLogger logger = new ServiceLogger(ProxyConfiguration.class);

    public static final String PROXY_USERNAME = "PROXY_USERNAME";
    public static final String PROXY_PASSWORD = "PROXY_PASSWORD";

    private final PasswordAuthentication credentials;
    private final Proxy proxyAddress;
    private final ProxyAuthenticationType authentication;

    public static final ProxyConfiguration SYSTEM_DEFAULTS = new ProxyConfiguration();

    ProxyConfiguration() {
        this.credentials = null;
        this.proxyAddress = null;
        this.authentication = null;
    }

    ProxyConfiguration(ProxyAuthenticationType authentication, Proxy proxyAddress, String username, String password) {
        Objects.requireNonNull(authentication);
        this.authentication = authentication;
        this.proxyAddress = proxyAddress;

        if (username != null && password != null) {
            this.credentials = new PasswordAuthentication(username, password.toCharArray());
        } else {
            logger.asInformational().log("Username or Password is NULL.");
            this.credentials = null;
        }
    }

    /**
     * Gets the proxy authentication type
     *
     * @return the proxy authentication type to use. Returns {@code null} if no authentication type was set.
     * This occurs when user uses {@link ProxyConfiguration#SYSTEM_DEFAULTS}.
     */
    public ProxyAuthenticationType authentication() {
        return this.authentication;
    }

    /**
     * Gets the proxy address
     *
     * @return the proxy address. Return {@code null} if no proxy address was set
     * This occurs when user uses {@link ProxyConfiguration#SYSTEM_DEFAULTS}.
     */
    public Proxy proxyAddress() {
        return this.proxyAddress;
    }

    /**
     * Gets the credentials user provided for authentication of proxy server
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

    @Override
    public void close() {
        if (credentials != null) {
            Arrays.fill(credentials.getPassword(), '\0');
        }
    }
}
