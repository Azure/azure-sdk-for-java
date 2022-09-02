/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.azure.monitor.opentelemetry.exporter.implementation.localstorage;

import com.azure.monitor.opentelemetry.exporter.implementation.logging.DiagnosticTelemetryPipelineListener;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipeline;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipelineListener;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.ThreadPoolUtils;
import io.opentelemetry.sdk.common.CompletableResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singletonList;

class LocalFileSender implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(LocalFileSender.class);
    private final LocalFileLoader localFileLoader;
    private final TelemetryPipeline telemetryPipeline;
    private final ScheduledExecutorService scheduledExecutor =
        Executors.newSingleThreadScheduledExecutor(
            ThreadPoolUtils.createDaemonThreadFactory(LocalFileLoader.class));

    private final TelemetryPipelineListener diagnosticListener;

    LocalFileSender(
        long intervalSeconds,
        LocalFileLoader localFileLoader,
        TelemetryPipeline telemetryPipeline,
        boolean suppressWarnings) { // used to suppress warnings from statsbeat
        this.localFileLoader = localFileLoader;
        this.telemetryPipeline = telemetryPipeline;

        diagnosticListener =
            suppressWarnings
                ? TelemetryPipelineListener.noop()
                : new DiagnosticTelemetryPipelineListener(
                "Sending telemetry to the ingestion service (retry from disk)", false);

        scheduledExecutor.scheduleWithFixedDelay(
            this, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
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
                CompletableResultCode resultCode =
                    telemetryPipeline.send(
                        singletonList(persistedFile.rawBytes),
                        persistedFile.connectionString,
                        TelemetryPipelineListener.composite(
                            diagnosticListener,
                            new LocalFileSenderTelemetryPipelineListener(
                                localFileLoader, persistedFile.file)));
                resultCode.join(30, TimeUnit.SECONDS); // wait max 30 seconds for request to be completed.
            }
        } catch (RuntimeException ex) {
            logger.error(
                "Unexpected error occurred while sending telemetries from the local storage.", ex);
        }
    }
}
