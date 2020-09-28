// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.paging;

import org.reactivestreams.Subscriber;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

/**
 * Internal class that is a blocking variant of {@link ContinuablePagedFluxCore}.
 * <p>
 * This class is used by {@link ContinuablePagedIterable} to retrieve pages from the service in a blocking manner while
 * also respecting the number of pages to be retrieved. This functions differently than just wrapping a {@link
 * ContinuablePagedFlux} as this will track the exact number of pages emitted and whether the previously retrieved
 * page/pages contain any additional items that could be emitted.
 *
 * @param <C> The continuation token type.
 * @param <T> The item type.
 * @param <P> The page type.
 */
class BlockingByPagePagedFlux<C, T, P extends ContinuablePage<C, T>> extends Flux<P> {
    private final Supplier<PageRetriever<C, P>> pageRetrieverProvider;
    private final C initialContinuationToken;
    private final Integer defaultPageSize;

    BlockingByPagePagedFlux(Supplier<PageRetriever<C, P>> pageRetrieverProvider, C initialContinuationToken,
        Integer defaultPageSize) {
        this.pageRetrieverProvider = pageRetrieverProvider;
        this.initialContinuationToken = initialContinuationToken;
        this.defaultPageSize = defaultPageSize;
    }

    @Override
    public void subscribe(CoreSubscriber<? super P> actual) {
        BlockingByPageSubscriber pageSubscription = new BlockingByPageSubscriber(actual,
            new ContinuationState<>(initialContinuationToken), this.pageRetrieverProvider.get(), this.defaultPageSize);

        actual.onSubscribe(pageSubscription);
    }

    /*
     * Internal implementation of BlockingSubscriberBase which handles by page paged iterables.
     */
    private final class BlockingByPageSubscriber extends BlockingSubscriberBase<C, T, P, P> {
        volatile Queue<P> pages = new ConcurrentLinkedQueue<>();

        BlockingByPageSubscriber(Subscriber<? super P> subscriber, ContinuationState<C> continuationState,
            PageRetriever<C, P> pageRetriever, Integer defaultPageSize) {
            super(subscriber, continuationState, pageRetriever, defaultPageSize);
        }

        @Override
        boolean needToRequestPage() {
            return !lastPage && pages.peek() == null;
        }

        @Override
        boolean hasNext() {
            return pages.peek() != null;
        }

        @Override
        P getNext() {
            return pages.poll();
        }

        @Override
        synchronized void addPage(P page, ContinuationState<C> continuationState) {
            this.lastPage = page.getContinuationToken() == null;
            continuationState.setLastContinuationToken(page.getContinuationToken());
            this.pages.add(page);
        }
    }
}
