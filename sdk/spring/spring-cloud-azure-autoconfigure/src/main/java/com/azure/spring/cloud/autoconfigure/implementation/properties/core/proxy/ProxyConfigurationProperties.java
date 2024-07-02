// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.properties.core.proxy;

import com.azure.spring.cloud.core.provider.ProxyOptionsProvider;

/**
 * Common proxy properties for all Azure SDKs.
 */
public class ProxyConfigurationProperties implements ProxyOptionsProvider.ProxyOptions {

    /**
     * Type of the proxy. For instance of http, 'http', 'socks4', 'socks5'. For instance of amqp, 'http', 'socks'.
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

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
