// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.routing;

import com.azure.data.cosmos.internal.PartitionKeyRange;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Collection;
import java.util.List;

/**
 * Used internally in request routing in the Azure Cosmos DB database service.
 */
public interface CollectionRoutingMap {
    List<PartitionKeyRange> getOrderedPartitionKeyRanges();

    PartitionKeyRange getRangeByEffectivePartitionKey(String effectivePartitionKeyValue);

    PartitionKeyRange getRangeByPartitionKeyRangeId(String partitionKeyRangeId);

    List<PartitionKeyRange> getOverlappingRanges(Range<String> range);

    List<PartitionKeyRange> getOverlappingRanges(Collection<Range<String>> providedPartitionKeyRanges);

    PartitionKeyRange tryGetRangeByPartitionKeyRangeId(String partitionKeyRangeId);

    IServerIdentity tryGetInfoByPartitionKeyRangeId(String partitionKeyRangeId);

    boolean IsGone(String partitionKeyRangeId);

    String getCollectionUniqueId();

    CollectionRoutingMap tryCombine(List<ImmutablePair<PartitionKeyRange, IServerIdentity>> ranges);
}
