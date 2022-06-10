// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import com.azure.core.http.ProxyOptions;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.guava27.Strings;

import java.time.Duration;

/**
 * Helper class internally used for instantiating reactor netty http client.
 */
public class HttpClientConfig {
    public final static String REACTOR_NETWORK_LOG_CATEGORY = "com.azure.cosmos.netty-network";

    private final Configs configs;
    private Integer maxPoolSize;
    private Duration maxIdleConnectionTimeout;
    private Duration networkRequestTimeout;
    private ProxyOptions proxy;
    private boolean connectionKeepAlive = true;

    public HttpClientConfig(Configs configs) {
        this.configs = configs;
    }

    public HttpClientConfig withPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
        return this;
    }

    public HttpClientConfig withProxy(ProxyOptions proxy) {
        this.proxy = proxy;
        return this;
    }

    public HttpClientConfig withMaxIdleConnectionTimeout(Duration maxIdleConnectionTimeout) {
        this.maxIdleConnectionTimeout = maxIdleConnectionTimeout;
        return this;
    }

    public HttpClientConfig withNetworkRequestTimeout(Duration requestTimeout) {
        this.networkRequestTimeout = requestTimeout;
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

    public Duration getNetworkRequestTimeout() {
        return networkRequestTimeout;
    }

    public ProxyOptions getProxy() {
        return proxy;
    }

    public boolean isConnectionKeepAlive() {
        return connectionKeepAlive;
    }

    //  TODO(kuthapar): Do we really need to use Strings.lenientFormat() here?
    //  Even the documentation of this API suggests to use String.format or just string appends if possible.
    public String toDiagnosticsString() {
        return Strings.lenientFormat("(cps:%s, nrto:%s, icto:%s, p:%s)",
            maxPoolSize,
            networkRequestTimeout,
            maxIdleConnectionTimeout,
            proxy != null);
    }
}
