// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.server.config;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.models.PriorityLevel;

import java.util.Objects;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

public class ThroughputBucketControlGroup extends ServerThroughputControlGroupInternal {
    private final Integer throughputBucket;

    public ThroughputBucketControlGroup(
        String groupName,
        boolean isDefault,
        PriorityLevel priorityLevel,
        Integer throughputBucket,
        CosmosAsyncContainer targetContainer) {

        super(groupName, isDefault, priorityLevel, targetContainer);

        checkArgument(throughputBucket != null && throughputBucket > 0, "Target throughput should be greater than 0");
        this.throughputBucket = throughputBucket;
    }

    public Integer getThroughputBucket() {
        return this.throughputBucket;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ThroughputBucketControlGroup that = (ThroughputBucketControlGroup) o;
        return Objects.equals(throughputBucket, that.throughputBucket);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), throughputBucket);
    }
}
