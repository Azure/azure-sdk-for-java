// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.client;

import io.clientcore.core.http.models.ProxyOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.util.SharedExecutorService;
import io.clientcore.core.util.configuration.Configuration;

import javax.net.ssl.SSLContext;
import java.time.Duration;
import java.util.concurrent.Executor;

/**
 * Builder to configure and build an instance of the core {@link HttpClient} type using the JDK
 * HttpURLConnection, first introduced in JDK 1.1.
 */
public class DefaultHttpClientBuilder {
    private static final ClientLogger LOGGER = new ClientLogger(DefaultHttpClientBuilder.class);

    static final String ERROR_MESSAGE = "Usage of DefaultHttpClient is only available when using Java 12 or "
        + "higher. For support with Java 11 or lower, please including a dependency on io.clientcore:http-okhttp3.";

    /**
     * Creates DefaultHttpClientBuilder.
     */
    public DefaultHttpClientBuilder() {
        throw LOGGER.logThrowableAsError(new UnsupportedOperationException(ERROR_MESSAGE));
    }

    /**
     * Creates DefaultHttpClientBuilder from the builder of an existing JDK HttpClient.Builder.
     * <p>
     * This method exists to support multi-release JARs where the JDK HttpClient is only available in Java 11 and later.
     * But since the baseline requirement is Java 8 there cannot be references to the class, so this method accepts
     * {@link Object} as a holder. The actual type of the {@link Object} passed must be an instance of
     * {@code HttpClient.Builder}, otherwise an exception will be thrown.
     *
     * @param httpClientBuilder the HttpClient builder to use
     * @throws ClassCastException if {@code httpClientBuilder} isn't an instance of {@code HttpClient.Builder}
     * @throws NullPointerException if {@code httpClientBuilder} is null
     */
    public DefaultHttpClientBuilder(Object httpClientBuilder) {
        throw LOGGER.logThrowableAsError(new UnsupportedOperationException(ERROR_MESSAGE));
    }

    /**
     * Sets the executor to be used for asynchronous and dependent tasks. This cannot be null.
     * <p>
     * If this method is not invoked prior to {@link #build() building}, handling for a default will be based on whether
     * the builder was created with the default constructor or the constructor that accepts an existing
     * {@code HttpClient.Builder}. If the default constructor was used, the default executor will be
     * {@link SharedExecutorService#getInstance()}. If the constructor that accepts an existing
     * {@code HttpClient.Builder} was used, the executor from the existing builder will be used.
     *
     * @param executor the executor to be used for asynchronous and dependent tasks
     * @return the updated {@link DefaultHttpClientBuilder} object
     * @throws NullPointerException if {@code executor} is null
     */
    public DefaultHttpClientBuilder executor(Executor executor) {
        throw LOGGER.logThrowableAsError(new UnsupportedOperationException(ERROR_MESSAGE));
    }

    /**
     * Sets the connection timeout.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed io.clientcore.core.http.client.DefaultHttpClientBuilder.connectionTimeout#Duration -->
     * <pre>
     * HttpClient client = new DefaultHttpClientBuilder&#40;&#41;
     *         .connectionTimeout&#40;Duration.ofSeconds&#40;250&#41;&#41; &#47;&#47; connection timeout of 250 seconds
     *         .build&#40;&#41;;
     * </pre>
     * <!-- end io.clientcore.core.http.client.DefaultHttpClientBuilder.connectionTimeout#Duration -->
     *
     * The default connection timeout is 10 seconds.
     *
     * @param connectionTimeout the connection timeout
     * @return the updated {@link DefaultHttpClientBuilder} object
     */
    public DefaultHttpClientBuilder connectionTimeout(Duration connectionTimeout) {
        throw LOGGER.logThrowableAsError(new UnsupportedOperationException(ERROR_MESSAGE));
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
     * @return The updated {@link DefaultHttpClientBuilder} object.
     */
    public DefaultHttpClientBuilder writeTimeout(Duration writeTimeout) {
        throw LOGGER.logThrowableAsError(new UnsupportedOperationException(ERROR_MESSAGE));
    }

    /**
     * Sets the response timeout duration used when waiting for a server to reply.
     * <p>
     * The response timeout begins once the request write completes and finishes once the first response read is
     * triggered when the server response is received.
     * <p>
     * If {@code responseTimeout} is null either {@link Configuration#PROPERTY_REQUEST_RESPONSE_TIMEOUT} or a
     * 60-second timeout will be used, if it is a {@link Duration} less than or equal to zero then no timeout will be
     * applied to the response. When applying the timeout the greatest of one millisecond and the value of {@code
     * responseTimeout} will be used.
     *
     * @param responseTimeout Response timeout duration.
     * @return The updated {@link DefaultHttpClientBuilder} object.
     */
    public DefaultHttpClientBuilder responseTimeout(Duration responseTimeout) {
        throw LOGGER.logThrowableAsError(new UnsupportedOperationException(ERROR_MESSAGE));
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
     * @return The updated {@link DefaultHttpClientBuilder} object.
     */
    public DefaultHttpClientBuilder readTimeout(Duration readTimeout) {
        throw LOGGER.logThrowableAsError(new UnsupportedOperationException(ERROR_MESSAGE));
    }

    /**
     * Sets the proxy.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed io.clientcore.core.http.client.DefaultHttpClientBuilder.proxy#ProxyOptions -->
     * <pre>
     * final String proxyHost = &quot;&lt;proxy-host&gt;&quot;; &#47;&#47; e.g. localhost
     * final int proxyPort = 9999; &#47;&#47; Proxy port
     * ProxyOptions proxyOptions = new ProxyOptions&#40;ProxyOptions.Type.HTTP,
     *     new InetSocketAddress&#40;proxyHost, proxyPort&#41;&#41;;
     * HttpClient client = new DefaultHttpClientBuilder&#40;&#41;
     *     .proxy&#40;proxyOptions&#41;
     *     .build&#40;&#41;;
     * </pre>
     * <!-- end io.clientcore.core.http.client.DefaultHttpClientBuilder.proxy#ProxyOptions -->
     *
     * @param proxyOptions The proxy configuration to use.
     * @return the updated {@link DefaultHttpClientBuilder} object
     * @throws NullPointerException If {@code proxyOptions} is not null and the proxy type or address is not set.
     */
    public DefaultHttpClientBuilder proxy(ProxyOptions proxyOptions) {
        throw LOGGER.logThrowableAsError(new UnsupportedOperationException(ERROR_MESSAGE));
    }

    /**
     * Sets the {@link SSLContext} to be used when opening secure connections.
     *
     * @param sslContext The SSL context to be used.
     * @return The updated {@link DefaultHttpClientBuilder} object.
     */
    public DefaultHttpClientBuilder sslContext(SSLContext sslContext) {
        throw LOGGER.logThrowableAsError(new UnsupportedOperationException(ERROR_MESSAGE));
    }

    /**
     * Sets the configuration store that is used during construction of the HTTP client.
     *
     * @param configuration The configuration store used to
     * @return The updated {@link DefaultHttpClientBuilder} object.
     */
    public DefaultHttpClientBuilder configuration(Configuration configuration) {
        throw LOGGER.logThrowableAsError(new UnsupportedOperationException(ERROR_MESSAGE));
    }

    /**
     * Build a HttpClient with current configurations.
     *
     * @return a {@link HttpClient}.
     */
    public HttpClient build() {
        throw LOGGER.logThrowableAsError(new UnsupportedOperationException(ERROR_MESSAGE));
    }
}
