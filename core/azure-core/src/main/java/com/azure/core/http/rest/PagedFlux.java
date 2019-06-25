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
 * well as the request details like status code and headers.
 *
 * @param <T> The type of items in a page
 */
public class PagedFlux<T> extends Flux<T> {
    private final Function<PagedResponse<T>, Mono<PagedResponse<T>>> pager;

    /**
     * Creates an instance of {@link PagedFlux}. The function provided as input in this constructor
     * will take in a {@PagedResponse} of the current page and retrieves the next page associated with the {@code nextLink}
     * of the current page.
     * <br>
     * If the current page is {@code null}, this function will retrieve the first page. The current page is used here
     * instead of {@code nextLink} available in {@link PagedResponse} because {@code nextLink} can be {@code null} in
     * two scenarios - first page and the last page. In order to differentiate these two scenarios, current page is used.
     *
     * @param pager Function that retrieves the next page given the current page. If current page is {@code null},
     * this function will retrieve the first page.
     */
    public PagedFlux(Function<PagedResponse<T>, Mono<PagedResponse<T>>> pager) {
        this.pager = pager;
    }

    /**
     * Creates a flux of {@link PagedResponse} starting from the next page relative to the current page
     * @param currentPage Current page which holds the link to next page. If {@code null}, start from first page
     * @return A {@link PagedFlux} starting from the next page relative to the current page
     */
    public Flux<PagedResponse<T>> byPage(PagedResponse<T> currentPage) {
        return pager.apply(currentPage).flatMapMany(this::extractAndFetchPage);
    }

    /**
     * Creates a flux of {@link PagedResponse} starting from the first page
     *
     * @return A {@link PagedFlux} starting from the first page
     */
    public Flux<PagedResponse<T>> byPage() {
        return byPage(null);
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
     * Helper method to return the flux of items contained in the page associated with the next page
     * relative to {@code currentPage}
     *
     * @param currentPage The continuation token that is used to fetch the next page
     * @return A {@link Flux} of items in this page
     */
    private Flux<T> byT(PagedResponse<T> currentPage) {
        return pager.apply(currentPage).flatMapMany(this::extractAndFetchT);
    }

    /**
     * Helper method to string together a flux of items transparently extracting items from
     * next pages, if available.
     * @param page Starting page
     * @return A {@link Flux} of items
     */
    private Publisher<T> extractAndFetchT(PagedResponse<T> page) {
        return Flux.fromIterable(page.items()).concatWith(byT(page));
    }

    /**
     * Helper method to string together a flux of {@link PagedResponse} transparently
     * fetching next pages, if available
     * @param page Starting page
     * @return A {@link Flux} of {@link PagedResponse}
     */
    private Publisher<? extends PagedResponse<T>> extractAndFetchPage(PagedResponse<T> page) {
        return Flux.just(page).concatWith(byPage(page));
    }
}
