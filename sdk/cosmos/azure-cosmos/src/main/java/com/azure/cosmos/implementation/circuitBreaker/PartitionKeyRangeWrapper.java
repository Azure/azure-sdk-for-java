// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.circuitBreaker;

import com.azure.cosmos.implementation.PartitionKeyRange;

import java.util.Objects;

public class PartitionKeyRangeWrapper {
    private final PartitionKeyRange partitionKeyRange;
    private final String resourceId;

    public PartitionKeyRangeWrapper(PartitionKeyRange partitionKeyRange, String resourceId) {
        this.partitionKeyRange = partitionKeyRange;
        this.resourceId = resourceId;
    }

    public PartitionKeyRange getPartitionKeyRange() {
        return partitionKeyRange;
    }

    public String getCollectionResourceId() {
        return resourceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartitionKeyRangeWrapper that = (PartitionKeyRangeWrapper) o;
        return Objects.equals(partitionKeyRange, that.partitionKeyRange) && Objects.equals(resourceId, that.resourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partitionKeyRange, resourceId);
    }
}
