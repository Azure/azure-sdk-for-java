// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.properties.core.proxy;

import com.azure.spring.cloud.core.provider.ProxyOptionsProvider;

/**
 * Common proxy properties for all Azure SDKs.
 */
public class ProxyConfigurationProperties implements ProxyOptionsProvider.ProxyOptions {

    /**
     * Type of the proxy.
     */
    private String type;
    /**
     * The host of the proxy.
     */
    private String hostname;
    /**
     * The port of the proxy.
     */
    private Integer port;
    /**
     * Username used to authenticate with the proxy.
     */
    private String username;
    /**
     * Password used to authenticate with the proxy.
     */
    private String password;

    @Override
    public String getType() {
        return type;
    }

    /**
     * Set proxy type.
     * @param type The proxy type.
     */
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getHostname() {
        return hostname;
    }

    /**
     * Set proxy hostname.
     * @param hostname The hostname.
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    /**
     * Set port.
     * @param port The port of the host.
     */
    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Set the username used for authentication.
     * @param username The username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Set the password for authentication.
     * @param password The password.
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
