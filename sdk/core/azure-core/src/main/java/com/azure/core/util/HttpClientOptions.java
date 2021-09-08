// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.logging.ClientLogger;

import java.time.Duration;

import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_READ_TIMEOUT;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_RESPONSE_TIMEOUT;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT;
import static com.azure.core.util.CoreUtils.getDefaultTimeoutFromEnvironment;

/**
 * General configuration options for {@link HttpClient HttpClients}.
 * <p>
 * {@link HttpClient} implementations may not support all configuration options in this class.
 */
@Fluent
public final class HttpClientOptions extends ClientOptions {
    private static final Duration MINIMUM_TIMEOUT = Duration.ofMillis(1);
    private static final Duration DEFAULT_CONNECT_TIMEOUT;
    private static final Duration DEFAULT_WRITE_TIMEOUT;
    private static final Duration DEFAULT_RESPONSE_TIMEOUT;
    private static final Duration DEFAULT_READ_TIMEOUT;
    private static final Duration DEFAULT_CONNECTION_IDLE_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration NO_TIMEOUT = Duration.ZERO;

    static {
        ClientLogger logger = new ClientLogger(HttpClientOptions.class);
        Configuration configuration = Configuration.getGlobalConfiguration();

        DEFAULT_CONNECT_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration,
            PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT, Duration.ofSeconds(10), logger);
        DEFAULT_WRITE_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration, PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT,
            Duration.ofSeconds(60), logger);
        DEFAULT_RESPONSE_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration,
            PROPERTY_AZURE_REQUEST_RESPONSE_TIMEOUT, Duration.ofSeconds(60), logger);
        DEFAULT_READ_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration, PROPERTY_AZURE_REQUEST_READ_TIMEOUT,
            Duration.ofSeconds(60), logger);
    }

    private final ClientLogger logger = new ClientLogger(HttpClientOptions.class);

    private ProxyOptions proxyOptions;
    private Configuration configuration;
    private Duration connectTimeout;
    private Duration writeTimeout;
    private Duration responseTimeout;
    private Duration readTimeout;
    private Integer maximumConnectionPoolSize;
    private Duration connectionIdleTimeout;

    @Override
    public HttpClientOptions setApplicationId(String applicationId) {
        super.setApplicationId(applicationId);

        return this;
    }

    @Override
    public HttpClientOptions setHeaders(Iterable<Header> headers) {
        super.setHeaders(headers);

        return this;
    }

    /**
     * Sets the {@link ProxyOptions proxy options} that the {@link HttpClient} will use.
     *
     * @param proxyOptions The proxy options to use.
     * @return The updated HttpClientOptions object.
     */
    public HttpClientOptions setProxyOptions(ProxyOptions proxyOptions) {
        this.proxyOptions = proxyOptions;
        return this;
    }

    /**
     * Gets the {@link ProxyOptions proxy options} that the {@link HttpClient} will use.
     *
     * @return The proxy options to use.
     */
    public ProxyOptions getProxyOptions() {
        return proxyOptions;
    }

    /**
     * Sets the configuration store that the {@link HttpClient} will use.
     *
     * @param configuration The configuration store to use.
     * @return The updated HttpClientOptions object.
     */
    public HttpClientOptions setConfiguration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Gets the configuration store that the {@link HttpClient} will use.
     *
     * @return The configuration store to use.
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Sets the connection timeout for a request to be sent.
     * <p>
     * The connection timeout begins once the request attempts to connect to the remote host and finishes when the
     * connection is resolved.
     * <p>
     * If {@code connectTimeout} is null either {@link Configuration#PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT} or a
     * 10-second timeout will be used, if it is a {@link Duration} less than or equal to zero then no timeout will be
     * applied. When applying the timeout the greatest of one millisecond and the value of {@code connectTimeout} will
     * be used.
     * <p>
     * By default the connection timeout is 10 seconds.
     *
     * @param connectTimeout Connect timeout duration.
     * @return The updated HttpClientOptions object.
     */
    public HttpClientOptions setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * Gets the connection timeout for a request to be sent.
     * <p>
     * By default the connection timeout is 10 seconds.
     *
     * @return The connection timeout of a request to be sent.
     */
    public Duration getConnectTimeout() {
        return getTimeout(connectTimeout, DEFAULT_CONNECT_TIMEOUT);
    }

    /**
     * Sets the writing timeout for a request to be sent.
     * <p>
     * The writing timeout does not apply to the entire request but to each emission being sent over the wire. For
     * example a request body which emits {@code 10} {@code 8KB} buffers will trigger {@code 10} write operations, the
     * outbound buffer will be periodically checked to determine if it is still draining.
     * <p>
     * If {@code writeTimeout} is null either {@link Configuration#PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT} or a 60-second
     * timeout will be used, if it is a {@link Duration} less than or equal to zero then no write timeout will be
     * applied. When applying the timeout the greatest of one millisecond and the value of {@code writeTimeout} will be
     * used.
     * <p>
     * By default the writing timeout is 60 seconds.
     *
     * @param writeTimeout Write operation timeout duration.
     * @return The updated HttpClientOptions object.
     */
    public HttpClientOptions setWriteTimeout(Duration writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    /**
     * Gets the writing timeout for a request to be sent.
     * <p>
     * By default the writing timeout is 60 seconds.
     *
     * @return The writing timeout of a request to be sent.
     */
    public Duration getWriteTimeout() {
        return getTimeout(writeTimeout, DEFAULT_WRITE_TIMEOUT);
    }

    /**
     * Sets the response timeout duration used when waiting for a server to reply.
     * <p>
     * The response timeout begins once the request write completes and finishes once the first response read is
     * triggered when the server response is received.
     * <p>
     * If {@code responseTimeout} is null either {@link Configuration#PROPERTY_AZURE_REQUEST_RESPONSE_TIMEOUT} or a
     * 60-second timeout will be used, if it is a {@link Duration} less than or equal to zero then no timeout will be
     * applied to the response. When applying the timeout the greatest of one millisecond and the value of {@code
     * responseTimeout} will be used.
     * <p>
     * By default the response timeout is 60 seconds.
     *
     * @param responseTimeout Response timeout duration.
     * @return The updated HttpClientOptions object.
     */
    public HttpClientOptions responseTimeout(Duration responseTimeout) {
        this.responseTimeout = responseTimeout;
        return this;
    }

    /**
     * Sets the response timeout duration used when waiting for a server to reply.
     * <p>
     * The response timeout begins once the request write completes and finishes once the first response read is
     * triggered when the server response is received.
     * <p>
     * If {@code responseTimeout} is null either {@link Configuration#PROPERTY_AZURE_REQUEST_RESPONSE_TIMEOUT} or a
     * 60-second timeout will be used, if it is a {@link Duration} less than or equal to zero then no timeout will be
     * applied to the response. When applying the timeout the greatest of one millisecond and the value of {@code
     * responseTimeout} will be used.
     * <p>
     * By default the response timeout is 60 seconds.
     *
     * @param responseTimeout Response timeout duration.
     * @return The updated HttpClientOptions object.
     */
    public HttpClientOptions setResponseTimeout(Duration responseTimeout) {
        this.responseTimeout = responseTimeout;
        return this;
    }

    /**
     * Gets the response timeout duration used when waiting for a server to reply.
     * <p>
     * By default the response timeout is 60 seconds.
     *
     * @return The response timeout duration.
     */
    public Duration getResponseTimeout() {
        return getTimeout(responseTimeout, DEFAULT_RESPONSE_TIMEOUT);
    }

    /**
     * Sets the read timeout duration used when reading the server response.
     * <p>
     * The read timeout begins once the first response read is triggered after the server response is received. This
     * timeout triggers periodically but won't fire its operation if another read operation has completed between when
     * the timeout is triggered and completes.
     * <p>
     * If {@code readTimeout} is null either {@link Configuration#PROPERTY_AZURE_REQUEST_READ_TIMEOUT} or a 60-second
     * timeout will be used, if it is a {@link Duration} less than or equal to zero then no timeout period will be
     * applied to response read. When applying the timeout the greatest of one millisecond and the value of {@code
     * readTimeout} will be used.
     * <p>
     * By default the read timeout is 60 seconds.
     *
     * @param readTimeout Read timeout duration.
     * @return The updated HttpClientOptions object.
     */
    public HttpClientOptions readTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    /**
     * Sets the read timeout duration used when reading the server response.
     * <p>
     * The read timeout begins once the first response read is triggered after the server response is received. This
     * timeout triggers periodically but won't fire its operation if another read operation has completed between when
     * the timeout is triggered and completes.
     * <p>
     * If {@code readTimeout} is null either {@link Configuration#PROPERTY_AZURE_REQUEST_READ_TIMEOUT} or a 60-second
     * timeout will be used, if it is a {@link Duration} less than or equal to zero then no timeout period will be
     * applied to response read. When applying the timeout the greatest of one millisecond and the value of {@code
     * readTimeout} will be used.
     * <p>
     * By default the read timeout is 60 seconds.
     *
     * @param readTimeout Read timeout duration.
     * @return The updated HttpClientOptions object.
     */
    public HttpClientOptions setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    /**
     * Gets the read timeout duration used when reading the server response.
     * <p>
     * By default the read timeout is 60 seconds.
     *
     * @return The read timeout duration.
     */
    public Duration getReadTimeout() {
        return getTimeout(readTimeout, DEFAULT_READ_TIMEOUT);
    }

    /**
     * Sets the maximum connection pool size used by the underlying HTTP client.
     * <p>
     * Modifying the maximum connection pool size may have effects on the performance of an application. Increasing the
     * maximum connection pool will result in more connections being available for an application but may result in more
     * contention for network resources. It is recommended to perform performance analysis on different maximum
     * connection pool sizes to find the right configuration for an application.
     * <p>
     * This maximum connection pool size is not a global configuration but an instance level configuration for each
     * {@link HttpClient} created using this {@link HttpClientOptions}.
     * <p>
     * By default the maximum connection pool size is determined by the underlying HTTP client. Setting the maximum
     * connection pool size to null resets the configuration to use the default determined by the underlying HTTP
     * client.
     *
     * @param maximumConnectionPoolSize The maximum connection pool size.
     * @return The updated HttpClientOptions object.
     * @throws IllegalArgumentException If {@code maximumConnectionPoolSize} is not null and is less than {@code 1}.
     */
    public HttpClientOptions setMaximumConnectionPoolSize(Integer maximumConnectionPoolSize) {
        if (maximumConnectionPoolSize != null && maximumConnectionPoolSize <= 0) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'maximumConnectionPoolSize' cannot be less than 1."));
        }

        this.maximumConnectionPoolSize = maximumConnectionPoolSize;
        return this;
    }

    /**
     * Gets the maximum connection pool size used by the underlying HTTP client.
     * <p>
     * Modifying the maximum connection pool size may have effects on the performance of an application. Increasing the
     * maximum connection pool will result in more connections being available for an application but may result in more
     * contention for network resources. It is recommended to perform performance analysis on different maximum
     * connection pool sizes to find the right configuration for an application.
     * <p>
     * This maximum connection pool size is not a global configuration but an instance level configuration for each
     * {@link HttpClient} created using this {@link HttpClientOptions}.
     * <p>
     * By default the maximum connection pool size is determined by the underlying HTTP client. Setting the maximum
     * connection pool size to null resets the configuration to use the default determined by the underlying HTTP
     * client.
     *
     * @return The maximum connection pool size.
     */
    public Integer getMaximumConnectionPoolSize() {
        return maximumConnectionPoolSize;
    }

    /**
     * Sets the duration of time before an idle connection.
     * <p>
     * The connection idle timeout begins once the connection has completed its last network request. Every time the
     * connection is used the idle timeout will reset.
     * <p>
     * If {@code connectionIdleTimeout} is null a 60-second timeout will be used, if it is a {@link Duration} less than
     * or equal to zero then no timeout period will be applied. When applying the timeout the greatest of one
     * millisecond and the value of {@code connectionIdleTimeout} will be used.
     * <p>
     * By default the connection idle timeout is 60 seconds.
     *
     * @param connectionIdleTimeout The connection idle timeout duration.
     * @return The updated HttpClientOptions object.
     */
    public HttpClientOptions setConnectionIdleTimeout(Duration connectionIdleTimeout) {
        this.connectionIdleTimeout = connectionIdleTimeout;
        return this;
    }

    /**
     * Gets the duration of time before an idle connection is closed.
     * <p>
     * By default the connection idle timeout is 60 seconds.
     *
     * @return The connection idle timeout duration.
     */
    public Duration getConnectionIdleTimeout() {
        return getTimeout(connectionIdleTimeout, DEFAULT_CONNECTION_IDLE_TIMEOUT);
    }

    private static Duration getTimeout(Duration configuredTimeout, Duration defaultTimeout) {
        // Timeout is null, use the default timeout.
        if (configuredTimeout == null) {
            return defaultTimeout;
        }

        // Timeout is less than or equal to zero, return no timeout.
        if (configuredTimeout.isZero() || configuredTimeout.isNegative()) {
            return NO_TIMEOUT;
        }

        // Return the maximum of the timeout period and the minimum allowed timeout period.
        return configuredTimeout.compareTo(MINIMUM_TIMEOUT) > 0 ? configuredTimeout : MINIMUM_TIMEOUT;
    }
}
