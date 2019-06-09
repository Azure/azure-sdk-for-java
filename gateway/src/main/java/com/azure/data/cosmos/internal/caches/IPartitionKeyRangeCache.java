/**
 * The MIT License (MIT)
 * Copyright (c) 2017 Microsoft Corporation
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
package com.azure.data.cosmos.internal.caches;

import java.util.List;
import java.util.Map;

import com.azure.data.cosmos.PartitionKeyRange;
import com.azure.data.cosmos.internal.routing.CollectionRoutingMap;
import com.azure.data.cosmos.internal.routing.Range;
import com.azure.data.cosmos.internal.ICollectionRoutingMapCache;
import com.azure.data.cosmos.internal.IRoutingMapProvider;

import rx.Single;

/**
 * 
 */
public interface IPartitionKeyRangeCache extends IRoutingMapProvider, ICollectionRoutingMapCache {

    Single<CollectionRoutingMap> tryLookupAsync(String collectionRid, CollectionRoutingMap previousValue, Map<String, Object> properties);

    Single<List<PartitionKeyRange>> tryGetOverlappingRangesAsync(String collectionRid, Range<String> range, boolean forceRefresh,
                                                                 Map<String, Object> properties);

    Single<PartitionKeyRange> tryGetPartitionKeyRangeByIdAsync(String collectionResourceId, String partitionKeyRangeId, boolean forceRefresh,
                                                               Map<String, Object> properties);

    Single<PartitionKeyRange> tryGetRangeByPartitionKeyRangeId(String collectionRid, String partitionKeyRangeId, Map<String, Object> properties);

}