// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.availabilitystrategy;

import java.time.Duration;
import java.util.List;

/**
 * The type Threshold based retry availability strategy.
 */
public class ThresholdBasedAvailabilityStrategy extends AvailabilityStrategy{
    private static final Duration DEFAULT_THRESHOLD_IN_MILLISECONDS = Duration.ofMillis(500);
    private static final Duration DEFAULT_THRESHOLD_STEP_IN_MILLISECONDS = Duration.ofMillis(100);
    private Duration threshold;
    private Duration thresholdStep;

    /**
     * Instantiates a new Threshold based retry availability strategy.
     */
    public ThresholdBasedAvailabilityStrategy() {
        this.threshold = DEFAULT_THRESHOLD_IN_MILLISECONDS;
        this.thresholdStep = DEFAULT_THRESHOLD_STEP_IN_MILLISECONDS;
    }

    /**
     * Instantiates a new Threshold based retry availability strategy.
     *
     * @param threshold            the threshold
     * @param thresholdStep        the threshold step
     * @param numberOfRegionsToTry the number of regions to retry
     */
    public ThresholdBasedAvailabilityStrategy(Duration threshold, Duration thresholdStep, int numberOfRegionsToTry) {
        this.threshold = threshold;
        this.thresholdStep = thresholdStep;
        this.numberOfRegionsToTry = numberOfRegionsToTry;
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

    @Override
    public List<String> getEffectiveRetryRegions(List<String> preferredRegions, List<String> excludeRegions) {
        preferredRegions.removeAll(excludeRegions); // remove all mutates the original list
        return preferredRegions.subList(0, this.numberOfRegionsToTry);
    }

}
