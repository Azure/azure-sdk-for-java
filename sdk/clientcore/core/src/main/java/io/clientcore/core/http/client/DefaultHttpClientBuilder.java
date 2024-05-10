// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.client;

import io.clientcore.core.http.models.ProxyOptions;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.configuration.Configuration;

import javax.net.ssl.SSLSocketFactory;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.time.Duration;

/**
 * Builder to configure and build an instance of the core {@link HttpClient} type using the JDK
 * HttpURLConnection, first introduced in JDK 1.1.
 */
public class DefaultHttpClientBuilder {
    private static final ClientLogger LOGGER = new ClientLogger(DefaultHttpClientBuilder.class);
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration MINIMUM_TIMEOUT = Duration.ofMillis(1);

    private Configuration configuration;
    private Duration connectionTimeout;
    private Duration readTimeout;
    private ProxyOptions proxyOptions;
    private SSLSocketFactory sslSocketFactory;

    /**
     * Creates a new instance of the builder with no set configuration.
     */
    public DefaultHttpClientBuilder() {
    }

    /**
     * Sets the connection timeout for a request to be sent.
     *
     * <p>The connection timeout begins once the request attempts to connect to the remote host and finishes once the
     * connection is resolved.</p>
     *
     * <p>If {@code connectionTimeout} is null, a 10-second timeout will be used, if it is a {@link Duration} less than or
     * equal to zero then no timeout will be applied. When applying the timeout the greatest of one millisecond and the
     * value of {@code connectTimeout} will be used.</p>
     *
     * <p>By default, the connection timeout is 10 seconds.</p>
     *
     * @param connectionTimeout Connect timeout {@link Duration}.
     *
     * @return The updated {@link DefaultHttpClientBuilder} object.
     */
    public DefaultHttpClientBuilder connectionTimeout(Duration connectionTimeout) {
        this.connectionTimeout = connectionTimeout;

        return this;
    }

    /**
     * Sets the read timeout duration used when reading the server response.
     *
     * <p>The read timeout begins once the first response read is triggered after the server response is received. This
     * timeout triggers periodically but won't fire its operation if another read operation has completed between when
     * the timeout is triggered and completes.</p>
     *
     * <p>If {@code readTimeout} is null a 60-second timeout will be used, if it is a {@link Duration} less than or equal
     * to zero then no timeout period will be applied to response read. When applying the timeout the greatest of one
     * millisecond and the value of {@code readTimeout} will be used.</p>
     *
     * @param readTimeout Read timeout duration.
     *
     * @return The updated {@link DefaultHttpClientBuilder} object.
     */
    public DefaultHttpClientBuilder readTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;

        return this;
    }

    /**
     * Returns the timeout in milliseconds to use based on the passed {@link Duration} and default timeout.
     *
     * <p>If the timeout is {@code null} the default timeout will be used. If the timeout is less than or equal to
     * zero no timeout will be used. If the timeout is less than one millisecond a timeout of one millisecond will be
     * used.</p>
     *
     * @param configuredTimeout The desired timeout {@link Duration} or null if using the default timeout.
     * @param defaultTimeout The default timeout {@link Duration} to be used if {@code configuredTimeout} is
     * {@code null}.
     *
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
        } else {
            return configuredTimeout;
        }
    }

    /**
     * Sets proxy configuration.
     *
     * @param proxyOptions The proxy configuration to use.
     *
     * @return The updated {@link DefaultHttpClientBuilder} object.
     */
    public DefaultHttpClientBuilder proxy(ProxyOptions proxyOptions) {
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
     * @return The updated {@link DefaultHttpClientBuilder} object.
     */
    public DefaultHttpClientBuilder sslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;

        return this;
    }

    /**
     * Sets the {@link Configuration} store to configure the {@link HttpClient} during construction.
     *
     * <p>The default configuration store is a clone of the
     * {@link Configuration#getGlobalConfiguration() global configuration store}, use your own {@link Configuration}
     * instance to bypass using global configuration settings during construction.</p>
     *
     * @param configuration The {@link Configuration} store used to configure the {@link HttpClient} during
     * construction.
     *
     * @return The updated {@link DefaultHttpClientBuilder} object.
     */
    public DefaultHttpClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;

        return this;
    }

    /**
     * Creates a new {@link HttpClient} instance on every call, using the configuration set in this builder at the time
     * of the {@code build()} method call.
     *
     * @return A new {@link HttpClient} instance.
     */
    public HttpClient build() {
        Configuration buildConfiguration =
            (configuration == null) ? Configuration.getGlobalConfiguration() : configuration;

        ProxyOptions buildProxyOptions =
            (proxyOptions == null) ? ProxyOptions.fromConfiguration(buildConfiguration) : proxyOptions;

        if (buildProxyOptions != null && buildProxyOptions.getUsername() != null) {
            Authenticator.setDefault(new ProxyAuthenticator(buildProxyOptions.getUsername(),
                buildProxyOptions.getPassword()));
        }

        if (buildProxyOptions != null
            && buildProxyOptions.getType() != ProxyOptions.Type.HTTP
            && buildProxyOptions.getType() != null) {

            throw LOGGER.logThrowableAsError(
                new IllegalArgumentException("Invalid proxy type. Only HTTP proxies are supported."));
        }

        return new DefaultHttpClient(getTimeout(connectionTimeout, DEFAULT_CONNECT_TIMEOUT),
            getTimeout(readTimeout, DEFAULT_READ_TIMEOUT), buildProxyOptions, sslSocketFactory);
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
