// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import org.reactivestreams.Publisher;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * This class is a flux that can operate on a {@link PagedResponse} and
 * also provides the ability to operate on individual items.
 *
 * When processing the response by page, each response will contain the items in the page as
 * well as the request details like status code and headers.
 *
 * @param <T> The type of items in a {@link PagedResponse}
 *
 * @see PagedResponse
 * @see Page
 * @see Flux
 */
public class PagedFlux<T> extends Flux<T> {
    private final Supplier<Mono<PagedResponse<T>>> firstPageRetriever;
    private final Function<String, Mono<PagedResponse<T>>> nextPageRetriever;

    /**
     * Creates an instance of {@link PagedFlux}. The constructor takes in two arguments. The first
     * argument is a supplier that fetches the first page of {@code T}. The second argument is a
     * function that fetches subsequent pages of {@code T}
     * <p></p><p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.core.http.rest.pagedflux.instantiation}
     *
     * @param firstPageRetriever Supplier that retrieves the first page
     * @param nextPageRetriever Function that retrieves the next page given a continuation token
     */
    public PagedFlux(Supplier<Mono<PagedResponse<T>>> firstPageRetriever,
        Function<String, Mono<PagedResponse<T>>> nextPageRetriever) {
        Objects.requireNonNull(firstPageRetriever, "First page supplier cannot be null");
        Objects.requireNonNull(nextPageRetriever, "Next page retriever function cannot be null");
        this.firstPageRetriever = firstPageRetriever;
        this.nextPageRetriever = nextPageRetriever;
    }

    /**
     * Creates a flux of {@link PagedResponse} starting from the first page.
     *
     * <p></p><p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.core.http.rest.pagedflux.bypage}
     *
     * @return A {@link PagedFlux} starting from the first page
     */
    public Flux<PagedResponse<T>> byPage() {
        return firstPageRetriever.get().flatMapMany(this::extractAndFetchPage);
    }

    /**
     * Creates a flux of {@link PagedResponse} starting from the next page associated with the given
     * continuation token. To start from first page, use {@link #byPage()} instead.
     *
     * <p></p><p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.core.http.rest.pagedflux.bypage#String}
     *
     * @param continuationToken The continuation token used to fetch the next page
     * @return A {@link PagedFlux} starting from the page associated with the continuation token
     */
    public Flux<PagedResponse<T>> byPage(String continuationToken) {
        return nextPageRetriever.apply(continuationToken).flatMapMany(this::extractAndFetchPage);
    }

    /**
     * Subscribe to this {@link PagedFlux} to process items in {@link PagedResponse}
     *
     * <p></p><p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.core.http.rest.pagedflux.subscribe}
     *
     * @param coreSubscriber The subscriber for this {@link PagedFlux}
     */
    @Override
    public void subscribe(CoreSubscriber<? super T> coreSubscriber) {
        byT(null).subscribe(coreSubscriber);
    }

    /**
     * Helper method to return the flux of items starting from the page associated with the {@code continuationToken}
     *
     * @param continuationToken The continuation token that is used to fetch the next page
     * @return A {@link Flux} of items in this page
     */
    private Flux<T> byT(String continuationToken) {
        if (continuationToken == null) {
            return firstPageRetriever.get().flatMapMany(this::extractAndFetchT);
        }
        return nextPageRetriever.apply(continuationToken).flatMapMany(this::extractAndFetchT);
    }

    /**
     * Helper method to string together a flux of items transparently extracting items from
     * next pages, if available.
     * @param page Starting page
     * @return A {@link Flux} of items
     */
    private Publisher<T> extractAndFetchT(PagedResponse<T> page) {
        String nextPageLink = page.nextLink();
        if (nextPageLink == null) {
            return Flux.fromIterable(page.items());
        }
        return Flux.fromIterable(page.items()).concatWith(byT(nextPageLink));
    }

    /**
     * Helper method to string together a flux of {@link PagedResponse} transparently
     * fetching next pages, if available
     * @param page Starting page
     * @return A {@link Flux} of {@link PagedResponse}
     */
    private Publisher<? extends PagedResponse<T>> extractAndFetchPage(PagedResponse<T> page) {
        return Flux.just(page).concatWith(byPage(page.nextLink()));
    }
}
