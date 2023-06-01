package com.azure.storage.stress;

import com.azure.core.http.HttpClient;

public abstract class StressScenarioBuilder {
    private int parallel;

    private long testTimeSeconds;

    private HttpClient faultInjectingClient;

    public abstract StorageStressScenario build();

    public int getParallel() {
        return parallel;
    }

    public void setParallel(int parallel) {
        this.parallel = parallel;
    }

    public long getTestTimeSeconds() {
        return testTimeSeconds;
    }

    public void setTestTimeSeconds(long testTimeNanoseconds) {
        this.testTimeSeconds = testTimeNanoseconds;
    }

    public HttpClient getFaultInjectingClient() {
        return faultInjectingClient;
    }

    public void setFaultInjectingClient(HttpClient faultInjectingClient) {
        this.faultInjectingClient = faultInjectingClient;
    }
}
