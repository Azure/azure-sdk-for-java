// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.paging;

import com.azure.core.http.rest.Page;
import com.azure.core.http.rest.PagedFluxBase;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * This class is a Flux that can operate on any type that extends {@link Page} and
 * also provides the ability to operate on individual items.
 *
 * @param <S> The type of state
 * @param <T> The type of items in a {@link Page}
 * @param <P> The {@link Page} holding items of type {@code T}.
 *
 * @see Page
 * @see Flux
 */
public abstract class PagedFluxCore<S, T, P extends Page<T>> extends Flux<T> {
    private final BiFunction<S, String, Flux<P>> pageRetriever;

    /**
     * Creates an instance of {@link PagedFluxCore}.
     *
     * @param pageRetriever Function that returns Flux of pages. The pageRetriever can get
     *                      called multiple times. The continuation-token from the last Page
     *                      emitted by the pageRetriever returned Flux will be provided to
     *                      the next pageRetriever invocation. If the last Page emitted by
     *                      the pageRetriever returned Flux has null continuation-token then
     *                      final completion signal will be send to the downstream subscriber.
     */
    public PagedFluxCore(BiFunction<S, String, Flux<P>> pageRetriever) {
        this.pageRetriever = Objects.requireNonNull(pageRetriever,
            "'pageRetriever' function cannot be null.");
    }

    /**
     * Creates a flux of {@link Page} starting from the first page.
     *
     * @return A flux of {@link Page} starting from the first page
     */
    public Flux<P> byPage() {
        return byPage(null);
    }

    /**
     * Creates a flux of {@link Page} starting from the next page associated with the given
     * continuation token. To start from first page, use {@link #byPage()} instead.
     *
     * @param continuationToken The continuation token used to fetch the next page
     * @return A flux of {@link Page} starting from the page associated with the continuation token
     */
    public Flux<P> byPage(String continuationToken) {
        final String [] lastPageContinuationToken = { continuationToken };
        return Mono.just(true)
            .repeat()
            .concatMap(b -> pageRetriever.apply(getState(), lastPageContinuationToken[0])
                .doOnNext(page -> lastPageContinuationToken[0] = page.getContinuationToken()))
            .takeUntil(page -> page.getContinuationToken() == null);
    }

    /**
     * Subscribe to consume all items of type {@code T} in the sequence respectively.
     * This is recommended for most common scenarios. This will seamlessly fetch next
     * page when required and provide with a {@link Flux} of items.
     *
     * @param coreSubscriber The subscriber for this {@link PagedFluxBase}
     */
    @Override
    public void subscribe(CoreSubscriber<? super T> coreSubscriber) {
        byPage(null)
            .flatMap(page -> Flux.fromIterable(page.getItems()))
            .subscribe(coreSubscriber);
    }

    /**
     * Get a state for a subscription to this flux. For each subscription, this method will
     * be called for the state instance to be associated with it, such a state instance will
     * be shared across multiple pageRetriever calls during the life time of that subscription.
     *
     * @return get state object
     */
    protected abstract S getState();
}
