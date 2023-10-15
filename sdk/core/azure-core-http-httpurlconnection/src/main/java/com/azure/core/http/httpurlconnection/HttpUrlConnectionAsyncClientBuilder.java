// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.httpurlconnection;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.Configuration;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Builder to configure and build an instance of the azure-core {@link HttpClient} type using the JDK HttpURLConnection,
 * first introduced in JDK 1.1.
 */
public class HttpUrlConnectionAsyncClientBuilder {

    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration DEFAULT_WRITE_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration DEFAULT_RESPONSE_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration MINIMUM_TIMEOUT = Duration.ofMillis(1);

    static final Set<String> DEFAULT_RESTRICTED_HEADERS;

    static {
        Set<String> treeSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        Collections.addAll(treeSet, "connection", "content-length", "expect", "host", "upgrade");
        DEFAULT_RESTRICTED_HEADERS = Collections.unmodifiableSet(treeSet);
    }

    private Duration connectionTimeout;
    private Duration readTimeout;
    private Duration writeTimeout;
    private Duration responseTimeout;
    private ProxyOptions proxyOptions;
    private Configuration configuration;

    /**
     * HttpUrlConnectionAsyncClientBuilder.
     */
    public HttpUrlConnectionAsyncClientBuilder() {
    }

    /**
     * Sets the connection timeout.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.core.http.httpurlconnection.HttpUrlConnectionAsyncClientBuilder.connectionTimeout#Duration -->
     * <pre>
     * HttpClient client = new HttpUrlConnectionAsyncClientBuilder&#40;&#41;
     *         .connectionTimeout&#40;Duration.ofSeconds&#40;250&#41;&#41; &#47;&#47; connection timeout of 250 seconds
     *         .build&#40;&#41;;
     * </pre>
     * <!-- end com.azure.core.http.httpurlconnection.HttpUrlConnectionAsyncClientBuilder.connectionTimeout#Duration -->
     *
     * The default connection timeout is 10 seconds.
     *
     * @param connectionTimeout the connection timeout
     * @return the updated HttpUrlConnectionAsyncClientBuilder object
     */
    public HttpUrlConnectionAsyncClientBuilder connectionTimeout(Duration connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public HttpUrlConnectionAsyncClientBuilder readTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public HttpUrlConnectionAsyncClientBuilder writeTimeout(Duration writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    public HttpUrlConnectionAsyncClientBuilder responseTimeout(Duration responseTimeout) {
        this.responseTimeout = responseTimeout;
        return this;
    }

    static Duration getTimeout(Duration configuredTimeout, Duration defaultTimeout) {
        if (configuredTimeout == null) {
            return defaultTimeout;
        }

        if (configuredTimeout.isZero() || configuredTimeout.isNegative()) {
            return Duration.ZERO;
        }

        if (configuredTimeout.compareTo(MINIMUM_TIMEOUT) < 0) {
            return MINIMUM_TIMEOUT;
        } else  {
            return configuredTimeout;
        }
    }

    /**
     * Sets the proxy.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.core.http.httpurlconnection.HttpUrlConnectionAsyncClientBuilder.proxy#ProxyOptions -->
     * <pre>
     * final String proxyHost = &quot;&lt;proxy-host&gt;&quot;; &#47;&#47; e.g. localhost
     * final int proxyPort = 9999; &#47;&#47; Proxy port
     * ProxyOptions proxyOptions = new ProxyOptions&#40;ProxyOptions.Type.HTTP,
     *         new InetSocketAddress&#40;proxyHost, proxyPort&#41;&#41;;
     * HttpClient client = new HttpUrlConnectionAsyncClientBuilder&#40;&#41;
     *         .proxy&#40;proxyOptions&#41;
     *         .build&#40;&#41;;
     * </pre>
     * <!-- end com.azure.core.http.httpurlconnection.HttpUrlConnectionAsyncClientBuilder.proxy#ProxyOptions -->
     *
     * @param proxyOptions The proxy configuration to use.
     * @return the updated HttpUrlConnectionAsyncClientBuilder object
     */
    public HttpUrlConnectionAsyncClientBuilder proxy(ProxyOptions proxyOptions) {
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
     * @return The updated HttpUrlConnectionAsyncClientBuilder object.
     */
    public HttpUrlConnectionAsyncClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Build a HttpClient with current configurations.
     *
     * @return a {@link HttpClient}.
     */
    public HttpClient build() {
        Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration()
            : configuration;

        ProxyOptions buildProxyOptions = (proxyOptions == null)
            ? ProxyOptions.fromConfiguration(buildConfiguration)
            : proxyOptions;

        if (buildProxyOptions != null && buildProxyOptions.getUsername() != null) {
            Authenticator.setDefault(new ProxyAuthenticator(buildProxyOptions.getUsername(),
                buildProxyOptions.getPassword()));
        }

        if (buildProxyOptions != null && buildProxyOptions.getType() != ProxyOptions.Type.HTTP
            && buildProxyOptions.getType() != null) {
            throw new IllegalArgumentException("Invalid proxy");
        }

        return new HttpUrlConnectionAsyncClient(
            getTimeout(connectionTimeout, DEFAULT_CONNECT_TIMEOUT),
            getTimeout(readTimeout, DEFAULT_READ_TIMEOUT),
            getTimeout(writeTimeout, DEFAULT_WRITE_TIMEOUT),
            getTimeout(responseTimeout, DEFAULT_RESPONSE_TIMEOUT),
            buildProxyOptions);
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
