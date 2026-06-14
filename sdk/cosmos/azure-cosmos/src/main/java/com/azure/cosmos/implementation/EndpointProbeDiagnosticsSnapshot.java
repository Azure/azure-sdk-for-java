// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import java.time.Instant;

/**
 * Immutable, intentionally-lean snapshot of {@link EndpointProbeClient} state for
 * client diagnostics. Exposes only the outcome of the most recent probe cycle
 * (or {@code null} if no cycle has run yet) and the wall-clock time at which that
 * state was last updated. Richer details (per-endpoint failures, hysteresis
 * counters, thresholds) are intentionally not surfaced here to keep the
 * diagnostic shape stable across releases &mdash; consult the logs for those
 * details.
 */
public final class EndpointProbeDiagnosticsSnapshot {

    private final Boolean lastCycleSuccess;
    private final Instant lastStateUpdatedAt;

    EndpointProbeDiagnosticsSnapshot(Boolean lastCycleSuccess, Instant lastStateUpdatedAt) {
        this.lastCycleSuccess = lastCycleSuccess;
        this.lastStateUpdatedAt = lastStateUpdatedAt;
    }

    /**
     * @return {@code true} if the most recent cycle was GREEN, {@code false} if RED,
     * {@code null} if no cycle has completed yet.
     */
    public Boolean getLastCycleSuccess() {
        return lastCycleSuccess;
    }

    /**
     * @return wall-clock instant at which {@link #getLastCycleSuccess()} was last updated,
     * or {@code null} if never.
     */
    public Instant getLastStateUpdatedAt() {
        return lastStateUpdatedAt;
    }
}
