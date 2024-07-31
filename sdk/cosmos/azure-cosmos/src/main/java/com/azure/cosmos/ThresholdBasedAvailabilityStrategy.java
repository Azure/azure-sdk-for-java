// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import java.time.Duration;

/**
 * <p>
 *  {@link ThresholdBasedAvailabilityStrategy} provides functionality for a data plane operation
 *  to be routed to the next preferred regions - (see {@link CosmosClientBuilder#getPreferredRegions()})
 *  defined for the Cosmos DB account if a given region did not respond in time. The response in case of a success is from
 *  the fastest responding region. This way the tail latency improves in case of transient delays from a particular region
 *  or even the availability improves if a particular replica or a partition is unhealthy in a particular region.
 *  </p>
 *
 * <p>
 *  {@link ThresholdBasedAvailabilityStrategy} captures settings concerning the {@code threshold} and {@code thresholdStep}. What these settings
 *  enforce is the time after which the Cosmos DB SDK reaches out to a different region
 *  (enforced by the {@link CosmosClientBuilder#getPreferredRegions()} property) in case the first
 *  preferred region didn't respond in time. Regions can be excluded here though - see {@link CosmosClientBuilder#getExcludedRegions()}.
 * </p>
 *
 * <p>
 *  Let's consider an example, say a Cosmos DB account is configured with 3 regions, namely - RegionA, RegionB and RegionC
 *  (regions chosen for illustrative purposes). Say, the downstream application issues a read operation to RegionA
 *  and RegionA does not respond within {@link ThresholdBasedAvailabilityStrategy#getThreshold()} duration, the read
 *  operation will be routed to RegionB. If RegionB does not respond in
 *  {@link ThresholdBasedAvailabilityStrategy#getThreshold()} + {@code (k-1)} * {@link ThresholdBasedAvailabilityStrategy#getThresholdStep()} duration, then the
 *  read operation will be routed to RegionC - ({@code k} is {@code k}th preferred region}. Effectively, whichever region responds with a successful response first is sent back to the
 *  downstream application.
 * </p>
 *
 * <p>
 *  It can so happen that the first preferred region responds with a non-transient error like a 400s, 401s, 403s, 404s, 405s, 409s, 412s, then
 *  the operation as a whole fails since at this point it is not a Cosmos DB service health issue which can be bypassed by sending an operation
 *  to a different region but more on the parameters of the operation and the service explicitly failing the operation with a status code which
 *  the Cosmos DB SDK deems as non-retriable.
 * </p>
 *
 * <p>
 *  NOTE: In order for {@link ThresholdBasedAvailabilityStrategy} to work for point write operations (create, upsert, replace, patch, delete), non-idempotent write
 *  retry policy has to be opted into - see {@link CosmosClientBuilder#nonIdempotentWriteRetryOptions(NonIdempotentWriteRetryOptions)}.
 * </p>
 *
 *  <p>
 *  NOTE: {@link ThresholdBasedAvailabilityStrategy} applies to multi-region accounts in case of reads and multi-write accounts for reads and writes to leverage
 *  the benefit from multiple regions for the account.
 *  </p>
 *
 *  <p>
 *  NOTE: Since multiple regions could be contacted, this could increase the aggregate RU utilized at an operation level and
 *  increase load on client-side compute as a tradeoff for tail latency and availability.
 *  </p>
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
