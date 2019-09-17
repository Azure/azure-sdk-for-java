package com.microsoft.storageperf;

import java.time.Duration;

public class BenchmarkResults {
    private final Duration timeTaken;
    private final Long requests;
    private final Float requestsPerSecond;

    public BenchmarkResults(Duration timeTaken, Long requests, Float requestsPerSecond) {
        this.timeTaken = timeTaken;
        this.requestsPerSecond = requestsPerSecond;
        this.requests = requests;
    }

    public Duration getTimeTaken() {
        return timeTaken;
    }

    public Float getRequestsPerSecond() {
        return requestsPerSecond;
    }

    public Long getRequests() {
        return requests;
    }
}
