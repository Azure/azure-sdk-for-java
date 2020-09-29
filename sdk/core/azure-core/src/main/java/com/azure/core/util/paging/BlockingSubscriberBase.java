// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.paging;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Operators;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * Internal class that is a blocking subscriber base class.
 * <p>
 * This class manages retrieving and maintaining previously retrieve page/pages in a synchronous fashion. It will ensure
 * the minimum number of pages are retrieved from a service by checking if any additional items/pages could be emitted
 * before requesting additional ones from the service.
 *
 * @param <C> The continuation token type.
 * @param <T> The item type.
 * @param <P> The page type.
 * @param <E> The type that the {@link ContinuablePagedIterable} will emit.
 */
abstract class BlockingSubscriberBase<C, T, P extends ContinuablePage<C, T>, E>
    implements Subscription {
    private final Subscriber<? super E> subscriber;
    private final ContinuationState<C> continuationState;
    private final PageRetriever<C, P> pageRetriever;
    private final Integer defaultPageSize;

    private volatile boolean receivedPages;
    volatile boolean lastPage;
    volatile boolean done;
    private Throwable error;
    private volatile boolean cancelled;

    volatile long wip;
    @SuppressWarnings("rawtypes")
    static final AtomicLongFieldUpdater<BlockingSubscriberBase> WIP =
        AtomicLongFieldUpdater.newUpdater(BlockingSubscriberBase.class, "wip");

    volatile long requested;
    @SuppressWarnings("rawtypes")
    static final AtomicLongFieldUpdater<BlockingSubscriberBase> REQUESTED =
        AtomicLongFieldUpdater.newUpdater(BlockingSubscriberBase.class, "requested");

    BlockingSubscriberBase(Subscriber<? super E> subscriber, ContinuationState<C> continuationState,
        PageRetriever<C, P> pageRetriever, Integer defaultPageSize) {
        this.subscriber = subscriber;
        this.continuationState = continuationState;
        this.pageRetriever = pageRetriever;
        this.defaultPageSize = defaultPageSize;
    }

    @Override
    public void request(long l) {
        if (Operators.validate(l)) {
            Operators.addCap(REQUESTED, this, l);
            drain(l);
        }
    }

    private void drain(long l) {
        if (WIP.getAndAccumulate(this, l, Long::sum) != 0) {
            return;
        }

        /*
         * Begin each request with checking if a page needs to be eagerly retrieved to begin satisfying downstream.
         */
        if (needToRequestPage()) {
            requestPage();
        }

        while (true) {
            if (cancelled) {
                return;
            }

            if (REQUESTED.get(this) > 0) {
                boolean emitted = false;
                boolean d = done;
                if (hasNext()) {
                    subscriber.onNext(getNext());
                    emitted = true;
                }

                /*
                 * If we are done due to error or we have reach the last page and there are no additional elements to
                 * emit complete the flux.
                 */
                if (d || (lastPage && !hasNext())) {
                    if (error != null) {
                        subscriber.onError(error);
                    } else {
                        subscriber.onComplete();
                    }

                    return;
                }

                if (emitted) {
                    Operators.produced(REQUESTED, this, 1);
                    if (WIP.decrementAndGet(this) == 0) {
                        return;
                    }
                } else {
                    // If we didn't emit we must request a page to continue satisfying downstream.
                    requestPage();
                }
            }
        }
    }

    /*
     * Indicates if a page needs to be requested.
     */
    abstract boolean needToRequestPage();

    /*
     * Indicates if an element can be emitted to satisfy a request.
     */
    abstract boolean hasNext();

    /*
     * Gets the next element to be emitted.
     */
    abstract E getNext();

    private void requestPage() {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        receivedPages = false;
        pageRetriever.get(continuationState.getLastContinuationToken(), defaultPageSize)
            .subscribe(page -> onConsume(page, continuationState),
                throwable -> onError(throwable, countDownLatch),
                () -> onComplete(countDownLatch));

        try {
            countDownLatch.await();
        } catch (InterruptedException ex) {
            error = ex;
        }
    }

    private synchronized void onConsume(P page, ContinuationState<C> continuationState) {
        receivedPages = true;
        addPage(page, continuationState);
    }

    /*
     * Add a page returned by the service and update the continuation state.
     */
    abstract void addPage(P page, ContinuationState<C> continuationState);

    private synchronized void onComplete(CountDownLatch countDownLatch) {
        lastPage = lastPage || (!receivedPages && !hasNext());
        countDownLatch.countDown();
    }

    private synchronized void onError(Throwable error, CountDownLatch countDownLatch) {
        this.error = error;
        this.done = true;
        countDownLatch.countDown();
    }

    @Override
    public final void cancel() {
        this.cancelled = true;
    }
}
