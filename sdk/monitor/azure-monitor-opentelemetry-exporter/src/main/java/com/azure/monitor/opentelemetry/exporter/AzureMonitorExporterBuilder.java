// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.AzureMonitorExporterProviderKeys;
import com.azure.monitor.opentelemetry.exporter.implementation.AzureMonitorLogRecordExporterProvider;
import com.azure.monitor.opentelemetry.exporter.implementation.AzureMonitorMetricExporterProvider;
import com.azure.monitor.opentelemetry.exporter.implementation.AzureMonitorSpanExporterProvider;
import com.azure.monitor.opentelemetry.exporter.implementation.LogDataMapper;
import com.azure.monitor.opentelemetry.exporter.implementation.MetricDataMapper;
import com.azure.monitor.opentelemetry.exporter.implementation.NoopTracer;
import com.azure.monitor.opentelemetry.exporter.implementation.SpanDataMapper;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.AbstractTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.configuration.ConnectionString;
import com.azure.monitor.opentelemetry.exporter.implementation.configuration.StatsbeatConnectionString;
import com.azure.monitor.opentelemetry.exporter.implementation.heartbeat.HeartbeatExporter;
import com.azure.monitor.opentelemetry.exporter.implementation.localstorage.LocalStorageStats;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ContextTagKeys;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryItemExporter;
import com.azure.monitor.opentelemetry.exporter.implementation.statsbeat.Feature;
import com.azure.monitor.opentelemetry.exporter.implementation.statsbeat.StatsbeatModule;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorHelper;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.PropertyHelper;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.TempDirs;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.VersionGenerator;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.ResourceParser;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * This class provides a fluent builder API to configure the OpenTelemetry SDK with Azure Monitor Exporters.
 */
public final class AzureMonitorExporterBuilder {

    private static final ClientLogger LOGGER = new ClientLogger(AzureMonitorExporterBuilder.class);

    private static final String APPLICATIONINSIGHTS_CONNECTION_STRING = "APPLICATIONINSIGHTS_CONNECTION_STRING";
    private static final String APPLICATIONINSIGHTS_AUTHENTICATION_SCOPE = "https://monitor.azure.com//.default";

    private static final String STATSBEAT_LONG_INTERVAL_SECONDS_PROPERTY_NAME
        = "STATSBEAT_LONG_INTERVAL_SECONDS_PROPERTY_NAME";
    private static final String STATSBEAT_SHORT_INTERVAL_SECONDS_PROPERTY_NAME
        = "STATSBEAT_SHORT_INTERVAL_SECONDS_PROPERTY_NAME";

    private static final Map<String, String> PROPERTIES
        = CoreUtils.getProperties("azure-monitor-opentelemetry-exporter.properties");

    private ConnectionString connectionString;
    private TokenCredential credential;

    // suppress warnings is needed in ApplicationInsights-Java repo, can be removed when upstreaming
    @SuppressWarnings({ "UnusedVariable", "FieldCanBeLocal" })
    private AzureMonitorExporterServiceVersion serviceVersion;

    private HttpPipeline httpPipeline;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private final List<HttpPipelinePolicy> httpPipelinePolicies = new ArrayList<>();
    private ClientOptions clientOptions;

    private boolean frozen;

    // this is only populated after the builder is frozen
    private TelemetryItemExporter builtTelemetryItemExporter;

    // this is only populated after the builder is frozen
    private StatsbeatModule statsbeatModule;

    /**
     * Creates an instance of {@link AzureMonitorExporterBuilder}.
     */
    public AzureMonitorExporterBuilder() {
    }

    /**
     * Sets the HTTP pipeline to use for the service client. If {@code httpPipeline} is set, all other
     * settings are ignored.
     *
     * @param httpPipeline The HTTP pipeline to use for sending service requests and receiving
     *                     responses.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    public AzureMonitorExporterBuilder httpPipeline(HttpPipeline httpPipeline) {
        if (frozen) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "httpPipeline cannot be changed after any of the build methods have been called"));
        }
        this.httpPipeline = httpPipeline;
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param httpClient The HTTP client to use for requests.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    public AzureMonitorExporterBuilder httpClient(HttpClient httpClient) {
        if (frozen) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "httpClient cannot be changed after any of the build methods have been called"));
        }
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Sets the logging configuration for HTTP requests and responses.
     *
     * <p>If logLevel is not provided, default value of {@link HttpLogDetailLevel#NONE} is set.
     *
     * @param httpLogOptions The logging configuration to use when sending and receiving HTTP
     *                       requests/responses.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    public AzureMonitorExporterBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        if (frozen) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "httpLogOptions cannot be changed after any of the build methods have been called"));
        }
        this.httpLogOptions = httpLogOptions;
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after required policies.
     *
     * @param httpPipelinePolicy a policy to be added to the http pipeline.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    public AzureMonitorExporterBuilder addHttpPipelinePolicy(HttpPipelinePolicy httpPipelinePolicy) {
        if (frozen) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "httpPipelinePolicies cannot be added after any of the build methods have been called"));
        }
        httpPipelinePolicies.add(Objects.requireNonNull(httpPipelinePolicy, "'policy' cannot be null."));
        return this;
    }

    /**
     * Sets the client options such as application ID and custom headers to set on a request.
     *
     * @param clientOptions The client options.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    public AzureMonitorExporterBuilder clientOptions(ClientOptions clientOptions) {
        if (frozen) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "clientOptions cannot be changed after any of the build methods have been called"));
        }
        this.clientOptions = clientOptions;
        return this;
    }

    /**
     * Sets the connection string to use for exporting telemetry events to Azure Monitor.
     *
     * @param connectionString The connection string for the Azure Monitor resource.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     * @throws NullPointerException If the connection string is {@code null}.
     * @throws IllegalArgumentException If the connection string is invalid.
     */
    public AzureMonitorExporterBuilder connectionString(String connectionString) {
        if (frozen) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "connectionString cannot be changed after any of the build methods have been called"));
        }
        this.connectionString = ConnectionString.parse(connectionString);
        return this;
    }

    /**
     * Sets the Azure Monitor service version.
     *
     * @param serviceVersion The Azure Monitor service version.
     * @return The update {@link AzureMonitorExporterBuilder} object.
     */
    public AzureMonitorExporterBuilder serviceVersion(AzureMonitorExporterServiceVersion serviceVersion) {
        if (frozen) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "serviceVersion cannot be changed after any of the build methods have been called"));
        }
        this.serviceVersion = serviceVersion;
        return this;
    }

    /**
     * Sets the token credential required for authentication with the ingestion endpoint service.
     *
     * @param credential The Azure Identity TokenCredential.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    public AzureMonitorExporterBuilder credential(TokenCredential credential) {
        if (frozen) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "credential cannot be changed after any of the build methods have been called"));
        }
        this.credential = credential;
        return this;
    }

    /**
     * Creates an {@link AzureMonitorTraceExporter} based on the options set in the builder. This
     * exporter is an implementation of OpenTelemetry {@link SpanExporter}.
     *
     * @return An instance of {@link AzureMonitorTraceExporter}.
     * @throws NullPointerException if the connection string is not set on this builder or if the
     * environment variable "APPLICATIONINSIGHTS_CONNECTION_STRING" is not set.
     */
    public SpanExporter buildTraceExporter() {
        ConfigProperties defaultConfig = DefaultConfigProperties.create(Collections.emptyMap());
        internalBuildAndFreeze(defaultConfig);
        // TODO (trask) how to pass along configuration properties?
        return buildTraceExporter(defaultConfig);
    }

    /**
     * Creates an {@link AzureMonitorMetricExporter} based on the options set in the builder. This
     * exporter is an implementation of OpenTelemetry {@link MetricExporter}.
     *
     * <p>When a new {@link MetricExporter} is created, it will automatically start {@link
     * HeartbeatExporter}.
     *
     * @return An instance of {@link AzureMonitorMetricExporter}.
     * @throws NullPointerException if the connection string is not set on this builder or if the
     * environment variable "APPLICATIONINSIGHTS_CONNECTION_STRING" is not set.
     */
    public MetricExporter buildMetricExporter() {
        ConfigProperties defaultConfig = DefaultConfigProperties.create(Collections.emptyMap());
        internalBuildAndFreeze(defaultConfig);
        // TODO (trask) how to pass along configuration properties?
        return buildMetricExporter(defaultConfig);
    }

    /**
     * Creates an {@link AzureMonitorLogRecordExporter} based on the options set in the builder. This
     * exporter is an implementation of OpenTelemetry {@link LogRecordExporter}.
     *
     * @return An instance of {@link AzureMonitorLogRecordExporter}.
     * @throws NullPointerException if the connection string is not set on this builder or if the
     * environment variable "APPLICATIONINSIGHTS_CONNECTION_STRING" is not set.
     */
    public LogRecordExporter buildLogRecordExporter() {
        ConfigProperties defaultConfig = DefaultConfigProperties.create(Collections.emptyMap());
        internalBuildAndFreeze(defaultConfig);
        // TODO (trask) how to pass along configuration properties?
        return buildLogRecordExporter(defaultConfig);
    }

    /**
     * Configures an {@link AutoConfiguredOpenTelemetrySdkBuilder} based on the options set in the builder.
     *
     * @param sdkBuilder the {@link AutoConfiguredOpenTelemetrySdkBuilder} in which to install the azure monitor exporters.
     */
    public void install(AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder) {
        sdkBuilder.addPropertiesSupplier(() -> {
            Map<String, String> props = new HashMap<>();
            props.put("otel.traces.exporter", AzureMonitorExporterProviderKeys.EXPORTER_NAME);
            props.put("otel.metrics.exporter", AzureMonitorExporterProviderKeys.EXPORTER_NAME);
            props.put("otel.logs.exporter", AzureMonitorExporterProviderKeys.EXPORTER_NAME);
            props.put(AzureMonitorExporterProviderKeys.INTERNAL_USING_AZURE_MONITOR_EXPORTER_BUILDER, "true");
            return props;
        });
        sdkBuilder.addSpanExporterCustomizer((spanExporter, configProperties) -> {
            if (spanExporter instanceof AzureMonitorSpanExporterProvider.MarkerSpanExporter) {
                internalBuildAndFreeze(configProperties);
                spanExporter = buildTraceExporter(configProperties);
            }
            return spanExporter;
        });
        sdkBuilder.addMetricExporterCustomizer((metricExporter, configProperties) -> {
            if (metricExporter instanceof AzureMonitorMetricExporterProvider.MarkerMetricExporter) {
                internalBuildAndFreeze(configProperties);
                metricExporter = buildMetricExporter(configProperties);
            }
            return metricExporter;
        });
        sdkBuilder.addLogRecordExporterCustomizer((logRecordExporter, configProperties) -> {
            if (logRecordExporter instanceof AzureMonitorLogRecordExporterProvider.MarkerLogRecordExporter) {
                internalBuildAndFreeze(configProperties);
                logRecordExporter = buildLogRecordExporter(configProperties);
            }
            return logRecordExporter;
        });
        // TODO (trask)
        //        sdkBuilder.addTracerProviderCustomizer((sdkTracerProviderBuilder, configProperties) -> {
        //            QuickPulse quickPulse = QuickPulse.create(getHttpPipeline());
        //            return sdkTracerProviderBuilder.addSpanProcessor(
        //                new LiveMetricsSpanProcessor(quickPulse, createSpanDataMapper()));
        //        });
        sdkBuilder.addMeterProviderCustomizer((sdkMeterProviderBuilder, config) -> sdkMeterProviderBuilder
            .registerView(InstrumentSelector.builder().setMeterName("io.opentelemetry.sdk.trace").build(),
                View.builder().setAggregation(Aggregation.drop()).build())
            .registerView(InstrumentSelector.builder().setMeterName("io.opentelemetry.sdk.logs").build(),
                View.builder().setAggregation(Aggregation.drop()).build()));
    }

    // One caveat: ConfigProperties will get used only once when initializing/starting StatsbeatModule.
    // When a customer call build(AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder) multiple times with a diff ConfigProperties each time,
    // the new ConfigProperties will not get applied to StatsbeatModule because of "frozen" guard. Luckily, we're using the config properties
    // in StatsbeatModule for testing only. We might need to revisit this approach later.
    private void internalBuildAndFreeze(ConfigProperties configProperties) {
        if (!frozen) {
            HttpPipeline httpPipeline = createHttpPipeline();
            statsbeatModule = initStatsbeatModule(configProperties);
            File tempDir = TempDirs.getApplicationInsightsTempDir(LOGGER,
                "Telemetry will not be stored to disk and retried on sporadic network failures");
            // TODO (heya) change LocalStorageStats.noop() to statsbeatModule.getNonessentialStatsbeat() when we decide to collect non-essential Statsbeat by default.
            builtTelemetryItemExporter = AzureMonitorHelper.createTelemetryItemExporter(httpPipeline, statsbeatModule,
                tempDir, LocalStorageStats.noop());
            startStatsbeatModule(statsbeatModule, configProperties, tempDir); // wait till TelemetryItemExporter has been initialized before starting StatsbeatModule
            frozen = true;
        }
    }

    private SpanExporter buildTraceExporter(ConfigProperties configProperties) {
        return new AzureMonitorTraceExporter(createSpanDataMapper(configProperties), builtTelemetryItemExporter,
            statsbeatModule);
    }

    private MetricExporter buildMetricExporter(ConfigProperties configProperties) {
        HeartbeatExporter.start(MINUTES.toSeconds(15), createDefaultsPopulator(configProperties),
            builtTelemetryItemExporter::send);
        return new AzureMonitorMetricExporter(new MetricDataMapper(createDefaultsPopulator(configProperties), true),
            builtTelemetryItemExporter);
    }

    private Set<Feature> initStatsbeatFeatures() {
        if (System.getProperty("org.graalvm.nativeimage.imagecode") != null) {
            return Collections.singleton(Feature.GRAAL_VM_NATIVE);
        }
        return Collections.emptySet();
    }

    private StatsbeatConnectionString getStatsbeatConnectionString() {
        return StatsbeatConnectionString.create(connectionString, null, null);
    }

    private LogRecordExporter buildLogRecordExporter(ConfigProperties configProperties) {
        return new AzureMonitorLogRecordExporter(
            new LogDataMapper(true, false, createDefaultsPopulator(configProperties)), builtTelemetryItemExporter);
    }

    private SpanDataMapper createSpanDataMapper(ConfigProperties configProperties) {
        return new SpanDataMapper(true, createDefaultsPopulator(configProperties),
            (event, instrumentationName) -> false, (span, event) -> false);
    }

    private BiConsumer<AbstractTelemetryBuilder, Resource> createDefaultsPopulator(ConfigProperties configProperties) {
        ConnectionString connectionString = getConnectionString(configProperties);
        ResourceParser resourceParser = new ResourceParser();
        return (builder, resource) -> {
            builder.setConnectionString(connectionString);
            builder.setResource(resource);
            builder.addTag(ContextTagKeys.AI_INTERNAL_SDK_VERSION.toString(), VersionGenerator.getSdkVersion());
            // TODO (trask) unify these
            resourceParser.updateRoleNameAndInstance(builder, resource);
        };
    }

    private ConnectionString getConnectionString(ConfigProperties configProperties) {
        if (connectionString != null) {
            return connectionString;
        }
        ConnectionString connectionString
            = ConnectionString.parse(configProperties.getString(APPLICATIONINSIGHTS_CONNECTION_STRING));
        return Objects.requireNonNull(connectionString, "'connectionString' cannot be null");
    }

    private HttpPipeline createHttpPipeline() {

        if (httpPipeline != null) {
            if (credential != null) {
                throw LOGGER.logExceptionAsError(
                    new IllegalStateException("'credential' is not supported when custom 'httpPipeline' is specified"));
            }
            if (httpClient != null) {
                throw LOGGER.logExceptionAsError(
                    new IllegalStateException("'httpClient' is not supported when custom 'httpPipeline' is specified"));
            }
            if (httpLogOptions != null) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "'httpLogOptions' is not supported when custom 'httpPipeline' is specified"));
            }
            if (!httpPipelinePolicies.isEmpty()) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "'httpPipelinePolicies' is not supported when custom 'httpPipeline' is specified"));
            }
            if (clientOptions != null) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "'clientOptions' is not supported when custom 'httpPipeline' is specified"));
            }
            return httpPipeline;
        }

        List<HttpPipelinePolicy> policies = new ArrayList<>();
        String clientName = PROPERTIES.getOrDefault("name", "UnknownName");
        String clientVersion = PROPERTIES.getOrDefault("version", "UnknownVersion");

        String applicationId = CoreUtils.getApplicationId(clientOptions, httpLogOptions);

        policies
            .add(new UserAgentPolicy(applicationId, clientName, clientVersion, Configuration.getGlobalConfiguration()));
        policies.add(new CookiePolicy());
        if (credential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(credential, APPLICATIONINSIGHTS_AUTHENTICATION_SCOPE));
        }
        policies.addAll(httpPipelinePolicies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));
        return new com.azure.core.http.HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .tracer(new NoopTracer())
            .build();
    }

    private StatsbeatModule initStatsbeatModule(ConfigProperties configProperties) {
        return new StatsbeatModule(PropertyHelper::lazyUpdateVmRpIntegration);
    }

    private void startStatsbeatModule(StatsbeatModule statsbeatModule, ConfigProperties configProperties,
        File tempDir) {
        HttpPipeline statsbeatHttpPipeline = createStatsbeatHttpPipeline();
        TelemetryItemExporter statsbeatTelemetryItemExporter
            = AzureMonitorHelper.createStatsbeatTelemetryItemExporter(statsbeatHttpPipeline, statsbeatModule, tempDir);

        statsbeatModule.start(statsbeatTelemetryItemExporter, this::getStatsbeatConnectionString,
            getConnectionString(configProperties)::getInstrumentationKey, false,
            configProperties.getLong(STATSBEAT_SHORT_INTERVAL_SECONDS_PROPERTY_NAME, MINUTES.toSeconds(15)), // Statsbeat short interval
            configProperties.getLong(STATSBEAT_LONG_INTERVAL_SECONDS_PROPERTY_NAME, DAYS.toSeconds(1)), // Statsbeat long interval
            false, initStatsbeatFeatures());
    }

    private HttpPipeline createStatsbeatHttpPipeline() {
        if (httpPipeline != null) {
            return httpPipeline;
        }

        List<HttpPipelinePolicy> policies = new ArrayList<>();
        String clientName = PROPERTIES.getOrDefault("name", "UnknownName");
        String clientVersion = PROPERTIES.getOrDefault("version", "UnknownVersion");

        String applicationId = CoreUtils.getApplicationId(clientOptions, httpLogOptions);

        policies
            .add(new UserAgentPolicy(applicationId, clientName, clientVersion, Configuration.getGlobalConfiguration()));
        policies.add(new CookiePolicy());
        policies.addAll(httpPipelinePolicies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));
        return new com.azure.core.http.HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .tracer(new NoopTracer())
            .build();
    }
}
