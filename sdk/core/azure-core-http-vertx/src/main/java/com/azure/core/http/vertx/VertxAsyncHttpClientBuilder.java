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

import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_READ_TIMEOUT;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT;
import static com.azure.core.util.CoreUtils.getDefaultTimeoutFromEnvironment;

/**
 * Builds a {@link VertxAsyncHttpClient}.
 */
public class VertxAsyncHttpClientBuilder {

    private static final ClientLogger LOGGER = new ClientLogger(VertxAsyncHttpClientBuilder.class);
    private static final Pattern NON_PROXY_HOSTS_SPLIT = Pattern.compile("(?<!\\\\)\\|");
    private static final Pattern NON_PROXY_HOST_DESANITIZE = Pattern.compile("(\\?|\\\\|\\(|\\)|\\\\E|\\\\Q|\\.\\.)");
    private static final Pattern NON_PROXY_HOST_DOT_STAR = Pattern.compile("(\\.\\*)");
    private static final long DEFAULT_CONNECT_TIMEOUT;
    private static final long DEFAULT_WRITE_TIMEOUT;
    private static final long DEFAULT_READ_TIMEOUT;

    static {
        Configuration configuration = Configuration.getGlobalConfiguration();
        DEFAULT_CONNECT_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration,
                PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT, Duration.ofSeconds(10), LOGGER).toMillis();
        DEFAULT_WRITE_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration, PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT,
                Duration.ofSeconds(60), LOGGER).getSeconds();
        DEFAULT_READ_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration, PROPERTY_AZURE_REQUEST_READ_TIMEOUT,
                Duration.ofSeconds(60), LOGGER).getSeconds();
    }

    private Duration readIdleTimeout;
    private Duration writeIdleTimeout;
    private Duration connectTimeout;
    private Duration idleTimeout = Duration.ofSeconds(60);
    private ProxyOptions proxyOptions;
    private Configuration configuration;
    private HttpClientOptions httpClientOptions;
    private Vertx vertx;

    /**
     * Sets the read idle timeout.
     *
     * The default read idle timeout is 60 seconds.
     *
     * @param readIdleTimeout the read idle timeout
     * @return the updated VertxAsyncHttpClientBuilder object
     */
    public VertxAsyncHttpClientBuilder readIdleTimeout(Duration readIdleTimeout) {
        this.readIdleTimeout = readIdleTimeout;
        return this;
    }

    /**
     * Sets the write idle timeout.
     *
     * The default read idle timeout is 60 seconds.
     *
     * @param writeIdleTimeout the write idle timeout
     * @return the updated VertxAsyncHttpClientBuilder object
     */
    public VertxAsyncHttpClientBuilder writeIdleTimeout(Duration writeIdleTimeout) {
        this.writeIdleTimeout = writeIdleTimeout;
        return this;
    }

    /**
     * Sets the connect timeout.
     *
     * The default connect timeout is 10 seconds.
     *
     * @param connectTimeout the connection timeout
     * @return the updated VertxAsyncHttpClientBuilder object
     */
    public VertxAsyncHttpClientBuilder connectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * Sets the connection idle timeout.
     *
     * The default connect timeout is 60 seconds.
     *
     * @param idleTimeout the connection idle timeout
     * @return the updated VertxAsyncHttpClientBuilder object
     */
    public VertxAsyncHttpClientBuilder idleTimeout(Duration idleTimeout) {
        this.idleTimeout = idleTimeout;
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
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.
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
     * Sets a custom {@link Vertx} instance that the constructed {@link io.vertx.core.http.HttpClient} will be created with.
     *
     * @param vertx The vertx instance.
     * @return The updated VertxAsyncHttpClientBuilder object
     */
    public VertxAsyncHttpClientBuilder vertx(Vertx vertx) {
        this.vertx = vertx;
        return this;
    }

    /**
     * Creates a new Vert.x {@link HttpClient} instance on every call, using the
     * configuration set in the builder at the time of the build method call.
     *
     * @return A new Vert.x backed {@link HttpClient} instance.
     */
    public HttpClient build() {
        boolean shutdownHookRequired = false;
        if (this.vertx == null) {
            ServiceLoader<VertxProvider> vertxProviders = ServiceLoader.load(VertxProvider.class, VertxProvider.class.getClassLoader());
            Iterator<VertxProvider> iterator = vertxProviders.iterator();
            if (iterator.hasNext()) {
                VertxProvider provider = iterator.next();
                this.vertx = provider.createVertx();
                LOGGER.verbose("Using {} as the VertxProvider.", provider.getClass().getName());

                while (iterator.hasNext()) {
                    VertxProvider ignoredProvider = iterator.next();
                    LOGGER.warning("Multiple VertxProviders were found on the classpath, ignoring {}.",
                        ignoredProvider.getClass().getName());
                }
            } else {
                this.vertx = Vertx.vertx();
                shutdownHookRequired = true;
            }
        }

        if (this.httpClientOptions == null) {
            this.httpClientOptions = new HttpClientOptions();

            if (this.connectTimeout != null) {
                this.httpClientOptions.setConnectTimeout((int) this.connectTimeout.toMillis());
            } else {
                this.httpClientOptions.setConnectTimeout((int) DEFAULT_CONNECT_TIMEOUT);
            }

            if (this.readIdleTimeout != null) {
                this.httpClientOptions.setReadIdleTimeout((int) this.readIdleTimeout.getSeconds());
            } else {
                this.httpClientOptions.setReadIdleTimeout((int) DEFAULT_READ_TIMEOUT);
            }

            if (this.writeIdleTimeout != null) {
                this.httpClientOptions.setWriteIdleTimeout((int) this.writeIdleTimeout.getSeconds());
            } else {
                this.httpClientOptions.setWriteIdleTimeout((int) DEFAULT_WRITE_TIMEOUT);
            }

            this.httpClientOptions.setIdleTimeout((int) this.idleTimeout.getSeconds());

            Configuration buildConfiguration = (this.configuration == null)
                ? Configuration.getGlobalConfiguration()
                : configuration;

            ProxyOptions buildProxyOptions = (this.proxyOptions == null)
                ? ProxyOptions.fromConfiguration(buildConfiguration, true)
                : this.proxyOptions;

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
                        throw LOGGER.logExceptionAsError(new IllegalArgumentException("Unknown Vert.x proxy type: " + type.name(), e));
                    }
                }

                String nonProxyHosts = buildProxyOptions.getNonProxyHosts();
                if (!CoreUtils.isNullOrEmpty(nonProxyHosts)) {
                    for (String nonProxyHost : desanitizedNonProxyHosts(nonProxyHosts)) {
                        this.httpClientOptions.addNonProxyHost(nonProxyHost);
                    }
                }

                this.httpClientOptions.setProxyOptions(vertxProxyOptions);
            }
        }

        if (shutdownHookRequired) {
            Runtime.getRuntime().addShutdownHook(new Thread(getVertxCloseRunnable()));
        }

        io.vertx.core.http.HttpClient client = this.vertx.createHttpClient(this.httpClientOptions);
        return new VertxAsyncHttpClient(client, this.vertx);
    }

    /**
     * Reverses non proxy host string sanitization applied by {@link ProxyOptions}.
     *
     * This is necessary as Vert.x will apply its own sanitization logic.
     *
     * @param nonProxyHosts The list of non proxy hosts
     * @return String array of desanitized proxy host strings
     */
    private String[] desanitizedNonProxyHosts(String nonProxyHosts) {
        String desanitzedNonProxyHosts = NON_PROXY_HOST_DESANITIZE.matcher(nonProxyHosts)
            .replaceAll("");

        desanitzedNonProxyHosts = NON_PROXY_HOST_DOT_STAR.matcher(desanitzedNonProxyHosts)
            .replaceAll("*");

        return NON_PROXY_HOSTS_SPLIT.split(desanitzedNonProxyHosts);
    }

    /**
     * Gets a {@link Runnable} to close the embedded {@link Vertx} instance on shutdown.
     *
     * @return The {@link Runnable} action to close the embedded {@link Vertx} instance
     */
    private Runnable getVertxCloseRunnable() {
        return () -> {
            CountDownLatch latch = new CountDownLatch(1);
            if (vertx != null) {
                vertx.close(event -> {
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
