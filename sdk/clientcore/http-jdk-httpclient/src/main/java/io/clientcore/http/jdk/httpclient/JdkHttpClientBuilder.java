// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.jdk.httpclient;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.ProxyOptions;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.SharedExecutorService;
import io.clientcore.core.util.configuration.Configuration;
import io.clientcore.http.jdk.httpclient.implementation.JdkHttpClientProxySelector;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.Reader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executor;

import static io.clientcore.core.util.configuration.Configuration.PROPERTY_REQUEST_CONNECT_TIMEOUT;
import static io.clientcore.core.util.configuration.Configuration.PROPERTY_REQUEST_READ_TIMEOUT;
import static io.clientcore.core.util.configuration.Configuration.PROPERTY_REQUEST_RESPONSE_TIMEOUT;
import static io.clientcore.core.util.configuration.Configuration.PROPERTY_REQUEST_WRITE_TIMEOUT;
import static io.clientcore.http.jdk.httpclient.implementation.JdkHttpUtils.getDefaultTimeoutFromEnvironment;

/**
 * Builder to configure and build an instance of the clientcore {@link HttpClient} type using the JDK HttpClient APIs,
 * first introduced as preview in JDK 9, but made generally available from JDK 11 onwards.
 */
public class JdkHttpClientBuilder {
    private static final ClientLogger LOGGER = new ClientLogger(JdkHttpClientBuilder.class);

    private static final Duration MINIMUM_TIMEOUT = Duration.ofMillis(1);
    private static final Duration DEFAULT_CONNECTION_TIMEOUT;
    private static final Duration DEFAULT_WRITE_TIMEOUT;
    private static final Duration DEFAULT_RESPONSE_TIMEOUT;
    private static final Duration DEFAULT_READ_TIMEOUT;

    private static final String JAVA_HOME = System.getProperty("java.home");
    private static final String JDK_HTTPCLIENT_ALLOW_RESTRICTED_HEADERS = "jdk.httpclient.allowRestrictedHeaders";

    // These headers are restricted by default in native JDK12 HttpClient.
    // These headers can be whitelisted by setting jdk.httpclient.allowRestrictedHeaders
    // property in the network properties file: 'JAVA_HOME/conf/net.properties'
    // e.g white listing 'host' header.
    //
    // jdk.httpclient.allowRestrictedHeaders=host
    // Also see - https://bugs.openjdk.java.net/browse/JDK-8213189
    static final Set<String> DEFAULT_RESTRICTED_HEADERS;

    static {
        Configuration configuration = Configuration.getGlobalConfiguration();

        DEFAULT_CONNECTION_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration, PROPERTY_REQUEST_CONNECT_TIMEOUT,
            Duration.ofSeconds(10), LOGGER);
        DEFAULT_WRITE_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration, PROPERTY_REQUEST_WRITE_TIMEOUT,
            Duration.ofSeconds(60), LOGGER);
        DEFAULT_RESPONSE_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration, PROPERTY_REQUEST_RESPONSE_TIMEOUT,
            Duration.ofSeconds(60), LOGGER);
        DEFAULT_READ_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration, PROPERTY_REQUEST_READ_TIMEOUT,
            Duration.ofSeconds(60), LOGGER);

        DEFAULT_RESTRICTED_HEADERS = Set.of("connection", "content-length", "expect", "host", "upgrade");
    }

    private java.net.http.HttpClient.Builder httpClientBuilder;
    private ProxyOptions proxyOptions;
    private Configuration configuration;
    private Executor executor;
    private SSLContext sslContext;

    private Duration connectionTimeout;
    private Duration writeTimeout;
    private Duration responseTimeout;
    private Duration readTimeout;

    /**
     * Creates JdkHttpClientBuilder.
     */
    public JdkHttpClientBuilder() {
        this.executor = SharedExecutorService.getInstance();
    }

    /**
     * Creates JdkHttpClientBuilder from the builder of an existing {@link java.net.http.HttpClient.Builder}.
     *
     * @param httpClientBuilder the HttpClient builder to use
     * @throws NullPointerException if {@code httpClientBuilder} is null
     */
    public JdkHttpClientBuilder(java.net.http.HttpClient.Builder httpClientBuilder) {
        this.httpClientBuilder = Objects.requireNonNull(httpClientBuilder, "'httpClientBuilder' cannot be null.");
    }

    /**
     * Sets the executor to be used for asynchronous and dependent tasks. This cannot be null.
     * <p>
     * If this method is not invoked prior to {@link #build() building}, handling for a default will be based on whether
     * the builder was created with the default constructor or the constructor that accepts an existing
     * {@link java.net.http.HttpClient.Builder}. If the default constructor was used, the default executor will be
     * {@link SharedExecutorService#getInstance()}. If the constructor that accepts an existing
     * {@link java.net.http.HttpClient.Builder} was used, the executor from the existing builder will be used.
     *
     * @param executor the executor to be used for asynchronous and dependent tasks
     * @return the updated JdkHttpClientBuilder object
     * @throws NullPointerException if {@code executor} is null
     */
    public JdkHttpClientBuilder executor(Executor executor) {
        this.executor = Objects.requireNonNull(executor, "executor can not be null");
        return this;
    }

    /**
     * Sets the connection timeout.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed io.clientcore.http.jdk.httpclient.JdkHttpClientBuilder.connectionTimeout#Duration -->
     * <pre>
     * HttpClient client = new JdkHttpClientBuilder&#40;&#41;
     *         .connectionTimeout&#40;Duration.ofSeconds&#40;250&#41;&#41; &#47;&#47; connection timeout of 250 seconds
     *         .build&#40;&#41;;
     * </pre>
     * <!-- end io.clientcore.http.jdk.httpclient.JdkHttpClientBuilder.connectionTimeout#Duration -->
     *
     * The default connection timeout is 10 seconds.
     *
     * @param connectionTimeout the connection timeout
     * @return the updated JdkHttpClientBuilder object
     */
    public JdkHttpClientBuilder connectionTimeout(Duration connectionTimeout) {
        // setConnectionTimeout can be null
        this.connectionTimeout = connectionTimeout;
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
     * @return The updated {@link JdkHttpClientBuilder} object.
     */
    public JdkHttpClientBuilder writeTimeout(Duration writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
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
     * @return The updated {@link JdkHttpClientBuilder} object.
     */
    public JdkHttpClientBuilder responseTimeout(Duration responseTimeout) {
        this.responseTimeout = responseTimeout;
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
     * @return The updated {@link JdkHttpClientBuilder} object.
     */
    public JdkHttpClientBuilder readTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    /**
     * Sets the proxy.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed io.clientcore.http.jdk.httpclient.JdkHttpClientBuilder.proxy#ProxyOptions -->
     * <pre>
     * final String proxyHost = &quot;&lt;proxy-host&gt;&quot;; &#47;&#47; e.g. localhost
     * final int proxyPort = 9999; &#47;&#47; Proxy port
     * ProxyOptions proxyOptions = new ProxyOptions&#40;ProxyOptions.Type.HTTP,
     *     new InetSocketAddress&#40;proxyHost, proxyPort&#41;&#41;;
     * HttpClient client = new JdkHttpClientBuilder&#40;&#41;
     *     .proxy&#40;proxyOptions&#41;
     *     .build&#40;&#41;;
     * </pre>
     * <!-- end io.clientcore.http.jdk.httpclient.JdkHttpClientBuilder.proxy#ProxyOptions -->
     *
     * @param proxyOptions The proxy configuration to use.
     * @return the updated JdkHttpClientBuilder object
     */
    public JdkHttpClientBuilder proxy(ProxyOptions proxyOptions) {
        // proxyOptions can be null
        this.proxyOptions = proxyOptions;
        return this;
    }

    /**
     * Sets the {@link SSLContext} to be used when opening secure connections.
     *
     * @param sslContext The SSL context to be used.
     * @return The updated JdkHttpClientBuilder object.
     */
    public JdkHttpClientBuilder sslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the HTTP client.
     *
     * @param configuration The configuration store used to
     * @return The updated JdkHttpClientBuilder object.
     */
    public JdkHttpClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Build a HttpClient with current configurations.
     *
     * @return a {@link HttpClient}.
     */
    public HttpClient build() {
        java.net.http.HttpClient.Builder httpClientBuilder
            = this.httpClientBuilder == null ? java.net.http.HttpClient.newBuilder() : this.httpClientBuilder;

        // Client Core JDK http client supports HTTP 1.1 by default.
        httpClientBuilder.version(java.net.http.HttpClient.Version.HTTP_1_1);

        httpClientBuilder = httpClientBuilder.connectTimeout(getTimeout(connectionTimeout, DEFAULT_CONNECTION_TIMEOUT));

        Duration writeTimeout = getTimeout(this.writeTimeout, DEFAULT_WRITE_TIMEOUT);
        Duration responseTimeout = getTimeout(this.responseTimeout, DEFAULT_RESPONSE_TIMEOUT);
        Duration readTimeout = getTimeout(this.readTimeout, DEFAULT_READ_TIMEOUT);

        Configuration buildConfiguration
            = (configuration == null) ? Configuration.getGlobalConfiguration() : configuration;

        ProxyOptions buildProxyOptions
            = (proxyOptions == null) ? ProxyOptions.fromConfiguration(buildConfiguration) : proxyOptions;

        if (executor != null) {
            httpClientBuilder.executor(executor);
        }

        if (sslContext != null) {
            httpClientBuilder.sslContext(sslContext);
        }

        if (buildProxyOptions != null) {
            httpClientBuilder
                = httpClientBuilder.proxy(new JdkHttpClientProxySelector(buildProxyOptions.getType().toProxyType(),
                    buildProxyOptions.getAddress(), buildProxyOptions.getNonProxyHosts()));

            if (buildProxyOptions.getUsername() != null) {
                httpClientBuilder.authenticator(
                    new ProxyAuthenticator(buildProxyOptions.getUsername(), buildProxyOptions.getPassword()));
            }
        }

        return new JdkHttpClient(httpClientBuilder.build(), Collections.unmodifiableSet(getRestrictedHeaders()),
            writeTimeout, responseTimeout, readTimeout);
    }

    Set<String> getRestrictedHeaders() {
        // Compute the effective restricted headers by removing the allowed headers from default restricted headers
        Set<String> restrictedHeaders = new HashSet<>(DEFAULT_RESTRICTED_HEADERS);
        removeAllowedHeaders(restrictedHeaders);
        return restrictedHeaders;
    }

    private void removeAllowedHeaders(Set<String> restrictedHeaders) {
        Properties properties = getNetworkProperties();
        String[] allowRestrictedHeadersNetProperties
            = properties.getProperty(JDK_HTTPCLIENT_ALLOW_RESTRICTED_HEADERS, "").split(",");

        // Read all allowed restricted headers from configuration
        Configuration config = (this.configuration == null) ? Configuration.getGlobalConfiguration() : configuration;
        String[] allowRestrictedHeadersSystemProperties
            = config.get(JDK_HTTPCLIENT_ALLOW_RESTRICTED_HEADERS, "").split(",");

        // Combine the set of all allowed restricted headers from both sources
        for (String header : allowRestrictedHeadersSystemProperties) {
            restrictedHeaders.remove(header.trim().toLowerCase(Locale.ROOT));
        }

        for (String header : allowRestrictedHeadersNetProperties) {
            restrictedHeaders.remove(header.trim().toLowerCase(Locale.ROOT));
        }
    }

    Properties getNetworkProperties() {
        // Read all allowed restricted headers from JAVA_HOME/conf/net.properties
        Path path = Paths.get(JAVA_HOME, "conf", "net.properties");
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(path)) {
            properties.load(reader);
        } catch (IOException e) {
            LOGGER.atWarning().addKeyValue("path", path).log("Cannot read net properties.", e);;
        }
        return properties;
    }

    private static class ProxyAuthenticator extends Authenticator {
        private final String userName;
        private final String password;

        ProxyAuthenticator(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(this.userName, password.toCharArray());
        }
    }

    private static Duration getTimeout(Duration configuredTimeout, Duration defaultTimeout) {
        if (configuredTimeout == null) {
            return defaultTimeout;
        }

        return configuredTimeout.compareTo(MINIMUM_TIMEOUT) < 0 ? MINIMUM_TIMEOUT : configuredTimeout;
    }
}
