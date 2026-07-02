// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

/**
 * Describes why the Azure Cosmos DB Java SDK dispatched a request to a specific region.
 * <p>
 * Used in combination with {@link RequestedRegion} on
 * {@link com.azure.cosmos.CosmosDiagnostics#getRequestedRegions()} and
 * {@link com.azure.cosmos.CosmosDiagnosticsContext#getRequestedRegions()}.
 * <p>
 * <strong>This enum is non-exhaustive.</strong> Future SDK versions may add additional values,
 * so callers MUST include a {@code default:} arm when switching on this enum to remain forward
 * compatible.
 */
public enum RequestedRegionReason {

    /**
     * Default sentinel value. The SDK does <strong>not</strong> emit this value from a real
     * dispatch; its presence indicates the {@link RequestedRegion} was produced by default
     * construction (for example a deserialized value where the reason was absent) rather than
     * by an SDK dispatch. Exposed so the Java enum stays aligned with the cross-SDK
     * {@code RequestedRegionReason} taxonomy, where it is the zero/default value.
     */
    UNKNOWN,

    /**
     * The initial attempt for the operation. Every operation has exactly one {@code INITIAL}
     * entry tied to the first region the SDK targeted.
     */
    INITIAL,

    /**
     * A retry triggered by the SDK's operation-level retry policy (for example, retrying the
     * same operation after a transient failure observed in another region).
     */
    OPERATION_RETRY,

    /**
     * A retry triggered by the direct-mode transport layer (for example, a {@code 410 Gone}
     * retry handled by {@code GoneAndRetryWithRetryPolicy}).
     */
    TRANSPORT_RETRY,

    /**
     * A request dispatched to a hedge region as part of a configured cross-region availability
     * strategy (such as {@link com.azure.cosmos.ThresholdBasedAvailabilityStrategy}).
     */
    HEDGING,

    /**
     * A request dispatched as part of region failover (for example, the previously preferred
     * region returned a {@code 503} and the SDK moved to the next region).
     */
    REGION_FAILOVER,

    /**
     * A probe request dispatched by the per-partition circuit breaker after the partition was
     * marked unavailable in a region.
     */
    CIRCUIT_BREAKER_PROBE
}
