// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

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

    private final LazyConnectionString connectionString;
    private final LazyHttpPipeline httpPipeline;
    private final LazyTelemetryItemExporter telemetryItemExporter;

    AzureMonitorExporterBuilderHelper(AzureMonitorExporterServiceVersion serviceVersion,
                                      LazyConnectionString connectionString,
                                      LazyTelemetryItemExporter telemetryItemExporter,
                                      LazyHttpPipeline httpPipeline) {
        this.serviceVersion = serviceVersion;
        this.connectionString = connectionString;
        this.telemetryItemExporter = telemetryItemExporter;
        this.httpPipeline = httpPipeline;
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
        TelemetryItemExporter telemetryItemExporter = getTelemetryItemExporter(configProperties);
        return new AzureMonitorTraceExporter(createSpanDataMapper(configProperties), telemetryItemExporter);
    }

    private MetricExporter buildMetricExporter(ConfigProperties configProperties) {
        TelemetryItemExporter telemetryItemExporter = getTelemetryItemExporter(configProperties);
        HeartbeatExporter.start(
            MINUTES.toSeconds(15), createDefaultPopulator(configProperties), telemetryItemExporter::send);
        return new AzureMonitorMetricExporter(
            new MetricDataMapper(createDefaultPopulator(configProperties), true), telemetryItemExporter);
    }

    private LogRecordExporter buildLogRecordExporter(ConfigProperties configProperties) {
        TelemetryItemExporter telemetryItemExporter = getTelemetryItemExporter(configProperties);
        return new AzureMonitorLogRecordExporter(
            new LogDataMapper(true, false, createDefaultPopulator(configProperties)), telemetryItemExporter);
    }

    private TelemetryItemExporter getTelemetryItemExporter(ConfigProperties configProperties) {
        return telemetryItemExporter.get(httpPipeline.get(), configProperties);
    }

    private SpanDataMapper createSpanDataMapper(ConfigProperties configProperties) {
        return new SpanDataMapper(
            true,
            createDefaultPopulator(configProperties),
            (event, instrumentationName) -> false,
            (span, event) -> false);
    }

    private BiConsumer<AbstractTelemetryBuilder, Resource> createDefaultPopulator(ConfigProperties configProperties) {
        ConnectionString connectionString = this.connectionString.get(configProperties);
        return (builder, resource) -> {
            builder.setConnectionString(connectionString);
            builder.addTag(
                ContextTagKeys.AI_INTERNAL_SDK_VERSION.toString(), VersionGenerator.getSdkVersion());
            ResourceParser.updateRoleNameAndInstance(builder, resource, configProperties);
        };
    }
}
