// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.okhttp.implementation.OkHttpProxySelector;
import com.azure.core.http.okhttp.implementation.ProxyAuthenticator;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_READ_TIMEOUT;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT;
import static com.azure.core.util.CoreUtils.getDefaultTimeoutFromEnvironment;

/**
 * Builder class responsible for creating instances of {@link com.azure.core.http.HttpClient} backed by OkHttp.
 * The client built from this builder can support sending requests synchronously and asynchronously.
 * Use {@link com.azure.core.http.HttpClient#sendSync(HttpRequest, Context)} to send the provided request
 * synchronously with contextual information.
 *
 * <p>
 * <strong>Building a new HttpClient instance</strong>
 * </p>
 *
 * <!-- src_embed com.azure.core.http.okhttp.instantiation-simple -->
 * <pre>
 * HttpClient client = new OkHttpAsyncHttpClientBuilder&#40;&#41;
 *         .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.core.http.okhttp.instantiation-simple -->
 *
 * <p>
 * <strong>Building a new HttpClient instance using http proxy.</strong>
 * </p>
 *
 * <p>
 * Configuring the OkHttp client with a proxy is relevant when your application needs to communicate with Azure
 * services through a proxy server.
 * </p>
 *
 * <!-- src_embed com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder.proxy#ProxyOptions -->
 * <pre>
 * final String proxyHost = &quot;&lt;proxy-host&gt;&quot;; &#47;&#47; e.g. localhost
 * final int proxyPort = 9999; &#47;&#47; Proxy port
 * ProxyOptions proxyOptions = new ProxyOptions&#40;ProxyOptions.Type.HTTP,
 *         new InetSocketAddress&#40;proxyHost, proxyPort&#41;&#41;;
 * HttpClient client = new OkHttpAsyncHttpClientBuilder&#40;&#41;
 *         .proxy&#40;proxyOptions&#41;
 *         .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder.proxy#ProxyOptions -->
 *
 * <p>
 * <strong>Building a new HttpClient instance with connection timeout.</strong>
 * </p>
 *
 * <p>
 * Setting a reasonable connection timeout is particularly important in scenarios where network conditions might
 * be unpredictable or where the server may not be responsive.
 * </p>
 *
 * <!-- src_embed com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder#connectionTimeout -->
 * <pre>
 * final Duration connectionTimeout = Duration.ofSeconds&#40;250&#41;; &#47;&#47; connection timeout of 250 seconds
 * HttpClient client = new OkHttpAsyncHttpClientBuilder&#40;&#41;
 *         .connectionTimeout&#40;connectionTimeout&#41;
 *         .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder#connectionTimeout -->
 *
 * <p>
 * <strong>Building a new HttpClient instance with HTTP/2 Support.</strong>
 * </p>
 *
 * <!-- src_embed com.azure.core.http.okhttp.instantiation-simple -->
 * <pre>
 * HttpClient client = new OkHttpAsyncHttpClientBuilder&#40;&#41;
 *         .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.core.http.okhttp.instantiation-simple -->
 *
 * <p>
 * It is also possible to create a OkHttp HttpClient that only supports HTTP/2.
 * </p>
 *
 * <!-- src_embed readme-sample-useHttp2OnlyWithConfiguredOkHttpClient -->
 * <pre>
 * &#47;&#47; Constructs an HttpClient that only supports HTTP&#47;2.
 * HttpClient client = new OkHttpAsyncHttpClientBuilder&#40;new OkHttpClient.Builder&#40;&#41;
 *     .protocols&#40;Collections.singletonList&#40;Protocol.H2_PRIOR_KNOWLEDGE&#41;&#41;
 *     .build&#40;&#41;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-useHttp2OnlyWithConfiguredOkHttpClient -->
 *
 * @see HttpClient
 * @see OkHttpAsyncHttpClient
 */
public class OkHttpAsyncHttpClientBuilder {

    private static final ClientLogger LOGGER = new ClientLogger(OkHttpAsyncHttpClientBuilder.class);

    private final okhttp3.OkHttpClient okHttpClient;

    private static final Duration MINIMUM_TIMEOUT = Duration.ofMillis(1);
    private static final Duration DEFAULT_CONNECT_TIMEOUT;
    private static final Duration DEFAULT_WRITE_TIMEOUT;
    private static final Duration DEFAULT_READ_TIMEOUT;

    static {
        ClientLogger logger = new ClientLogger(OkHttpAsyncHttpClientBuilder.class);
        Configuration configuration = Configuration.getGlobalConfiguration();
        DEFAULT_CONNECT_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration,
            PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT, Duration.ofSeconds(10), logger);
        DEFAULT_WRITE_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration, PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT,
            Duration.ofSeconds(60), logger);
        DEFAULT_READ_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration, PROPERTY_AZURE_REQUEST_READ_TIMEOUT,
            Duration.ofSeconds(60), logger);
    }

    private List<Interceptor> networkInterceptors = new ArrayList<>();
    private Duration readTimeout;
    private Duration writeTimeout;
    private Duration connectionTimeout;
    private Duration callTimeout;
    private ConnectionPool connectionPool;
    private Dispatcher dispatcher;
    private ProxyOptions proxyOptions;
    private Configuration configuration;
    private boolean followRedirects;

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
     * Sets the read timeout duration used when reading the server response.
     * <p>
     * The read timeout begins once the first response read is triggered after the server response is received. This
     * timeout triggers periodically but won't fire its operation if another read operation has completed between when
     * the timeout is triggered and completes.
     * <p>
     * If {@code readTimeout} is null or {@link Configuration#PROPERTY_AZURE_REQUEST_READ_TIMEOUT} or a 60-second
     * timeout will be used, if it is a {@link Duration} less than or equal to zero then no timeout period will be
     * applied to response read. When applying the timeout the greatest of one millisecond and the value of {@code
     * readTimeout} will be used.
     *
     * @param readTimeout Read timeout duration.
     * @return The updated OkHttpAsyncHttpClientBuilder object.
     * @see OkHttpClient.Builder#readTimeout(Duration)
     */
    public OkHttpAsyncHttpClientBuilder readTimeout(Duration readTimeout) {
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
     * If {@code writeTimeout} is null either {@link Configuration#PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT} or a 60-second
     * timeout will be used, if it is a {@link Duration} less than or equal to zero then no write timeout will be
     * applied. When applying the timeout the greatest of one millisecond and the value of {@code writeTimeout} will be
     * used.
     *
     * @param writeTimeout Write operation timeout duration.
     * @return The updated OkHttpAsyncHttpClientBuilder object.
     * @see OkHttpClient.Builder#writeTimeout(Duration)
     */
    public OkHttpAsyncHttpClientBuilder writeTimeout(Duration writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    /**
     * Sets the connection timeout for a request to be sent.
     * <p>
     * The connection timeout begins once the request attempts to connect to the remote host and finishes once the
     * connection is resolved.
     * <p>
     * If {@code connectTimeout} is null either {@link Configuration#PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT} or a
     * 10-second timeout will be used, if it is a {@link Duration} less than or equal to zero then no timeout will be
     * applied. When applying the timeout the greatest of one millisecond and the value of {@code connectTimeout} will
     * be used.
     * <p>
     * By default, the connection timeout is 10 seconds.
     *
     * @param connectionTimeout Connect timeout duration.
     * @return The updated OkHttpAsyncHttpClientBuilder object.
     * @see OkHttpClient.Builder#connectTimeout(Duration)
     */
    public OkHttpAsyncHttpClientBuilder connectionTimeout(Duration connectionTimeout) {
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
     * @return The updated OkHttpAsyncHttpClientBuilder object.
     * @see OkHttpClient.Builder#callTimeout(Duration)
     */
    public OkHttpAsyncHttpClientBuilder callTimeout(Duration callTimeout) {
        // callTimeout can be null
        if (callTimeout != null && callTimeout.isNegative()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'callTimeout' cannot be negative"));
        }
        this.callTimeout = callTimeout;
        return this;
    }

    /**
     * Sets the Http connection pool.
     *
     * @param connectionPool The OkHttp connection pool to use.
     * @return The updated OkHttpAsyncHttpClientBuilder object.
     * @see OkHttpClient.Builder#connectionPool(ConnectionPool)
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
     * @see OkHttpClient.Builder#dispatcher(Dispatcher)
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
     * <p>Sets the followRedirect flag on the underlying OkHttp-backed {@link com.azure.core.http.HttpClient}.</p>
     *
     * <p>If this is set to 'true' redirects will be followed automatically, and
     * if your HTTP pipeline is configured with a redirect policy it will not be called.</p>
     *
     * @param followRedirects The followRedirects value to use.
     * @return The updated OkHttpAsyncHttpClientBuilder object.
     */
    public OkHttpAsyncHttpClientBuilder followRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }

    /**
     * Creates a new OkHttp-backed {@link com.azure.core.http.HttpClient} instance on every call, using the
     * configuration set in the builder at the time of the build method call.
     *
     * @return A new OkHttp-backed {@link com.azure.core.http.HttpClient} instance.
     */
    public HttpClient build() {
        OkHttpClient.Builder httpClientBuilder
            = this.okHttpClient == null ? new OkHttpClient.Builder() : this.okHttpClient.newBuilder();

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

        Configuration buildConfiguration
            = (configuration == null) ? Configuration.getGlobalConfiguration() : configuration;

        ProxyOptions buildProxyOptions
            = (proxyOptions == null) ? ProxyOptions.fromConfiguration(buildConfiguration, true) : proxyOptions;

        if (buildProxyOptions != null) {
            httpClientBuilder
                = httpClientBuilder.proxySelector(new OkHttpProxySelector(buildProxyOptions.getType().toProxyType(),
                    buildProxyOptions::getAddress, buildProxyOptions.getNonProxyHosts()));

            if (buildProxyOptions.getUsername() != null) {
                ProxyAuthenticator proxyAuthenticator
                    = new ProxyAuthenticator(buildProxyOptions.getUsername(), buildProxyOptions.getPassword());

                httpClientBuilder = httpClientBuilder.proxyAuthenticator(proxyAuthenticator)
                    .addInterceptor(proxyAuthenticator.getProxyAuthenticationInfoInterceptor());
            }
        }

        // Set the followRedirects property.
        httpClientBuilder.followRedirects(this.followRedirects);

        return new OkHttpAsyncHttpClient(httpClientBuilder.build());
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
