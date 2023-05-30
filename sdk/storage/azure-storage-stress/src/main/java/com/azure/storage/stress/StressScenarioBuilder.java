package com.azure.storage.stress;

import com.azure.core.http.HttpClient;

public abstract class StressScenarioBuilder {
    private int parallel;

    private long testTimeNanoseconds;

    private HttpClient faultInjectingClient;

    public abstract StorageStressScenario build();

    public int getParallel() {
        return parallel;
    }

    public void setParallel(int parallel) {
        this.parallel = parallel;
    }

    public long getTestTimeNanoseconds() {
        return testTimeNanoseconds;
    }

    public void setTestTimeNanoseconds(long testTimeNanoseconds) {
        this.testTimeNanoseconds = testTimeNanoseconds;
    }

    public HttpClient getFaultInjectingClient() {
        return faultInjectingClient;
    }

    public void setFaultInjectingClient(HttpClient faultInjectingClient) {
        this.faultInjectingClient = faultInjectingClient;
    }
}
