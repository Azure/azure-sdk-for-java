/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
        Mono<List<PartitionKeyRange>> tryGetOverlappingRangesAsync(String collectionResourceId, Range<String> range,
                                                                   boolean forceRefresh /* = false */, Map<String, Object> properties);

        Mono<PartitionKeyRange> tryGetPartitionKeyRangeByIdAsync(String collectionResourceId, String partitionKeyRangeId,
                                                                   boolean forceRefresh /* = false */, Map<String, Object> properties);
}
