// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl;

import com.azure.cosmos.ThroughputControlGroupConfig;
import com.azure.cosmos.ThroughputControlGroupConfigBuilder;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class ThroughputControlGroupConfigTests {

    @Test(groups = "unit")
    public void throughputControlGroup_throughputBucket_priorityLevel() {
        // validate throughputBucket >= 0
        assertThatThrownBy(
            () -> new ThroughputControlGroupConfigBuilder()
                    .groupName("throughputBucket-" + UUID.randomUUID())
                    .throughputBucket(-1)
                    .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Throughput bucket should be no smaller than 0");

        // valid config
        ThroughputControlGroupConfig throughputControlGroupConfig =
            new ThroughputControlGroupConfigBuilder()
                .groupName("throughputBucket-" + UUID.randomUUID())
                .throughputBucket(1)
                .build();

        assertThat(throughputControlGroupConfig.getThroughputBucket()).isEqualTo(1);

        // neither priorityLevel nor throughput bucket configured
        assertThatThrownBy(
            () -> new ThroughputControlGroupConfigBuilder()
                .groupName("throughputBucket-" + UUID.randomUUID())
                .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("All targetThroughput, targetThroughputThreshold, priorityLevel and throughput bucket cannot be null or empty.");
    }
}
