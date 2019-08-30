// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import com.azure.core.http.HttpClient;
import com.azure.core.util.logging.ClientLogger;
import okhttp3.Authenticator;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builder to configure and build an implementation of com.azure.core.http.HttpClient for OkHttp.
 */
public class OkHttpAsyncHttpClientBuilder {
    private final ClientLogger logger = new ClientLogger(OkHttpAsyncHttpClientBuilder.class);
    private final okhttp3.OkHttpClient okHttpClient;
    //
    private final static Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(120);
    private final static Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(60);
    //
    private List<Interceptor> networkInterceptors = new ArrayList<>();
    private Duration readTimeout;
    private Duration connectionTimeout;
    private ConnectionPool connectionPool;
    private Dispatcher dispatcher;
    private java.net.Proxy proxy;
    private Authenticator proxyAuthenticator;

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
    public OkHttpAsyncHttpClientBuilder(okhttp3.OkHttpClient okHttpClient) {
        this.okHttpClient = Objects.requireNonNull(okHttpClient, "okHttpClient == null");
    }

    /**
     * Add a network layer interceptor to Http request pipeline.
     *
     * @param networkInterceptor the interceptor to add
     * @return the builder
     */
    public OkHttpAsyncHttpClientBuilder networkInterceptor(Interceptor networkInterceptor) {
        Objects.requireNonNull(networkInterceptor);
        this.networkInterceptors.add(networkInterceptor);
        return this;
    }

    /**
     * Add network layer interceptors to Http request pipeline.
     *
     * This replaces all previously-set interceptors.
     *
     * @param networkInterceptors the interceptors to add
     * @return the builder
     */
    public OkHttpAsyncHttpClientBuilder networkInterceptors(List<Interceptor> networkInterceptors) {
        this.networkInterceptors = Objects.requireNonNull(networkInterceptors);
        return this;
    }

    /**
     * Sets the read timeout.
     *
     * The default read timeout is 120 seconds.
     *
     * @param readTimeout the timeout
     * @return the builder
     */
    public OkHttpAsyncHttpClientBuilder readTimeout(Duration readTimeout) {
        // readTimeout can be null
        this.readTimeout = readTimeout;
        return this;
    }

    /**
     * Sets the connection timeout.
     *
     * The default read timeout is 60 seconds.
     *
     * @param connectionTimeout the timeout
     * @return the builder
     */
    public OkHttpAsyncHttpClientBuilder connectionTimeout(Duration connectionTimeout) {
        // connectionTimeout can be null
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    /**
     * Sets the Http connection pool.
     *
     * @param connectionPool the OkHttp connection pool to use
     * @return the builder
     */
    public OkHttpAsyncHttpClientBuilder connectionPool(ConnectionPool connectionPool) {
        // Null ConnectionPool is not allowed
        this.connectionPool = Objects.requireNonNull(connectionPool, "connectionPool == null");
        return this;
    }

    /**
     * Sets the dispatcher that also composes the thread pool for executing HTTP requests.
     *
     * @param dispatcher the dispatcher to use
     * @return the builder
     */
    public OkHttpAsyncHttpClientBuilder dispatcher(Dispatcher dispatcher) {
        // Null Dispatcher is not allowed
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
        // Proxy can be null
        this.proxy = proxy;
        return this;
    }

    /**
     * Sets the proxy authenticator.
     *
     * @param proxyAuthenticator the proxy authenticator
     * @return the builder
     */
    public OkHttpAsyncHttpClientBuilder proxyAuthenticator(Authenticator proxyAuthenticator) {
        // Null Authenticator is not allowed
        this.proxyAuthenticator = Objects.requireNonNull(proxyAuthenticator, "proxyAuthenticator == null");
        return this;
    }

    /**
     * Build a HttpClient with current configurations.
     *
     * @return a {@link HttpClient}.
     */
    public HttpClient build() {
        OkHttpClient.Builder httpClientBuilder = this.okHttpClient == null
                ? new OkHttpClient.Builder()
                : this.okHttpClient.newBuilder();
        //
        for (Interceptor interceptor : this.networkInterceptors) {
            httpClientBuilder = httpClientBuilder.addNetworkInterceptor(interceptor);
        }
        if (this.readTimeout != null) {
            httpClientBuilder = httpClientBuilder.readTimeout(this.readTimeout);
        } else {
            httpClientBuilder = httpClientBuilder.readTimeout(DEFAULT_READ_TIMEOUT);
        }
        if (this.connectionTimeout != null) {
            httpClientBuilder = httpClientBuilder.connectTimeout(this.connectionTimeout);
        } else {
            httpClientBuilder = httpClientBuilder.connectTimeout(DEFAULT_CONNECT_TIMEOUT);
        }
        if (this.connectionPool != null) {
            httpClientBuilder = httpClientBuilder.connectionPool(connectionPool);
        }
        if (this.dispatcher != null) {
            httpClientBuilder = httpClientBuilder.dispatcher(dispatcher);
        }
        httpClientBuilder = httpClientBuilder.proxy(this.proxy);
        if (this.proxyAuthenticator != null) {
            httpClientBuilder = httpClientBuilder.authenticator(this.proxyAuthenticator);
        }
        return new OkHttpAsyncHttpClient(httpClientBuilder.build());
    }
}
