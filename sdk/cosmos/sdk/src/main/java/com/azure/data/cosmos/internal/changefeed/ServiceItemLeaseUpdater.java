// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed;

import com.azure.data.cosmos.CosmosItem;
import com.azure.data.cosmos.CosmosItemRequestOptions;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * Interface for service lease updater.
 */
public interface ServiceItemLeaseUpdater {
    Mono<Lease> updateLease(Lease cachedLease, CosmosItem itemLink, CosmosItemRequestOptions requestOptions, Function<Lease, Lease> updateLease);
}
