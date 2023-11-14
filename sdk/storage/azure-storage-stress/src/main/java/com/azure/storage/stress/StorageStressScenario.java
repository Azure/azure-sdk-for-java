package com.azure.storage.stress;

import com.azure.core.util.logging.ClientLogger;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;


public abstract class StorageStressScenario {
    private static final ClientLogger LOGGER = new ClientLogger(StorageStressScenario.class);
    private final long testTimeSeconds;

    private AtomicInteger successfulRuns =  new AtomicInteger();
    private AtomicInteger failedRuns = new AtomicInteger();

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

    protected void trackSuccess(Span span) {
        LOGGER.atInfo()
            .addKeyValue("status", "success")
            .log("run ended");
        span.end();
        successfulRuns.incrementAndGet();
    }

    protected void trackFailure(Span span, Throwable ex) {
        LOGGER.atInfo()
            .addKeyValue("status", "failed")
            .log("run ended", ex);
        span.setStatus(StatusCode.ERROR, ex.getMessage());
        span.end();
        failedRuns.incrementAndGet();
    }

    protected void trackCancellation(Span span) {
        LOGGER.atInfo()
            .addKeyValue("status", "cancelled")
            .log("run ended");
        span.setStatus(StatusCode.ERROR, "cancelled");
        span.end();
        failedRuns.incrementAndGet();
    }

    public int getSuccessfulRunCount() {
        return successfulRuns.get();
    }

    public int getFailedRunCount() {
        return failedRuns.get();
    }
}
