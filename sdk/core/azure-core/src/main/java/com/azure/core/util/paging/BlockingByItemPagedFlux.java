// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.paging;

import org.reactivestreams.Subscriber;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

/**
 * Internal class that is a blocking variant of {@link ContinuablePagedFluxCore}.
 * <p>
 * This class is used by {@link ContinuablePagedIterable} to retrieve pages from the service in a blocking manner while
 * also respecting the number of items to be retrieved. This functions differently than just wrapping a {@link
 * ContinuablePagedFlux} as this will track the exact number of items emitted and whether the previously retrieve
 * page/pages contain any additional items that could be emitted.
 *
 * @param <C> The continuation token type.
 * @param <T> The item type.
 * @param <P> The page type.
 */
class BlockingByItemPagedFlux<C, T, P extends ContinuablePage<C, T>> extends ContinuablePagedFluxCore<C, T, P> {
    BlockingByItemPagedFlux(Supplier<PageRetriever<C, P>> pageRetrieverSupplier) {
        super(pageRetrieverSupplier);
    }

    BlockingByItemPagedFlux(Supplier<PageRetriever<C, P>> pageRetrieverProvider, int pageSize) {
        super(pageRetrieverProvider, pageSize);
    }

    @Override
    public Flux<P> byPage() {
        return new BlockingByPagePagedFlux<>(pageRetrieverProvider, null, defaultPageSize);
    }

    @Override
    public Flux<P> byPage(C continuationToken) {
        return new BlockingByPagePagedFlux<>(pageRetrieverProvider, continuationToken, null);
    }

    @Override
    public Flux<P> byPage(int preferredPageSize) {
        return new BlockingByPagePagedFlux<>(pageRetrieverProvider, null, preferredPageSize);
    }

    @Override
    public Flux<P> byPage(C continuationToken, int preferredPageSize) {
        return new BlockingByPagePagedFlux<>(pageRetrieverProvider, continuationToken, preferredPageSize);
    }

    @Override
    public void subscribe(CoreSubscriber<? super T> actual) {
        BlockingByItemSubscriber itemSubscription = new BlockingByItemSubscriber(actual,
            new ContinuationState<>(null), this.pageRetrieverProvider.get(), this.defaultPageSize);

        actual.onSubscribe(itemSubscription);
    }

    /*
     * Internal implementation of BlockingSubscriberBase which handles by item paged iterables.
     */
    private final class BlockingByItemSubscriber extends BlockingSubscriberBase<C, T, P, T> {
        volatile Queue<Iterator<T>> pages = new ConcurrentLinkedQueue<>();
        volatile Iterator<T> currentPage;

        BlockingByItemSubscriber(Subscriber<? super T> subscriber, ContinuationState<C> continuationState,
            PageRetriever<C, P> pageRetriever, Integer defaultPageSize) {
            super(subscriber, continuationState, pageRetriever, defaultPageSize);
        }

        @Override
        boolean needToRequestPage() {
            return (currentPage == null || !currentPage.hasNext()) && pages.peek() == null && !lastPage;
        }

        @Override
        boolean hasNext() {
            return (currentPage != null && currentPage.hasNext())
                || (pages.peek() != null && pages.peek().hasNext());
        }

        @Override
        T getNext() {
            if ((currentPage == null || !currentPage.hasNext()) && pages.peek() != null) {
                currentPage = pages.poll();
            }

            return currentPage.next();
        }

        @Override
        void addPage(P page, ContinuationState<C> continuationState) {
            this.lastPage = page.getContinuationToken() == null;
            continuationState.setLastContinuationToken(page.getContinuationToken());

            Iterator<T> pageValues = page.getElements().iterator();
            if (pageValues.hasNext()) {
                this.pages.add(pageValues);
            }
        }
    }
}
