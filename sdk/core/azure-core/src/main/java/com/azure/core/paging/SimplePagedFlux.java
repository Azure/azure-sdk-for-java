// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.paging;

import com.azure.core.http.rest.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class is a Flux that can operate on a type that extends {@link Page} and also provides
 * the ability to operate on individual items. This type support user providing string based
 * continuation token (next link) and retrieve pages using it.
 *
 * The constructor of this type takes a Page Retriever Function which accepts string continuation
 * token. The Page Retriever Function can get called multiple times in serial fashion, first time
 * with {@code null} as continuation token and then each time with the non-null continuation token
 * of the {@link Page} emitted from the Flux returned by the last Page Retriever invocation.
 * Completion signal will be send to the subscriber when the last {@link Page} emitted from the Flux
 * has {@code null} continuation token. Note that unlike {@link PagedFluxCore} and {@link ContinuablePagedFlux}
 * this type does not support capture based state management.
 *
 * @param <T> Type of items in the page
 * @param <P> The of the page
 */
public abstract class SimplePagedFlux<T, P extends Page<T>> extends PagedFluxCore<T, P> {

    private final Function<String, Flux<P>> pageRetriever;

    /**
     * Creates an instance of {@link SimplePagedFlux}.
     *
     * @param pageRetriever the Page Retriever Function.
     */
    public SimplePagedFlux(Function<String, Flux<P>> pageRetriever) {
        super(new Supplier<>() {
            final ContinuationState<String> state = new ContinuationState(null);
            @Override
            public Supplier<Flux<P>> get() {
                return () -> {
                    if (state.isDone()) {
                        // PagedFluxCore contract to send completion signal to subscriber.
                        return null;
                    } else {
                        return pageRetriever.apply(state.getLastContinuationToken())
                            .doOnNext(p -> state.setLastContinuationToken(p.getContinuationToken()));
                    }
                };
            }
        });
        this.pageRetriever = pageRetriever;
    }

    /**
     * @return a flux of {@link Page} starting from the Page identified by the given token.
     */
    public Flux<P> byPage(String continuationToken) {
        final ContinuationState<String> state = new ContinuationState(continuationToken);
        return Mono.just(true)
            .repeat(() -> !state.isDone())
            .concatMap(b -> pageRetriever.apply(state.getLastContinuationToken())
                .doOnNext(p -> state.setLastContinuationToken(p.getContinuationToken())));
    }
}
