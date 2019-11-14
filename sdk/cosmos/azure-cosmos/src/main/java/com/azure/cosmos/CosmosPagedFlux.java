// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.core.http.rest.PagedFluxBase;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class is a paged flux that can operate on any type that extends {@link FeedResponse} and
 * also provides the ability to operate on individual items. When processing the response by page,
 * each response will contain the items in the page as well as the request details like
 * status code and headers.
 *
 * @param <T> The type of items in {@code FeedResponse}.
 *
 * @see com.azure.core.http.rest.PagedResponse
 * @see com.azure.core.http.rest.Page
 * @see com.azure.core.http.rest.PagedFlux
 */
public class CosmosPagedFlux<T> extends PagedFluxBase<T, FeedResponse<T>> {

    public CosmosPagedFlux(Supplier<Mono<FeedResponse<T>>> firstPageRetriever) {
        super(firstPageRetriever);
    }

    public CosmosPagedFlux(Supplier<Mono<FeedResponse<T>>> firstPageRetriever, Function<String, Mono<FeedResponse<T>>> nextPageRetriever) {
        super(firstPageRetriever, nextPageRetriever);
    }
}
