// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import com.azure.cosmos.implementation.Configs;

import java.net.InetSocketAddress;
import java.time.Duration;

/**
 * Helper class internally used for instantiating reactor netty http client.
 */
public class HttpClientConfig {
    public final static String REACTOR_NETWORK_LOG_CATEGORY = "com.azure.cosmos.netty-network";

    private final Configs configs;
    private Integer maxPoolSize;
    private Duration maxIdleConnectionTimeout;
    private Duration requestTimeout;
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

    public HttpClientConfig withMaxIdleConnectionTimeout(Duration maxIdleConnectionTimeout) {
        this.maxIdleConnectionTimeout = maxIdleConnectionTimeout;
        return this;
    }

    public HttpClientConfig withRequestTimeout(Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
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

    public Duration getMaxIdleConnectionTimeout() {
        return maxIdleConnectionTimeout;
    }

    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    public InetSocketAddress getProxy() {
        return proxy;
    }

    public boolean isConnectionKeepAlive() {
        return connectionKeepAlive;
    }
}
