// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.properties.client;

import com.azure.spring.cloud.core.provider.ClientOptionsProvider;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Properties shared by all http client builders.
 */
public final class HttpClientProperties extends ClientProperties implements ClientOptionsProvider.HttpClientOptions {

    /**
     * Amount of time each request being sent over the wire.
     */
    private Duration writeTimeout;
    /**
     * Amount of time used when waiting for a server to reply.
     */
    private Duration responseTimeout;
    /**
     * Amount of time used when reading the server response.
     */
    private Duration readTimeout;
    /**
     * Amount of time the request attempts to connect to the remote host and the connection is resolved.
     */
    private Duration connectTimeout;
    /**
     * Maximum connection pool size used by the underlying HTTP client.
     */
    private Integer maximumConnectionPoolSize;
    /**
     * Amount of time before an idle connection.
     */
    private Duration connectionIdleTimeout;

    /**
     * List of headers applied to each request sent with client.
     */
    private final List<HeaderProperties> headers = new ArrayList<>();

    private final HttpLoggingProperties logging = new HttpLoggingProperties();

    /**
     * Get write timeout.
     * @return Write timeout.
     */
    public Duration getWriteTimeout() {
        return writeTimeout;
    }

    /**
     * Set write timeout.
     * @param writeTimeout Write timeout.
     */
    public void setWriteTimeout(Duration writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    /**
     * Get the response timeout.
     * @return The response timeout.
     */
    public Duration getResponseTimeout() {
        return responseTimeout;
    }

    /**
     * Set the response timeout.
     * @param responseTimeout The response timeout.
     */
    public void setResponseTimeout(Duration responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    /**
     * Get the read timeout.
     * @return The read timeout.
     */
    public Duration getReadTimeout() {
        return readTimeout;
    }

    /**
     * Set the read timeout.
     * @param readTimeout The read timeout.
     */
    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     * Get connect timeout.
     * @return Connect timeout.
     */
    @Override
    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Set connect timeout.
     * @param connectTimeout Connect timeout.
     */
    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * Get the max connection pool size.
     * @return The max connection pool size.
     */
    @Override
    public Integer getMaximumConnectionPoolSize() {
        return maximumConnectionPoolSize;
    }

    /**
     * Set the max connection pool size.
     * @param maximumConnectionPoolSize The max connection pool size.
     */
    public void setMaximumConnectionPoolSize(Integer maximumConnectionPoolSize) {
        this.maximumConnectionPoolSize = maximumConnectionPoolSize;
    }

    /**
     * Get the connection idle timeout.
     * @return The connection idle timeout.
     */
    @Override
    public Duration getConnectionIdleTimeout() {
        return connectionIdleTimeout;
    }

    /**
     * Set the connection idle timeout.
     * @param connectionIdleTimeout The connection idle timeout.
     */
    public void setConnectionIdleTimeout(Duration connectionIdleTimeout) {
        this.connectionIdleTimeout = connectionIdleTimeout;
    }

    /**
     * Get the headers.
     * @return The headers.
     */
    public List<HeaderProperties> getHeaders() {
        return headers;
    }

    /**
     * Get the {@link HttpLoggingProperties}.
     * @return The http client logging properties.
     */
    @Override
    public HttpLoggingProperties getLogging() {
        return logging;
    }
}
