// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.localstorage;

import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.logging.DiagnosticTelemetryPipelineListener;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipeline;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipelineListener;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.ThreadPoolUtils;
import io.opentelemetry.sdk.common.CompletableResultCode;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singletonList;

class LocalFileSender implements Runnable {

    private static final ClientLogger logger = new ClientLogger(LocalFileSender.class);

    private final LocalFileLoader localFileLoader;
    private final TelemetryPipeline telemetryPipeline;
    private final ScheduledExecutorService scheduledExecutor
        = Executors.newSingleThreadScheduledExecutor(ThreadPoolUtils.createDaemonThreadFactory(LocalFileLoader.class));

    private final TelemetryPipelineListener diagnosticListener;

    LocalFileSender(long intervalSeconds, LocalFileLoader localFileLoader, TelemetryPipeline telemetryPipeline,
        boolean suppressWarnings) { // used to suppress warnings from statsbeat
        this.localFileLoader = localFileLoader;
        this.telemetryPipeline = telemetryPipeline;

        diagnosticListener = suppressWarnings
            ? TelemetryPipelineListener.noop()
            : new DiagnosticTelemetryPipelineListener("Sending telemetry to the ingestion service (retry from disk)",
                true, " (will be retried again)");
        scheduledExecutor.scheduleWithFixedDelay(this, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
    }

    void shutdown() {
        scheduledExecutor.shutdown();
    }

    @Override
    public void run() {
        // NOTE this sends telemetry that was stored to disk and the ikey is encoded into the file
        // so even if the customer has changed the ikey in their applicationinsights.json file, this
        // will still send out the telemetry to the original destination
        // (and same for azure spring cloud, if ikey is changed dynamically at runtime, this will still
        // send out the telemetry to the original destination)

        // TODO (heya) load all persisted files on disk in one or more batch per batch capacity?
        try {
            LocalFileLoader.PersistedFile persistedFile = localFileLoader.loadTelemetriesFromDisk();
            if (persistedFile != null) {
                CompletableResultCode resultCode = telemetryPipeline.send(singletonList(persistedFile.rawBytes),
                    persistedFile.connectionString, TelemetryPipelineListener.composite(diagnosticListener,
                        new LocalFileSenderTelemetryPipelineListener(localFileLoader, persistedFile.file)));
                resultCode.join(30, TimeUnit.SECONDS); // wait max 30 seconds for request to be completed.
            }
        } catch (RuntimeException ex) {
            logger.error("Unexpected error occurred while sending telemetries from the local storage.", ex);
        }
    }
}
