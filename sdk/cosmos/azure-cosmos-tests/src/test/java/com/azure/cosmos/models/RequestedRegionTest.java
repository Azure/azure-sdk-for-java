// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link RequestedRegion} value-type semantics (AC1 / spec section 3.1).
 */
public class RequestedRegionTest {

    @Test(groups = {"unit"})
    public void gettersReturnConstructorArguments() {
        RequestedRegion region = new RequestedRegion("East US", RequestedRegionReason.INITIAL);

        assertThat(region.getRegionName()).isEqualTo("East US");
        assertThat(region.getReason()).isEqualTo(RequestedRegionReason.INITIAL);
    }

    @Test(groups = {"unit"})
    public void equalsAndHashCodeAreCaseInsensitiveOnRegionName() {
        RequestedRegion a = new RequestedRegion("East US", RequestedRegionReason.HEDGING);
        RequestedRegion b = new RequestedRegion("east us", RequestedRegionReason.HEDGING);
        RequestedRegion c = new RequestedRegion("EAST US", RequestedRegionReason.HEDGING);

        assertThat(a).isEqualTo(b);
        assertThat(a).isEqualTo(c);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a.hashCode()).isEqualTo(c.hashCode());
    }

    @Test(groups = {"unit"})
    public void equalsDistinguishesByReason() {
        RequestedRegion initial = new RequestedRegion("East US", RequestedRegionReason.INITIAL);
        RequestedRegion hedge = new RequestedRegion("East US", RequestedRegionReason.HEDGING);

        assertThat(initial).isNotEqualTo(hedge);
    }

    @Test(groups = {"unit"})
    public void equalsHandlesNullAndOtherTypes() {
        RequestedRegion region = new RequestedRegion("East US", RequestedRegionReason.INITIAL);

        assertThat(region.equals(null)).isFalse();
        assertThat(region.equals("East US")).isFalse();
        assertThat(region).isEqualTo(region);
    }

    @Test(groups = {"unit"})
    public void toStringFormatIsRegionColonReason() {
        RequestedRegion region = new RequestedRegion("West US 2", RequestedRegionReason.OPERATION_RETRY);

        assertThat(region.toString()).isEqualTo("West US 2:OPERATION_RETRY");
    }

    @Test(groups = {"unit"})
    public void constructorRejectsNullRegionName() {
        assertThatThrownBy(() -> new RequestedRegion(null, RequestedRegionReason.INITIAL))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("regionName");
    }

    @Test(groups = {"unit"})
    public void constructorRejectsNullReason() {
        assertThatThrownBy(() -> new RequestedRegion("East US", null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("reason");
    }

    @Test(groups = {"unit"})
    public void enumContainsAllVariants() {
        // Aligned with the cross-SDK RequestedRegionReason taxonomy (matches the merged
        // .NET hedging-detection enum, which exposes UNKNOWN as the zero/default sentinel).
        RequestedRegionReason[] values = RequestedRegionReason.values();

        assertThat(values).containsExactlyInAnyOrder(
            RequestedRegionReason.UNKNOWN,
            RequestedRegionReason.INITIAL,
            RequestedRegionReason.OPERATION_RETRY,
            RequestedRegionReason.TRANSPORT_RETRY,
            RequestedRegionReason.HEDGING,
            RequestedRegionReason.REGION_FAILOVER,
            RequestedRegionReason.CIRCUIT_BREAKER_PROBE);
    }
}
