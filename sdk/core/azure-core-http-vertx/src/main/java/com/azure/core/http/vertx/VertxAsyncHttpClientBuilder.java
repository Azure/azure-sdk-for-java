// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.net.ProxyType;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.azure.core.implementation.util.HttpUtils.getDefaultConnectTimeout;
import static com.azure.core.implementation.util.HttpUtils.getDefaultReadTimeout;
import static com.azure.core.implementation.util.HttpUtils.getDefaultResponseTimeout;
import static com.azure.core.implementation.util.HttpUtils.getDefaultWriteTimeout;
import static com.azure.core.implementation.util.HttpUtils.getTimeout;

/**
 * Builds a {@link VertxAsyncHttpClient}.
 */
public class VertxAsyncHttpClientBuilder {

    private static final ClientLogger LOGGER = new ClientLogger(VertxAsyncHttpClientBuilder.class);
    private static final Pattern NON_PROXY_HOSTS_SPLIT = Pattern.compile("(?<!\\\\)\\|");
    private static final Pattern NON_PROXY_HOST_DESANITIZE = Pattern.compile("(\\?|\\\\|\\(|\\)|\\\\E|\\\\Q|\\.\\.)");
    private static final Pattern NON_PROXY_HOST_DOT_STAR = Pattern.compile("(\\.\\*)");

    private Duration connectTimeout;
    private Duration writeTimeout;
    private Duration responseTimeout;
    private Duration readTimeout;
    private ProxyOptions proxyOptions;
    private Configuration configuration;
    private HttpClientOptions httpClientOptions;
    private Vertx vertx;

    /**
     * Creates an instance of {@link VertxAsyncHttpClientBuilder}.
     */
    public VertxAsyncHttpClientBuilder() {
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
     * @param connectTimeout Connect timeout duration.
     * @return The updated OkHttpAsyncHttpClientBuilder object.
     */
    public VertxAsyncHttpClientBuilder connectTimeout(Duration connectTimeout) {
        // setConnectionTimeout can be null
        this.connectTimeout = connectTimeout;
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
     */
    public VertxAsyncHttpClientBuilder readTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
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
     * Given OkHttp doesn't have an equivalent timeout for just responses, this is handled manually.
     *
     * @param responseTimeout Response timeout duration.
     * @return The updated VertxAsyncHttpClientBuilder object.
     */
    public VertxAsyncHttpClientBuilder responseTimeout(Duration responseTimeout) {
        this.responseTimeout = responseTimeout;
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
     * @return The updated VertxAsyncHttpClientBuilder object.
     */
    public VertxAsyncHttpClientBuilder writeTimeout(Duration writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    /**
     * Sets proxy configuration.
     *
     * @param proxyOptions The proxy configuration to use.
     * @return The updated VertxAsyncHttpClientBuilder object.
     */
    public VertxAsyncHttpClientBuilder proxy(ProxyOptions proxyOptions) {
        this.proxyOptions = proxyOptions;
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the HTTP client.
     * <p>
     * The default configuration store is a clone of the
     * {@link Configuration#getGlobalConfiguration() global configuration store}, use {@link Configuration#NONE} to
     * bypass using configuration settings during construction.
     *
     * @param configuration The configuration store.
     * @return The updated VertxAsyncHttpClientBuilder object.
     */
    public VertxAsyncHttpClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets custom {@link HttpClientOptions} for the constructed {@link io.vertx.core.http.HttpClient}.
     *
     * @param httpClientOptions The options of the web client.
     * @return The updated VertxAsyncHttpClientBuilder object
     */
    public VertxAsyncHttpClientBuilder httpClientOptions(HttpClientOptions httpClientOptions) {
        this.httpClientOptions = httpClientOptions;
        return this;
    }

    /**
     * Sets a custom {@link Vertx} instance that the constructed {@link io.vertx.core.http.HttpClient} will be created
     * with.
     *
     * @param vertx The vertx instance.
     * @return The updated VertxAsyncHttpClientBuilder object
     */
    public VertxAsyncHttpClientBuilder vertx(Vertx vertx) {
        this.vertx = vertx;
        return this;
    }

    /**
     * Creates a new Vert.x {@link HttpClient} instance on every call, using the configuration set in the builder at the
     * time of the build method call.
     *
     * @return A new Vert.x backed {@link HttpClient} instance.
     */
    public HttpClient build() {
        Vertx configuredVertx = this.vertx;
        if (configuredVertx == null) {
            ServiceLoader<VertxProvider> vertxProviders
                = ServiceLoader.load(VertxProvider.class, VertxProvider.class.getClassLoader());
            configuredVertx = getVertx(vertxProviders.iterator());
        }

        HttpClientOptions buildOptions = this.httpClientOptions;
        if (buildOptions == null) {
            buildOptions = new HttpClientOptions().setIdleTimeoutUnit(TimeUnit.MILLISECONDS)
                .setConnectTimeout((int) getTimeout(this.connectTimeout, getDefaultConnectTimeout()).toMillis())
                .setReadIdleTimeout((int) getTimeout(this.readTimeout, getDefaultReadTimeout()).toMillis())
                .setWriteIdleTimeout((int) getTimeout(this.writeTimeout, getDefaultWriteTimeout()).toMillis());

            Configuration buildConfiguration
                = (configuration == null) ? Configuration.getGlobalConfiguration() : configuration;

            ProxyOptions buildProxyOptions
                = (proxyOptions == null) ? ProxyOptions.fromConfiguration(buildConfiguration, true) : proxyOptions;

            if (buildProxyOptions != null) {
                io.vertx.core.net.ProxyOptions vertxProxyOptions = new io.vertx.core.net.ProxyOptions();
                InetSocketAddress proxyAddress = buildProxyOptions.getAddress();

                if (proxyAddress != null) {
                    vertxProxyOptions.setHost(proxyAddress.getHostName());
                    vertxProxyOptions.setPort(proxyAddress.getPort());
                }

                String proxyUsername = buildProxyOptions.getUsername();
                String proxyPassword = buildProxyOptions.getPassword();
                if (!CoreUtils.isNullOrEmpty(proxyUsername) && !CoreUtils.isNullOrEmpty(proxyPassword)) {
                    vertxProxyOptions.setUsername(proxyUsername);
                    vertxProxyOptions.setPassword(proxyPassword);
                }

                ProxyOptions.Type type = buildProxyOptions.getType();
                if (type != null) {
                    try {
                        ProxyType proxyType = ProxyType.valueOf(type.name());
                        vertxProxyOptions.setType(proxyType);
                    } catch (IllegalArgumentException e) {
                        throw LOGGER.logExceptionAsError(
                            new IllegalArgumentException("Unknown Vert.x proxy type: " + type.name(), e));
                    }
                }

                String nonProxyHosts = buildProxyOptions.getNonProxyHosts();
                if (!CoreUtils.isNullOrEmpty(nonProxyHosts)) {
                    for (String nonProxyHost : desanitizedNonProxyHosts(nonProxyHosts)) {
                        buildOptions.addNonProxyHost(nonProxyHost);
                    }
                }

                buildOptions.setProxyOptions(vertxProxyOptions);
            }
        }

        io.vertx.core.http.HttpClient client = configuredVertx.createHttpClient(buildOptions);
        return new VertxAsyncHttpClient(client, getTimeout(this.responseTimeout, getDefaultResponseTimeout()));
    }

    static Vertx getVertx(Iterator<VertxProvider> iterator) {
        Vertx configuredVertx;
        if (iterator.hasNext()) {
            VertxProvider provider = iterator.next();
            configuredVertx = provider.createVertx();
            LOGGER.verbose("Using {} as the VertxProvider.", provider.getClass().getName());

            while (iterator.hasNext()) {
                VertxProvider ignoredProvider = iterator.next();
                LOGGER.warning("Multiple VertxProviders were found on the classpath, ignoring {}.",
                    ignoredProvider.getClass().getName());
            }
        } else {
            configuredVertx = DefaultVertx.DEFAULT_VERTX.getVertx();
        }

        return configuredVertx;
    }

    /**
     * Reverses non-proxy host string sanitization applied by {@link ProxyOptions}.
     * <p>
     * This is necessary as Vert.x will apply its own sanitization logic.
     *
     * @param nonProxyHosts The list of non-proxy hosts
     * @return String array of desanitized proxy host strings
     */
    private String[] desanitizedNonProxyHosts(String nonProxyHosts) {
        String desanitzedNonProxyHosts = NON_PROXY_HOST_DESANITIZE.matcher(nonProxyHosts).replaceAll("");

        desanitzedNonProxyHosts = NON_PROXY_HOST_DOT_STAR.matcher(desanitzedNonProxyHosts).replaceAll("*");

        return NON_PROXY_HOSTS_SPLIT.split(desanitzedNonProxyHosts);
    }

    // Enum Singleton Pattern
    private enum DefaultVertx {
        DEFAULT_VERTX(Vertx.vertx());

        private final Vertx vertx;

        DefaultVertx(Vertx vertx) {
            this.vertx = vertx;
            Runtime.getRuntime().addShutdownHook(new Thread(getVertxCloseRunnable(vertx)));
        }

        private Vertx getVertx() {
            return vertx;
        }
    }

    /**
     * Gets a {@link Runnable} to close the embedded {@link Vertx} instance on shutdown.
     *
     * @param vertxToClose The {@link Vertx} instance to close on shutdown
     * @return The {@link Runnable} action to close the embedded {@link Vertx} instance
     */
    private static Runnable getVertxCloseRunnable(Vertx vertxToClose) {
        return () -> {
            CountDownLatch latch = new CountDownLatch(1);
            if (vertxToClose != null) {
                vertxToClose.close(event -> {
                    if (event.failed() && event.cause() != null) {
                        LOGGER.logThrowableAsError(event.cause());
                    }
                    latch.countDown();
                });
            }

            try {
                if (!latch.await(1, TimeUnit.MINUTES)) {
                    LOGGER.warning("Timeout closing Vertx");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
    }
}
