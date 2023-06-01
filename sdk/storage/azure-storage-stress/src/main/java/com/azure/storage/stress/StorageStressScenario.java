package com.azure.storage.stress;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public abstract class StorageStressScenario {

    private final long testTimeSeconds;

    private int successfulRuns = 0;
    private List<String> failedRunMessages = new ArrayList<>();

    public StorageStressScenario(StressScenarioBuilder builder) {
        testTimeSeconds = builder.getTestTimeSeconds();
    }

    public long getTestTimeSeconds() {
        return testTimeSeconds;
    }

    public void globalSetup() {}
    public void setup() {}

    public abstract void run(Duration timeout);
    public abstract Mono<Void> runAsync();

    public void teardown() {}
    public void globalTeardown() {}

    protected void logSuccess() {
        successfulRuns += 1;
    }

    protected void logFailure(String message) {
        failedRunMessages.add(message);
    }

    public int getSuccessfulRunCount() {
        return successfulRuns;
    }

    public int getFailedRunCount() {
        return failedRunMessages.size();
    }
}
