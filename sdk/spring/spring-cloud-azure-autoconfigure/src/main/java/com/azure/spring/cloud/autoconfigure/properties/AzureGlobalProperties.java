// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.properties;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.spring.cloud.autoconfigure.properties.core.authentication.TokenCredentialCP;
import com.azure.spring.cloud.autoconfigure.properties.core.client.ClientCP;
import com.azure.spring.cloud.autoconfigure.properties.core.client.HttpLoggingCP;
import com.azure.spring.cloud.autoconfigure.properties.core.profile.AzureProfileCP;
import com.azure.spring.cloud.autoconfigure.properties.core.proxy.ProxyCP;
import com.azure.spring.cloud.autoconfigure.properties.core.retry.RetryCP;
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
    private final GlobalClientCP client = new GlobalClientCP();

    @NestedConfigurationProperty
    private final GlobalProxyCP proxy = new GlobalProxyCP();

    @NestedConfigurationProperty
    private final GlobalRetryCP retry = new GlobalRetryCP();

    @NestedConfigurationProperty
    private final TokenCredentialCP credential = new TokenCredentialCP();

    @NestedConfigurationProperty
    private final AzureProfileCP profile = new AzureProfileCP();

    @Override
    public GlobalClientCP getClient() {
        return client;
    }

    @Override
    public GlobalProxyCP getProxy() {
        return proxy;
    }

    @Override
    public GlobalRetryCP getRetry() {
        return retry;
    }

    @Override
    public TokenCredentialCP getCredential() {
        return credential;
    }

    @Override
    public AzureProfileCP getProfile() {
        return profile;
    }

    /**
     * Global configurations for the transport client underneath.
     */
    public static final class GlobalClientCP extends ClientCP {

        private final HttpClientCP http = new HttpClientCP();
        private final AmqpClientCP amqp = new AmqpClientCP();

        public HttpClientCP getHttp() {
            return http;
        }

        public AmqpClientCP getAmqp() {
            return amqp;
        }
    }

    /**
     * Global configurations for proxy.
     */
    public static final class GlobalProxyCP extends ProxyCP {

        private final HttpProxyCP http = new HttpProxyCP();

        public HttpProxyCP getHttp() {
            return http;
        }
    }

    /**
     * Global configurations for proxy.
     */
    public static final class GlobalRetryCP extends RetryCP {

        private final HttpRetryCP http = new HttpRetryCP();

        public HttpRetryCP getHttp() {
            return http;
        }
    }

    /**
     * Retry properties only apply to http-based clients.
     */
    public static final class HttpRetryCP {

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
    public static final class HttpProxyCP {

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
    public static final class HttpClientCP {
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
        private final HttpLoggingCP logging = new HttpLoggingCP();

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

        public HttpLoggingCP getLogging() {
            return logging;
        }
    }

    /**
     * Transport properties for amqp-based clients.
     */
    public static final class AmqpClientCP {

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
