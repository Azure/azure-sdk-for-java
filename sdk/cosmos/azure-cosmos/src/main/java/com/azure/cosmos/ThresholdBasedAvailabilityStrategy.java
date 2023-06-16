// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Threshold based retry availability strategy.
 */
public final class ThresholdBasedAvailabilityStrategy extends AvailabilityStrategy{
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
        if (excludeRegions == null) {
            return preferredRegions
                .subList(0, this.numberOfRegionsToTry);
        }

        List<String> collectList = preferredRegions
            .stream()
            .filter(region -> !excludeRegions.contains(region))
            .collect(Collectors.toList());
        return collectList.subList(0, Math.min(numberOfRegionsToTry, collectList.size()));
    }

}
