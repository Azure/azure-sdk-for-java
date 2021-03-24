// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This type is a thin wrapper around a PagedFlux that ensures the page size will be requested when they customer passes
 * the value to byPage method when iterating rather than setting the page size on the options.
 *
 * @param <T> The type of items in a {@link PagedResponse}
 */
public class StoragePagedFlux<T> extends PagedFlux<T> {

    public StoragePagedFlux(Supplier<Mono<PagedResponse<T>>> firstPageRetriever) {
        super(firstPageRetriever);
    }

    public static <T> PagedFlux<T> create(Function<Integer, Mono<PagedResponse<T>>> firstPageRetriever,
        BiFunction<String, Integer, Mono<PagedResponse<T>>> nextPageRetriever) {
        return PagedFlux.create(() -> (continuationToken, pageSize) ->
            continuationToken == null
                ? firstPageRetriever.apply(pageSize).flux()
                : nextPageRetriever.apply(continuationToken, pageSize).flux());
    }
}
