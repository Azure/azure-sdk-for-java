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

import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipeline;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipelineListener;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipelineRequest;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipelineResponse;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.StatusCode;
import io.opentelemetry.sdk.common.CompletableResultCode;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public class LocalStorageTelemetryPipelineListener implements TelemetryPipelineListener {

    private final LocalFileWriter localFileWriter;
    private final LocalFileSender localFileSender;
    private final LocalFilePurger localFilePurger;

    private final AtomicBoolean shutdown = new AtomicBoolean();

    // telemetryFolder must already exist and be writable
    public LocalStorageTelemetryPipelineListener(
        int diskPersistenceMaxSizeMb,
        File telemetryFolder,
        TelemetryPipeline pipeline,
        LocalStorageStats stats,
        boolean suppressWarnings) { // used to suppress warnings from statsbeat

        LocalFileCache localFileCache = new LocalFileCache(telemetryFolder);
        LocalFileLoader loader =
            new LocalFileLoader(localFileCache, telemetryFolder, stats, suppressWarnings);
        localFileWriter =
            new LocalFileWriter(
                diskPersistenceMaxSizeMb, localFileCache, telemetryFolder, stats, suppressWarnings);

        // send persisted telemetries from local disk every 30 seconds by default.
        // if diskPersistenceMaxSizeMb is greater than 50, it will get changed to 10 seconds.
        long intervalSeconds = diskPersistenceMaxSizeMb > 50 ? 10 : 30;
        localFileSender = new LocalFileSender(intervalSeconds, loader, pipeline, suppressWarnings);
        localFilePurger = new LocalFilePurger(telemetryFolder, suppressWarnings);
    }

    @Override
    public void onResponse(TelemetryPipelineRequest request, TelemetryPipelineResponse response) {
        if (StatusCode.isRetryable(response.getStatusCode())) {
            localFileWriter.writeToDisk(request.getConnectionString(), request.getTelemetry());
        }
    }

    @Override
    public void onException(
        TelemetryPipelineRequest request, String errorMessage, Throwable throwable) {
        localFileWriter.writeToDisk(request.getConnectionString(), request.getTelemetry());
    }

    @Override
    public CompletableResultCode shutdown() {
        // guarding against multiple shutdown calls because this can get called if statsbeat shuts down
        // early because it cannot reach breeze and later on real shut down (when running not as agent)
        if (!shutdown.getAndSet(true)) {
            localFileSender.shutdown();
            localFilePurger.shutdown();
        }
        return CompletableResultCode.ofSuccess();
    }
}
