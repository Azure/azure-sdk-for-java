package com.azure.tools.benchmark;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;

public class BenchmarkResults {

    @JsonProperty("id")
    private final String benchmarkId;

    @JsonProperty("requests")
    private final Long requests;

    @JsonProperty("rps")
    private final Float requestsPerSecond;

    @JsonProperty("parallel")
    private final int parallel;

    @JsonProperty("duration")
    private final Duration timeTaken;

    public BenchmarkResults(String benchmarkId, int parallel, Duration timeTaken, Long requests) {
        this.benchmarkId = benchmarkId;
        this.timeTaken = timeTaken;
        this.requestsPerSecond = requests.floatValue() / Long.valueOf(timeTaken.getSeconds()).floatValue();
        this.requests = requests;
        this.parallel = parallel;
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

    public String getBenchmarkId() {
        return benchmarkId;
    }
}
