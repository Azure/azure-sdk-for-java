// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.http;

import com.azure.data.cosmos.internal.Configs;

import java.net.InetSocketAddress;

/**
 * Helper class internally used for instantiating reactor netty http client.
 */
public class HttpClientConfig {
    public final static String REACTOR_NETWORK_LOG_CATEGORY = "com.azure.data.cosmos.netty-network";

    private final Configs configs;
    private Integer maxPoolSize;
    private Integer maxIdleConnectionTimeoutInMillis;
    private Integer requestTimeoutInMillis;
    private InetSocketAddress proxy;
    private boolean connectionKeepAlive = true;

    public HttpClientConfig(Configs configs) {
        this.configs = configs;
    }

    public HttpClientConfig withPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
        return this;
    }

    public HttpClientConfig withHttpProxy(InetSocketAddress proxy) {
        this.proxy = proxy;
        return this;
    }

    public HttpClientConfig withMaxIdleConnectionTimeoutInMillis(int maxIdleConnectionTimeoutInMillis) {
        this.maxIdleConnectionTimeoutInMillis = maxIdleConnectionTimeoutInMillis;
        return this;
    }

    public HttpClientConfig withRequestTimeoutInMillis(int requestTimeoutInMillis) {
        this.requestTimeoutInMillis = requestTimeoutInMillis;
        return this;
    }

    public HttpClientConfig withConnectionKeepAlive(boolean connectionKeepAlive) {
        this.connectionKeepAlive = connectionKeepAlive;
        return this;
    }

    public Configs getConfigs() {
        return configs;
    }

    public Integer getMaxPoolSize() {
        return maxPoolSize;
    }

    public Integer getMaxIdleConnectionTimeoutInMillis() {
        return maxIdleConnectionTimeoutInMillis;
    }

    public Integer getRequestTimeoutInMillis() {
        return requestTimeoutInMillis;
    }

    public InetSocketAddress getProxy() {
        return proxy;
    }

    public boolean isConnectionKeepAlive() {
        return connectionKeepAlive;
    }
}
