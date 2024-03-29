// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyImpl;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Represents a feed range.
 */
public interface FeedRange {
    /**
     * Creates a range from a previously obtained string representation.
     *
     * @param json A string representation of a feed range
     * @return A feed range
     */
    public static FeedRange fromString(String json) {
        return FeedRangeInternal.fromBase64EncodedJsonString(json);
    }

    /**
     * Gets a json representation of the feed range - the returned json string can be used
     * to create a new feed range instance from it - (use factory method fromJsonString to do so)
     * @return a JSON string representing the feed range
     */
    public String toString();

    /**
     * Creates a range for a certain logical partition
     * @param partitionKey the logical partition key value
     * @return A feed range for a certain logical partition
     */
    public static FeedRange forLogicalPartition(PartitionKey partitionKey) {
        checkNotNull(partitionKey, "Argument 'partitionKey' must not be null.");

        return new FeedRangePartitionKeyImpl(
            ModelBridgeInternal.getPartitionKeyInternal(partitionKey));
    }

    /**
     * Creates a range for an entire container
     * @return A feed range for an entire container
     */
    public static FeedRange forFullRange() {
        return FeedRangeEpkImpl.forFullRange();
    }
}
