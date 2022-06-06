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
    private static boolean DEFAULT_CLIENT_TELEMETRY_ENABLED = false;
    private static final Duration DEFAULT_NETWORK_REQUEST_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration DEFAULT_IDLE_CONNECTION_TIMEOUT = Duration.ofSeconds(60);
    private static final int DEFAULT_MAX_CONNECTION_POOL_SIZE = 1000;

    private boolean clientTelemetryEnabled;
    private final Duration httpNetworkRequestTimeout;
    private final int maxConnectionPoolSize;
    private final Duration idleHttpConnectionTimeout;
    private final ProxyOptions proxy;

    public ClientTelemetryConfig() {
        this.clientTelemetryEnabled = DEFAULT_CLIENT_TELEMETRY_ENABLED;
        this.httpNetworkRequestTimeout = DEFAULT_NETWORK_REQUEST_TIMEOUT;
        this.maxConnectionPoolSize = DEFAULT_MAX_CONNECTION_POOL_SIZE;
        this.idleHttpConnectionTimeout = DEFAULT_IDLE_CONNECTION_TIMEOUT;
        this.proxy = this.getProxyOptions();
    }

    public static ClientTelemetryConfig getDefaultConfig() {
        return new ClientTelemetryConfig();
    }

    public void setClientTelemetryEnabled(boolean clientTelemetryEnabled) {
        this.clientTelemetryEnabled = clientTelemetryEnabled;
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
                JsonProxyOptionsConfig proxyOptionsConfig = Utils.getSimpleObjectMapper().readValue(config, JsonProxyOptionsConfig.class);
                ProxyOptions.Type type = ProxyOptions.Type.valueOf(proxyOptionsConfig.type);

                if (type != ProxyOptions.Type.HTTP) {
                    throw new IllegalArgumentException("Only http proxy type is supported.");
                }

                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Enable proxy with type {}, host {}, port {}, userName {}, password length {}",
                            type,
                            proxyOptionsConfig.host,
                            proxyOptionsConfig.port,
                            proxyOptionsConfig.username,
                            proxyOptionsConfig.password != null ? proxyOptionsConfig.password.length() : -1
                        );
                }

                ProxyOptions proxyOptions = new ProxyOptions(
                    type,
                    new InetSocketAddress(proxyOptionsConfig.host, proxyOptionsConfig.port));

                if (!Strings.isNullOrEmpty(proxyOptionsConfig.username) ||
                    !Strings.isNullOrEmpty(proxyOptionsConfig.password)) {

                    proxyOptions.setCredentials(
                        proxyOptionsConfig.username != null ? proxyOptionsConfig.username : "",
                        proxyOptionsConfig.password != null ? proxyOptionsConfig.password : "");
                }

                return proxyOptions;
            } catch (JsonProcessingException e) {
                logger.error("Failed to parse client telemetry proxy option config", e);
            }
        }

        return null;
    }

    private static class JsonProxyOptionsConfig {
        @JsonProperty
        private String host;
        @JsonProperty
        private int port;
        @JsonProperty
        private String type;
        @JsonProperty
        private String username;
        @JsonProperty
        private String password;

        private JsonProxyOptionsConfig() {}
        private JsonProxyOptionsConfig(String host, int port, String type, String username, String password) {
            this.host = host;
            this.port = port;
            this.type = type;
            this.username = username;
            this.password = password;
        }
    }
}
