// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.properties;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.authentication.TokenCredentialConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.client.ClientConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.client.HttpLoggingConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.profile.AzureProfileConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.proxy.ProxyConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.retry.RetryConfigurationProperties;
import com.azure.spring.core.properties.AzureProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 *
 */
@ConfigurationProperties(prefix = AzureGlobalProperties.PREFIX)
public class AzureGlobalProperties implements AzureProperties {

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

        public GlobalHttpClientConfigurationProperties getHttp() {
            return http;
        }

        public GlobalAmqpClientConfigurationProperties getAmqp() {
            return amqp;
        }
    }

    /**
     * Global configurations for proxy.
     */
    public static final class GlobalProxyConfigurationProperties extends ProxyConfigurationProperties {

        private final GlobalHttpProxyConfigurationProperties http = new GlobalHttpProxyConfigurationProperties();

        public GlobalHttpProxyConfigurationProperties getHttp() {
            return http;
        }
    }

    /**
     * Global configurations for proxy.
     */
    public static final class GlobalRetryConfigurationProperties extends RetryConfigurationProperties {

        private final GlobalHttpRetryConfigurationProperties http = new GlobalHttpRetryConfigurationProperties();

        public GlobalHttpRetryConfigurationProperties getHttp() {
            return http;
        }
    }

    /**
     * Retry properties only apply to http-based clients.
     */
    public static final class GlobalHttpRetryConfigurationProperties {

        /**
         * HTTP header, such as Retry-After or x-ms-retry-after-ms, to lookup for the retry delay.
         * If the value is null, will calculate the delay using backoff and ignore the delay provided in response header.
         */
        private String retryAfterHeader;
        /**
         * Time unit to use when applying the retry delay.
         */
        private ChronoUnit retryAfterTimeUnit;

        public String getRetryAfterHeader() {
            return retryAfterHeader;
        }

        public void setRetryAfterHeader(String retryAfterHeader) {
            this.retryAfterHeader = retryAfterHeader;
        }

        public ChronoUnit getRetryAfterTimeUnit() {
            return retryAfterTimeUnit;
        }

        public void setRetryAfterTimeUnit(ChronoUnit retryAfterTimeUnit) {
            this.retryAfterTimeUnit = retryAfterTimeUnit;
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

        public String getNonProxyHosts() {
            return nonProxyHosts;
        }

        public void setNonProxyHosts(String nonProxyHosts) {
            this.nonProxyHosts = nonProxyHosts;
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

        @NestedConfigurationProperty
        private final HttpLoggingConfigurationProperties logging = new HttpLoggingConfigurationProperties();

        public Duration getWriteTimeout() {
            return writeTimeout;
        }

        public void setWriteTimeout(Duration writeTimeout) {
            this.writeTimeout = writeTimeout;
        }

        public Duration getResponseTimeout() {
            return responseTimeout;
        }

        public void setResponseTimeout(Duration responseTimeout) {
            this.responseTimeout = responseTimeout;
        }

        public Duration getReadTimeout() {
            return readTimeout;
        }

        public void setReadTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
        }

        public Duration getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public Integer getMaximumConnectionPoolSize() {
            return maximumConnectionPoolSize;
        }

        public void setMaximumConnectionPoolSize(Integer maximumConnectionPoolSize) {
            this.maximumConnectionPoolSize = maximumConnectionPoolSize;
        }

        public Duration getConnectionIdleTimeout() {
            return connectionIdleTimeout;
        }

        public void setConnectionIdleTimeout(Duration connectionIdleTimeout) {
            this.connectionIdleTimeout = connectionIdleTimeout;
        }

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

        public AmqpTransportType getTransportType() {
            return transportType;
        }

        public void setTransportType(AmqpTransportType transportType) {
            this.transportType = transportType;
        }
    }
}
