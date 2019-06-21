// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import java.util.function.Function;
import org.reactivestreams.Publisher;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * TODO:
 * @param <T> The type for paginated flux
 */
public class PagedFlux<T> extends Flux<T> {

    private final Mono<PagedResponse<T>> firstPage;
    private final Function<String, Mono<PagedResponse<T>>> pager;

    /**
     * TODO
     *
     * @param pager Function to get the next page for a given next page link
     */
    public PagedFlux(Mono<PagedResponse<T>> firstPage,
        Function<String, Mono<PagedResponse<T>>> pager) {
        this.firstPage = firstPage;
        this.pager = pager;
    }

    public Flux<PagedResponse<T>> byPage(String continuationToken) {
        Mono<PagedResponse<T>> page = pager.apply(continuationToken);
        return page.flatMapMany(pagedResponse -> extractAndFetchPage(pagedResponse));
    }

    /**
     * TODO:
     *
     * @return Returns a flux of paged response
     */
    public Flux<PagedResponse<T>> byPage() {
        return firstPage.flatMapMany(r -> extractAndFetchPage(r));
    }

    /**
     * {@inheritDoc}
     * @param coreSubscriber The subscriber for this {@link PagedFlux}
     */
    @Override
    public void subscribe(CoreSubscriber<? super T> coreSubscriber) {
        byT(null).subscribe(coreSubscriber);
    }

    private Flux<T> byT(String nextPageLink) {
        if (nextPageLink == null) {
            return firstPage.flatMapMany(page -> extractAndFetchT(page));
        }
        return pager.apply(nextPageLink).flatMapMany(page -> extractAndFetchT(page));
    }

    private Publisher<T> extractAndFetchT(PagedResponse<T> page) {
        String nextPageLink = page.nextLink();
        if (nextPageLink == null) {
            return Flux.fromIterable(page.items());
        }
        return Flux.fromIterable(page.items()).concatWith(byT(nextPageLink));
    }

    private Publisher<? extends PagedResponse<T>> extractAndFetchPage(PagedResponse<T> r) {
        String nextPageLink = r.nextLink();
        if (nextPageLink == null) {
            return Flux.just(r);
        }
        return Flux.just(r).concatWith(byPage(nextPageLink));
    }
}
