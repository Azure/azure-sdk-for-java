// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpRequest;

import java.util.stream.Collectors;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class is a flux that can operate on a {@link PagedResponse} and also provides the ability to operate on
 * individual items. When processing the response by page, each response will contain the items in the page as well as
 * the request details like status code and headers.
 *
 * <p>To process one item at a time, simply subscribe to this flux as shown below </p>
 * <p><strong>Code sample</strong></p>
 * {@codesnippet com.azure.core.http.rest.pagedflux.items}
 *
 * <p>To process one page at a time, use {@link #byPage} method as shown below </p>
 * <p><strong>Code sample</strong></p>
 * {@codesnippet com.azure.core.http.rest.pagedflux.pages}
 *
 * <p>To process items one page at a time starting from any page associated with a continuation token,
 * use {@link #byPage(String)} as shown below</p>
 * <p><strong>Code sample</strong></p>
 * {@codesnippet com.azure.core.http.rest.pagedflux.pagesWithContinuationToken}
 *
 * @param <T> The type of items in a {@link PagedResponse}
 *
 * @see PagedResponse
 * @see Page
 * @see Flux
 */
public class PagedFlux<T> extends PagedFluxBase<T, PagedResponse<T>> {

    /**
     * Creates an instance of {@link PagedFlux} that consists of only a single page of results. The only argument to
     * this constructor therefore is a supplier that fetches the first (and known-only) page of {@code T}.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.core.http.rest.pagedflux.singlepage.instantiation}
     *
     * @param firstPageRetriever Supplier that retrieves the first page.
     */
    public PagedFlux(Supplier<Mono<PagedResponse<T>>> firstPageRetriever) {
        super(firstPageRetriever);
    }

    /**
     * Creates an instance of {@link PagedFlux}. The constructor takes in two arguments. The first argument is a
     * supplier that fetches the first page of {@code T}. The second argument is a function that fetches subsequent
     * pages of {@code T}
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.core.http.rest.pagedflux.instantiation}
     *
     * @param firstPageRetriever Supplier that retrieves the first page
     * @param nextPageRetriever Function that retrieves the next page given a continuation token
     */
    public PagedFlux(Supplier<Mono<PagedResponse<T>>> firstPageRetriever,
                     Function<String, Mono<PagedResponse<T>>> nextPageRetriever) {
        super(firstPageRetriever, nextPageRetriever);
    }

    /**
     * Maps this PagedFlux instance of T to a PagedFlux instance of type S as per the provided mapper function.
     *
     * @param mapper The mapper function to convert from type T to type S.
     * @param <S> The mapped type.
     * @return A PagedFlux of type S.
     */
    public <S> PagedFlux<S> mapPage(Function<T, S> mapper) {
        return new PagedFlux<S>(() -> getFirstPageRetriever().get()
            .map(mapPagedResponse(mapper)),
            continuationToken -> getNextPageRetriever().apply(continuationToken)
                .map(mapPagedResponse(mapper)));
    }

    private <S> Function<PagedResponse<T>, PagedResponse<S>> mapPagedResponse(Function<T, S> mapper) {
        return pagedResponse -> new PagedResponseBase<HttpRequest, S>(pagedResponse.getRequest(),
            pagedResponse.getStatusCode(),
            pagedResponse.getHeaders(),
            pagedResponse.getValue().stream().map(mapper).collect(Collectors.toList()),
            pagedResponse.getContinuationToken(),
            null);
    }
}
