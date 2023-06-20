// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Threshold based retry availability strategy.
 */
public final class ThresholdBasedAvailabilityStrategy extends AvailabilityStrategy {
    private static final Duration DEFAULT_THRESHOLD = Duration.ofMillis(500);
    private static final Duration DEFAULT_THRESHOLD_STEP = Duration.ofMillis(100);
    private Duration threshold;
    private Duration thresholdStep;

    /**
     * Instantiates a new Threshold based retry availability strategy.
     */
    public ThresholdBasedAvailabilityStrategy() {
        this.threshold = DEFAULT_THRESHOLD;
        this.thresholdStep = DEFAULT_THRESHOLD_STEP;
    }

    /**
     * Instantiates a new Threshold based retry availability strategy.
     *
     * @param threshold            the threshold
     * @param thresholdStep        the threshold step
     */
    public ThresholdBasedAvailabilityStrategy(Duration threshold, Duration thresholdStep) {
        this.threshold = threshold;
        this.thresholdStep = thresholdStep;
    }

    /**
     * Gets threshold.
     *
     * @return the threshold
     */
    public Duration getThreshold() {
        return this.threshold;
    }


    /**
     * Gets threshold step.
     *
     * @return the threshold step
     */
    public Duration getThresholdStep() {
        return this.thresholdStep;
    }

    /**
     * Sets threshold.
     *
     * @param threshold the threshold
     */
    public void setThreshold(Duration threshold) {
        this.threshold = threshold;
    }

    /**
     * Sets threshold step.
     *
     * @param thresholdStep the threshold step
     */
    public void setThresholdStep(Duration thresholdStep) {
        this.thresholdStep = thresholdStep;
    }

}
