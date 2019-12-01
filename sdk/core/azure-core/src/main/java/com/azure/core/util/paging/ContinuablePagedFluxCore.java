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
 * The Flux that provides the ability to operate on individual items in pages of type {@link ContinuablePage},
 * it also provide ability to operate on individual pages.
 *
 * The constructor of this type takes a provider method, that when called should provides Page Retriever
 * Function which accepts continuation token. The provider is called for each Subscription to the
 * ContinuablePagedFluxCore instance. Given provider is called per Subscription, the provider implementation
 * can create one or more objects to store any state and Page Retriever Function can capture and use those objects.
 * This indirectly associate the state objects to the Subscription. The Page Retriever Function can get called
 * multiple times in serial fashion, each time after the completion of the Flux returned by the previous invocation.
 * The final completion signal will be send to the downstream subscriber when the last Page emitted by the Flux
 * returned by Page Continuation Function has {@code null} continuation token.
 *
 * <p><strong>Extending PagedFluxCore for Custom Continuation Token support</strong></p>
 * {@codesnippet com.azure.core.util.paging.pagedfluxcore.continuationtoken}
 *
 * @param <T> The type of items in a {@link ContinuablePage}
 * @param <P> The {@link ContinuablePage} holding items of type {@code T}.
 *
 * @see ContinuablePage
 * @see Flux
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
     * @param pageRetrieverProvider a provider that returns Page Retriever Function.
     * @param prefetch the number of Pages to be pre-fetched from the Page Retriever Function upon
     *                 subscription.
     */
    protected ContinuablePagedFluxCore(Supplier<Function<C, Flux<P>>> pageRetrieverProvider, int prefetch) {
        this.pageRetrieverProvider = Objects.requireNonNull(pageRetrieverProvider,
            "'pageRetrieverProvider' function cannot be null.");
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
            throw new IllegalArgumentException("prefetch > 0 required but provided: " + prefetch);
        }
        return byPageIntern(this.pageRetrieverProvider, null, prefetch);
    }

    /**
     * Flux of {@link ContinuablePage} identified by a continuation token.
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
            throw new IllegalArgumentException("prefetch > 0 required but provided: " + prefetch);
        }
        return byPageIntern(this.pageRetrieverProvider, continuationToken, prefetch);
    }

    private static <C, T, P extends ContinuablePage<C, T>>
        Flux<P> byPageIntern(Supplier<Function<C, Flux<P>>> pageRetrieverProvider,
                             C continuationToken,
                             int prefetch) {
        return Flux.defer(() -> {
            Function<C, Flux<P>> pageRetriever = pageRetrieverProvider.get();
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
}
