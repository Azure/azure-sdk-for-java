// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client.httpurlconnection;

import com.generic.core.http.client.HttpClient;
import com.generic.core.http.models.ProxyOptions;
import com.generic.core.util.configuration.Configuration;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Builder to configure and build an instance of the generic-core {@link HttpClient} type using the JDK HttpURLConnection,
 * first introduced in JDK 1.1.
 */
public class HttpUrlConnectionClientBuilder {

    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration DEFAULT_RESPONSE_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration MINIMUM_TIMEOUT = Duration.ofMillis(1);
    private Duration connectionTimeout;
    private Duration readTimeout;
    private ProxyOptions proxyOptions;
    private Configuration configuration;

    /**
     * HttpUrlConnectionAsyncClientBuilder.
     */
    public HttpUrlConnectionClientBuilder() {
    }

    /**
     * Sets the connection timeout for a request to be sent.
     * <p>
     * The connection timeout begins once the request attempts to connect to the remote host and finishes once the
     * connection is resolved.
     * <p>
     * If {@code connectionTimeout} is null, a 10-second timeout will be used, if it is a {@link Duration} less than
     * or equal to zero then no timeout will be applied. When applying the timeout the greatest of one millisecond
     * and the value of {@code connectTimeout} will be used.
     * <p>
     * By default, the connection timeout is 10 seconds.
     *
     * @param connectionTimeout Connect timeout duration.
     * @return The updated HttpUrlConnectionAsyncClientBuilder object.
     */
    public HttpUrlConnectionClientBuilder connectionTimeout(Duration connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    /**
     * Sets the read timeout duration used when reading the server response.
     * <p>
     * The read timeout begins once the first response read is triggered after the server response is received. This
     * timeout triggers periodically but won't fire its operation if another read operation has completed between when
     * the timeout is triggered and completes.
     * <p>
     * If {@code readTimeout} is null a 60-second timeout will be used, if it is a {@link Duration} less than or equal
     * to zero then no timeout period will be applied to response read. When applying the timeout the greatest of one
     * millisecond and the value of {@code readTimeout} will be used.
     *
     * @param readTimeout Read timeout duration.
     * @return The updated HttpUrlConnectionAsyncClientBuilder object.
     */
    public HttpUrlConnectionClientBuilder readTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    /**
     * Returns the timeout in milliseconds to use based on the passed Duration and default timeout.
     *
     * If the timeout is {@code null} the default timeout will be used. If the timeout is less than or equal to zero
     * no timeout will be used. If the timeout is less than one millisecond a timeout of one millisecond will be used.
     *
     * @param configuredTimeout The desired timeout Duration or null if using the default timeout.
     * @param defaultTimeout The default timeout Duration to be used if configuredTimeout is null.
     * @return The timeout in milliseconds
     */
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
     * Sets proxy configuration.
     *
     * @param proxyOptions The proxy configuration to use.
     * @return The updated HttpUrlConnectionAsyncClientBuilder object.
     */
    public HttpUrlConnectionClientBuilder proxy(ProxyOptions proxyOptions) {
        this.proxyOptions = proxyOptions;
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the HTTP client.
     * <p>
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}, use {@link Configuration} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to
     * @return The updated HttpUrlConnectionAsyncClientBuilder object.
     */
    public HttpUrlConnectionClientBuilder configuration(Configuration configuration) {
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

        return new HttpUrlConnectionClient(
            getTimeout(connectionTimeout, DEFAULT_CONNECT_TIMEOUT),
            getTimeout(readTimeout, DEFAULT_READ_TIMEOUT),
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
