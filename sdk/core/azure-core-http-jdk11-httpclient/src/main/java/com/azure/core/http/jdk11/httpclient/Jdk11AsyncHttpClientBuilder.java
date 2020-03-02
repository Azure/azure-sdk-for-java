// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk11.httpclient;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.jdk11.httpclient.implementation.Jdk11HttpClientProxySelector;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Builder to configure and build an implementation of {@link HttpClient} for OkHttp.
 */
public class Jdk11AsyncHttpClientBuilder {
    private final ClientLogger logger = new ClientLogger(Jdk11AsyncHttpClientBuilder.class);

    private final java.net.http.HttpClient jdk11HttpClient;

//    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(120);
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(60);

//    private List<Interceptor> networkInterceptors = new ArrayList<>();
//    private Duration readTimeout;
    private Duration connectionTimeout;
//    private ConnectionPool connectionPool;
//    private Dispatcher dispatcher;
    private ProxyOptions proxyOptions;
    private Configuration configuration;

    /**
     * Creates OkHttpAsyncHttpClientBuilder.
     */
    public Jdk11AsyncHttpClientBuilder() {
        this.jdk11HttpClient = null;
    }

    /**
     * Creates OkHttpAsyncHttpClientBuilder from the builder of an existing OkHttpClient.
     *
     * @param jdk11HttpClient the httpclient
     */
    public Jdk11AsyncHttpClientBuilder(java.net.http.HttpClient jdk11HttpClient) {
        this.jdk11HttpClient = Objects.requireNonNull(jdk11HttpClient, "'jdk11HttpClient' cannot be null.");
    }

//    /**
//     * Add a network layer interceptor to Http request pipeline.
//     *
//     * @param networkInterceptor the interceptor to add
//     * @return the updated OkHttpAsyncHttpClientBuilder object
//     */
//    public Jdk11AsyncHttpClientBuilder addNetworkInterceptor(Interceptor networkInterceptor) {
//        Objects.requireNonNull(networkInterceptor, "'networkInterceptor' cannot be null.");
//        this.networkInterceptors.add(networkInterceptor);
//        return this;
//    }
//
//    /**
//     * Add network layer interceptors to Http request pipeline.
//     *
//     * This replaces all previously-set interceptors.
//     *
//     * @param networkInterceptors the interceptors to add
//     * @return the updated OkHttpAsyncHttpClientBuilder object
//     */
//    public Jdk11AsyncHttpClientBuilder networkInterceptors(List<Interceptor> networkInterceptors) {
//        this.networkInterceptors = Objects.requireNonNull(networkInterceptors, "'networkInterceptors' cannot be null.");
//        return this;
//    }
//
//    /**
//     * Sets the read timeout.
//     *
//     * The default read timeout is 120 seconds.
//     *
//     * @param readTimeout the read timeout
//     * @return the updated OkHttpAsyncHttpClientBuilder object
//     */
//    public Jdk11AsyncHttpClientBuilder readTimeout(Duration readTimeout) {
//        // setReadTimeout can be null
//        this.readTimeout = readTimeout;
//        return this;
//    }
//
    /**
     * Sets the connection timeout.
     *
     * The default connection timeout is 60 seconds.
     *
     * @param connectionTimeout the connection timeout
     * @return the updated OkHttpAsyncHttpClientBuilder object
     */
    public Jdk11AsyncHttpClientBuilder connectionTimeout(Duration connectionTimeout) {
        // setConnectionTimeout can be null
        this.connectionTimeout = connectionTimeout;
        return this;
    }
//
//    /**
//     * Sets the Http connection pool.
//     *
//     * @param connectionPool the OkHttp connection pool to use
//     * @return the updated OkHttpAsyncHttpClientBuilder object
//     */
//    public Jdk11AsyncHttpClientBuilder connectionPool(ConnectionPool connectionPool) {
//        // Null ConnectionPool is not allowed
//        this.connectionPool = Objects.requireNonNull(connectionPool, "'connectionPool' cannot be null.");
//        return this;
//    }
//
//    /**
//     * Sets the dispatcher that also composes the thread pool for executing HTTP requests.
//     *
//     * @param dispatcher the dispatcher to use
//     * @return the updated OkHttpAsyncHttpClientBuilder object
//     */
//    public Jdk11AsyncHttpClientBuilder dispatcher(Dispatcher dispatcher) {
//        // Null Dispatcher is not allowed
//        this.dispatcher = Objects.requireNonNull(dispatcher, "'dispatcher' cannot be null.");
//        return this;
//    }
//
    /**
     * Sets the proxy.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder.proxy#ProxyOptions}
     *
     * @param proxyOptions The proxy configuration to use.
     * @return the updated {@link Jdk11AsyncHttpClientBuilder} object
     */
    public Jdk11AsyncHttpClientBuilder proxy(ProxyOptions proxyOptions) {
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
     * @param configuration The configuration store used to
     * @return The updated OkHttpAsyncHttpClientBuilder object.
     */
    public Jdk11AsyncHttpClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Build a HttpClient with current configurations.
     *
     * @return a {@link HttpClient}.
     */
    public HttpClient build() {
        java.net.http.HttpClient.Builder httpClientBuilder = java.net.http.HttpClient.newBuilder();

        // Use the configured connection timeout if set, otherwise use the default (60s).
        httpClientBuilder = (this.connectionTimeout != null)
            ? httpClientBuilder.connectTimeout(this.connectionTimeout)
            : httpClientBuilder.connectTimeout(DEFAULT_CONNECT_TIMEOUT);

        Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration()
            : configuration;

        ProxyOptions buildProxyOptions = (proxyOptions == null && buildConfiguration != Configuration.NONE)
            ? ProxyOptions.fromConfiguration(buildConfiguration)
            : proxyOptions;

        if (buildProxyOptions != null) {
            httpClientBuilder = httpClientBuilder.proxy(new Jdk11HttpClientProxySelector(
                mapProxyType(buildProxyOptions.getType(), logger), buildProxyOptions.getAddress(),
                buildProxyOptions.getNonProxyHosts()));

            if (buildProxyOptions.getUsername() != null) {
                httpClientBuilder.authenticator(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(proxyOptions.getUsername(), proxyOptions.getPassword().toCharArray());
                    }
                });
            }
        }

        return new Jdk11AsyncHttpClient(httpClientBuilder.build());
    }

    /*
     * Maps a 'ProxyOptions.Type' to a 'ProxyProvider.Proxy', if the type is unknown or cannot be mapped an
     * IllegalStateException will be thrown.
     */
    private static Proxy.Type mapProxyType(ProxyOptions.Type type, ClientLogger logger) {
        Objects.requireNonNull(type, "'ProxyOptions.getType()' cannot be null.");

        switch (type) {
            case HTTP:
                return Proxy.Type.HTTP;
            case SOCKS4:
            case SOCKS5:
                return Proxy.Type.SOCKS;
            default:
                throw logger.logExceptionAsError(new IllegalStateException(
                    String.format("Unknown proxy type '%s' in use. Use a proxy type from 'ProxyOptions.Type'.", type)));
        }
    }
}
