// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.caches;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.implementation.ICollectionRoutingMapCache;
import com.azure.cosmos.implementation.IRoutingMapProvider;
import com.azure.cosmos.implementation.PartitionKeyRange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 *
 */
public interface IPartitionKeyRangeCache extends IRoutingMapProvider, ICollectionRoutingMapCache {

    Mono<Utils.ValueHolder<CollectionRoutingMap>> tryLookupAsync(String collectionRid, CollectionRoutingMap previousValue, Map<String, Object> properties);

    Mono<Utils.ValueHolder<List<PartitionKeyRange>>> tryGetOverlappingRangesAsync(String collectionRid, Range<String> range, boolean forceRefresh,
                                                                                  Map<String, Object> properties);

    Mono<Utils.ValueHolder<PartitionKeyRange>> tryGetPartitionKeyRangeByIdAsync(String collectionResourceId, String partitionKeyRangeId, boolean forceRefresh,
                                                                                Map<String, Object> properties);

    Mono<Utils.ValueHolder<PartitionKeyRange>> tryGetRangeByPartitionKeyRangeId(String collectionRid, String partitionKeyRangeId, Map<String, Object> properties);

}
