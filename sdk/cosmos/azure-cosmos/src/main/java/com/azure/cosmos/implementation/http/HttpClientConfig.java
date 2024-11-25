// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import com.azure.core.http.ProxyOptions;
import com.azure.cosmos.Http2ConnectionConfig;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.util.Beta;

import java.time.Duration;

/**
 * Helper class internally used for instantiating reactor netty http client.
 */
public class HttpClientConfig {

    private final Configs configs;
    private Duration connectionAcquireTimeout = Configs.getConnectionAcquireTimeout();
    private int maxPoolSize = Configs.getDefaultHttpPoolSize();
    private Duration maxIdleConnectionTimeout = Configs.getMaxIdleConnectionTimeout();
    private Duration networkRequestTimeout = Duration.ofSeconds(Configs.getHttpResponseTimeoutInSeconds());
    private String connectionPoolName = Configs.getReactorNettyConnectionPoolName();
    private int maxHeaderSize = Configs.getMaxHttpHeaderSize();
    private int maxInitialLineLength = Configs.getMaxHttpInitialLineLength();
    private int maxChunkSize = Configs.getMaxHttpChunkSize();
    private int maxBodyLength = Configs.getMaxHttpBodyLength();
    public String reactorNetworkLogCategory = "com.azure.cosmos.netty-network";
    private ProxyOptions proxy;
    private boolean connectionKeepAlive = true;
    private boolean serverCertValidationDisabled = false;
    private Http2ConnectionConfig http2ConnectionConfig;

    public HttpClientConfig(Configs configs) {
        this.configs = configs;
    }

    public HttpClientConfig withMaxHeaderSize(int maxHeaderSize) {
        this.maxHeaderSize = maxHeaderSize;
        return this;
    }

    public HttpClientConfig withMaxInitialLineLength(int maxInitialLineLength) {
        this.maxInitialLineLength = maxInitialLineLength;
        return this;
    }

    public HttpClientConfig withMaxChunkSize(int maxChunkSize) {
        this.maxChunkSize = maxChunkSize;
        return this;
    }

    public HttpClientConfig withMaxBodyLength(int maxBodyLength) {
        this.maxBodyLength = maxBodyLength;
        return this;
    }

    public HttpClientConfig withReactorNetworkLogCategory(String reactorNetworkLogCategory) {
        this.reactorNetworkLogCategory = reactorNetworkLogCategory;
        return this;
    }

    public HttpClientConfig withConnectionPoolName(String connectionPoolName) {
        this.connectionPoolName = connectionPoolName;
        return this;
    }

    public HttpClientConfig withConnectionAcquireTimeout(Duration connectionAcquireTimeout) {
        this.connectionAcquireTimeout = connectionAcquireTimeout;
        return this;
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

    public HttpClientConfig withServerCertValidationDisabled(boolean serverCertValidationDisabled) {
        this.serverCertValidationDisabled = serverCertValidationDisabled;
        return this;
    }

    public HttpClientConfig withHttp2Config(Http2ConnectionConfig http2ConnectionConfig) {
        this.http2ConnectionConfig = http2ConnectionConfig;
        return this;
    }

    public Configs getConfigs() {
        return configs;
    }

    public int getMaxPoolSize() {
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

    public Duration getConnectionAcquireTimeout() {
        return connectionAcquireTimeout;
    }

    public String getConnectionPoolName() {
        return this.connectionPoolName;
    }

    public String getReactorNetworkLogCategory() {
        return reactorNetworkLogCategory;
    }

    public int getMaxHeaderSize() {
        return maxHeaderSize;
    }

    public int getMaxInitialLineLength() {
        return maxInitialLineLength;
    }

    public int getMaxChunkSize() {
        return maxChunkSize;
    }

    public int getMaxBodyLength() {
        return maxBodyLength;
    }

    public boolean isServerCertValidationDisabled() {
        return serverCertValidationDisabled;
    }

    public Http2ConnectionConfig getHttp2ConnectionConfig() {
        return this.http2ConnectionConfig;
    }

    public String toDiagnosticsString() {
        return String.format("(cps:%s, nrto:%s, icto:%s, cto:%s, p:%s)",
            maxPoolSize,
            networkRequestTimeout,
            maxIdleConnectionTimeout,
            connectionAcquireTimeout,
            proxy != null);
    }
}
