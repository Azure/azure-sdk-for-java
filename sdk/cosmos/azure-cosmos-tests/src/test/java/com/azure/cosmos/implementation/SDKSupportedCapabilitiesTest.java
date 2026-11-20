// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SDKSupportedCapabilitiesTest {

    @Test(groups = {"unit"})
    public void capabilityBitValues() {
        assertThat(HttpConstants.SDKSupportedCapabilities.NONE).isEqualTo(0L);
        assertThat(HttpConstants.SDKSupportedCapabilities.PARTITION_MERGE).isEqualTo(1L);
        assertThat(HttpConstants.SDKSupportedCapabilities.CHANGE_FEED_WITH_START_TIME_POST_MERGE).isEqualTo(2L);
        assertThat(HttpConstants.SDKSupportedCapabilities.THROUGHPUT_BUCKETING).isEqualTo(4L);
        assertThat(HttpConstants.SDKSupportedCapabilities.IGNORE_UNKNOWN_RNTBD_TOKENS).isEqualTo(8L);
        assertThat(HttpConstants.SDKSupportedCapabilities.CHANGE_FEED_TOKEN_WITH_GCN).isEqualTo(16L);
    }

    @Test(groups = {"unit"})
    public void capabilityBitsDoNotOverlap() {
        long[] capabilities = {
            HttpConstants.SDKSupportedCapabilities.PARTITION_MERGE,
            HttpConstants.SDKSupportedCapabilities.CHANGE_FEED_WITH_START_TIME_POST_MERGE,
            HttpConstants.SDKSupportedCapabilities.THROUGHPUT_BUCKETING,
            HttpConstants.SDKSupportedCapabilities.IGNORE_UNKNOWN_RNTBD_TOKENS,
            HttpConstants.SDKSupportedCapabilities.CHANGE_FEED_TOKEN_WITH_GCN
        };

        long combined = 0;
        for (long cap : capabilities) {
            // Verify no overlap with previously combined flags
            assertThat(combined & cap)
                .as("Capability value %d overlaps with previously seen capabilities", cap)
                .isEqualTo(0L);
            combined |= cap;
        }
    }

    @Test(groups = {"unit"})
    public void supportedCapabilitiesIncludesExpectedFlags() {
        long expected =
            HttpConstants.SDKSupportedCapabilities.PARTITION_MERGE
                | HttpConstants.SDKSupportedCapabilities.CHANGE_FEED_WITH_START_TIME_POST_MERGE
                | HttpConstants.SDKSupportedCapabilities.IGNORE_UNKNOWN_RNTBD_TOKENS;

        assertThat(HttpConstants.SDKSupportedCapabilities.SUPPORTED_CAPABILITIES)
            .isEqualTo(String.valueOf(expected));
    }

    @Test(groups = {"unit"})
    public void supportedCapabilitiesNone() {
        assertThat(HttpConstants.SDKSupportedCapabilities.SUPPORTED_CAPABILITIES_NONE)
            .isEqualTo(String.valueOf(HttpConstants.SDKSupportedCapabilities.NONE));
        assertThat(HttpConstants.SDKSupportedCapabilities.SUPPORTED_CAPABILITIES_NONE)
            .isEqualTo("0");
    }

    @Test(groups = {"unit"})
    public void supportedCapabilitiesNumericValue() {
        // PARTITION_MERGE (1) | CHANGE_FEED_WITH_START_TIME_POST_MERGE (2) | IGNORE_UNKNOWN_RNTBD_TOKENS (8) = 11
        assertThat(HttpConstants.SDKSupportedCapabilities.SUPPORTED_CAPABILITIES).isEqualTo("11");
    }

    @Test(groups = {"unit"})
    public void supportedCapabilitiesContainsIgnoreUnknownRntbdTokens() {
        long value = Long.parseLong(HttpConstants.SDKSupportedCapabilities.SUPPORTED_CAPABILITIES);
        assertThat(value & HttpConstants.SDKSupportedCapabilities.IGNORE_UNKNOWN_RNTBD_TOKENS)
            .as("SUPPORTED_CAPABILITIES should include IGNORE_UNKNOWN_RNTBD_TOKENS flag")
            .isNotEqualTo(0L);
    }
}
