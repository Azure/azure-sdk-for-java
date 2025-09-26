// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import reactor.core.publisher.Mono;

import java.util.Map;

// TODO: add documentation
/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 **/
public interface ICollectionRoutingMapCache {
    default Mono<Utils.ValueHolder<CollectionRoutingMap>> tryLookupAsync(
            MetadataDiagnosticsContext metaDataDiagnosticsContext,
            String collectionRid,
            CollectionRoutingMap previousValue,
            Map<String, Object> properties,
            StringBuilder sb) {
        return tryLookupAsync(metaDataDiagnosticsContext, collectionRid, previousValue, false, properties, sb);
    }

    Mono<Utils.ValueHolder<CollectionRoutingMap>> tryLookupAsync(
            MetadataDiagnosticsContext metaDataDiagnosticsContext,
            String collectionRid,
            CollectionRoutingMap previousValue,
            boolean forceRefreshCollectionRoutingMap,
            Map<String, Object> properties,
            StringBuilder sb);
}
