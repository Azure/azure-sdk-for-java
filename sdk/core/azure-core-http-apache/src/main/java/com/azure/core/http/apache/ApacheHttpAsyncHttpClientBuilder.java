// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.apache;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.async.MinimalHttpAsyncClient;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.nio.AsyncClientConnectionManager;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Objects;


/**
 * Builder class responsible for creating instance of {@link com.azure.core.http.HttpClient} backed by
 * Apache Http Client.
 */
public class ApacheHttpAsyncHttpClientBuilder {
    private static final ClientLogger LOGGER = new ClientLogger(ApacheHttpAsyncHttpClientBuilder.class);

//    private static final long MINIMUM_TIMEOUT = TimeUnit.MILLISECONDS.toMillis(1);
//    private static final long DEFAULT_CONNECT_TIMEOUT;
//    private static final long DEFAULT_WRITE_TIMEOUT;
//    private static final long DEFAULT_RESPONSE_TIMEOUT;
//    private static final long DEFAULT_READ_TIMEOUT;

    private final CloseableHttpAsyncClient apacheHttpClient;

    static {
        Configuration configuration = Configuration.getGlobalConfiguration();

//        DEFAULT_CONNECT_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration,
//            PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT, Duration.ofSeconds(10), LOGGER).toMillis();
//        DEFAULT_WRITE_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration, PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT,
//            Duration.ofSeconds(60), LOGGER).toMillis();
//
//        DEFAULT_RESPONSE_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration,
//            PROPERTY_AZURE_REQUEST_RESPONSE_TIMEOUT, Duration.ofSeconds(60), LOGGER).toMillis();
//
//        DEFAULT_READ_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration, PROPERTY_AZURE_REQUEST_READ_TIMEOUT,
//            Duration.ofSeconds(60), LOGGER).toMillis();
    }

    private ProxyOptions proxyOptions;
    private Configuration configuration;
    private AsyncClientConnectionManager connectionManager;
    private Boolean connectionManagerShared;
    private IOReactorConfig ioReactorConfig;
    private Http1Config h1Config;

    /**
     * Creates ApacheHttpAsyncHttpClientBuilder.
     */
    public ApacheHttpAsyncHttpClientBuilder() {
        apacheHttpClient = null;
    }

    /**
     * Creates ApacheHttpAsyncHttpClientBuilder from the builder of an existing Apache HttpClient.
     * @param apacheHttpClient
     */
    public ApacheHttpAsyncHttpClientBuilder(CloseableHttpAsyncClient apacheHttpClient) {
        this.apacheHttpClient = Objects.requireNonNull(apacheHttpClient, "'apacheHttpClient' cannot be null.");
    }

    /**
     * Sets the proxy.
     *
     * @param proxyOptions The proxy configuration to use.
     * @return The updated ApacheHttpAsyncHttpClientBuilder object.
     */
    public ApacheHttpAsyncHttpClientBuilder proxy(ProxyOptions proxyOptions) {
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
     * @return The updated ApacheHttpAsyncHttpClientBuilder object.
     */
    public ApacheHttpAsyncHttpClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    public ApacheHttpAsyncHttpClientBuilder connectionManager(AsyncClientConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        return this;
    }

    public ApacheHttpAsyncHttpClientBuilder connectionManagerShared(Boolean connectionManagerShared) {
        this.connectionManagerShared = connectionManagerShared;
        return this;
    }

    public ApacheHttpAsyncHttpClientBuilder h1Config(Http1Config h1Config) {
        this.h1Config = h1Config;
        return this;
    }

    public ApacheHttpAsyncHttpClientBuilder ioReactorConfig(IOReactorConfig ioReactorConfig) {
        this.ioReactorConfig = ioReactorConfig;
        return this;
    }

    /**
     * Creates a new Apache Http backed {@link com.azure.core.http.HttpClient} instance on every call, using the
     * configuration set in the builder at the time of the build method call.
     *
     * @return a new Apache-Http backed {@link com.azure.core.http.HttpClient} instance.
     */
    public HttpClient build() {
        if (apacheHttpClient != null) {
            return new ApacheHttpAsyncHttpClient(apacheHttpClient);
        }

        HttpAsyncClientBuilder httpClientBuilder = HttpAsyncClientBuilder.create();

        httpClientBuilder
            .disableAutomaticRetries()
            .disableRedirectHandling();

        // Configure operation timeouts.

        // If set use the configured connection pool.
        httpClientBuilder = httpClientBuilder.setConnectionManager(
            connectionManager != null ? connectionManager : PoolingAsyncClientConnectionManagerBuilder.create().build());

        // Shared or not connections pool
        if (connectionManagerShared != null) {
            httpClientBuilder.setConnectionManagerShared(connectionManagerShared);
        }

        // HTTP/1 configuration
        httpClientBuilder.setHttp1Config(h1Config != null ? h1Config : Http1Config.DEFAULT);

        // I/O reactor configuration parameters.
        httpClientBuilder.setIOReactorConfig(ioReactorConfig != null ? ioReactorConfig :
                                                 IOReactorConfig.custom()
                                                     .setSoTimeout(Timeout.ofSeconds(5))
                                                     .build());

        Configuration buildConfiguration = (configuration == null)
                                               ? Configuration.getGlobalConfiguration()
                                               : configuration;

        // Proxy setup
        ProxyOptions buildProxyOptions = (proxyOptions == null)
                                             ? ProxyOptions.fromConfiguration(buildConfiguration, true)
                                             : proxyOptions;

        if (buildProxyOptions != null) {
            final String username = buildProxyOptions.getUsername();
            final InetSocketAddress proxyAddress = buildProxyOptions.getAddress();

            if (!CoreUtils.isNullOrEmpty(username)) {
                final BasicCredentialsProvider basicCredentialsProvider = new BasicCredentialsProvider();
                basicCredentialsProvider.setCredentials(
                    new AuthScope(proxyAddress.getHostName(), proxyAddress.getPort()),
                    new UsernamePasswordCredentials(username,
                        buildProxyOptions.getPassword().toCharArray()));
                httpClientBuilder.setDefaultCredentialsProvider(basicCredentialsProvider);
            }

            final InetAddress inetAddress = proxyAddress.getAddress();
            if (proxyAddress != null && inetAddress != null) {
                httpClientBuilder.setProxy(new HttpHost(inetAddress));
            }
        }

        // Add the interceptor to remove "Content-Length" header. In the Apache RequestContent class, if the request
        // already has HttpHeaders.CONTENT_LENGTH header, it will throw "Content-Length header already present".
        httpClientBuilder.addRequestInterceptorFirst(new HttpRequestInterceptor() {
            /**
             * Processes a request.
             * On the client side, this step is performed before the request is
             * sent to the server. On the server side, this step is performed
             * on incoming messages before the message body is evaluated.
             *
             * @param request the request to process
             * @param entity the request entity details or {@code null} if not available
             * @param context the context for the request
             */
            @Override
            public void process(HttpRequest request, EntityDetails entity, HttpContext context) {
                request.removeHeaders("Content-Length");
            }
        });

        // Create an instance of CloseableHttpAsyncClient
        final CloseableHttpAsyncClient closeableHttpAsyncClient = httpClientBuilder.build();

        // Start the client
        closeableHttpAsyncClient.start();

        return new ApacheHttpAsyncHttpClient(closeableHttpAsyncClient);
    }
}
