package com.azure.storage.stress;

import reactor.core.publisher.Mono;

public abstract class StorageStressScenario {

    private final long testTimeNanoseconds;

    public StorageStressScenario(StressScenarioBuilder builder) {
        testTimeNanoseconds = builder.getTestTimeNanoseconds();
    }

    public long getTestTimeNanoseconds() {
        return testTimeNanoseconds;
    }

    public void globalSetup() {}
    public void setup() {}

    public abstract void run();
    public abstract Mono<Void> runAsync();

    public void teardown() {}
    public void globalTeardown() {}
}
