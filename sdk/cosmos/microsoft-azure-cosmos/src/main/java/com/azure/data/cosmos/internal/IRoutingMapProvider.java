// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.internal.routing.Range;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

//TODO: update documentation
//TODO: add two overload methods for forceRefresh = false
/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 **/
public interface IRoutingMapProvider {
        /// <summary>
        /// Returns list of effective partition key ranges for a collection.
        /// </summary>
        /// <param name="collectionResourceId">Collection for which to retrieve routing map.</param>
        /// <param name="range">This method will return all ranges which overlap this range.</param>
        /// <param name="forceRefresh">Whether forcefully refreshing the routing map is necessary</param>
        /// <returns>List of effective partition key ranges for a collection or null if collection doesn't exist.</returns>
        Mono<Utils.ValueHolder<List<PartitionKeyRange>>> tryGetOverlappingRangesAsync(String collectionResourceId, Range<String> range,
                                                                   boolean forceRefresh /* = false */, Map<String, Object> properties);

        Mono<Utils.ValueHolder<PartitionKeyRange>> tryGetPartitionKeyRangeByIdAsync(String collectionResourceId, String partitionKeyRangeId,
                                                                   boolean forceRefresh /* = false */, Map<String, Object> properties);
}
