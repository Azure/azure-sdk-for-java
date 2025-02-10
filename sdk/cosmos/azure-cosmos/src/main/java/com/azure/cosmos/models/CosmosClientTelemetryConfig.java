// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.core.http.ProxyOptions;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.MetricsOptions;
import com.azure.core.util.TracingOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProvider;
import com.azure.cosmos.CosmosDiagnosticsHandler;
import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.implementation.*;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.clienttelemetry.ClientTelemetry;
import com.azure.cosmos.implementation.clienttelemetry.CosmosMeterOptions;
import com.azure.cosmos.implementation.clienttelemetry.MetricCategory;
import com.azure.cosmos.implementation.clienttelemetry.TagName;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Class with config options for Cosmos Client telemetry
 */
public final class CosmosClientTelemetryConfig {
    private static final Logger logger = LoggerFactory.getLogger(CosmosClientTelemetryConfig.class);
    private static final Duration DEFAULT_NETWORK_REQUEST_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration DEFAULT_IDLE_CONNECTION_TIMEOUT = Duration.ofSeconds(60);
    private static final int DEFAULT_MAX_CONNECTION_POOL_SIZE = 1000;

    private Boolean clientTelemetryEnabled;
    private final Duration httpNetworkRequestTimeout;
    private final int maxConnectionPoolSize;
    private final Duration idleHttpConnectionTimeout;
    private final ProxyOptions proxy;
    private String clientCorrelationId = null;
    private EnumSet<TagName> metricTagNamesOverride = null;
    private boolean isClientMetricsEnabled = false;
    private final HashSet<CosmosDiagnosticsHandler> customDiagnosticHandlers;
    private final CopyOnWriteArrayList<CosmosDiagnosticsHandler> diagnosticHandlers;
    private boolean useLegacyOpenTelemetryTracing = Configs.useLegacyTracing();
    private boolean isTransportLevelTracingEnabled = false;
    private Tag clientCorrelationTag;
    private String accountName;
    private ClientTelemetry clientTelemetry;

    private Boolean effectiveIsClientTelemetryEnabled = null;
    private CosmosMicrometerMetricsOptions micrometerMetricsOptions = null;
    private CosmosDiagnosticsThresholds diagnosticsThresholds = new CosmosDiagnosticsThresholds();
    private Tracer tracer;
    private TracingOptions tracingOptions;

    private double samplingRate;
    
    private ShowQueryMode showQueryMode = ShowQueryMode.NONE;

    /**
     * Instantiates a new Cosmos client telemetry configuration.
     */
    public CosmosClientTelemetryConfig() {
        this.clientTelemetryEnabled = null;
        this.effectiveIsClientTelemetryEnabled = null;
        this.httpNetworkRequestTimeout = DEFAULT_NETWORK_REQUEST_TIMEOUT;
        this.maxConnectionPoolSize = DEFAULT_MAX_CONNECTION_POOL_SIZE;
        this.idleHttpConnectionTimeout = DEFAULT_IDLE_CONNECTION_TIMEOUT;
        this.proxy = this.getProxyOptions();
        this.customDiagnosticHandlers = new HashSet<>();
        this.diagnosticHandlers = new CopyOnWriteArrayList<>();
        this.tracer = null;
        this.tracingOptions = null;
        this.samplingRate = Configs.getMetricsConfig().getSampleRate();
        CosmosMicrometerMetricsOptions defaultMetricsOptions = new CosmosMicrometerMetricsOptions();
        this.isClientMetricsEnabled = defaultMetricsOptions.isEnabled();
        if (this.isClientMetricsEnabled) {
            this.micrometerMetricsOptions = defaultMetricsOptions;
        }
    }

    /**
     * Enables or disables sending Cosmos DB client telemetry to the Azure Cosmos DB Service
     * @param enabled a flag indicating whether sending client telemetry to the backend should be
     * enabled or not
     * @return current CosmosClientTelemetryConfig
     */
    public CosmosClientTelemetryConfig sendClientTelemetryToService(boolean enabled) {
        this.clientTelemetryEnabled = enabled;

        return this;
    }

    Boolean isSendClientTelemetryToServiceEnabled() {
        Boolean effectiveSnapshot = this.effectiveIsClientTelemetryEnabled;
        Boolean currentSnapshot = this.clientTelemetryEnabled;

        return  effectiveSnapshot != null ? effectiveSnapshot : currentSnapshot;
    }

    void resetIsSendClientTelemetryToServiceEnabled() {
        this.clientTelemetryEnabled = null;
    }

    void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    String getAccountName() {
        return this.accountName;
    }

    void setClientCorrelationTag(Tag clientCorrelationTag) {
        this.clientCorrelationTag = clientCorrelationTag;
    }

    Tag getClientCorrelationTag() {
        return this.clientCorrelationTag;
    }

    void setUseLegacyOpenTelemetryTracing(boolean useLegacyTracing) {
        this.useLegacyOpenTelemetryTracing = useLegacyTracing;
    }

    /**
     * Sets MetricsOptions to be used to emit client metrics
     *
     * @param clientMetricsOptions - the client MetricsOptions - NOTE: for now only
     * CosmosMicrometerMetricsOptions are supported
     * @return current CosmosClientTelemetryConfig
     */
    public CosmosClientTelemetryConfig metricsOptions(MetricsOptions clientMetricsOptions) {
        checkNotNull(clientMetricsOptions, "expected non-null clientMetricsOptions");

        if (! (clientMetricsOptions instanceof CosmosMicrometerMetricsOptions)) {
            throw new IllegalArgumentException(
                "Currently only MetricsOptions of type CosmosMicrometerMetricsOptions are supported");
        }

        CosmosMicrometerMetricsOptions candidate = (CosmosMicrometerMetricsOptions)clientMetricsOptions;
        if (this.metricTagNamesOverride != null &&
            !this.metricTagNamesOverride.equals(candidate.getDefaultTagNames())) {

            if (TagName.DEFAULT_TAGS.equals(candidate.getDefaultTagNames())) {
                candidate.configureDefaultTagNames(this.metricTagNamesOverride);
            } else {
                throw new IllegalArgumentException(
                    "Tags for meters cannot be specified via the deprecated CosmosClientTelemetryConfig " +
                        "when they are also specified in CosmosMicrometerMetricOptions.");
            }
        }

        this.micrometerMetricsOptions = candidate;
        this.isClientMetricsEnabled = micrometerMetricsOptions.isEnabled();

        return this;
    }

    MeterRegistry getClientMetricRegistry() {
        if (this.micrometerMetricsOptions == null) {
            return null;
        }

        return this.micrometerMetricsOptions.getClientMetricRegistry();
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
    public CosmosClientTelemetryConfig clientCorrelationId(String clientCorrelationId) {
        this.clientCorrelationId = clientCorrelationId;
        return this;
    }

    /**
     * Request diagnostics for operations will be logged if their latency, request charge or payload size exceeds
     * one of the defined thresholds. This method can be used to customize the default thresholds, which are used
     * across different types of diagnostics (logging, tracing, client telemetry).
     * @param thresholds the default thresholds across all diagnostic types
     * @return current CosmosClientTelemetryConfig
     */
    public CosmosClientTelemetryConfig diagnosticsThresholds(CosmosDiagnosticsThresholds thresholds) {
        checkNotNull(thresholds, "Argument 'thresholds' must not be null.");
        this.diagnosticsThresholds = thresholds;

        return this;
    }

    /**
     * Sets the Tracer to trace Cosmos DB operations and requests. If not specified the tracer will be
     * created via a TracerProvider from the class path - if any exists. This means if a TracerProvider is available
     * for example because the applicationinsights-agent java-agent is on the class path, a tracer will be created
     * automatically.
     *
     * @param tracer The Tracer instance.
     * @return current CosmosClientTelemetryConfig
     */
    CosmosClientTelemetryConfig tracer(Tracer tracer) {
        this.tracer = tracer;
        return this;
    }

    /**
     * Sets {@link TracingOptions} that are applied to each tracing reported by the client.
     * Use tracing options to enable and disable tracing or pass implementation-specific configuration.
     *
     * @param tracingOptions instance of {@link TracingOptions} to set.
     * @return The updated {@link ClientOptions} object.
     */
    public CosmosClientTelemetryConfig tracingOptions(TracingOptions tracingOptions) {
        this.tracingOptions = tracingOptions;
        return this;
    }

    CosmosDiagnosticsThresholds getDiagnosticsThresholds() {
        return this.diagnosticsThresholds;
    }

    String getClientCorrelationId() {
        return this.clientCorrelationId;
    }

    List<CosmosDiagnosticsHandler> getDiagnosticHandlers() {
        ArrayList<CosmosDiagnosticsHandler> snapshot = new ArrayList<>(this.diagnosticHandlers);
        snapshot.addAll(this.customDiagnosticHandlers);
        return snapshot;
    }

    /**
     * Sets the tags that should be considered for metrics. By default all supported tags are used - and for most
     * use-cases that should be sufficient. But each tag/dimension adds some overhead when collecting the metrics -
     * especially for percentile calculations - so, when it is clear that a certain dimension is not needed, it can
     * be prevented from even considering it when collecting metrics.
     *
     * @param tagNames - a comma-separated list of tag names that should be considered
     * @return current CosmosClientTelemetryConfig
     *
     * @deprecated Use {@link CosmosMicrometerMetricsOptions#configureDefaultTagNames(CosmosMetricTagName...)} or
     * {@link CosmosMicrometerMeterOptions#suppressTagNames(CosmosMetricTagName...)} instead.
     */
    @Deprecated
    public CosmosClientTelemetryConfig metricTagNames(String... tagNames) {
        if (tagNames == null || tagNames.length == 0) {
            this.metricTagNamesOverride = TagName.DEFAULT_TAGS.clone();
        }

        Map<String, TagName> tagNameMap = new HashMap<>();
        for (TagName tagName : TagName.values()) {
            tagNameMap.put(tagName.toLowerCase(), tagName);
        }

        Stream<TagName> tagNameStream =
            Arrays.stream(tagNames)
                  .filter(tagName -> !Strings.isNullOrWhiteSpace(tagName))
                  .map(rawTagName -> rawTagName.toLowerCase(Locale.ROOT))
                  .map(tagName -> {
                      String trimmedTagName = tagName.trim();

                      if (!tagNameMap.containsKey(trimmedTagName)) {

                          String validTagNames = String.join(
                              ", ",
                              (String[]) Arrays.stream(TagName.values()).map(TagName::toString).toArray());

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
        tagNameStream.forEach(newTagNames::add);

        this.metricTagNamesOverride = newTagNames;

        return this;
    }

    private CosmosClientTelemetryConfig setEffectiveIsClientTelemetryEnabled(
        boolean effectiveIsClientTelemetryEnabled) {

        this.effectiveIsClientTelemetryEnabled = effectiveIsClientTelemetryEnabled;
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

    /**
     * Injects a custom diagnostics handler
     * @param handler the custom diagnostics handler.
     * @return current CosmosClientTelemetryConfig
     */
    public CosmosClientTelemetryConfig diagnosticsHandler(CosmosDiagnosticsHandler handler) {
        checkNotNull(handler, "Argument 'handler' must not be null.");
        this.customDiagnosticHandlers.add(handler);
        return this;
    }

    /**
     * Enables transport level tracing. By default, transport-level tracing is not enabled - but
     * when operations fail or exceed thresholds the diagnostics are traced. Enabling transport level tracing
     * can be useful when latency is still beneath the defined thresholds.
     * @return current CosmosClientTelemetryConfig
     */
    public CosmosClientTelemetryConfig enableTransportLevelTracing() {
        this.isTransportLevelTracingEnabled = true;
        return this;
    }
    
    /**
     * Enables printing query in db.statement attribute and diagnostic logs. By default, query is not printed.
     * Users have the option to enable printing parameterized or all queries, 
     * but has to beware that customer data may be shown when the later option is chosen
     * It's the user's responsibility to sanitize the queries if necessary.
     * @param showQueryMode the mode for printing none, parameterized or all of the query statements
     * @return current CosmosClientTelemetryConfig
     */
    public CosmosClientTelemetryConfig showQueryMode(ShowQueryMode showQueryMode) {
        this.showQueryMode = showQueryMode;
        return this;
    }

    /**
     * Can be used to enable sampling for capturing all diagnostics to reduce/disable any client resource
     * overhead (CPU and/or memory). The sampling rate can for example be reduced when an application has high CPU
     * usage to reduce overhead for capturing diagnostics temporarily.
     * The sampling is applied to operations in the SDK - so, a single operation is either sampled out completely or
     * all diagnostics (logs, tracing, metrics depending on what diagnostics are enabled) are captured. The main
     * motivation for applying sampling for an entire operation is that a significant part of the CPU overhead happens
     * when injecting the Context into the reactor pipeline - and the CPU usage reduction would be lower when sampling
     * out metrics on one operation (but still capture traces) and traces on another operation (but still capturing
     * metrics). In this case both operations would still have significant overhead - so, it is more efficient to
     * cover both metrics and traces for one operation and disable diagnostics completely for the second operation.
     * It also makes it easier to correlate metrics and traces for example when sampling is applied on the
     * operation-level as described above.
     * @param samplingRate the sampling rate - 0 means no diagnostics will be capture at all, 1 means no sampling
     * applies and all diagnostics are captured.
     * @return current CosmosClientTelemetryConfig
     */
    public CosmosClientTelemetryConfig sampleDiagnostics(double samplingRate) {
        checkArgument(0 <= samplingRate && samplingRate <= 1,
            "The samplingRate must be between 0 and 1 (both inclusive).");
        this.samplingRate = samplingRate;
        return this;
    }

    @Override
    public String toString() {

        String handlers = "()";
        if (!this.customDiagnosticHandlers.isEmpty()) {
            handlers = "(" + this.customDiagnosticHandlers
                .stream()
                .map(h -> h.getClass().getCanonicalName())
                .collect(Collectors.joining(", ")) + ")";
        }

        return "{" +
            "samplingRate=" + this.samplingRate +
            ", thresholds=" + this.diagnosticsThresholds +
            ", clientCorrelationId=" + this.clientCorrelationId +
            ", clientTelemetryEnabled=" + this.effectiveIsClientTelemetryEnabled +
            ", clientMetricsEnabled=" + this.isClientMetricsEnabled +
            ", transportLevelTracingEnabled=" + this.isTransportLevelTracingEnabled +
            ", showQueryMode=" + this.showQueryMode +
            ", customTracerProvided=" + (this.tracer != null) +
            ", customDiagnosticHandlers=" + handlers +
            "}";
    }

    Tracer getOrCreateTracer() {
        if (this.tracer != null) {
            return this.tracer;
        }

        return TracerProvider.getDefaultProvider().createTracer(
            "azure-cosmos",
            HttpConstants.Versions.getSdkVersion(),
            DiagnosticsProvider.RESOURCE_PROVIDER_NAME,
            tracingOptions);
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
                public EnumSet<MetricCategory> getMetricCategories(CosmosClientTelemetryConfig config) {
                    return config.micrometerMetricsOptions.getMetricCategories();
                }

                @Override
                public EnumSet<TagName> getMetricTagNames(CosmosClientTelemetryConfig config) {
                    return config.micrometerMetricsOptions.getDefaultTagNames();
                }

                @Override
                public CosmosMeterOptions getMeterOptions(
                    CosmosClientTelemetryConfig config,
                    CosmosMetricName name) {
                    if (config != null &&
                        config.micrometerMetricsOptions != null) {

                        return config.micrometerMetricsOptions.getMeterOptions(name);
                    }

                    return createDisabledMeterOptions(name);
                }

                @Override
                public CosmosMeterOptions createDisabledMeterOptions(CosmosMetricName name) {
                    return new CosmosMeterOptions(
                        name,
                        false,
                        new double[0],
                        false,
                        EnumSet.noneOf(TagName.class),
                        false);
                }

                @Override
                public String getClientCorrelationId(CosmosClientTelemetryConfig config) {
                    return config.getClientCorrelationId();
                }

                @Override
                public MeterRegistry getClientMetricRegistry(CosmosClientTelemetryConfig config) {
                    if (!config.isClientMetricsEnabled) {
                        return null;
                    }

                    return config.getClientMetricRegistry();
                }

                @Override
                public Boolean isSendClientTelemetryToServiceEnabled(CosmosClientTelemetryConfig config) {
                    return config.isSendClientTelemetryToServiceEnabled();
                }

                @Override
                public boolean isClientMetricsEnabled(CosmosClientTelemetryConfig config) {
                    return config.isClientMetricsEnabled;
                }

                @Override
                public CosmosClientTelemetryConfig createSnapshot(
                    CosmosClientTelemetryConfig config,
                    boolean effectiveIsClientTelemetryEnabled) {

                    return config.setEffectiveIsClientTelemetryEnabled(effectiveIsClientTelemetryEnabled);
                }

                @Override
                public Collection<CosmosDiagnosticsHandler> getDiagnosticHandlers(CosmosClientTelemetryConfig config) {
                    return config.getDiagnosticHandlers();
                }

                @Override
                public void setAccountName(CosmosClientTelemetryConfig config, String accountName) {
                    config.setAccountName(accountName);
                }

                @Override
                public String getAccountName(CosmosClientTelemetryConfig config) {
                    return config.getAccountName();
                }

                @Override
                public void setClientCorrelationTag(CosmosClientTelemetryConfig config, Tag clientCorrelationTag) {
                    config.setClientCorrelationTag(clientCorrelationTag);
                }

                @Override
                public Tag getClientCorrelationTag(CosmosClientTelemetryConfig config) {
                    return config.getClientCorrelationTag();
                }

                @Override
                public void setClientTelemetry(CosmosClientTelemetryConfig config, ClientTelemetry clientTelemetry) {
                    config.clientTelemetry = clientTelemetry;
                }

                @Override
                public ClientTelemetry getClientTelemetry(CosmosClientTelemetryConfig config) {
                    return config.clientTelemetry;
                }

                @Override
                public void addDiagnosticsHandler(CosmosClientTelemetryConfig config,
                                                  CosmosDiagnosticsHandler handler) {

                    for (CosmosDiagnosticsHandler existingHandler : config.diagnosticHandlers) {
                        if (existingHandler.getClass().getCanonicalName().equals(handler.getClass().getCanonicalName())) {
                            // Handler already had been added - this can happen for example when multiple
                            // Cosmos(Async)Clients are created from a single CosmosClientBuilder.
                            return;
                        }
                    }
                    config.diagnosticHandlers.add(handler);
                }

                @Override
                public void resetIsSendClientTelemetryToServiceEnabled(CosmosClientTelemetryConfig config) {

                    config.resetIsSendClientTelemetryToServiceEnabled();
                }

                @Override
                public CosmosDiagnosticsThresholds getDiagnosticsThresholds(CosmosClientTelemetryConfig config) {
                    return config.diagnosticsThresholds;
                }

                @Override
                public boolean isLegacyTracingEnabled(CosmosClientTelemetryConfig config) {
                    return config.useLegacyOpenTelemetryTracing;
                }

                @Override
                public boolean isTransportLevelTracingEnabled(CosmosClientTelemetryConfig config) {
                    return config.isTransportLevelTracingEnabled;
                }

                @Override
                public Tracer getOrCreateTracer(CosmosClientTelemetryConfig config) {
                    return config.getOrCreateTracer();
                }

                @Override
                public void setUseLegacyTracing(CosmosClientTelemetryConfig config, boolean useLegacyTracing) {
                    config.setUseLegacyOpenTelemetryTracing(useLegacyTracing);
                }

                @Override
                public void setTracer(CosmosClientTelemetryConfig config, Tracer tracer) {
                    if (tracer != null) {
                        config.tracer = tracer;
                    }
                }

                @Override
                public double getSamplingRate(CosmosClientTelemetryConfig config) {
                    return config.samplingRate;
                }

                @Override
                public ShowQueryMode showQueryMode(CosmosClientTelemetryConfig config) {
                    return config.showQueryMode;
                }
       
                @Override
                public double[] getDefaultPercentiles(CosmosClientTelemetryConfig config) {
                    return config.micrometerMetricsOptions.getDefaultPercentiles();
                }

                @Override
                public boolean shouldPublishHistograms(CosmosClientTelemetryConfig config) {
                    return config.micrometerMetricsOptions.shouldPublishHistograms();
                }

                @Override
                public boolean shouldApplyDiagnosticThresholdsForTransportLevelMeters(CosmosClientTelemetryConfig config) {
                    return config.micrometerMetricsOptions.shouldApplyDiagnosticThresholdsForTransportLevelMeters();
                }
            });
    }

    static { initialize(); }
}
