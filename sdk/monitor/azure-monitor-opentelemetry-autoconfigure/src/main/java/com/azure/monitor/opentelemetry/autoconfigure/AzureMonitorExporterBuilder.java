// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.LiveMetricsSpanProcessor;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.LogDataMapper;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.MetricDataMapper;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.NoopTracer;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.SpanDataMapper;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.configuration.ConnectionString;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.localstorage.LocalStorageStats;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.builders.AbstractTelemetryBuilder;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.configuration.StatsbeatConnectionString;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.heartbeat.HeartbeatExporter;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.ContextTagKeys;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.pipeline.TelemetryItemExporter;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.QuickPulse;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.statsbeat.Feature;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.statsbeat.StatsbeatModule;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.AzureMonitorHelper;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.PropertyHelper;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.ResourceParser;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.TempDirs;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.VersionGenerator;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.MINUTES;

class AzureMonitorExporterBuilder {

    private static final ClientLogger LOGGER = new ClientLogger(AzureMonitorExporterBuilder.class);

    private static final String STATSBEAT_LONG_INTERVAL_SECONDS_PROPERTY_NAME
        = "STATSBEAT_LONG_INTERVAL_SECONDS_PROPERTY_NAME";
    private static final String STATSBEAT_SHORT_INTERVAL_SECONDS_PROPERTY_NAME
        = "STATSBEAT_SHORT_INTERVAL_SECONDS_PROPERTY_NAME";

    private static final Map<String, String> PROPERTIES
        = CoreUtils.getProperties("azure-monitor-opentelemetry-exporter.properties");

    private AzureMonitorAutoConfigureOptions exporterOptions;

    private TelemetryItemExporter builtTelemetryItemExporter;

    private StatsbeatModule statsbeatModule;

    private ConfigProperties configProperties;

    private HttpPipeline httpPipeline;

    private QuickPulse quickPulse;

    private SpanDataMapper spanDataMapper;

    private boolean initialized;

    void initializeIfNot(AzureMonitorAutoConfigureOptions exporterOptions, ConfigProperties configProperties,
        Resource resource) {
        if (initialized) {
            return;
        }
        this.initialized = true;
        this.exporterOptions = exporterOptions;
        this.configProperties = configProperties;
        this.httpPipeline = createHttpPipeline();
        this.statsbeatModule = initStatsbeatModule(configProperties);
        this.spanDataMapper = createSpanDataMapper();
        File tempDir = TempDirs.getApplicationInsightsTempDir(LOGGER,
            "Telemetry will not be stored to disk and retried on sporadic network failures");
        // TODO (heya) change LocalStorageStats.noop() to statsbeatModule.getNonessentialStatsbeat() when we decide to collect non-essential Statsbeat by default.
        this.builtTelemetryItemExporter = AzureMonitorHelper.createTelemetryItemExporter(httpPipeline, statsbeatModule,
            tempDir, LocalStorageStats.noop());
        if (LiveMetrics.isEnabled(configProperties)) {
            this.quickPulse = createQuickPulse(resource);
        }
        startStatsbeatModule(statsbeatModule, configProperties, tempDir); // wait till TelemetryItemExporter has been initialized before starting StatsbeatModule
    }

    private QuickPulse createQuickPulse(Resource resource) {
        String roleName = resource.getAttribute(AttributeKey.stringKey("service.name"));
        String roleInstance = resource.getAttribute(AttributeKey.stringKey("service.instance.id"));
        ConnectionString connectionString = getConnectionString();
        return QuickPulse.create(httpPipeline, () -> connectionString.getLiveEndpoint(),
            () -> connectionString.getInstrumentationKey(), roleName, roleInstance, VersionGenerator.getSdkVersion());
    }

    SpanExporter buildSpanExporter() {
        return new AzureMonitorTraceExporter(spanDataMapper, builtTelemetryItemExporter, statsbeatModule);
    }

    LogRecordExporter buildLogRecordExporter() {
        return new AzureMonitorLogRecordExporter(new LogDataMapper(true, false, createDefaultsPopulator()),
            builtTelemetryItemExporter, quickPulse);
    }

    MetricExporter buildMetricExporter() {
        HeartbeatExporter.start(MINUTES.toSeconds(15), createDefaultsPopulator(), builtTelemetryItemExporter::send);
        return new AzureMonitorMetricExporter(new MetricDataMapper(createDefaultsPopulator(), true),
            builtTelemetryItemExporter);
    }

    public SpanProcessor buildLiveMetricsSpanProcesor() {
        return new LiveMetricsSpanProcessor(quickPulse, spanDataMapper);
    }

    private Set<Feature> initStatsbeatFeatures() {
        if (System.getProperty("org.graalvm.nativeimage.imagecode") != null) {
            return Collections.singleton(Feature.GRAAL_VM_NATIVE);
        }
        return Collections.emptySet();
    }

    private StatsbeatConnectionString getStatsbeatConnectionString() {
        return StatsbeatConnectionString.create(exporterOptions.connectionString, null, null);
    }

    private SpanDataMapper createSpanDataMapper() {
        return new SpanDataMapper(true, createDefaultsPopulator(), (event, instrumentationName) -> false,
            (span, event) -> false);
    }

    private BiConsumer<AbstractTelemetryBuilder, Resource> createDefaultsPopulator() {
        ConnectionString connectionString = getConnectionString();
        ResourceParser resourceParser = new ResourceParser();
        return (builder, resource) -> {
            builder.setConnectionString(connectionString);
            builder.setResource(resource);
            builder.addTag(ContextTagKeys.AI_INTERNAL_SDK_VERSION.toString(), VersionGenerator.getSdkVersion());
            // TODO (trask) unify these
            resourceParser.updateRoleNameAndInstance(builder, resource);
        };
    }

    private ConnectionString getConnectionString() {
        if (exporterOptions.connectionString != null) {
            return exporterOptions.connectionString;
        }
        ConnectionString connectionString
            = ConnectionString.parse(configProperties.getString("applicationinsights.connection.string"));
        return connectionString;
    }

    private HttpPipeline createHttpPipeline() {

        if (exporterOptions.httpPipeline != null) {
            if (exporterOptions.credential != null) {
                throw LOGGER.logExceptionAsError(
                    new IllegalStateException("'credential' is not supported when custom 'httpPipeline' is specified"));
            }
            if (exporterOptions.httpClient != null) {
                throw LOGGER.logExceptionAsError(
                    new IllegalStateException("'httpClient' is not supported when custom 'httpPipeline' is specified"));
            }
            if (exporterOptions.httpLogOptions != null) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "'httpLogOptions' is not supported when custom 'httpPipeline' is specified"));
            }
            if (!exporterOptions.httpPipelinePolicies.isEmpty()) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "'httpPipelinePolicies' is not supported when custom 'httpPipeline' is specified"));
            }
            if (exporterOptions.clientOptions != null) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "'clientOptions' is not supported when custom 'httpPipeline' is specified"));
            }
            if (exporterOptions.retryOptions != null) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "'retryOptions' is not supported when custom 'httpPipeline' is specified"));
            }
            return exporterOptions.httpPipeline;
        }

        List<HttpPipelinePolicy> policies = new ArrayList<>();
        String clientName = PROPERTIES.getOrDefault("name", "UnknownName");
        String clientVersion = PROPERTIES.getOrDefault("version", "UnknownVersion");

        String applicationId
            = CoreUtils.getApplicationId(exporterOptions.clientOptions, exporterOptions.httpLogOptions);

        policies
            .add(new UserAgentPolicy(applicationId, clientName, clientVersion, Configuration.getGlobalConfiguration()));
        policies.add(new CookiePolicy());
        if (exporterOptions.credential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(exporterOptions.credential,
                getConnectionString().getAadAudienceWithScope()));
        }

        if (exporterOptions.retryOptions != null) {
            policies.add(new RetryPolicy(exporterOptions.retryOptions));
        }

        policies.addAll(exporterOptions.httpPipelinePolicies);
        policies.add(new HttpLoggingPolicy(exporterOptions.httpLogOptions));
        return new com.azure.core.http.HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(exporterOptions.httpClient)
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
            getConnectionString()::getInstrumentationKey, false,
            configProperties.getLong(STATSBEAT_SHORT_INTERVAL_SECONDS_PROPERTY_NAME, MINUTES.toSeconds(15)), // Statsbeat short interval
            configProperties.getLong(STATSBEAT_LONG_INTERVAL_SECONDS_PROPERTY_NAME, DAYS.toSeconds(1)), // Statsbeat long interval
            false, initStatsbeatFeatures());
    }

    private HttpPipeline createStatsbeatHttpPipeline() {
        if (exporterOptions.httpPipeline != null) {
            return exporterOptions.httpPipeline;
        }

        List<HttpPipelinePolicy> policies = new ArrayList<>();
        String clientName = PROPERTIES.getOrDefault("name", "UnknownName");
        String clientVersion = PROPERTIES.getOrDefault("version", "UnknownVersion");

        String applicationId
            = CoreUtils.getApplicationId(exporterOptions.clientOptions, exporterOptions.httpLogOptions);

        policies
            .add(new UserAgentPolicy(applicationId, clientName, clientVersion, Configuration.getGlobalConfiguration()));
        policies.add(new CookiePolicy());
        policies.addAll(exporterOptions.httpPipelinePolicies);
        policies.add(new HttpLoggingPolicy(exporterOptions.httpLogOptions));
        return new com.azure.core.http.HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(exporterOptions.httpClient)
            .tracer(new NoopTracer())
            .build();
    }
}
