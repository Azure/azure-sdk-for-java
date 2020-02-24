// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.caches;

import com.azure.data.cosmos.internal.ICollectionRoutingMapCache;
import com.azure.data.cosmos.internal.IRoutingMapProvider;
import com.azure.data.cosmos.internal.PartitionKeyRange;
import com.azure.data.cosmos.internal.Utils;
import com.azure.data.cosmos.internal.routing.CollectionRoutingMap;
import com.azure.data.cosmos.internal.routing.Range;
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