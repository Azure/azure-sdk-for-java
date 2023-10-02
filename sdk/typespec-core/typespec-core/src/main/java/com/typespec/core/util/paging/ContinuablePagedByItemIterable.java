// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util.paging;

import com.typespec.core.util.logging.ClientLogger;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;

/**
 * Internal class that is a blocking iterable for {@link ContinuablePagedIterable}.
 * <p>
 * This class retrieves pages from the service in a blocking manner while also respecting the number of items to be
 * retrieved. This functions differently than just wrapping a {@link ContinuablePagedFlux} as this will track the exact
 * number of items emitted and whether the previously retrieve page/pages contain any additional items that could be
 * emitted.
 *
 * @param <C> The continuation token type.
 * @param <T> The item type.
 * @param <P> The page type.
 */
final class ContinuablePagedByItemIterable<C, T, P extends ContinuablePage<C, T>> implements Iterable<T> {
    private final PageRetriever<C, P> pageRetriever;

    private final PageRetrieverSync<C, P> pageRetrieverSync;

    private final C continuationToken;
    private final Predicate<C> continuationPredicate;
    private final Integer preferredPageSize;

    ContinuablePagedByItemIterable(PageRetriever<C, P> pageRetriever, C continuationToken,
        Predicate<C> continuationPredicate, Integer preferredPageSize) {
        this.pageRetriever = pageRetriever;
        this.continuationToken = continuationToken;
        this.continuationPredicate = continuationPredicate;
        this.preferredPageSize = preferredPageSize;
        this.pageRetrieverSync = null;
    }

    ContinuablePagedByItemIterable(PageRetrieverSync<C, P> pageRetrieverSync, C continuationToken,
                                   Predicate<C> continuationPredicate, Integer preferredPageSize) {
        this.pageRetrieverSync = pageRetrieverSync;
        this.continuationToken = continuationToken;
        this.continuationPredicate = continuationPredicate;
        this.preferredPageSize = preferredPageSize;
        this.pageRetriever = null;
    }

    @Override
    public Iterator<T> iterator() {
        if (pageRetriever != null) {
            return new ContinuablePagedByItemIterator<>(pageRetriever, continuationToken, continuationPredicate,
                preferredPageSize);
        }
        return new ContinuablePagedByItemIterator<>(pageRetrieverSync, continuationToken, continuationPredicate,
            preferredPageSize);
    }

    private static final class ContinuablePagedByItemIterator<C, T, P extends ContinuablePage<C, T>>
        extends ContinuablePagedByIteratorBase<C, T, P, T> {
        // ContinuablePagedByItemIterator is a commonly used class, use a static logger.
        private static final ClientLogger LOGGER = new ClientLogger(ContinuablePagedByItemIterator.class);

        private volatile Queue<Iterator<T>> pages = new ConcurrentLinkedQueue<>();
        private volatile Iterator<T> currentPage;

        ContinuablePagedByItemIterator(PageRetriever<C, P> pageRetriever, C continuationToken,
            Predicate<C> continuationPredicate, Integer preferredPageSize) {
            super(pageRetriever, new ContinuationState<>(continuationToken, continuationPredicate), preferredPageSize,
                LOGGER);

            requestPage();
        }

        ContinuablePagedByItemIterator(PageRetrieverSync<C, P> pageRetrieverSync, C continuationToken,
                                       Predicate<C> continuationPredicate, Integer preferredPageSize) {
            super(pageRetrieverSync, new ContinuationState<>(continuationToken, continuationPredicate), preferredPageSize,
                LOGGER);

            requestPage();
        }

        @Override
        boolean needToRequestPage() {
            return (currentPage == null || !currentPage.hasNext()) && pages.peek() == null;
        }

        @Override
        public boolean isNextAvailable() {
            return (currentPage != null && currentPage.hasNext()) || pages.peek() != null;
        }

        @Override
        T getNext() {
            if ((currentPage == null || !currentPage.hasNext()) && pages.peek() != null) {
                currentPage = pages.poll();
            }

            return currentPage.next();
        }

        @Override
        void addPage(P page) {
            Iterator<T> pageValues = page.getElements().iterator();
            if (pageValues.hasNext()) {
                this.pages.add(pageValues);
            }
        }
    }
}
