// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import com.azure.core.http.HttpClient;
import com.azure.core.util.logging.ClientLogger;
import okhttp3.Authenticator;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Builder to configure and build an implementation of com.azure.core.http.HttpClient for OkHttp.
 */
public class OkHttpAsyncHttpClientBuilder {
    private final ClientLogger logger = new ClientLogger(OkHttpAsyncHttpClientBuilder.class);
    private okhttp3.OkHttpClient.Builder httpClientBuilder;
    private Dispatcher dispatcher;
    private ConnectionPool connectionPool;
    //
    private final static long DEFAULT_READ_TIMEOUT_IN_SEC = 120;
    private final static long DEFAULT_CONNECT_TIMEOUT_IN_SEC = 60;

    /**
     * Creates OkHttpAsyncHttpClientBuilder.
     */
    public OkHttpAsyncHttpClientBuilder() {
        this(new okhttp3.OkHttpClient.Builder());
    }

    /**
     * Creates OkHttpAsyncHttpClientBuilder from an existing OkHttp builder.
     *
     * @param httpClientBuilder the base builder to use.
     */
    public OkHttpAsyncHttpClientBuilder(okhttp3.OkHttpClient.Builder httpClientBuilder) {
        Objects.requireNonNull(httpClientBuilder);
        // Builder with default settings
        this.httpClientBuilder = httpClientBuilder
                .readTimeout(DEFAULT_READ_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT_IN_SEC, TimeUnit.SECONDS);
    }


    /**
     * Add a network layer interceptor to Http request pipeline.
     *
     * @param networkInterceptor the interceptor to add
     * @return the builder
     */
    public OkHttpAsyncHttpClientBuilder networkInterceptor(Interceptor networkInterceptor) {
        Objects.requireNonNull(networkInterceptor);
        this.httpClientBuilder.addNetworkInterceptor(networkInterceptor);
        return this;
    }

    /**
     * Set the read timeout, default is 120 seconds.
     *
     * @param timeout the timeout
     * @param unit the time unit for the timeout
     * @return the builder
     */
    public OkHttpAsyncHttpClientBuilder readTimeout(long timeout, TimeUnit unit) {
        this.httpClientBuilder.readTimeout(timeout, unit);
        return this;
    }

    /**
     * Set the connection timeout, default is 60 seconds.
     *
     * @param timeout the timeout
     * @param unit the time unit for the timeout
     * @return the builder
     */
    public OkHttpAsyncHttpClientBuilder connectionTimeout(long timeout, TimeUnit unit) {
        this.httpClientBuilder.connectTimeout(timeout, unit);
        return this;
    }

    /**
     * Sets the Http connection pool.
     *
     * @param connectionPool the OkHttp connection pool to use
     * @return the builder
     */
    public OkHttpAsyncHttpClientBuilder connectionPool(ConnectionPool connectionPool) {
        this.connectionPool = Objects.requireNonNull(connectionPool, "connectionPool == null");
        return this;
    }

    /**
     * Sets the dispatcher that also composes  the thread pool for executing HTTP requests.
     *
     * @param dispatcher the dispatcher to use
     * @return the builder
     */
    public OkHttpAsyncHttpClientBuilder dispatcher(Dispatcher dispatcher) {
        this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher == null");
        return this;
    }

    /**
     * Sets the proxy.
     *
     * @param proxy the proxy
     * @return the builder
     */
    public OkHttpAsyncHttpClientBuilder proxy(java.net.Proxy proxy) {
        this.httpClientBuilder.proxy(proxy);
        return this;
    }

    /**
     * Sets the proxy authenticator.
     *
     * @param proxyAuthenticator the proxy authenticator
     * @return the builder
     */
    public OkHttpAsyncHttpClientBuilder proxyAuthenticator(Authenticator proxyAuthenticator) {
        this.httpClientBuilder.proxyAuthenticator(proxyAuthenticator);
        return this;
    }

    /**
     * Build a HttpClient with current configurations.
     *
     * @return a {@link HttpClient}.
     */
    public HttpClient build() {
        if (connectionPool != null) {
            this.httpClientBuilder = httpClientBuilder.connectionPool(connectionPool);
        }
        if (dispatcher != null) {
            this.httpClientBuilder = httpClientBuilder.dispatcher(dispatcher);
        }
        return new OkHttpAsyncHttpClient(this.httpClientBuilder.build());
    }

}
