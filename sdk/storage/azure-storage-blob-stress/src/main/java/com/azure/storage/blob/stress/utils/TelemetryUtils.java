package com.azure.storage.blob.stress.utils;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProvider;

import java.util.concurrent.atomic.AtomicInteger;


public class TelemetryUtils {
    private static final Tracer TRACER = TracerProvider.getDefaultProvider().createTracer("TelemetryUtils", null, null, null);
    private static final ClientLogger LOGGER = new ClientLogger(TelemetryUtils.class);
    private final AtomicInteger successfulRuns =  new AtomicInteger();
    private final AtomicInteger failedRuns = new AtomicInteger();

    public static Tracer getTracer() {
        return TRACER;
    }

    public void trackSuccess(Context span) {
        LOGGER.atInfo()
            .addKeyValue("status", "success")
            .log("run ended");
        TRACER.end(null, null, span);
        successfulRuns.incrementAndGet();
    }

    public void trackMismatch(Context span) {
        LOGGER.atInfo()
                .addKeyValue("status", "content mismatch")
                .log("run ended");
        TRACER.setAttribute("error.type", "content mismatch", span);
        TRACER.end("content mismatch", null, span);
        failedRuns.incrementAndGet();
    }

    public void trackFailure(Context span, Throwable ex) {
        LOGGER.atInfo()
            .addKeyValue("status", "failed")
            .log("run ended", ex);
        TRACER.setAttribute("error.type", ex.getClass().getName(), span);
        TRACER.end(null, ex, span);
        failedRuns.incrementAndGet();
    }

    public void trackCancellation(Context span) {
        LOGGER.atInfo()
                .addKeyValue("status", "cancelled")
                .log("run ended");

        TRACER.setAttribute("error.type", "cancelled", span);
        TRACER.end("cancelled", null, span);
        failedRuns.incrementAndGet();
    }

    public int getSuccessfulRunCount() {
        return successfulRuns.get();
    }

    public int getFailedRunCount() {
        return failedRuns.get();
    }
}
