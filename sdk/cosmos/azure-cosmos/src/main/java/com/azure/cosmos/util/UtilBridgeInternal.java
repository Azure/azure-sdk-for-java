// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.util;

import com.azure.core.util.paging.ContinuablePagedFlux;
import com.azure.cosmos.implementation.CosmosPagedFluxOptions;
import com.azure.cosmos.models.FeedResponse;
import reactor.core.publisher.Flux;

import java.util.function.Function;

/**
 * DO NOT USE.
 * This is meant to be used only internally as a bridge access to classes in
 * com.azure.cosmos.util package
 **/
public final class UtilBridgeInternal {

    public static <T> CosmosPagedFlux<T> createCosmosPagedFlux(Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> pagedFluxOptionsFluxFunction) {
        return new CosmosPagedFlux<>(pagedFluxOptionsFluxFunction);
    }

    public static <T> CosmosPagedIterable<T> createCosmosPagedIterable(ContinuablePagedFlux<String, T, FeedResponse<T>> pagedFlux) {
        return new CosmosPagedIterable<>(pagedFlux);
    }
}
