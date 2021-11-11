// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.client;

import com.azure.spring.core.aware.ClientAware;

import java.time.Duration;

/**
 * Properties shared by all http client builders.
 */
public final class HttpClientProperties extends ClientProperties implements ClientAware.HttpClient {

    private Duration writeTimeout;
    private Duration responseTimeout;
    private Duration readTimeout;
    private Duration connectTimeout;
    private Duration connectionIdleTimeout;
    private Integer maximumConnectionPoolSize;

    public Duration getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(Duration writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public Duration getResponseTimeout() {
        return responseTimeout;
    }

    public void setResponseTimeout(Duration responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    @Override
    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    @Override
    public Integer getMaximumConnectionPoolSize() {
        return maximumConnectionPoolSize;
    }

    public void setMaximumConnectionPoolSize(Integer maximumConnectionPoolSize) {
        this.maximumConnectionPoolSize = maximumConnectionPoolSize;
    }

    @Override
    public Duration getConnectionIdleTimeout() {
        return connectionIdleTimeout;
    }

    public void setConnectionIdleTimeout(Duration connectionIdleTimeout) {
        this.connectionIdleTimeout = connectionIdleTimeout;
    }
}
