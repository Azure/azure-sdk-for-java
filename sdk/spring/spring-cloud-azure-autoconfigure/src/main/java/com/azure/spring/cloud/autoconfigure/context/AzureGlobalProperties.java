// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.spring.cloud.autoconfigure.properties.core.authentication.TokenCredentialConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.properties.core.client.ClientConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.properties.core.client.HttpLoggingConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.properties.core.profile.AzureProfileConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.properties.core.proxy.ProxyConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.properties.core.retry.RetryConfigurationProperties;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.properties.client.HeaderProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Azure global properties.
 */
@ConfigurationProperties(prefix = AzureGlobalProperties.PREFIX)
public class AzureGlobalProperties implements AzureProperties, RetryOptionsProvider {

    /**
     * Global properties prefix.
     */
    public static final String PREFIX = "spring.cloud.azure";

    @NestedConfigurationProperty
    private final GlobalClientConfigurationProperties client = new GlobalClientConfigurationProperties();

    @NestedConfigurationProperty
    private final GlobalProxyConfigurationProperties proxy = new GlobalProxyConfigurationProperties();

    @NestedConfigurationProperty
    private final GlobalRetryConfigurationProperties retry = new GlobalRetryConfigurationProperties();

    @NestedConfigurationProperty
    private final TokenCredentialConfigurationProperties credential = new TokenCredentialConfigurationProperties();

    @NestedConfigurationProperty
    private final AzureProfileConfigurationProperties profile = new AzureProfileConfigurationProperties();

    @Override
    public GlobalClientConfigurationProperties getClient() {
        return client;
    }

    @Override
    public GlobalProxyConfigurationProperties getProxy() {
        return proxy;
    }

    @Override
    public GlobalRetryConfigurationProperties getRetry() {
        return retry;
    }

    @Override
    public TokenCredentialConfigurationProperties getCredential() {
        return credential;
    }

    @Override
    public AzureProfileConfigurationProperties getProfile() {
        return profile;
    }

    /**
     * Global configurations for the transport client underneath.
     */
    public static final class GlobalClientConfigurationProperties extends ClientConfigurationProperties {

        private final GlobalHttpClientConfigurationProperties http = new GlobalHttpClientConfigurationProperties();
        private final GlobalAmqpClientConfigurationProperties amqp = new GlobalAmqpClientConfigurationProperties();

        /**
         * Gets the global HTTP client configuration properties.
         *
         * @return The global HTTP client configuration properties.
         */
        public GlobalHttpClientConfigurationProperties getHttp() {
            return http;
        }

        /**
         * Gets the global AMQP client configuration properties.
         *
         * @return The global AMQP client configuration properties.
         */
        public GlobalAmqpClientConfigurationProperties getAmqp() {
            return amqp;
        }
    }

    /**
     * Global configurations for proxy.
     */
    public static final class GlobalProxyConfigurationProperties extends ProxyConfigurationProperties {

        private final GlobalHttpProxyConfigurationProperties http = new GlobalHttpProxyConfigurationProperties();
        private final GlobalAmqpProxyConfigurationProperties amqp = new GlobalAmqpProxyConfigurationProperties();

        /**
         * Gets the global HTTP proxy configuration properties.
         *
         * @return The global HTTP proxy configuration properties.
         */
        public GlobalHttpProxyConfigurationProperties getHttp() {
            return http;
        }

        /**
         * Gets the global AMQP proxy configuration properties.
         *
         * @return The global AMQP proxy configuration properties.
         */
        public GlobalAmqpProxyConfigurationProperties getAmqp() {
            return amqp;
        }
    }

    /**
     * Global configurations for proxy.
     */
    public static final class GlobalRetryConfigurationProperties extends RetryConfigurationProperties {

        private final GlobalAmqpRetryConfigurationProperties amqp = new GlobalAmqpRetryConfigurationProperties();

        /**
         * Gets the global AMQP retry configuration properties.
         *
         * @return The global AMQP retry configuration properties.
         */
        public GlobalAmqpRetryConfigurationProperties getAmqp() {
            return amqp;
        }
    }

    /**
     * Retry properties only apply to amqp-based clients.
     */
    public static final class GlobalAmqpRetryConfigurationProperties {

        /**
         * How long to wait until a timeout.
         */
        private Duration tryTimeout;

        /**
         * Gets the try timeout.
         *
         * @return The try timeout.
         */
        public Duration getTryTimeout() {
            return tryTimeout;
        }

        /**
         * Sets the try timeout.
         *
         * @param tryTimeout The try timeout.
         */
        public void setTryTimeout(Duration tryTimeout) {
            this.tryTimeout = tryTimeout;
        }

    }

    /**
     * Proxy properties only apply to http-based clients.
     */
    public static final class GlobalHttpProxyConfigurationProperties {

        /**
         * A list of hosts or CIDR to not use proxy HTTP/HTTPS connections through.
         */
        private String nonProxyHosts;

        /**
         * Gets the non-proxy hosts.
         *
         * @return The non-proxy hosts.
         */
        public String getNonProxyHosts() {
            return nonProxyHosts;
        }

        /**
         * Sets the non-proxy hosts.
         *
         * @param nonProxyHosts The non-proxy hosts.
         */
        public void setNonProxyHosts(String nonProxyHosts) {
            this.nonProxyHosts = nonProxyHosts;
        }
    }

    /**
     * Proxy properties only apply to amqp-based clients.
     */
    public static final class GlobalAmqpProxyConfigurationProperties {

        /**
         * Authentication type used against the proxy.
         */
        private String authenticationType;

        /**
         * Gets the authentication type.
         *
         * @return The authentication type.
         */
        public String getAuthenticationType() {
            return authenticationType;
        }

        /**
         * Sets the authentication type.
         *
         * @param authenticationType The authentication type.
         */
        public void setAuthenticationType(String authenticationType) {
            this.authenticationType = authenticationType;
        }
    }

    /**
     * Transport properties for http-based clients.
     */
    public static final class GlobalHttpClientConfigurationProperties {
        /**
         * Amount of time each request being sent over the wire.
         */
        private Duration writeTimeout;
        /**
         * Amount of time used when waiting for a server to reply.
         */
        private Duration responseTimeout;
        /**
         * Amount of time used when reading the server response.
         */
        private Duration readTimeout;
        /**
         * Amount of time the request attempts to connect to the remote host and the connection is resolved.
         */
        private Duration connectTimeout;
        /**
         * Maximum connection pool size used by the underlying HTTP client.
         */
        private Integer maximumConnectionPoolSize;
        /**
         * Amount of time before an idle connection.
         */
        private Duration connectionIdleTimeout;

        /**
         * List of headers applied to each request sent with client.
         */
        private final List<HeaderProperties> headers = new ArrayList<>();

        @NestedConfigurationProperty
        private final HttpLoggingConfigurationProperties logging = new HttpLoggingConfigurationProperties();

        /**
         * Gets the write timeout.
         *
         * @return The write timeout.
         */
        public Duration getWriteTimeout() {
            return writeTimeout;
        }

        /**
         * Sets the write timeout.
         *
         * @param writeTimeout The write timeout.
         */
        public void setWriteTimeout(Duration writeTimeout) {
            this.writeTimeout = writeTimeout;
        }

        /**
         * Gets the response timeout.
         *
         * @return The response timeout.
         */
        public Duration getResponseTimeout() {
            return responseTimeout;
        }

        /**
         * Sets the response timeout.
         *
         * @param responseTimeout The response timeout.
         */
        public void setResponseTimeout(Duration responseTimeout) {
            this.responseTimeout = responseTimeout;
        }

        /**
         * Gets the read timeout.
         *
         * @return The read timeout.
         */
        public Duration getReadTimeout() {
            return readTimeout;
        }

        /**
         * Sets the read timeout.
         *
         * @param readTimeout The read timeout.
         */
        public void setReadTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
        }

        /**
         * Gets the connect timeout.
         *
         * @return The connect timeout.
         */
        public Duration getConnectTimeout() {
            return connectTimeout;
        }

        /**
         * Sets the connect timeout.
         *
         * @param connectTimeout The connect timeout.
         */
        public void setConnectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        /**
         * Gets the maximum connection pool size.
         *
         * @return The maximum connection pool size.
         */
        public Integer getMaximumConnectionPoolSize() {
            return maximumConnectionPoolSize;
        }

        /**
         * Sets the maximum connection pool size.
         *
         * @param maximumConnectionPoolSize The maximum connection pool size.
         */
        public void setMaximumConnectionPoolSize(Integer maximumConnectionPoolSize) {
            this.maximumConnectionPoolSize = maximumConnectionPoolSize;
        }

        /**
         * Gets the connection idle timeout.
         *
         * @return The connection idle timeout.
         */
        public Duration getConnectionIdleTimeout() {
            return connectionIdleTimeout;
        }

        /**
         * Sets the connection idle timeout.
         *
         * @param connectionIdleTimeout The connection idle timeout.
         */
        public void setConnectionIdleTimeout(Duration connectionIdleTimeout) {
            this.connectionIdleTimeout = connectionIdleTimeout;
        }

        /**
         * Gets the headers.
         *
         * @return The headers.
         */
        public List<HeaderProperties> getHeaders() {
            return headers;
        }

        /**
         * Gets the HTTP logging configuration properties.
         *
         * @return The HTTP logging configuration properties.
         */
        public HttpLoggingConfigurationProperties getLogging() {
            return logging;
        }
    }

    /**
     * Transport properties for amqp-based clients.
     */
    public static final class GlobalAmqpClientConfigurationProperties {

        /**
         * Transport type for AMQP-based client.
         */
        private AmqpTransportType transportType = AmqpTransportType.AMQP;

        /**
         * Gets the AMQP transport type.
         *
         * @return The AMQP transport type.
         */
        public AmqpTransportType getTransportType() {
            return transportType;
        }

        /**
         * Sets the AMQP transport type.
         *
         * @param transportType The AMQP transport type.
         */
        public void setTransportType(AmqpTransportType transportType) {
            this.transportType = transportType;
        }
    }
}
