// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;

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

    boolean isGone(String partitionKeyRangeId);

    String getCollectionUniqueId();

    CollectionRoutingMap tryCombine(List<ImmutablePair<PartitionKeyRange, IServerIdentity>> ranges, String changeFeedNextIfNoneMatch);

    String getChangeFeedNextIfNoneMatch();
}
