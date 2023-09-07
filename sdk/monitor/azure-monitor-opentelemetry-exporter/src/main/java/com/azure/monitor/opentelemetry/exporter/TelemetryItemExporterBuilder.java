// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.http.HttpPipeline;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
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
import com.azure.monitor.opentelemetry.exporter.implementation.localstorage.LocalStorageStats;
import com.azure.monitor.opentelemetry.exporter.implementation.localstorage.LocalStorageTelemetryPipelineListener;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ContextTagKeys;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryItemExporter;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipeline;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipelineListener;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.ResourceParser;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.TempDirs;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.VersionGenerator;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.autoconfigure.ResourceConfiguration;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.util.concurrent.TimeUnit.MINUTES;

class TelemetryItemExporterBuilder {

    private static final ClientLogger LOGGER = new ClientLogger(TelemetryItemExporterBuilder.class);




    TelemetryItemExporter build(HttpPipeline httpPipeline, ConfigProperties configProperties) {

        TelemetryPipeline pipeline = new TelemetryPipeline(httpPipeline);

        File tempDir =
            TempDirs.getApplicationInsightsTempDir(
                LOGGER,
                "Telemetry will not be stored to disk and retried on sporadic network failures");

        TelemetryItemExporter telemetryItemExporter;
        if (tempDir != null) {
            telemetryItemExporter =
                new TelemetryItemExporter(
                    pipeline,
                    new LocalStorageTelemetryPipelineListener(
                        50, // default to 50MB
                        TempDirs.getSubDir(tempDir, "telemetry"),
                        pipeline,
                        LocalStorageStats.noop(),
                        false),
                    ResourceConfiguration.createEnvironmentResource(configProperties));
        } else {
            telemetryItemExporter = new TelemetryItemExporter(
                pipeline,
                TelemetryPipelineListener.noop(),
                ResourceConfiguration.createEnvironmentResource(configProperties));
        }
        return telemetryItemExporter;
    }
}
