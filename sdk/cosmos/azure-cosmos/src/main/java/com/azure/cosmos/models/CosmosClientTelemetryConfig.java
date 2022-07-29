// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.core.http.ProxyOptions;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.clienttelemetry.TagName;
import com.azure.cosmos.util.Beta;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Class with config options for Cosmos Client telemetry
 */
@Beta(value = Beta.SinceVersion.V4_34_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public class CosmosClientTelemetryConfig {
    private static Logger logger = LoggerFactory.getLogger(CosmosClientTelemetryConfig.class);
    private static boolean DEFAULT_CLIENT_TELEMETRY_ENABLED = false;
    private static final Duration DEFAULT_NETWORK_REQUEST_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration DEFAULT_IDLE_CONNECTION_TIMEOUT = Duration.ofSeconds(60);
    private static final int DEFAULT_MAX_CONNECTION_POOL_SIZE = 1000;

    private boolean clientTelemetryEnabled;
    private final Duration httpNetworkRequestTimeout;
    private final int maxConnectionPoolSize;
    private final Duration idleHttpConnectionTimeout;
    private final ProxyOptions proxy;
    private String clientCorrelationId = null;
    private EnumSet<TagName> metricTagNames = EnumSet.allOf(TagName.class);
    private MeterRegistry clientMetricRegistry = null;
    private boolean isClientMetricsEnabled = false;

    private CosmosClientTelemetryConfig() {
        this.clientTelemetryEnabled = DEFAULT_CLIENT_TELEMETRY_ENABLED;
        this.httpNetworkRequestTimeout = DEFAULT_NETWORK_REQUEST_TIMEOUT;
        this.maxConnectionPoolSize = DEFAULT_MAX_CONNECTION_POOL_SIZE;
        this.idleHttpConnectionTimeout = DEFAULT_IDLE_CONNECTION_TIMEOUT;
        this.proxy = this.getProxyOptions();
    }

    /**
     * Creates a new instance of the CosmosClientTelemetryConfig class with default values.
     * @return new CosmosClientTelemetryConfig instance with defautl values
     */
    static CosmosClientTelemetryConfig getDefaultConfig() {
        return new CosmosClientTelemetryConfig();
    }

    /**
     * Enables or disables sending Cosmos DB client telemetry to the Azure Cosmos DB Service
     * @param sendClientTelemetryToServiceEnabled a flag indicating whether sending client telemetry to the backend should be
     * enabled or not
     * @return current CosmosClientTelemetryConfig
     */
    @Beta(value = Beta.SinceVersion.V4_34_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosClientTelemetryConfig sendClientTelemetryToServiceEnabled(
        boolean sendClientTelemetryToServiceEnabled) {

        this.clientTelemetryEnabled = sendClientTelemetryToServiceEnabled;

        return this;
    }

    boolean isSendClientTelemetryToServiceEnabled() {
        return this.clientTelemetryEnabled;
    }

    MeterRegistry getClientMetricRegistry() {
        return this.clientMetricRegistry;
    }

    /**
     * Sets the client correlationId used for tags in metrics. While we strongly encourage usage of singleton
     * instances of CosmosClient there are cases when it is necessary to instantiate multiple CosmosClient instances -
     * for example when an application connects to multiple Cosmos accounts. The client correlationId is used to
     * distinguish client instances in metrics. By default an auto-incrementing number is used but with this method
     * you can define your own correlationId (for example an identifier for the account)
     *
     * @param clientCorrelationId the client correlationId to be used to identify this client instance in metrics
     * @return current CosmosClientTelemetryConfig
     */
    @Beta(value = Beta.SinceVersion.V4_34_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosClientTelemetryConfig clientCorrelationId(String clientCorrelationId) {
        this.clientCorrelationId = clientCorrelationId;
        return this;
    }

    String getClientCorrelationId() {
        return this.clientCorrelationId;
    }

    /**
     * Sets the tags that should be considered for metrics. By default all supported tags are used - and for most
     * use-cases that should be sufficient. But each tag/dimension adds some overhead when collecting the metrics -
     * especially for percentile calculations - so, when it is clear that a certain dimension is not needed, it can
     * be prevented from even considering it when collecting metrics.
     *
     * @param tagNames - a comma-separated list of tag names that should be considered
     * @return current CosmosClientTelemetryConfig
     */
    @Beta(value = Beta.SinceVersion.V4_34_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosClientTelemetryConfig metricTagNames(String tagNames) {
        if (Strings.isNullOrWhiteSpace(tagNames)) {
            this.metricTagNames = EnumSet.allOf(TagName.class);
        }

        Map<String, TagName> tagNameMap = new HashMap<>();
        for (TagName tagName : TagName.values()) {
            tagNameMap.put(tagName.toLowerCase(), tagName);
        }

        Stream<TagName> tagNameStream =
            Arrays.stream(tagNames.toLowerCase(Locale.ROOT).split(","))
                  .filter(tagName -> !Strings.isNullOrWhiteSpace(tagName))
                  .map(tagName -> {
                      String trimmedTagName = tagName.trim();

                      if (!tagNameMap.containsKey(trimmedTagName)) {

                          String validTagNames = String.join(
                              ", ",
                              (String[]) Arrays.stream(TagName.values()).map(tag -> tag.toString()).toArray());

                          throw new IllegalArgumentException(
                              String.format(
                                  "TagName '%s' is invalid. Valid tag names are:"
                                      + " %s",
                                  tagName,
                                  validTagNames));
                      }

                      return tagNameMap.get(trimmedTagName);
                  });

        EnumSet<TagName> newTagNames = EnumSet.noneOf(TagName.class);
        tagNameStream.forEach(tagName -> newTagNames.add(tagName));

        this.metricTagNames = newTagNames;

        return this;
    }

    EnumSet<TagName> getMetricTagNames() {
        return this.metricTagNames;
    }

    /**
     * Sets MetricRegistry to be used to emit client metrics
     *
     * @param clientMetricRegistry - the MetricRegistry to be used to emit client metrics
     * @return current CosmosClientTelemetryConfig
     */
    @Beta(value = Beta.SinceVersion.V4_34_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosClientTelemetryConfig clientMetrics(MeterRegistry clientMetricRegistry) {
        this.clientMetricRegistry = clientMetricRegistry;
        this.isClientMetricsEnabled = clientMetricRegistry != null;

        return this;
    }

    Duration getHttpNetworkRequestTimeout() {
        return this.httpNetworkRequestTimeout;
    }

    int getMaxConnectionPoolSize() {
        return this.maxConnectionPoolSize;
    }

    Duration getIdleHttpConnectionTimeout() {
        return this.idleHttpConnectionTimeout;
    }

    ProxyOptions getProxy() {
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

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.CosmosClientTelemetryConfigHelper.setCosmosClientTelemetryConfigAccessor(
            new ImplementationBridgeHelpers.CosmosClientTelemetryConfigHelper.CosmosClientTelemetryConfigAccessor() {

                @Override
                public Duration getHttpNetworkRequestTimeout(CosmosClientTelemetryConfig config) {
                    return config.getHttpNetworkRequestTimeout();
                }

                @Override
                public int getMaxConnectionPoolSize(CosmosClientTelemetryConfig config) {
                    return config.getMaxConnectionPoolSize();
                }

                @Override
                public Duration getIdleHttpConnectionTimeout(CosmosClientTelemetryConfig config) {
                    return config.getIdleHttpConnectionTimeout();
                }

                @Override
                public ProxyOptions getProxy(CosmosClientTelemetryConfig config) {
                    return config.getProxy();
                }

                @Override
                public EnumSet<TagName> getMetricTagNames(CosmosClientTelemetryConfig config) {
                    return config.getMetricTagNames();
                }

                @Override
                public String getClientCorrelationId(CosmosClientTelemetryConfig config) {
                    return config.getClientCorrelationId();
                }

                @Override
                public MeterRegistry getClientMetricRegistry(CosmosClientTelemetryConfig config) {
                    return config.getClientMetricRegistry();
                }

                @Override
                public boolean isSendClientTelemetryToServiceEnabled(CosmosClientTelemetryConfig config) {
                    return config.isSendClientTelemetryToServiceEnabled();
                }

                @Override
                public boolean isClientMetricsEnabled(CosmosClientTelemetryConfig config) {
                    return config.isClientMetricsEnabled;
                }

                @Override
                public CosmosClientTelemetryConfig getDefaultConfig() {
                    return CosmosClientTelemetryConfig.getDefaultConfig();
                }
            });
    }

    static { initialize(); }
}
