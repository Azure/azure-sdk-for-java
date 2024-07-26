// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import java.time.Duration;

/**
 * <p>
 *  {@code ThresholdBasedAvailabilityStrategy} is a type which captures settings
 *  concerning the {@code threshold} and {@code thresholdStep}. What these settings
 *  enforce is the time after which the Cosmos DB SDK reaches out to a different region
 *  (enforced by the {@link CosmosClientBuilder#getPreferredRegions()} property) in case the first
 *  preferred region didn't respond in time.
 * </p>
 *
 * <p>
 *
 * </p>
 */
public final class ThresholdBasedAvailabilityStrategy extends AvailabilityStrategy {
    private static final Duration DEFAULT_THRESHOLD = Duration.ofMillis(500);
    private static final Duration DEFAULT_THRESHOLD_STEP = Duration.ofMillis(100);
    private final Duration threshold;
    private final Duration thresholdStep;

    private final String toStringValue;

    /**
     * Instantiates a new Threshold based retry availability strategy.
     */
    public ThresholdBasedAvailabilityStrategy() {
        this.threshold = DEFAULT_THRESHOLD;
        this.thresholdStep = DEFAULT_THRESHOLD_STEP;
        this.toStringValue = getCachedStringValue(DEFAULT_THRESHOLD, DEFAULT_THRESHOLD_STEP);
    }

    /**
     * Instantiates a new Threshold based retry availability strategy.
     *
     * @param threshold     the threshold at which the request has to be tried on next region
     * @param thresholdStep the threshold step at which the request has to be tried on subsequent regions
     */
    public ThresholdBasedAvailabilityStrategy(Duration threshold, Duration thresholdStep) {
        validateDuration(threshold);
        validateDuration(thresholdStep);
        this.threshold = threshold;
        this.thresholdStep = thresholdStep;
        this.toStringValue = getCachedStringValue(threshold, thresholdStep);
    }

    private static String getCachedStringValue(Duration threshold, Duration thresholdStep) {
        return "{" + "threshold=" + threshold + ", step=" + thresholdStep + "}";
    }

    private static void validateDuration(Duration threshold) {
        if (threshold == null || threshold.isNegative()) {
            throw new IllegalArgumentException("threshold should be a non negative Duration");
        }
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

    @Override
    public String toString() {
        return toStringValue;
    }
}
