// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import java.util.Locale;
import java.util.Objects;

/**
 * Represents a single Azure Cosmos DB region the SDK dispatched a request to, tagged with the
 * reason for dispatch.
 * <p>
 * Instances are immutable and safe to share across threads. Region-name comparison via
 * {@link #equals(Object)} and {@link #hashCode()} is case-insensitive to match the SDK's
 * region-normalization conventions.
 * <p>
 * <strong>Contract:</strong> a {@code RequestedRegion} reflects the SDK's decision to dispatch
 * a request to a region (post-threshold-delay, post-non-cancellation for hedge arms); it does
 * <em>not</em> guarantee a wire-issued request. See
 * {@link com.azure.cosmos.CosmosDiagnostics#getRequestedRegions()} for the full "dispatched,
 * not necessarily wire-issued" semantic.
 */
public final class RequestedRegion {

    private final String regionName;
    private final RequestedRegionReason reason;

    /**
     * Creates a new {@link RequestedRegion}.
     *
     * @param regionName the human-readable region name (for example {@code "East US"}); must not be {@code null}.
     * @param reason the {@link RequestedRegionReason} describing why the SDK dispatched to this region; must not be {@code null}.
     * @throws NullPointerException if either argument is {@code null}.
     */
    public RequestedRegion(String regionName, RequestedRegionReason reason) {
        this.regionName = Objects.requireNonNull(regionName, "regionName must not be null");
        this.reason = Objects.requireNonNull(reason, "reason must not be null");
    }

    /**
     * Returns the human-readable region name.
     *
     * @return the region name; never {@code null}.
     */
    public String getRegionName() {
        return this.regionName;
    }

    /**
     * Returns the reason the SDK dispatched a request to this region.
     *
     * @return the {@link RequestedRegionReason}; never {@code null}.
     */
    public RequestedRegionReason getReason() {
        return this.reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RequestedRegion)) {
            return false;
        }
        RequestedRegion that = (RequestedRegion) o;
        return this.regionName.equalsIgnoreCase(that.regionName) && this.reason == that.reason;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.regionName.toLowerCase(Locale.ROOT), this.reason);
    }

    @Override
    public String toString() {
        return this.regionName + ":" + this.reason;
    }
}
