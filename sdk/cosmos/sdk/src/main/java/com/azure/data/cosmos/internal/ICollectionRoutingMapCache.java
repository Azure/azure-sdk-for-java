// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.internal.routing.CollectionRoutingMap;
import reactor.core.publisher.Mono;

import java.util.Map;

// TODO: add documentation
/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 **/
public interface ICollectionRoutingMapCache {
    default Mono<CollectionRoutingMap> tryLookupAsync(
            String collectionRid,
            CollectionRoutingMap previousValue,
            Map<String, Object> properties) {
        return tryLookupAsync(collectionRid, previousValue, false, properties);
    }

    Mono<CollectionRoutingMap> tryLookupAsync(
            String collectionRid,
            CollectionRoutingMap previousValue,
            boolean forceRefreshCollectionRoutingMap,
            Map<String, Object> properties);
}
