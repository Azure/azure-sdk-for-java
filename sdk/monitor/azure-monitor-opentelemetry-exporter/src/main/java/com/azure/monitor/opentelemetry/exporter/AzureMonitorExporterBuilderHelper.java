// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.http.HttpPipeline;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.monitor.opentelemetry.exporter.implementation.AzureMonitorExporterProviderKeys;
import com.azure.monitor.opentelemetry.exporter.implementation.AzureMonitorLogRecordExporterProvider;
import com.azure.monitor.opentelemetry.exporter.implementation.AzureMonitorMetricExporterProvider;
import com.azure.monitor.opentelemetry.exporter.implementation.AzureMonitorSpanExporterProvider;
import com.azure.monitor.opentelemetry.exporter.implementation.LogDataMapper;
import com.azure.monitor.opentelemetry.exporter.implementation.MetricDataMapper;
import com.azure.monitor.opentelemetry.exporter.implementation.SpanDataMapper;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.AbstractTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.configuration.ConnectionString;
import com.azure.monitor.opentelemetry.exporter.implementation.heartbeat.HeartbeatExporter;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ContextTagKeys;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryItemExporter;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.ResourceParser;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.VersionGenerator;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.util.concurrent.TimeUnit.MINUTES;

class AzureMonitorExporterBuilderHelper {

    // TODO (trask) this is unused
    private final AzureMonitorExporterServiceVersion serviceVersion;

    private final Configuration configuration;
    private final ConnectionStringBuilder connectionStringBuilder;
    private final HttpPipelineBuilder httpPipelineBuilder;
    private final TelemetryItemExporterBuilder telemetryItemExporterBuilder;

    // lazy created
    private ConnectionString connectionString;
    // lazy created
    private HttpPipeline httpPipeline;
    // lazy created
    private TelemetryItemExporter telemetryItemExporter;

    AzureMonitorExporterBuilderHelper(AzureMonitorExporterServiceVersion serviceVersion,
                                      Configuration configuration,
                                      ConnectionStringBuilder connectionStringBuilder,
                                      TelemetryItemExporterBuilder telemetryItemExporterBuilder,
                                      HttpPipelineBuilder httpPipelineBuilder) {
        this.serviceVersion = serviceVersion;
        this.configuration = configuration;
        this.connectionStringBuilder=connectionStringBuilder;
        this.telemetryItemExporterBuilder = telemetryItemExporterBuilder;
        this.httpPipelineBuilder = httpPipelineBuilder;
    }


    /**
     * Configures an {@link AutoConfiguredOpenTelemetrySdkBuilder} based on the options set in the builder.
     */
    public void build(AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder) {

        sdkBuilder.addPropertiesSupplier(() -> {
            Map<String, String> props = new HashMap<>();
            props.put("otel.traces.exporter", AzureMonitorExporterProviderKeys.EXPORTER_NAME);
            props.put("otel.metrics.exporter", AzureMonitorExporterProviderKeys.EXPORTER_NAME);
            props.put("otel.logs.exporter", AzureMonitorExporterProviderKeys.EXPORTER_NAME);
            props.put(AzureMonitorExporterProviderKeys.INTERNAL_USING_BUILDER, "true");
            return props;
        });
        sdkBuilder.addSpanExporterCustomizer(
            (spanExporter, configProperties) -> {
                if (spanExporter instanceof AzureMonitorSpanExporterProvider.MarkerSpanExporter) {
                    spanExporter = buildTraceExporter(configProperties);
                }
                return spanExporter;
            });
        sdkBuilder.addMetricExporterCustomizer(
            (metricExporter, configProperties) -> {
                if (metricExporter instanceof AzureMonitorMetricExporterProvider.MarkerMetricExporter) {
                    metricExporter = buildMetricExporter(configProperties);
                }
                return metricExporter;
            });
        sdkBuilder.addLogRecordExporterCustomizer(
            (logRecordExporter, configProperties) -> {
                if (logRecordExporter instanceof AzureMonitorLogRecordExporterProvider.MarkerLogRecordExporter) {
                    logRecordExporter = buildLogRecordExporter(configProperties);
                }
                return logRecordExporter;
            });
        // TODO
//        sdkBuilder.addTracerProviderCustomizer((sdkTracerProviderBuilder, configProperties) -> {
//            QuickPulse quickPulse = QuickPulse.create(getHttpPipeline());
//            return sdkTracerProviderBuilder.addSpanProcessor(
//                new LiveMetricsSpanProcessor(quickPulse, createSpanDataMapper()));
//        });
        sdkBuilder.addMeterProviderCustomizer((sdkMeterProviderBuilder, configProperties) ->
            sdkMeterProviderBuilder.registerView(
                InstrumentSelector.builder()
                    .setMeterName("io.opentelemetry.sdk.trace")
                    .build(),
                View.builder()
                    .setAggregation(Aggregation.drop())
                    .build()
            ).registerView(
                InstrumentSelector.builder()
                    .setMeterName("io.opentelemetry.sdk.logs")
                    .build(),
                View.builder()
                    .setAggregation(Aggregation.drop())
                    .build()
            ));
    }


    private SpanExporter buildTraceExporter(ConfigProperties configProperties) {
        return new AzureMonitorTraceExporter(createSpanDataMapper(), getItemExporter(configProperties));
    }

    private MetricExporter buildMetricExporter(ConfigProperties configProperties) {
        TelemetryItemExporter telemetryItemExporter = getItemExporter(configProperties);
        HeartbeatExporter.start(
            MINUTES.toSeconds(15), populateDefaults(connectionString), telemetryItemExporter::send);
        return new AzureMonitorMetricExporter(
            new MetricDataMapper(populateDefaults(connectionString), true), telemetryItemExporter);
    }

    private LogRecordExporter buildLogRecordExporter(ConfigProperties configProperties) {
        return new AzureMonitorLogRecordExporter(
            new LogDataMapper(true, false, populateDefaults(connectionString, configProperties)),
            getItemExporter(configProperties));
    }

    private SpanDataMapper createSpanDataMapper(ConnectionString connectionString, ConfigProperties configProperties) {
        return new SpanDataMapper(
            true,
            populateDefaults(connectionString, configProperties),
            (event, instrumentationName) -> false,
            (span, event) -> false);
    }

    private static BiConsumer<AbstractTelemetryBuilder, Resource> populateDefaults(ConnectionString connectionString,
                                                                                   ConfigProperties configProperties) {
        return (builder, resource) -> {
            builder.setConnectionString(connectionString);
            builder.addTag(
                ContextTagKeys.AI_INTERNAL_SDK_VERSION.toString(), VersionGenerator.getSdkVersion());
            ResourceParser.updateRoleNameAndInstance(builder, resource, configProperties);
        };
    }

    private synchronized ConnectionString getConnectionString(ConfigProperties configProperties) {
        if (connectionString == null) {
            connectionString = connectionStringBuilder.build(configProperties);
        }
        return connectionString;
    }

    private synchronized TelemetryItemExporter getItemExporter(ConfigProperties configProperties) {
        if (telemetryItemExporter == null) {
            telemetryItemExporter = telemetryItemExporterBuilder.build(getHttpPipeline(), configProperties);
        }
        return telemetryItemExporter;
    }

    private synchronized HttpPipeline getHttpPipeline() {
        if (httpPipeline == null) {
            httpPipeline = httpPipelineBuilder.build();
        }
        return httpPipeline;
    }
}
