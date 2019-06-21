// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import java.util.function.Function;
import org.reactivestreams.Publisher;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * This class is a flux that can operate on a {@link PagedResponse} and
 * also provides the ability to operate on individual items.
 *
 * When used with paged responses, each response will contain the items in the page as
 * well as the request details like status code and headers
 *
 * @param <T> The type of items in a page
 */
public class PagedFlux<T> extends Flux<T> {
    private final Mono<PagedResponse<T>> firstPage;
    private final Function<String, Mono<PagedResponse<T>>> pager;

    /**
     * Creates an instance of {@link PagedFlux}
     * @param firstPage The first page
     * @param pager Function that retrieves the page for a given continuation token
     */
    public PagedFlux(Mono<PagedResponse<T>> firstPage,
        Function<String, Mono<PagedResponse<T>>> pager) {
        this.firstPage = firstPage;
        this.pager = pager;
    }

    /**
     * Creates a flux of {@link PagedResponse} starting from the given continuation token
     * @param continuationToken The continuation token from which to start the flux
     * @return A {@link PagedFlux} starting from the continuation token
     */
    public Flux<PagedResponse<T>> byPage(String continuationToken) {
        return pager.apply(continuationToken).flatMapMany(this::extractAndFetchPage);
    }

    /**
     * Creates a flux of {@link PagedResponse} starting from the first page
     *
     * @return A {@link PagedFlux} starting from the first page
     */
    public Flux<PagedResponse<T>> byPage() {
        return firstPage.flatMapMany(this::extractAndFetchPage);
    }

    /**
     * {@inheritDoc}
     * @param coreSubscriber The subscriber for this {@link PagedFlux}
     */
    @Override
    public void subscribe(CoreSubscriber<? super T> coreSubscriber) {
        byT(null).subscribe(coreSubscriber);
    }

    /**
     * Helper method to return the flux of items contained in the page associated with
     * given continuation token
     * @param continuationToken The continuation token that is used to fetch the next page
     * @return A {@link Flux} of items in this page
     */
    private Flux<T> byT(String continuationToken) {
        if (continuationToken == null) {
            return firstPage.flatMapMany(this::extractAndFetchT);
        }
        return pager.apply(continuationToken).flatMapMany(this::extractAndFetchT);
    }

    /**
     * Helper method to string together a flux of items transparently extracting items from
     * next pages, if available.
     * @param page Starting page
     * @return A {@link Flux} of items
     */
    private Publisher<T> extractAndFetchT(PagedResponse<T> page) {
        String continuationToken = page.nextLink();
        if (continuationToken == null) {
            return Flux.fromIterable(page.items());
        }
        return Flux.fromIterable(page.items()).concatWith(byT(continuationToken));
    }

    /**
     * Helper method to string together a flux of {@link PagedResponse} transparently
     * fetching next pages, if available
     * @param page Starting page
     * @return A {@link Flux} of {@link PagedResponse}
     */
    private Publisher<? extends PagedResponse<T>> extractAndFetchPage(PagedResponse<T> page) {
        String continuationToken = page.nextLink();
        if (continuationToken == null) {
            return Flux.just(page);
        }
        return Flux.just(page).concatWith(byPage(continuationToken));
    }
}
