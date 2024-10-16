// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.okhttp3;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.ProxyOptions;
import io.clientcore.core.implementation.util.auth.DigestHandler;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.SharedExecutorService;
import io.clientcore.core.util.auth.ChallengeHandler;
import io.clientcore.core.util.configuration.Configuration;
import io.clientcore.http.okhttp3.implementation.OkHttpProxySelector;
import io.clientcore.http.okhttp3.implementation.ProxyAuthenticator;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builder class responsible for creating instances of {@link HttpClient} backed by OkHttp.
 */
public class OkHttpHttpClientBuilder {
    private static final ClientLogger LOGGER = new ClientLogger(OkHttpHttpClientBuilder.class);
    private static final Duration MINIMUM_TIMEOUT = Duration.ofMillis(1);
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration DEFAULT_WRITE_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(60);

    private final okhttp3.OkHttpClient okHttpClient;

    private boolean followRedirects;
    private Configuration configuration;
    private ConnectionPool connectionPool;
    private Dispatcher dispatcher;
    private Duration connectionTimeout;
    private Duration callTimeout;
    private Duration readTimeout;
    private Duration writeTimeout;
    private List<Interceptor> networkInterceptors = new ArrayList<>();
    private ProxyOptions proxyOptions;
    private SSLSocketFactory sslSocketFactory;
    private X509TrustManager trustManager;
    private HostnameVerifier hostnameVerifier;

    /**
     * Creates OkHttpHttpClientBuilder.
     */
    public OkHttpHttpClientBuilder() {
        this.okHttpClient = null;
        this.dispatcher = new Dispatcher(SharedExecutorService.getInstance());
    }

    /**
     * Creates OkHttpHttpClientBuilder from the builder of an existing OkHttpClient.
     *
     * @param okHttpClient the httpclient
     */
    public OkHttpHttpClientBuilder(OkHttpClient okHttpClient) {
        this.okHttpClient = Objects.requireNonNull(okHttpClient, "'okHttpClient' cannot be null.");
    }

    /**
     * <p>Sets the followRedirect flag on the underlying OkHttp-backed {@link HttpClient}.</p>
     *
     * <p>If this is set to 'true' redirects will be followed automatically, and
     * if your HTTP pipeline is configured with a redirect policy it will not be called.</p>
     *
     * @param followRedirects The followRedirects value to use.
     * @return The updated {@link OkHttpHttpClientBuilder} object.
     */
    public OkHttpHttpClientBuilder followRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;

        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the HTTP client.
     * <p>
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}.
     *
     * @param configuration The configuration store.
     * @return The updated {@link OkHttpHttpClientBuilder} object.
     */
    public OkHttpHttpClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;

        return this;
    }

    /**
     * Sets the Http connection pool.
     *
     * @param connectionPool The OkHttp connection pool to use.
     * @return The updated {@link OkHttpHttpClientBuilder} object.
     * @see OkHttpClient.Builder#connectionPool(ConnectionPool)
     */
    public OkHttpHttpClientBuilder connectionPool(ConnectionPool connectionPool) {
        // Null ConnectionPool is not allowed
        this.connectionPool = Objects.requireNonNull(connectionPool, "'connectionPool' cannot be null.");

        return this;
    }

    /**
     * Sets the dispatcher that also composes the thread pool for executing HTTP requests.
     * <p>
     * If this method is not invoked prior to {@link #build() building}, handling for a default will be based on whether
     * the builder was created with the default constructor or the constructor that accepts an existing
     * {@link OkHttpClient}. If the default constructor was used, a new {@link Dispatcher} will be created with based on
     * {@link SharedExecutorService#getInstance()}}. If the constructor that accepts an existing {@link OkHttpClient}
     * was used, the dispatcher from the existing {@link OkHttpClient} will be used.
     *
     * @param dispatcher The dispatcher to use.
     * @return The updated {@link OkHttpHttpClientBuilder} object.
     * @see OkHttpClient.Builder#dispatcher(Dispatcher)
     */
    public OkHttpHttpClientBuilder dispatcher(Dispatcher dispatcher) {
        // Null Dispatcher is not allowed
        this.dispatcher = Objects.requireNonNull(dispatcher, "'dispatcher' cannot be null.");

        return this;
    }

    /**
     * Sets the connection timeout for a request to be sent.
     * <p>
     * The connection timeout begins once the request attempts to connect to the remote host and finishes once the
     * connection is resolved.
     * <p>
     * If {@code connectTimeout} is null either {@link Configuration#PROPERTY_REQUEST_CONNECT_TIMEOUT} or a
     * 10-second timeout will be used, if it is a {@link Duration} less than or equal to zero then no timeout will be
     * applied. When applying the timeout the greatest of one millisecond and the value of {@code connectTimeout} will
     * be used.
     * <p>
     * By default, the connection timeout is 10 seconds.
     *
     * @param connectionTimeout Connect timeout duration.
     * @return The updated {@link OkHttpHttpClientBuilder} object.
     * @see OkHttpClient.Builder#connectTimeout(Duration)
     */
    public OkHttpHttpClientBuilder connectionTimeout(Duration connectionTimeout) {
        // setConnectionTimeout can be null
        this.connectionTimeout = connectionTimeout;

        return this;
    }

    /**
     * Sets the default timeout for complete calls.
     * <p>
     * The call timeout spans the entire call: resolving DNS, connecting, writing the request body,
     * server processing, and reading the response body.
     * <p>
     * Null or {@link Duration#ZERO} means no call timeout, otherwise values
     * must be between 1 and {@link Integer#MAX_VALUE} when converted to milliseconds.
     * <p>
     * By default, call timeout is not enabled.
     *
     * @param callTimeout Call timeout duration.
     * @return The updated {@link OkHttpHttpClientBuilder} object.
     * @see OkHttpClient.Builder#callTimeout(Duration)
     */
    public OkHttpHttpClientBuilder callTimeout(Duration callTimeout) {
        // callTimeout can be null
        if (callTimeout != null && callTimeout.isNegative()) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException("'callTimeout' cannot be negative"));
        }

        this.callTimeout = callTimeout;

        return this;
    }

    /**
     * Sets the read timeout duration used when reading the server response.
     * <p>
     * The read timeout begins once the first response read is triggered after the server response is received. This
     * timeout triggers periodically but won't fire its operation if another read operation has completed between when
     * the timeout is triggered and completes.
     * <p>
     * If {@code readTimeout} is null or {@link Configuration#PROPERTY_REQUEST_READ_TIMEOUT} or a 60-second
     * timeout will be used, if it is a {@link Duration} less than or equal to zero then no timeout period will be
     * applied to response read. When applying the timeout the greatest of one millisecond and the value of {@code
     * readTimeout} will be used.
     *
     * @param readTimeout Read timeout duration.
     * @return The updated {@link OkHttpHttpClientBuilder} object.
     * @see OkHttpClient.Builder#readTimeout(Duration)
     */
    public OkHttpHttpClientBuilder readTimeout(Duration readTimeout) {
        // setReadTimeout can be null
        this.readTimeout = readTimeout;

        return this;
    }

    /**
     * Sets the writing timeout for a request to be sent.
     * <p>
     * The writing timeout does not apply to the entire request but to the request being sent over the wire. For example
     * a request body which emits {@code 10} {@code 8KB} buffers will trigger {@code 10} write operations, the last
     * write tracker will update when each operation completes and the outbound buffer will be periodically checked to
     * determine if it is still draining.
     * <p>
     * If {@code writeTimeout} is null either {@link Configuration#PROPERTY_REQUEST_WRITE_TIMEOUT} or a 60-second
     * timeout will be used, if it is a {@link Duration} less than or equal to zero then no write timeout will be
     * applied. When applying the timeout the greatest of one millisecond and the value of {@code writeTimeout} will be
     * used.
     *
     * @param writeTimeout Write operation timeout duration.
     * @return The updated {@link OkHttpHttpClientBuilder} object.
     * @see OkHttpClient.Builder#writeTimeout(Duration)
     */
    public OkHttpHttpClientBuilder writeTimeout(Duration writeTimeout) {
        this.writeTimeout = writeTimeout;

        return this;
    }

    /**
     * Add a network layer interceptor to Http request pipeline.
     *
     * @param networkInterceptor the interceptor to add
     * @return The updated {@link OkHttpHttpClientBuilder} object
     */
    public OkHttpHttpClientBuilder addNetworkInterceptor(Interceptor networkInterceptor) {
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
     * @return The updated {@link OkHttpHttpClientBuilder} object.
     */
    public OkHttpHttpClientBuilder networkInterceptors(List<Interceptor> networkInterceptors) {
        this.networkInterceptors = Objects.requireNonNull(networkInterceptors, "'networkInterceptors' cannot be null.");

        return this;
    }

    /**
     * Sets the proxy.
     *
     * @param proxyOptions The proxy configuration to use.
     * @return The updated {@link OkHttpHttpClientBuilder} object.
     */
    public OkHttpHttpClientBuilder proxy(ProxyOptions proxyOptions) {
        // proxyOptions can be null
        this.proxyOptions = proxyOptions;

        return this;
    }

    /**
     * Sets the {@link SSLSocketFactory} to use for HTTPS connections.
     * <p>
     * If left unset, or set to null, HTTPS connections will use the default SSL socket factory
     * ({@link SSLSocketFactory#getDefault()}).
     *
     * @param sslSocketFactory The {@link SSLSocketFactory} to use for HTTPS connections.
     * @param trustManager The {@link X509TrustManager} to use for HTTPS connections.
     * @return The updated {@link OkHttpHttpClientBuilder} object.
     */
    public OkHttpHttpClientBuilder sslSocketFactory(SSLSocketFactory sslSocketFactory, X509TrustManager trustManager) {
        this.sslSocketFactory = sslSocketFactory;
        this.trustManager = trustManager;

        return this;
    }

    /**
     * Sets the {@link HostnameVerifier} to use for HTTPS connections.
     * <p>
     * If left unset, or set to null, HTTPS connections will use a default hostname verifier.
     *
     * @param hostnameVerifier The {@link HostnameVerifier} to use for HTTPS connections.
     * @return The updated {@link OkHttpHttpClientBuilder} object.
     */
    public OkHttpHttpClientBuilder hostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;

        return this;
    }

    /**
     * Creates a new OkHttp-backed {@link HttpClient} instance on every call, using the configuration set in this
     * builder at the time of the {@code build()} method call.
     *
     * @return A new OkHttp-backed {@link HttpClient} instance.
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
        httpClientBuilder = httpClientBuilder.connectTimeout(getTimeout(connectionTimeout, DEFAULT_CONNECT_TIMEOUT))
            .writeTimeout(getTimeout(writeTimeout, DEFAULT_WRITE_TIMEOUT))
            .readTimeout(getTimeout(readTimeout, DEFAULT_READ_TIMEOUT));

        if (callTimeout != null) {
            // Call timeout is disabled by default.
            httpClientBuilder.callTimeout(callTimeout);
        }

        // If set use the configured connection pool.
        if (this.connectionPool != null) {
            httpClientBuilder = httpClientBuilder.connectionPool(connectionPool);
        }

        // If set use the configured dispatcher.
        if (this.dispatcher != null) {
            httpClientBuilder = httpClientBuilder.dispatcher(dispatcher);
        }

        if (this.sslSocketFactory != null) {
            httpClientBuilder = httpClientBuilder.sslSocketFactory(sslSocketFactory, trustManager);
        }

        if (this.hostnameVerifier != null) {
            httpClientBuilder = httpClientBuilder.hostnameVerifier(hostnameVerifier);
        }

        Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration()
            : configuration;

        ProxyOptions buildProxyOptions = (proxyOptions == null) ? ProxyOptions.fromConfiguration(buildConfiguration,
            true) : proxyOptions;

        if (buildProxyOptions != null) {
            httpClientBuilder = httpClientBuilder.proxySelector(
                new OkHttpProxySelector(buildProxyOptions.getType().toProxyType(), buildProxyOptions::getAddress,
                    buildProxyOptions.getNonProxyHosts()));

            if (buildProxyOptions.getUsername() != null) {
                ProxyAuthenticator proxyAuthenticator = new ProxyAuthenticator(ChallengeHandler.of(new DigestHandler(
                    buildProxyOptions.getUsername(),
                    buildProxyOptions.getPassword())));

                httpClientBuilder = httpClientBuilder.proxyAuthenticator(proxyAuthenticator)
                    .addInterceptor(proxyAuthenticator.getProxyAuthenticationInfoInterceptor());
            }
        }

        // Set the followRedirects property.
        httpClientBuilder.followRedirects(this.followRedirects);

        return new OkHttpHttpClient(httpClientBuilder.build());
    }

    /*
     * Returns the timeout in milliseconds to use based on the passed Duration and default timeout.
     *
     * If the timeout is {@code null} the default timeout will be used. If the timeout is less than or equal to zero
     * no timeout will be used. If the timeout is less than one millisecond a timeout of one millisecond will be used.
     */
    static Duration getTimeout(Duration configuredTimeout, Duration defaultTimeout) {
        // Timeout is null, use the default timeout.
        if (configuredTimeout == null) {
            return defaultTimeout;
        }

        // Timeout is less than or equal to zero, return no timeout.
        if (configuredTimeout.isZero() || configuredTimeout.isNegative()) {
            return Duration.ZERO;
        }

        // Return the maximum of the timeout period and the minimum allowed timeout period.
        if (configuredTimeout.compareTo(MINIMUM_TIMEOUT) < 0) {
            return MINIMUM_TIMEOUT;
        } else {
            return configuredTimeout;
        }
    }
}
