// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.client;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.http.models.ProxyOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.SharedExecutorService;
import io.clientcore.core.utils.configuration.Configuration;

import javax.net.ssl.SSLContext;
import java.time.Duration;
import java.util.concurrent.Executor;

/**
 * Builder to configure and build an instance of the JDK {@code HttpClient} introduced in Java 11.
 * <p>
 * Due to the JDK preventing some headers from being sent on requests, Java 12 is required to create an instance of this
 * {@link HttpClient} implementation.
 * <p>
 * This class leverages multi-release JAR functionality. If the JDK version is 11 or lower, this class will throw an
 * {@link UnsupportedOperationException} when any method is invoked. This same issue will also happen if the application
 * using this functionality is running Java 12 or later but doesn't have {@code Multi-Release: true} in its
 * {@code META-INF/MANIFEST.MF} file.
 */
@Metadata(properties = MetadataProperties.FLUENT)
public class JdkHttpClientBuilder {
    private static final ClientLogger LOGGER = new ClientLogger(JdkHttpClientBuilder.class);

    private static final String ERROR_MESSAGE = "It is recommended that libraries be deployed on the latest LTS "
        + "version of Java, however the Java client will support down to Java 8. In the case where the client is to "
        + "operate on Java versions below Java 11, it is required to include additional dependencies. Usage of "
        + "DefaultHttpClient is only available when using Java 12 or higher. For support with Java 11 or lower, "
        + "include a dependency on io.clientcore:http-okhttp3.";

    /**
     * Creates JdkHttpClientBuilder.
     */
    public JdkHttpClientBuilder() {
        throw LOGGER.throwableAtError().log(ERROR_MESSAGE, UnsupportedOperationException::new);
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
     * @return the updated {@link JdkHttpClientBuilder} object
     * @throws NullPointerException if {@code executor} is null
     */
    public JdkHttpClientBuilder executor(Executor executor) {
        throw LOGGER.throwableAtError().log(ERROR_MESSAGE, UnsupportedOperationException::new);
    }

    /**
     * Sets the connection timeout.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed io.clientcore.core.http.client.JdkHttpClientBuilder.connectionTimeout#Duration -->
     * <pre>
     * HttpClient client = new JdkHttpClientBuilder&#40;&#41;
     *         .connectionTimeout&#40;Duration.ofSeconds&#40;250&#41;&#41; &#47;&#47; connection timeout of 250 seconds
     *         .build&#40;&#41;;
     * </pre>
     * <!-- end io.clientcore.core.http.client.JdkHttpClientBuilder.connectionTimeout#Duration -->
     *
     * The default connection timeout is 10 seconds.
     *
     * @param connectionTimeout the connection timeout
     * @return the updated {@link JdkHttpClientBuilder} object
     */
    public JdkHttpClientBuilder connectionTimeout(Duration connectionTimeout) {
        throw LOGGER.throwableAtError().log(ERROR_MESSAGE, UnsupportedOperationException::new);
    }

    /**
     * Sets the writing timeout for a request to be sent.
     * <p>
     * The writing timeout does not apply to the entire request but to the request being sent over the wire. For example
     * a request body which emits {@code 10} {@code 8KB} buffers will trigger {@code 10} write operations, the last
     * write tracker will update when each operation completes and the outbound buffer will be periodically checked to
     * determine if it is still draining.
     * <p>
     * If {@code writeTimeout} is null either {@link Configuration#REQUEST_WRITE_TIMEOUT_IN_MS} or a 60-second
     * timeout will be used, if it is a {@link Duration} less than or equal to zero then no write timeout will be
     * applied. When applying the timeout the greatest of one millisecond and the value of {@code writeTimeout} will be
     * used.
     *
     * @param writeTimeout Write operation timeout duration.
     * @return The updated {@link JdkHttpClientBuilder} object.
     */
    public JdkHttpClientBuilder writeTimeout(Duration writeTimeout) {
        throw LOGGER.throwableAtError().log(ERROR_MESSAGE, UnsupportedOperationException::new);
    }

    /**
     * Sets the response timeout duration used when waiting for a server to reply.
     * <p>
     * The response timeout begins once the request write completes and finishes once the first response read is
     * triggered when the server response is received.
     * <p>
     * If {@code responseTimeout} is null either {@link Configuration#REQUEST_RESPONSE_TIMEOUT_IN_MS} or a
     * 60-second timeout will be used, if it is a {@link Duration} less than or equal to zero then no timeout will be
     * applied to the response. When applying the timeout the greatest of one millisecond and the value of {@code
     * responseTimeout} will be used.
     *
     * @param responseTimeout Response timeout duration.
     * @return The updated {@link JdkHttpClientBuilder} object.
     */
    public JdkHttpClientBuilder responseTimeout(Duration responseTimeout) {
        throw LOGGER.throwableAtError().log(ERROR_MESSAGE, UnsupportedOperationException::new);
    }

    /**
     * Sets the read timeout duration used when reading the server response.
     * <p>
     * The read timeout begins once the first response read is triggered after the server response is received. This
     * timeout triggers periodically but won't fire its operation if another read operation has completed between when
     * the timeout is triggered and completes.
     * <p>
     * If {@code readTimeout} is null or {@link Configuration#REQUEST_READ_TIMEOUT_IN_MS} or a 60-second
     * timeout will be used, if it is a {@link Duration} less than or equal to zero then no timeout period will be
     * applied to response read. When applying the timeout the greatest of one millisecond and the value of {@code
     * readTimeout} will be used.
     *
     * @param readTimeout Read timeout duration.
     * @return The updated {@link JdkHttpClientBuilder} object.
     */
    public JdkHttpClientBuilder readTimeout(Duration readTimeout) {
        throw LOGGER.throwableAtError().log(ERROR_MESSAGE, UnsupportedOperationException::new);
    }

    /**
     * Sets the proxy.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed io.clientcore.core.http.client.JdkHttpClientBuilder.proxy#ProxyOptions -->
     * <pre>
     * final String proxyHost = &quot;&lt;proxy-host&gt;&quot;; &#47;&#47; e.g. localhost
     * final int proxyPort = 9999; &#47;&#47; Proxy port
     * ProxyOptions proxyOptions = new ProxyOptions&#40;ProxyOptions.Type.HTTP,
     *     new InetSocketAddress&#40;proxyHost, proxyPort&#41;&#41;;
     * HttpClient client = new JdkHttpClientBuilder&#40;&#41;
     *     .proxy&#40;proxyOptions&#41;
     *     .build&#40;&#41;;
     * </pre>
     * <!-- end io.clientcore.core.http.client.JdkHttpClientBuilder.proxy#ProxyOptions -->
     *
     * @param proxyOptions The proxy configuration to use.
     * @return the updated {@link JdkHttpClientBuilder} object
     * @throws NullPointerException If {@code proxyOptions} is not null and the proxy type or address is not set.
     */
    public JdkHttpClientBuilder proxy(ProxyOptions proxyOptions) {
        throw LOGGER.throwableAtError().log(ERROR_MESSAGE, UnsupportedOperationException::new);
    }

    /**
     * Sets the {@link SSLContext} to be used when opening secure connections.
     *
     * @param sslContext The SSL context to be used.
     * @return The updated {@link JdkHttpClientBuilder} object.
     */
    public JdkHttpClientBuilder sslContext(SSLContext sslContext) {
        throw LOGGER.throwableAtError().log(ERROR_MESSAGE, UnsupportedOperationException::new);
    }

    /**
     * Sets the maximum {@link HttpProtocolVersion HTTP protocol version} that the HTTP client will support.
     * <p>
     * By default, the maximum HTTP protocol version is set to {@link HttpProtocolVersion#HTTP_2 HTTP_2}.
     * <p>
     * If {@code httpVersion} is null, it will reset the maximum HTTP protocol version to
     * {@link HttpProtocolVersion#HTTP_2 HTTP_2}.
     *
     * @param httpVersion The maximum HTTP protocol version that the HTTP client will support.
     * @return The updated {@link JdkHttpClientBuilder} object.
     */
    public JdkHttpClientBuilder setMaximumHttpVersion(HttpProtocolVersion httpVersion) {
        throw LOGGER.throwableAtError().log(ERROR_MESSAGE, UnsupportedOperationException::new);
    }

    /**
     * Sets the configuration store that is used during construction of the HTTP client.
     *
     * @param configuration The configuration store used to
     * @return The updated {@link JdkHttpClientBuilder} object.
     */
    public JdkHttpClientBuilder configuration(Configuration configuration) {
        throw LOGGER.throwableAtError().log(ERROR_MESSAGE, UnsupportedOperationException::new);
    }

    /**
     * Build a HttpClient with current configurations.
     *
     * @return a {@link HttpClient}.
     */
    public HttpClient build() {
        throw LOGGER.throwableAtError().log(ERROR_MESSAGE, UnsupportedOperationException::new);
    }
}
