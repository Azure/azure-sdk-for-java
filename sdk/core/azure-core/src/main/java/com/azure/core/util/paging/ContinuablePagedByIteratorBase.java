// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.paging;

import com.azure.core.util.logging.ClientLogger;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;

/**
 * Internal class that is a blocking iterator base class.
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
abstract class ContinuablePagedByIteratorBase<C, T, P extends ContinuablePage<C, T>, E> implements Iterator<E> {
    private final PageRetriever<C, P> pageRetriever;
    private final ContinuationState<C> continuationState;
    private final Integer defaultPageSize;
    private final ClientLogger logger;

    private volatile boolean receivedPages;
    volatile boolean lastPage;
    private Throwable error;

    ContinuablePagedByIteratorBase(PageRetriever<C, P> pageRetriever, ContinuationState<C> continuationState,
        Integer defaultPageSize, ClientLogger logger) {
        this.continuationState = continuationState;
        this.pageRetriever = pageRetriever;
        this.defaultPageSize = defaultPageSize;
        this.logger = logger;
    }

    @Override
    public E next() {
        if (!hasNext()) {
            throw logger.logExceptionAsError(new NoSuchElementException("Iterator contains no more elements."));
        }

        return getNext();
    }

    @Override
    public boolean hasNext() {
        if (needToRequestPage()) {
            requestPage();
        }

        return isNextAvailable();
    }

    /*
     * Indicates if a page needs to be requested.
     */
    abstract boolean needToRequestPage();

    /*
     * Indicates if another element is available.
     */
    abstract boolean isNextAvailable();

    /*
     * Gets the next element to be emitted.
     */
    abstract E getNext();

    void requestPage() {
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

        if (error != null) {
            throw logger.logExceptionAsError(new RuntimeException(error));
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
        lastPage = lastPage || (!receivedPages && !isNextAvailable());
        countDownLatch.countDown();
    }

    private synchronized void onError(Throwable error, CountDownLatch countDownLatch) {
        this.error = error;
        countDownLatch.countDown();
    }
}
