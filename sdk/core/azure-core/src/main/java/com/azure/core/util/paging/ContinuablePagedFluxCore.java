// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.paging;

import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The default implementation of {@link ContinuablePagedFlux}.
 *
 * This type is a Flux provides the ability to operate on pages of type {@link ContinuablePage}
 * and individual items in such pages. This type supports user-provided continuation tokens,
 * allowing for restarting from a previously-retrieved continuation token.
 *
 * The constructor takes a provider, that when called should return Page Retriever Function which
 * accepts continuation token. The provider is called for each Subscription to this Flux.
 * Given provider is called per Subscription, the provider implementation can create one or more
 * objects to store any state and Page Retriever Function can capture and use those objects.
 * This indirectly associate the state objects to the Subscription. The Page Retriever Function
 * can get called multiple times in serial fashion, each time after the completion of the Flux
 * returned by the previous invocation. The final completion signal will be send to the Subscriber
 * when the last Page emitted by the Flux returned by the Page Retrieval Function has {@code null}
 * continuation token.
 *
 * <p><strong>Extending PagedFluxCore for Custom Continuation Token support</strong></p>
 * {@codesnippet com.azure.core.util.paging.pagedfluxcore.continuationtoken}
 *
 * @param <T> The type of items in a {@link ContinuablePage}
 * @param <P> The {@link ContinuablePage} holding items of type {@code T}.
 *
 * @see ContinuablePagedFlux
 * @see ContinuablePage
 */
public abstract class ContinuablePagedFluxCore<C, T, P extends ContinuablePage<C, T>>
    extends ContinuablePagedFlux<C, T, P> {

    private final Supplier<Function<C, Flux<P>>> pageRetrieverProvider;
    private final int defaultPrefetch;

    /**
     * Creates an instance of {@link ContinuablePagedFluxCore}.
     *
     * @param pageRetrieverProvider a provider that returns Page Retriever Function.
     */
    protected ContinuablePagedFluxCore(Supplier<Function<C, Flux<P>>> pageRetrieverProvider) {
        this.pageRetrieverProvider = Objects.requireNonNull(pageRetrieverProvider,
            "'pageRetrieverProvider' function cannot be null.");
        this.defaultPrefetch = -1;
    }

    /**
     * Creates an instance of {@link ContinuablePagedFluxCore}.
     *
     * @param provider a provider that returns Page Retriever Function.
     * @param prefetch the number of Pages to be pre-fetched from the Page Retriever Function upon
     *                 subscription.
     */
    protected ContinuablePagedFluxCore(Supplier<Function<C, Flux<P>>> provider, int prefetch) {
        this.pageRetrieverProvider = Objects.requireNonNull(provider, "'provider' function cannot be null.");
        if (prefetch <= 0) {
            throw new IllegalArgumentException("prefetch > 0 required but provided: " + prefetch);
        }
        this.defaultPrefetch = prefetch;
    }

    @Override
    public Flux<P> byPage() {
        return byPageIntern(this.pageRetrieverProvider, null, this.defaultPrefetch);
    }

    @Override
    public Flux<P> byPage(C continuationToken) {
        if (continuationToken == null) {
            return Flux.empty();
        }
        return byPageIntern(this.pageRetrieverProvider, continuationToken, this.defaultPrefetch);
    }

    /**
     * Flux of {@link ContinuablePage} that this Paged Flux represents.
     *
     * @param prefetch the number of Pages to be pre-fetched from the Page Retriever Function upon
     *                 subscription
     * @return a Flux of {@link ContinuablePage} that this Paged Flux represents.
     */
    public Flux<P> byPage(int prefetch) {
        if (prefetch <= 0) {
            return Flux.error(new IllegalArgumentException("prefetch > 0 required but provided: " + prefetch));
        }
        return byPageIntern(this.pageRetrieverProvider, null, prefetch);
    }

    /**
     * Flux of {@link ContinuablePage} starting from the Page identified by a continuation token.
     *
     * @param continuationToken the token to identify the pages to be retrieved
     * @param prefetch the number of Pages to be pre-fetched from the Page Retriever Function upon
     *                 subscription
     * @return a Flux of {@link ContinuablePage} identified by the given continuation token
     */
    public Flux<P> byPage(C continuationToken, int prefetch) {
        if (continuationToken == null) {
            return Flux.empty();
        }
        if (prefetch <= 0) {
            return Flux.error(new IllegalArgumentException("prefetch > 0 required but provided: " + prefetch));
        }
        return byPageIntern(this.pageRetrieverProvider, continuationToken, prefetch);
    }

    /**
     * Get a Flux of {@link ContinuablePage} created by concat-ing Flux instances returned
     * Page Retriever Function calls.
     *
     * @param provider the provider that when called returns Page Retriever Function
     * @param continuationToken  the token to identify the pages to be retrieved
     * @param prefetch the prefetch, a value -1 will result in using Reactor defined default prefetch
     *
     * @param <C> the type of Continuation token
     * @param <T> The type of items in a {@link ContinuablePage}
     * @param <P> The {@link ContinuablePage} holding items of type {@code T}
     * @return a Flux of {@link ContinuablePage} identified by the given continuation token
     */
    private static <C, T, P extends ContinuablePage<C, T>>
        Flux<P> byPageIntern(Supplier<Function<C, Flux<P>>> provider,
                             C continuationToken,
                             int prefetch) {
        return Flux.defer(() -> {
            final Function<C, Flux<P>> pageRetriever = provider.get();
            final ContinuationState<C> state = new ContinuationState<>(continuationToken);
            //
            Flux<Boolean> repeatUntilDone = Mono.just(true)
                .repeat(() -> !state.isDone());
            //
            if (prefetch == -1) {
                return repeatUntilDone
                    .concatMap(b -> {
                        return pageRetriever.apply(state.getLastContinuationToken())
                            .doOnNext(page -> state.setLastContinuationToken(page.getContinuationToken()));
                    });
            } else {
                return repeatUntilDone
                    .concatMap(b -> {
                        return pageRetriever.apply(state.getLastContinuationToken())
                            .doOnNext(page -> state.setLastContinuationToken(page.getContinuationToken()));
                    }, prefetch);
            }
        });
    }

    /**
     * Subscribe to consume all items of type {@code T} in the sequence respectively.
     * This is recommended for most common scenarios. This will seamlessly fetch next
     * page when required and provide with a {@link Flux} of items.
     *
     * @param coreSubscriber The subscriber for this {@link ContinuablePagedFluxCore}
     */
    @Override
    public void subscribe(CoreSubscriber<? super T> coreSubscriber) {
        byPage()
            .flatMap(page -> Flux.fromIterable(page.getItems()))
            .subscribe(coreSubscriber);
    }

    /**
     * Internal type to store Continuation State.
     *
     * @param <C> the Continuation Token type
     */
    private static class ContinuationState<C> {
        // The last seen continuation token
        private C lastContinuationToken;
        // Indicate whether to call the PageRetrieval Function
        private boolean isDone;

        /**
         * Creates ContinuationState.
         *
         * @param token the token to start with
         */
        ContinuationState(C token) {
            this.lastContinuationToken = token;
        }

        /**
         * Store the last seen continuation token.
         *
         * @param token the token
         */
        void setLastContinuationToken(C token) {
            this.isDone = (token == null);
            this.lastContinuationToken = token;
        }

        /**
         * @return the last seen token
         */
        C getLastContinuationToken() {
            return this.lastContinuationToken;
        }

        /**
         * @return true if the PageRetrieval Function needs to be invoked
         * for next set of pages.
         */
        boolean isDone() {
            return this.isDone;
        }
    }
}
