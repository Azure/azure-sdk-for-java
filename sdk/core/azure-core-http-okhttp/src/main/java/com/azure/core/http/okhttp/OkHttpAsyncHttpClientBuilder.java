// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.okhttp.implementation.OkHttpProxySelector;
import com.azure.core.http.okhttp.implementation.ProxyAuthenticator;
import com.azure.core.util.Configuration;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builder class responsible for creating instances of {@link com.azure.core.http.HttpClient} backed by OkHttp.
 */
public class OkHttpAsyncHttpClientBuilder {

    private final okhttp3.OkHttpClient okHttpClient;

    private static final Duration MINIMUM_TIMEOUT = Duration.ofMillis(1);
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration DEFAULT_IO_TIMEOUT = Duration.ofSeconds(60);

    private List<Interceptor> networkInterceptors = new ArrayList<>();
    private Duration readTimeout;
    private Duration writeTimeout;
    private Duration connectionTimeout;
    private ConnectionPool connectionPool;
    private Dispatcher dispatcher;
    private ProxyOptions proxyOptions;
    private Configuration configuration;

    /**
     * Creates OkHttpAsyncHttpClientBuilder.
     */
    public OkHttpAsyncHttpClientBuilder() {
        this.okHttpClient = null;
    }

    /**
     * Creates OkHttpAsyncHttpClientBuilder from the builder of an existing OkHttpClient.
     *
     * @param okHttpClient the httpclient
     */
    public OkHttpAsyncHttpClientBuilder(OkHttpClient okHttpClient) {
        this.okHttpClient = Objects.requireNonNull(okHttpClient, "'okHttpClient' cannot be null.");
    }

    /**
     * Add a network layer interceptor to Http request pipeline.
     *
     * @param networkInterceptor the interceptor to add
     * @return the updated OkHttpAsyncHttpClientBuilder object
     */
    public OkHttpAsyncHttpClientBuilder addNetworkInterceptor(Interceptor networkInterceptor) {
        Objects.requireNonNull(networkInterceptor, "'networkInterceptor' cannot be null.");
        this.networkInterceptors.add(networkInterceptor);
        return this;
    }

    /**
     * Add network layer interceptors to Http request pipeline.
     * <p>
     * This replaces all previously-set interceptors.
     *
     * @param networkInterceptors The interceptors to add.
     * @return The updated OkHttpAsyncHttpClientBuilder object.
     */
    public OkHttpAsyncHttpClientBuilder networkInterceptors(List<Interceptor> networkInterceptors) {
        this.networkInterceptors = Objects.requireNonNull(networkInterceptors, "'networkInterceptors' cannot be null.");
        return this;
    }

    /**
     * Sets the read timeout.
     * <p>
     * If {@code readTimeout} is {@code null} a default timeout of 60 seconds will be used. If the timeout is less than
     * or equal to zero then no timeout will be used. Otherwise, the maximum of one millisecond and the passed timeout
     * will be used.
     *
     * @param readTimeout The read timeout.
     * @return The updated OkHttpAsyncHttpClientBuilder object.
     */
    public OkHttpAsyncHttpClientBuilder readTimeout(Duration readTimeout) {
        // setReadTimeout can be null
        this.readTimeout = readTimeout;
        return this;
    }

    /**
     * Sets the write timeout.
     * <p>
     * If {@code writeTimeout} is {@code null} a default timeout of 60 seconds will be used. If the timeout is less than
     * or equal to zero then no timeout will be used. Otherwise, the maximum of one millisecond and the passed timeout
     * will be used.
     *
     * @param writeTimeout The write timeout.
     * @return The updated OkHttpAsyncHttpClientBuilder object.
     */
    public OkHttpAsyncHttpClientBuilder writeTimeout(Duration writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    /**
     * Sets the connection timeout.
     * <p>
     * If {@code connectionTimeout} is {@code null} or less than or equal to zero a default timeout of 10 seconds will
     * be used. Otherwise, the maximum of one millisecond and the passed timeout will be used.
     *
     * @param connectionTimeout The connection timeout.
     * @return The updated OkHttpAsyncHttpClientBuilder object.
     */
    public OkHttpAsyncHttpClientBuilder connectionTimeout(Duration connectionTimeout) {
        // setConnectionTimeout can be null
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    /**
     * Sets the Http connection pool.
     *
     * @param connectionPool The OkHttp connection pool to use.
     * @return The updated OkHttpAsyncHttpClientBuilder object.
     */
    public OkHttpAsyncHttpClientBuilder connectionPool(ConnectionPool connectionPool) {
        // Null ConnectionPool is not allowed
        this.connectionPool = Objects.requireNonNull(connectionPool, "'connectionPool' cannot be null.");
        return this;
    }

    /**
     * Sets the dispatcher that also composes the thread pool for executing HTTP requests.
     *
     * @param dispatcher The dispatcher to use.
     * @return The updated OkHttpAsyncHttpClientBuilder object.
     */
    public OkHttpAsyncHttpClientBuilder dispatcher(Dispatcher dispatcher) {
        // Null Dispatcher is not allowed
        this.dispatcher = Objects.requireNonNull(dispatcher, "'dispatcher' cannot be null.");
        return this;
    }

    /**
     * Sets the proxy.
     *
     * @param proxyOptions The proxy configuration to use.
     * @return The updated OkHttpAsyncHttpClientBuilder object.
     */
    public OkHttpAsyncHttpClientBuilder proxy(ProxyOptions proxyOptions) {
        // proxyOptions can be null
        this.proxyOptions = proxyOptions;
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the HTTP client.
     * <p>
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store.
     * @return The updated OkHttpAsyncHttpClientBuilder object.
     */
    public OkHttpAsyncHttpClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Creates a new OkHttp-backed {@link com.azure.core.http.HttpClient} instance on every call, using the
     * configuration set in the builder at the time of the build method call.
     *
     * @return A new OkHttp-backed {@link com.azure.core.http.HttpClient} instance.
     */
    public HttpClient build() {
        OkHttpClient.Builder httpClientBuilder = this.okHttpClient == null
            ? new OkHttpClient.Builder()
            : this.okHttpClient.newBuilder();

        // Add each interceptor that has been added.
        for (Interceptor interceptor : this.networkInterceptors) {
            httpClientBuilder = httpClientBuilder.addNetworkInterceptor(interceptor);
        }

        // Configure operation timeouts.
        httpClientBuilder = httpClientBuilder.connectTimeout(convertConnectTimeout(connectionTimeout))
            .writeTimeout(convertIoTimeout(writeTimeout))
            .readTimeout(convertIoTimeout(readTimeout));

        // If set use the configured connection pool.
        if (this.connectionPool != null) {
            httpClientBuilder = httpClientBuilder.connectionPool(connectionPool);
        }

        // If set use the configured dispatcher.
        if (this.dispatcher != null) {
            httpClientBuilder = httpClientBuilder.dispatcher(dispatcher);
        }

        Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration()
            : configuration;

        ProxyOptions buildProxyOptions = (proxyOptions == null && buildConfiguration != Configuration.NONE)
            ? ProxyOptions.fromConfiguration(buildConfiguration)
            : proxyOptions;

        if (buildProxyOptions != null) {
            httpClientBuilder = httpClientBuilder.proxySelector(new OkHttpProxySelector(
                buildProxyOptions.getType().toProxyType(),
                buildProxyOptions.getAddress(),
                buildProxyOptions.getNonProxyHosts()));

            if (buildProxyOptions.getUsername() != null) {
                ProxyAuthenticator proxyAuthenticator = new ProxyAuthenticator(buildProxyOptions.getUsername(),
                    buildProxyOptions.getPassword());

                httpClientBuilder = httpClientBuilder.proxyAuthenticator(proxyAuthenticator)
                    .addInterceptor(proxyAuthenticator.getProxyAuthenticationInfoInterceptor());
            }
        }

        return new OkHttpAsyncHttpClient(httpClientBuilder.build());
    }

    /*
     * Convert the connect timeout configured in the builder. If the timeout is null or less than or equal to zero a
     * default timeout of 10 seconds will be used. Otherwise, the maximum of the configured timeout and one millisecond
     * is used.
     */
    private static Duration convertConnectTimeout(Duration timeout) {
        return convertTimeout(timeout, DEFAULT_CONNECT_TIMEOUT, true);
    }

    /*
     * Convert the IO timeout configured in the builder. If the timeout is null a default timeout of 60 seconds will be
     * used. If the timeout is less than or equal to zero a zero duration timeout will be used it indicate no timeout.
     * Finally, if neither of the cases above are true then the maximum of the configured timeout and one millisecond is
     * used.
     */
    private static Duration convertIoTimeout(Duration timeout) {
        return convertTimeout(timeout, DEFAULT_IO_TIMEOUT, false);
    }

    private static Duration convertTimeout(Duration timeout, Duration defaultTimeout,
        boolean useDefaultWhenLessThanZero) {
        if (timeout == null) {
            return defaultTimeout;
        }

        if (timeout.isNegative() || timeout.isZero()) {
            return useDefaultWhenLessThanZero ? defaultTimeout : Duration.ZERO;
        }

        return (timeout.compareTo(MINIMUM_TIMEOUT) < 0) ? MINIMUM_TIMEOUT : timeout;
    }
}
