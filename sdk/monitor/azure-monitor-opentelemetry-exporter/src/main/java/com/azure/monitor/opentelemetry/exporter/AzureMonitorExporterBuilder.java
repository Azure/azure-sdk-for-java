// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
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
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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
@Fluent
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

    // this is only populated after the builder is frozen
    private TelemetryItemExporter builtTelemetryItemExporter;

    // this is only populated after the builder is frozen
    private StatsbeatModule statsbeatModule;

    private boolean frozen;

    private final ExportOptions exportOptions;

    /**
     * Creates an instance of {@link AzureMonitorExporterBuilder}.
     */
    public AzureMonitorExporterBuilder() {
        this.exportOptions = new ExportOptions();
    }

    /**
     * Creates an instance of {@link AzureMonitorExporterBuilder}.
     * @param exportOptions The export options.
     */
    public AzureMonitorExporterBuilder(ExportOptions exportOptions) {
        this.exportOptions = exportOptions;
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
        return buildTraceExporter(defaultConfig);
    }

    /**
     * Creates an {@link AzureMonitorTraceExporter} based on the options set in the builder. This
     * exporter is an implementation of OpenTelemetry {@link SpanExporter}.
     *
     * @param configProperties The OpenTelemetry configuration properties.
     * @return An instance of {@link AzureMonitorTraceExporter}.
     * @throws NullPointerException if the connection string is not set on this builder or if the
     * environment variable "APPLICATIONINSIGHTS_CONNECTION_STRING" is not set.
     */
    public SpanExporter buildTraceExporter(ConfigProperties configProperties) {
        internalBuildAndFreeze(configProperties);
        return new AzureMonitorTraceExporter(createSpanDataMapper(configProperties), builtTelemetryItemExporter,
            statsbeatModule);
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
        return buildLogRecordExporter(defaultConfig);
    }

    /**
     * Creates an {@link AzureMonitorLogRecordExporter} based on the options set in the builder. This
     * exporter is an implementation of OpenTelemetry {@link LogRecordExporter}.
     *
     * @param configProperties The OpenTelemetry configuration properties.
     * @return An instance of {@link AzureMonitorLogRecordExporter}.
     * @throws NullPointerException if the connection string is not set on this builder or if the
     * environment variable "APPLICATIONINSIGHTS_CONNECTION_STRING" is not set.
     */
    public LogRecordExporter buildLogRecordExporter(ConfigProperties configProperties) {
        internalBuildAndFreeze(configProperties);
        return new AzureMonitorLogRecordExporter(
            new LogDataMapper(true, false, createDefaultsPopulator(configProperties)), builtTelemetryItemExporter);
    }

    // One caveat: ConfigProperties will get used only once when initializing/starting StatsbeatModule.
    // When a customer call build(AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder) multiple times with a diff ConfigProperties each time,
    // the new ConfigProperties will not get applied to StatsbeatModule because of "frozen" guard. Luckily, we're using the config properties
    // in StatsbeatModule for testing only. We might need to revisit this approach later.
    void internalBuildAndFreeze(ConfigProperties configProperties) {
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

    /**
     * Creates an {@link AzureMonitorMetricExporter} based on the options set in the builder. This
     * exporter is an implementation of OpenTelemetry {@link MetricExporter}.
     *
     * <p>When a new {@link MetricExporter} is created, it will automatically start {@link
     * HeartbeatExporter}.
     *
     * @return An instance of {@link AzureMonitorMetricExporter}.
     * @throws NullPointerException if the connection string is not set on this builder or if the
     *                              environment variable "APPLICATIONINSIGHTS_CONNECTION_STRING" is not set.
     */
    public MetricExporter buildMetricExporter() {
        ConfigProperties defaultConfig = DefaultConfigProperties.create(Collections.emptyMap());
        return buildMetricExporter(defaultConfig);
    }

    /**
     * Creates an {@link AzureMonitorMetricExporter} based on the options set in the builder. This
     * exporter is an implementation of OpenTelemetry {@link MetricExporter}.
     *
     * <p>When a new {@link MetricExporter} is created, it will automatically start {@link
     * HeartbeatExporter}.
     *
     *
     * @param configProperties The OpenTelemetry configuration properties.
     * @return An instance of {@link AzureMonitorMetricExporter}.
     * @throws NullPointerException if the connection string is not set on this builder or if the
     * environment variable "APPLICATIONINSIGHTS_CONNECTION_STRING" is not set.
     */
    public MetricExporter buildMetricExporter(ConfigProperties configProperties) {
        internalBuildAndFreeze(configProperties);
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
        return StatsbeatConnectionString.create(exportOptions.connectionString, null, null);
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
        if (exportOptions.connectionString != null) {
            return exportOptions.connectionString;
        }
        ConnectionString connectionString
            = ConnectionString.parse(configProperties.getString(APPLICATIONINSIGHTS_CONNECTION_STRING));
        return Objects.requireNonNull(connectionString, "'connectionString' cannot be null");
    }

    private HttpPipeline createHttpPipeline() {

        if (exportOptions.httpPipeline != null) {
            if (exportOptions.credential != null) {
                throw LOGGER.logExceptionAsError(
                    new IllegalStateException("'credential' is not supported when custom 'httpPipeline' is specified"));
            }
            if (exportOptions.httpClient != null) {
                throw LOGGER.logExceptionAsError(
                    new IllegalStateException("'httpClient' is not supported when custom 'httpPipeline' is specified"));
            }
            if (exportOptions.httpLogOptions != null) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "'httpLogOptions' is not supported when custom 'httpPipeline' is specified"));
            }
            if (!exportOptions.httpPipelinePolicies.isEmpty()) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "'httpPipelinePolicies' is not supported when custom 'httpPipeline' is specified"));
            }
            if (exportOptions.clientOptions != null) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "'clientOptions' is not supported when custom 'httpPipeline' is specified"));
            }
            return exportOptions.httpPipeline;
        }

        List<HttpPipelinePolicy> policies = new ArrayList<>();
        String clientName = PROPERTIES.getOrDefault("name", "UnknownName");
        String clientVersion = PROPERTIES.getOrDefault("version", "UnknownVersion");

        String applicationId = CoreUtils.getApplicationId(exportOptions.clientOptions, exportOptions.httpLogOptions);

        policies
            .add(new UserAgentPolicy(applicationId, clientName, clientVersion, Configuration.getGlobalConfiguration()));
        policies.add(new CookiePolicy());
        if (exportOptions.credential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(exportOptions.credential,
                APPLICATIONINSIGHTS_AUTHENTICATION_SCOPE));
        }
        policies.addAll(exportOptions.httpPipelinePolicies);
        policies.add(new HttpLoggingPolicy(exportOptions.httpLogOptions));
        return new com.azure.core.http.HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(exportOptions.httpClient)
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
        if (exportOptions.httpPipeline != null) {
            return exportOptions.httpPipeline;
        }

        List<HttpPipelinePolicy> policies = new ArrayList<>();
        String clientName = PROPERTIES.getOrDefault("name", "UnknownName");
        String clientVersion = PROPERTIES.getOrDefault("version", "UnknownVersion");

        String applicationId = CoreUtils.getApplicationId(exportOptions.clientOptions, exportOptions.httpLogOptions);

        policies
            .add(new UserAgentPolicy(applicationId, clientName, clientVersion, Configuration.getGlobalConfiguration()));
        policies.add(new CookiePolicy());
        policies.addAll(exportOptions.httpPipelinePolicies);
        policies.add(new HttpLoggingPolicy(exportOptions.httpLogOptions));
        return new com.azure.core.http.HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(exportOptions.httpClient)
            .tracer(new NoopTracer())
            .build();
    }
}
