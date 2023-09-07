// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.http.HttpPipeline;
import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.localstorage.LocalStorageStats;
import com.azure.monitor.opentelemetry.exporter.implementation.localstorage.LocalStorageTelemetryPipelineListener;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryItemExporter;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipeline;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipelineListener;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.TempDirs;
import io.opentelemetry.sdk.autoconfigure.ResourceConfiguration;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;

import java.io.File;

class LazyTelemetryItemExporter {

    private static final ClientLogger LOGGER = new ClientLogger(LazyTelemetryItemExporter.class);


    private TelemetryItemExporter telemetryItemExporter;

    synchronized TelemetryItemExporter get(HttpPipeline httpPipeline, ConfigProperties configProperties) {
        if (telemetryItemExporter == null) {
            telemetryItemExporter = create(httpPipeline, configProperties);
        }
        return telemetryItemExporter;
    }

    private TelemetryItemExporter create(HttpPipeline httpPipeline, ConfigProperties configProperties) {
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
