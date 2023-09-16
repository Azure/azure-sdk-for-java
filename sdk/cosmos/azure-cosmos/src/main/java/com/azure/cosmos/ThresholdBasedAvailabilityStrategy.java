// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The type Threshold based retry availability strategy.
 */
public final class ThresholdBasedAvailabilityStrategy extends AvailabilityStrategy {
    private static final Duration DEFAULT_THRESHOLD = Duration.ofMillis(500);
    private static final Duration DEFAULT_THRESHOLD_STEP = Duration.ofMillis(100);
    private volatile Duration threshold;
    private volatile Duration thresholdStep;
    private String toStringValue;

    /**
     * Instantiates a new Threshold based retry availability strategy.
     */
    public ThresholdBasedAvailabilityStrategy() {
        this.threshold = DEFAULT_THRESHOLD;
        this.thresholdStep = DEFAULT_THRESHOLD_STEP;
        this.toStringValue = getCachedStringValue(threshold, thresholdStep);
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
     * Sets the threshold at which the request will be retried on the next region.
     *
     * @param threshold the threshold to be set. */
    public void setThreshold(Duration threshold) {
        validateDuration(threshold);
        this.threshold = threshold;
        this.toStringValue = getCachedStringValue(this.threshold, this.thresholdStep);
    }

    /**
     * Sets the threshold step at which the request will be retried on subsequent regions.
     *
     * @param thresholdStep the threshold step to be set.
     * */
    public void setThresholdStep(Duration thresholdStep) {
        validateDuration(thresholdStep);
        this.thresholdStep = thresholdStep;
        this.toStringValue = getCachedStringValue(this.threshold, this.thresholdStep);
    }

    @Override
    public String toString() {
        return this.toStringValue;
    }

    private static String getCachedStringValue(Duration threshold, Duration thresholdStep) {
        return "{" + "threshold=" + threshold + ", step=" + thresholdStep + "}";
    }

    private static void validateDuration(Duration threshold) {
        if (threshold == null || threshold.isNegative()) {
            throw new IllegalArgumentException("threshold should be a non negative Duration");
        }
    }
}
