// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import java.time.Instant;

/**
 * Immutable, intentionally-lean snapshot of {@link EndpointProbeClient} state for
 * client diagnostics. Exposes the effective routing-gate health after the most recent
 * state update ({@code null} if no probe state has been recorded yet), the wall-clock
 * time at which that state was last updated, and the per-region counts that drive the
 * gate (how many thin-client regions are currently known, and how many of those have a
 * cached successful probe). Per-endpoint failure reasons are intentionally not surfaced
 * here to keep the diagnostic shape stable across releases &mdash; consult the logs for
 * those details.
 */
public final class EndpointProbeDiagnosticsSnapshot {

    private final Boolean lastCycleSuccess;
    private final Instant lastStateUpdatedAt;
    private final int knownRegionCount;
    private final int succeededRegionCount;

    EndpointProbeDiagnosticsSnapshot(
        Boolean lastCycleSuccess,
        Instant lastStateUpdatedAt,
        int knownRegionCount,
        int succeededRegionCount) {
        this.lastCycleSuccess = lastCycleSuccess;
        this.lastStateUpdatedAt = lastStateUpdatedAt;
        this.knownRegionCount = knownRegionCount;
        this.succeededRegionCount = succeededRegionCount;
    }

    /**
     * @return {@code true} if, after the most recent state update, every currently-known
     * thin-client region has a cached successful probe (routing gate is healthy);
     * {@code false} if the gate is unhealthy; {@code null} if no probe state has been
     * recorded yet.
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

    /**
     * @return number of thin-client regional endpoints in the most recently observed
     * (non-empty) topology that the probe gate is evaluated against.
     */
    public int getKnownRegionCount() {
        return knownRegionCount;
    }

    /**
     * @return number of currently-known thin-client regional endpoints that have a cached
     * successful probe. The routing gate is healthy when this equals
     * {@link #getKnownRegionCount()} and that count is greater than zero.
     */
    public int getSucceededRegionCount() {
        return succeededRegionCount;
    }
}
