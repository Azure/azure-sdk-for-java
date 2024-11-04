// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.properties.core.client;

import com.azure.spring.cloud.core.properties.client.HeaderProperties;
import com.azure.spring.cloud.core.provider.ClientOptionsProvider;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class HttpClientConfigurationProperties extends ClientConfigurationProperties implements ClientOptionsProvider.HttpClientOptions {

    /**
     * Amount of time(Duration) each request being sent over the wire.
     */
    private Duration writeTimeout;
    /**
     * Amount of time(Duration) used when waiting for a server to reply.
     */
    private Duration responseTimeout;
    /**
     * Amount of time(Duration) used when reading the server response.
     */
    private Duration readTimeout;
    /**
     * Amount of time(Duration) the request attempts to connect to the remote host and the connection is resolved.
     */
    private Duration connectTimeout;
    /**
     * Maximum connection pool size used by the underlying HTTP client.
     */
    private Integer maximumConnectionPoolSize;
    /**
     * Amount of time(Duration) before an idle connection.
     */
    private Duration connectionIdleTimeout;

    /**
     * List of headers applied to each request sent with client. For instance, '"myCustomHeader", "myStaticValue"'.
     */
    private final List<HeaderProperties> headers = new ArrayList<>();

    @NestedConfigurationProperty
    private final HttpLoggingConfigurationProperties logging = new HttpLoggingConfigurationProperties();

    @Override
    public Duration getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(Duration writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    @Override
    public Duration getResponseTimeout() {
        return responseTimeout;
    }

    public void setResponseTimeout(Duration responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    @Override
    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Integer getMaximumConnectionPoolSize() {
        return maximumConnectionPoolSize;
    }

    public void setMaximumConnectionPoolSize(Integer maximumConnectionPoolSize) {
        this.maximumConnectionPoolSize = maximumConnectionPoolSize;
    }

    public Duration getConnectionIdleTimeout() {
        return connectionIdleTimeout;
    }

    public void setConnectionIdleTimeout(Duration connectionIdleTimeout) {
        this.connectionIdleTimeout = connectionIdleTimeout;
    }

    @Override
    public List<HeaderProperties> getHeaders() {
        return headers;
    }

    @Override
    public HttpLoggingConfigurationProperties getLogging() {
        return logging;
    }

}
