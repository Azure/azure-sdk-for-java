// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress.utils;

import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProvider;
import com.azure.storage.stress.StorageStressOptions;

import java.util.concurrent.atomic.AtomicInteger;

public class TelemetryHelper {
    private final Tracer tracer;
    private final ClientLogger logger;
    private final AtomicInteger successfulRuns = new AtomicInteger();
    private final AtomicInteger failedRuns = new AtomicInteger();
    private final String scenarioName;

    public TelemetryHelper(String scenarioName) {
        this.scenarioName = scenarioName;
        this.tracer = TracerProvider.getDefaultProvider().createTracer("scenarioName", null, null, null);
        this.logger = new ClientLogger(scenarioName);
    }

    public void trackSuccess(Context span) {
        logger.atInfo()
            .addKeyValue("status", "success")
            .log("run ended");
        tracer.end(null, null, span);
        successfulRuns.incrementAndGet();
    }

    public void trackMismatch(Context span) {
        logger.atInfo()
            .addKeyValue("status", "content mismatch")
            .log("run ended");
        tracer.setAttribute("error.type", "content mismatch", span);
        tracer.end("content mismatch", null, span);
        failedRuns.incrementAndGet();
    }

    public void trackFailure(Context span, Throwable ex) {
        logger.atInfo()
            .addKeyValue("status", "failed")
            .log("run ended", ex);
        tracer.setAttribute("error.type", ex.getClass().getName(), span);
        tracer.end(null, ex, span);
        failedRuns.incrementAndGet();
    }

    public void trackCancellation(Context span) {
        logger.atInfo()
            .addKeyValue("status", "cancelled")
            .log("run ended");

        tracer.setAttribute("error.type", "cancelled", span);
        tracer.end("cancelled", null, span);
        failedRuns.incrementAndGet();
    }

    public void logStart(StorageStressOptions options) {
        String storageBlobPackageVersion = "unknown";
        try {
            Class<?> storageBlobBusPackage = Class.forName("com.azure.storage.blob.BlobServiceClientBuilder");
            storageBlobPackageVersion = storageBlobBusPackage.getPackage().getImplementationVersion();
            if (storageBlobPackageVersion == null) {
                storageBlobPackageVersion = "null";
            }
        } catch (ClassNotFoundException e) {
            logger.warning("could not find BlobServiceClientBuilder class", e);
        }

        logger.atInfo()
            .addKeyValue("duration", options.getDuration())
            .addKeyValue("blobName", options.getBlobName())
            .addKeyValue("blobSize", options.getSize())
            .addKeyValue("concurrency", options.getParallel())
            .addKeyValue("faultInjection", options.isFaultInjectionEnabled())
            .addKeyValue("storageBlobPackageVersion", storageBlobPackageVersion)
            .addKeyValue("sync", options.isSync())
            .addKeyValue("scenarioName", scenarioName)
            .addKeyValue("connectionStringProvided", !CoreUtils.isNullOrEmpty(options.getConnectionString()))
            .log("starting test");
    }

    public void logEnd() {
        logger.atInfo()
            .addKeyValue("scenarioName", scenarioName)
            .addKeyValue("succeeded", successfulRuns.get())
            .addKeyValue("failed", failedRuns.get())
            .log("test finished");
    }

    public Tracer getTracer() {
        return tracer;
    }
}
