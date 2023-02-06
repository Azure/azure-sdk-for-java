package com.azure.cosmos.models;

import java.time.Duration;

public class FaultInjectionConnectionErrorResult implements IFaultInjectionResult{
    private final FaultInjectionConnectionErrorType errorTypes;
    private Duration interval;
    private double threshold;

    public FaultInjectionConnectionErrorResult(
        FaultInjectionConnectionErrorType errorTypes,
        Duration interval,
        double threshold) {
        this.errorTypes = errorTypes;
        this.interval = interval;
        this.threshold = threshold;
    }

    public FaultInjectionConnectionErrorType getErrorTypes() {
        return errorTypes;
    }

    public Duration getInterval() {
        return interval;
    }

    public double getThreshold() {
        return threshold;
    }
}
