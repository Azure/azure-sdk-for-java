// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import com.azure.core.http.HttpClient;
import okhttp3.Authenticator;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import java.net.Proxy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builder to configure and build an implementation of {@link HttpClient} for OkHttp.
 */
public class OkHttpAsyncHttpClientBuilder {
    private final okhttp3.OkHttpClient okHttpClient;

    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(120);
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(60);

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
    public OkHttpAsyncHttpClientBuilder(OkHttpClient okHttpClient) {
        this.okHttpClient = Objects.requireNonNull(okHttpClient, "okHttpClient cannot be null.");
    }

    /**
     * Add a network layer interceptor to Http request pipeline.
     *
     * @param networkInterceptor the interceptor to add
     * @return the updated OkHttpAsyncHttpClientBuilder object
     */
    public OkHttpAsyncHttpClientBuilder addNetworkInterceptor(Interceptor networkInterceptor) {
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
     * @return the updated OkHttpAsyncHttpClientBuilder object
     */
    public OkHttpAsyncHttpClientBuilder networkInterceptors(List<Interceptor> networkInterceptors) {
        this.networkInterceptors = Objects.requireNonNull(networkInterceptors, "networkInterceptors cannot be null.");
        return this;
    }

    /**
     * Sets the read timeout.
     *
     * The default read timeout is 120 seconds.
     *
     * @param readTimeout the read timeout
     * @return the updated OkHttpAsyncHttpClientBuilder object
     */
    public OkHttpAsyncHttpClientBuilder readTimeout(Duration readTimeout) {
        // setReadTimeout can be null
        this.readTimeout = readTimeout;
        return this;
    }

    /**
     * Sets the connection timeout.
     *
     * The default connection timeout is 60 seconds.
     *
     * @param connectionTimeout the connection timeout
     * @return the updated OkHttpAsyncHttpClientBuilder object
     */
    public OkHttpAsyncHttpClientBuilder connectionTimeout(Duration connectionTimeout) {
        // setConnectionTimeout can be null
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    /**
     * Sets the Http connection pool.
     *
     * @param connectionPool the OkHttp connection pool to use
     * @return the updated OkHttpAsyncHttpClientBuilder object
     */
    public OkHttpAsyncHttpClientBuilder connectionPool(ConnectionPool connectionPool) {
        // Null ConnectionPool is not allowed
        this.connectionPool = Objects.requireNonNull(connectionPool, "connectionPool cannot be null.");
        return this;
    }

    /**
     * Sets the dispatcher that also composes the thread pool for executing HTTP requests.
     *
     * @param dispatcher the dispatcher to use
     * @return the updated OkHttpAsyncHttpClientBuilder object
     */
    public OkHttpAsyncHttpClientBuilder dispatcher(Dispatcher dispatcher) {
        // Null Dispatcher is not allowed
        this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher cannot be null.");
        return this;
    }

    /**
     * Sets the proxy.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder#proxy}
     *
     * @param proxy the proxy
     * @return the updated OkHttpAsyncHttpClientBuilder object
     */
    public OkHttpAsyncHttpClientBuilder proxy(Proxy proxy) {
        // Proxy can be null
        this.proxy = proxy;
        return this;
    }

    /**
     * Sets the proxy authenticator.
     *
     * @param proxyAuthenticator the proxy authenticator
     * @return the updated OkHttpAsyncHttpClientBuilder object
     */
    public OkHttpAsyncHttpClientBuilder proxyAuthenticator(Authenticator proxyAuthenticator) {
        // Null Authenticator is not allowed
        this.proxyAuthenticator = Objects.requireNonNull(proxyAuthenticator, "proxyAuthenticator cannot be null.");
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
