package com.azure.cosmos.models;

import java.time.Duration;

public class FaultInjectionServerErrorResult implements IFaultInjectionResult{
    private final FaultInjectionServerErrorType serverErrorType;
    private final int times;
    private final Duration delay;

    public FaultInjectionServerErrorResult(FaultInjectionServerErrorType serverErrorTypes, int times, Duration delay) {
        this.serverErrorType = serverErrorTypes;
        this.times = times;
        this.delay = delay;
    }

    public FaultInjectionServerErrorType getServerErrorType() {
        return serverErrorType;
    }

    public int getTimes() {
        return times;
    }

    public Duration getDelay() {
        return delay;
    }
}
