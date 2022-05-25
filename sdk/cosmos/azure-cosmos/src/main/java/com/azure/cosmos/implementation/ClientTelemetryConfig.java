// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.core.http.ProxyOptions;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.time.Duration;

public class ClientTelemetryConfig {
    private static Logger logger = LoggerFactory.getLogger(ClientTelemetryConfig.class);

    private static final Duration DEFAULT_NETWORK_REQUEST_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration DEFAULT_IDLE_CONNECTION_TIMEOUT = Duration.ofSeconds(60);
    private static final int DEFAULT_MAX_CONNECTION_POOL_SIZE = 1000;

    private final boolean clientTelemetryEnabled;
    private final Duration httpNetworkRequestTimeout;
    private final int maxConnectionPoolSize;
    private final Duration idleHttpConnectionTimeout;
    private final ProxyOptions proxy;

    public ClientTelemetryConfig(boolean clientTelemetryEnabled) {
        this.clientTelemetryEnabled = clientTelemetryEnabled;
        this.httpNetworkRequestTimeout = DEFAULT_NETWORK_REQUEST_TIMEOUT;
        this.maxConnectionPoolSize = DEFAULT_MAX_CONNECTION_POOL_SIZE;
        this.idleHttpConnectionTimeout = DEFAULT_IDLE_CONNECTION_TIMEOUT;
        this.proxy = this.getProxyOptions();
    }

    public boolean isClientTelemetryEnabled() {
        return this.clientTelemetryEnabled;
    }

    public Duration getHttpNetworkRequestTimeout() {
        return this.httpNetworkRequestTimeout;
    }

    public int getMaxConnectionPoolSize() {
        return this.maxConnectionPoolSize;
    }

    public Duration getIdleHttpConnectionTimeout() {
        return this.idleHttpConnectionTimeout;
    }

    public ProxyOptions getProxy() {
        return this.proxy;
    }

    private ProxyOptions getProxyOptions() {
        String config = Configs.getClientTelemetryProxyOptionsConfig();

        if (StringUtils.isNotEmpty(config)) {
            try {
                ProxyOptionsConfig proxyOptionsConfig = Utils.getSimpleObjectMapper().readValue(config, ProxyOptionsConfig.class);
                ProxyOptions.Type type = ProxyOptions.Type.valueOf(proxyOptionsConfig.type);

                if (type != ProxyOptions.Type.HTTP) {
                    throw new IllegalArgumentException("Only http proxy type is supported.");
                }

                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Enable proxy with type {}, host {}, port {}",
                            type,
                            proxyOptionsConfig.host,
                            proxyOptionsConfig.port);
                }

                return new ProxyOptions(type, new InetSocketAddress(proxyOptionsConfig.host, proxyOptionsConfig.port));
            } catch (JsonMappingException e) {
                logger.error("Failed to parse client telemetry proxy option config", e);
            } catch (JsonProcessingException e) {
                logger.error("Failed to parse client telemetry proxy option config", e);
            }
        }

        return null;
    }

    private static class ProxyOptionsConfig {
        @JsonProperty
        private String host;
        @JsonProperty
        private int port;
        @JsonProperty
        private String type;

        private ProxyOptionsConfig() {}
        private ProxyOptionsConfig(String host, int port, String type) {
            this.host = host;
            this.port = port;
            this.type = type;
        }
    }
}
