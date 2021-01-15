// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.paging;

import com.azure.core.util.logging.ClientLogger;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private volatile boolean done;

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
        // Request next pages in a loop in case we are returned empty pages for the by item implementation.
        while (!done && needToRequestPage()) {
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

    synchronized void requestPage() {
        /*
         * In the scenario where multiple threads were waiting on synchronization, check that no earlier thread made a
         * request that would satisfy the current element request. Additionally, check to make sure that any earlier
         * requests didn't consume the paged responses to completion.
         */
        if (isNextAvailable() || done) {
            return;
        }

        AtomicBoolean receivedPages = new AtomicBoolean(false);
        pageRetriever.get(continuationState.getLastContinuationToken(), defaultPageSize)
            .map(page -> {
                receivedPages.set(true);
                addPage(page);

                continuationState.setLastContinuationToken(page.getContinuationToken());
                this.done = continuationState.isDone();

                return page;
            }).blockLast();

        /*
         * In the scenario when the subscription completes without emitting an element indicate we are done by checking
         * if we have any additional elements to return.
         */
        this.done = done || (!receivedPages.get() && !isNextAvailable());
    }

    /*
     * Add a page returned by the service and update the continuation state.
     */
    abstract void addPage(P page);
}
