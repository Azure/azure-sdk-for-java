// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.jdk.httpclient.implementation.JdkHttpClientProxySelector;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;

import java.io.IOException;
import java.io.Reader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executor;

/**
 * Builder to configure and build an instance of the azure-core {@link HttpClient} type using the JDK HttpClient APIs,
 * first introduced as preview in JDK 9, but made generally available from JDK 11 onwards.
 */
public class JdkAsyncHttpClientBuilder {

    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(60);
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
        TreeSet<String> treeSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        treeSet.addAll(Set.of(
            "connection",
            "content-length",
            "expect",
            "host",
            "upgrade"
        ));
        DEFAULT_RESTRICTED_HEADERS = Collections.unmodifiableSet(treeSet);
    }

    private final ClientLogger logger = new ClientLogger(JdkAsyncHttpClientBuilder.class);

    private java.net.http.HttpClient.Builder httpClientBuilder;
    private Duration connectionTimeout;
    private ProxyOptions proxyOptions;
    private Configuration configuration;
    private Executor executor;

    /**
     * Creates JdkAsyncHttpClientBuilder.
     */
    public JdkAsyncHttpClientBuilder() {
    }

    /**
     * Creates JdkAsyncHttpClientBuilder from the builder of an existing {@link java.net.http.HttpClient.Builder}.
     *
     * @param httpClientBuilder the HttpClient builder to use
     * @throws NullPointerException if {@code httpClientBuilder} is null
     */
    public JdkAsyncHttpClientBuilder(java.net.http.HttpClient.Builder httpClientBuilder) {
        this.httpClientBuilder = Objects.requireNonNull(httpClientBuilder, "'httpClientBuilder' cannot be null.");
    }

    /**
     * Sets the executor to be used for asynchronous and dependent tasks. This cannot be null.
     * <p>
     * If this method is not invoked prior to {@linkplain #build() building}, a default executor is created for each
     * newly built {@code HttpClient}.
     *
     * @param executor the executor to be used for asynchronous and dependent tasks
     * @return the updated JdkAsyncHttpClientBuilder object
     * @throws NullPointerException if {@code executor} is null
     */
    public JdkAsyncHttpClientBuilder executor(Executor executor) {
        this.executor = Objects.requireNonNull(executor, "executor can not be null");
        return this;
    }

    /**
     * Sets the connection timeout.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.core.http.jdk.httpclient.JdkAsyncHttpClientBuilder.connectionTimeout#Duration -->
     * <pre>
     * HttpClient client = new JdkAsyncHttpClientBuilder&#40;&#41;
     *         .connectionTimeout&#40;Duration.ofSeconds&#40;250&#41;&#41; &#47;&#47; connection timeout of 250 seconds
     *         .build&#40;&#41;;
     * </pre>
     * <!-- end com.azure.core.http.jdk.httpclient.JdkAsyncHttpClientBuilder.connectionTimeout#Duration -->
     *
     * The default connection timeout is 60 seconds.
     *
     * @param connectionTimeout the connection timeout
     * @return the updated JdkAsyncHttpClientBuilder object
     */
    public JdkAsyncHttpClientBuilder connectionTimeout(Duration connectionTimeout) {
        // setConnectionTimeout can be null
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    /**
     * Sets the proxy.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.core.http.jdk.httpclient.JdkAsyncHttpClientBuilder.proxy#ProxyOptions -->
     * <pre>
     * final String proxyHost = &quot;&lt;proxy-host&gt;&quot;; &#47;&#47; e.g. localhost
     * final int proxyPort = 9999; &#47;&#47; Proxy port
     * ProxyOptions proxyOptions = new ProxyOptions&#40;ProxyOptions.Type.HTTP,
     *         new InetSocketAddress&#40;proxyHost, proxyPort&#41;&#41;;
     * HttpClient client = new JdkAsyncHttpClientBuilder&#40;&#41;
     *         .proxy&#40;proxyOptions&#41;
     *         .build&#40;&#41;;
     * </pre>
     * <!-- end com.azure.core.http.jdk.httpclient.JdkAsyncHttpClientBuilder.proxy#ProxyOptions -->
     *
     * @param proxyOptions The proxy configuration to use.
     * @return the updated {@link JdkAsyncHttpClientBuilder} object
     */
    public JdkAsyncHttpClientBuilder proxy(ProxyOptions proxyOptions) {
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
     * @return The updated JdkAsyncHttpClientBuilder object.
     */
    public JdkAsyncHttpClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Build a HttpClient with current configurations.
     *
     * @return a {@link HttpClient}.
     */
    public HttpClient build() {
        java.net.http.HttpClient.Builder httpClientBuilder = this.httpClientBuilder == null
            ? java.net.http.HttpClient.newBuilder()
            : this.httpClientBuilder;

        httpClientBuilder = (this.connectionTimeout != null)
            ? httpClientBuilder.connectTimeout(this.connectionTimeout)
            : httpClientBuilder.connectTimeout(DEFAULT_CONNECT_TIMEOUT);

        Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration()
            : configuration;

        ProxyOptions buildProxyOptions = (proxyOptions == null && buildConfiguration != Configuration.NONE)
            ? ProxyOptions.fromConfiguration(buildConfiguration)
            : proxyOptions;

        if (executor != null) {
            httpClientBuilder.executor(executor);
        }

        if (buildProxyOptions != null) {
            httpClientBuilder = httpClientBuilder.proxy(new JdkHttpClientProxySelector(
                buildProxyOptions.getType().toProxyType(),
                buildProxyOptions.getAddress(),
                buildProxyOptions.getNonProxyHosts()));

            if (buildProxyOptions.getUsername() != null) {
                httpClientBuilder
                    .authenticator(new ProxyAuthenticator(buildProxyOptions.getUsername(),
                        buildProxyOptions.getPassword()));
            }
        }
        return new JdkAsyncHttpClient(httpClientBuilder.build(), Collections.unmodifiableSet(getRestrictedHeaders()));
    }

    Set<String> getRestrictedHeaders() {
        // Compute the effective restricted headers by removing the allowed headers from default restricted headers
        Set<String> allowRestrictedHeaders = getAllowRestrictedHeaders();
        Set<String> restrictedHeaders = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        restrictedHeaders.addAll(DEFAULT_RESTRICTED_HEADERS);
        restrictedHeaders.removeAll(allowRestrictedHeaders);
        return restrictedHeaders;
    }

    private Set<String> getAllowRestrictedHeaders() {
        Properties properties = getNetworkProperties();
        String[] allowRestrictedHeadersNetProperties =
            properties.getProperty(JDK_HTTPCLIENT_ALLOW_RESTRICTED_HEADERS, "").split(",");

        // Read all allowed restricted headers from configuration
        Configuration config = (this.configuration == null)
            ? Configuration.getGlobalConfiguration()
            : configuration;
        String[] allowRestrictedHeadersSystemProperties = config.get(JDK_HTTPCLIENT_ALLOW_RESTRICTED_HEADERS, "")
            .split(",");

        Set<String> allowRestrictedHeaders = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        // Combine the set of all allowed restricted headers from both sources
        for (String header : allowRestrictedHeadersSystemProperties) {
            allowRestrictedHeaders.add(header.trim());
        }

        for (String header : allowRestrictedHeadersNetProperties) {
            allowRestrictedHeaders.add(header.trim());
        }

        return allowRestrictedHeaders;
    }

    Properties getNetworkProperties() {
        // Read all allowed restricted headers from JAVA_HOME/conf/net.properties
        Path path = Paths.get(JAVA_HOME, "conf", "net.properties");
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(path)) {
            properties.load(reader);
        } catch (IOException e) {
            logger.warning("Cannot read net properties file at path {}", path, e);
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
}
