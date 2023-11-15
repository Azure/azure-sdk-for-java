package com.azure.storage.stress;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProvider;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public abstract class StorageStressScenario {
    private static final ClientLogger LOGGER = new ClientLogger(StorageStressScenario.class);
    private final long testTimeSeconds;
    protected static final Tracer TRACER = TracerProvider.getDefaultProvider().createTracer("StorageStressScenario", null, null, null);
    private final AtomicInteger successfulRuns =  new AtomicInteger();
    private final AtomicInteger failedRuns = new AtomicInteger();
    private volatile boolean done = false;

    public StorageStressScenario(StressScenarioBuilder builder) {
        testTimeSeconds = builder.getTestTimeSeconds();
    }

    public long getTestTimeSeconds() {
        return testTimeSeconds;
    }

    public void setup() {}

    public abstract void run(Duration timeout);
    public abstract Mono<Void> runAsync();

    public void teardown() {}

    protected void trackSuccess(Context span) {
        LOGGER.atInfo()
            .addKeyValue("status", "success")
            .log("run ended");
        TRACER.end(null, null, span);
        successfulRuns.incrementAndGet();
    }

    protected void trackMismatch(Context span) {
        LOGGER.atInfo()
                .addKeyValue("status", "content mismatch")
                .log("run ended");
        TRACER.setAttribute("error.type", "content mismatch", span);
        TRACER.end("content mismatch", null, span);
        failedRuns.incrementAndGet();
    }

    protected void trackFailure(Context span, Throwable ex) {
        LOGGER.atInfo()
            .addKeyValue("status", "failed")
            .log("run ended", ex);
        TRACER.setAttribute("error.type", ex.getClass().getName(), span);
        TRACER.end(null, ex, span);
        failedRuns.incrementAndGet();
    }

    protected void trackCancellation(Context span) {
        LOGGER.atInfo()
                .addKeyValue("status", "cancelled")
                .log("run ended");

        if (done) {
            return;
        }
        TRACER.setAttribute("error.type", "cancelled", span);
        TRACER.end("cancelled", null, span);
        failedRuns.incrementAndGet();
    }

    public void done() {
        if (!done) {
            LOGGER.atInfo().log("done");
            done = true;
        }
    }

    public int getSuccessfulRunCount() {
        return successfulRuns.get();
    }

    public int getFailedRunCount() {
        return failedRuns.get();
    }
}
