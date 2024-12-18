// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import com.azure.core.http.HttpPipeline;
import com.azure.monitor.opentelemetry.exporter.implementation.localstorage.LocalStorageStats;
import com.azure.monitor.opentelemetry.exporter.implementation.localstorage.LocalStorageTelemetryPipelineListener;
import com.azure.monitor.opentelemetry.exporter.implementation.logging.DiagnosticTelemetryPipelineListener;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryItemExporter;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipeline;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipelineListener;
import com.azure.monitor.opentelemetry.exporter.implementation.statsbeat.NonessentialStatsbeat;
import com.azure.monitor.opentelemetry.exporter.implementation.statsbeat.StatsbeatModule;
import com.azure.monitor.opentelemetry.exporter.implementation.statsbeat.StatsbeatTelemetryPipelineListener;
import reactor.util.annotation.NonNull;

import java.io.File;

public final class AzureMonitorHelper {

    public static TelemetryItemExporter createTelemetryItemExporter(HttpPipeline httpPipeline,
        StatsbeatModule statsbeatModule, File tempDir, LocalStorageStats localStorageStats) {
        TelemetryPipeline telemetryPipeline = new TelemetryPipeline(httpPipeline, statsbeatModule::shutdown);

        TelemetryPipelineListener telemetryPipelineListener;
        if (tempDir == null) {
            telemetryPipelineListener = new DiagnosticTelemetryPipelineListener(
                "Sending telemetry to the ingestion service", true, " (telemetry will be lost)");
        } else {
            telemetryPipelineListener = TelemetryPipelineListener.composite(
                // suppress warnings on retryable failures, in order to reduce sporadic/annoying
                // warnings when storing to disk and retrying shortly afterwards anyways
                // will log if that retry from disk fails
                new DiagnosticTelemetryPipelineListener("Sending telemetry to the ingestion service", false, ""),
                new LocalStorageTelemetryPipelineListener(50, // default to 50MB
                    TempDirs.getSubDir(tempDir, "telemetry"), telemetryPipeline, localStorageStats, false));
        }

        return new TelemetryItemExporter(telemetryPipeline, telemetryPipelineListener);
    }

    public static TelemetryItemExporter createStatsbeatTelemetryItemExporter(HttpPipeline httpPipeline,
        StatsbeatModule statsbeatModule, File tempDir) {
        TelemetryPipeline statsbeatTelemetryPipeline = new TelemetryPipeline(httpPipeline, null);

        TelemetryPipelineListener statsbeatTelemetryPipelineListener;
        if (tempDir == null) {
            statsbeatTelemetryPipelineListener = new StatsbeatTelemetryPipelineListener(statsbeatModule::shutdown);
        } else {
            LocalStorageTelemetryPipelineListener localStorageTelemetryPipelineListener
                = new LocalStorageTelemetryPipelineListener(1, // only store at most 1mb of statsbeat telemetry
                    TempDirs.getSubDir(tempDir, "statsbeat"), statsbeatTelemetryPipeline, LocalStorageStats.noop(),
                    true);
            statsbeatTelemetryPipelineListener
                = TelemetryPipelineListener.composite(new StatsbeatTelemetryPipelineListener(() -> {
                    statsbeatModule.shutdown();
                    localStorageTelemetryPipelineListener.shutdown();
                }), localStorageTelemetryPipelineListener);
        }

        return new TelemetryItemExporter(statsbeatTelemetryPipeline, statsbeatTelemetryPipelineListener);
    }

    private AzureMonitorHelper() {
    }
}
