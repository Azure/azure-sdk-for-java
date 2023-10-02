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
 * number of items emitted and whether the previously retrieve page/pages contain any additional pages that could be
 * emitted.
 *
 * @param <C> The continuation token type.
 * @param <T> The item type.
 * @param <P> The page type.
 */
final class ContinuablePagedByPageIterable<C, T, P extends ContinuablePage<C, T>> implements Iterable<P> {
    private final PageRetriever<C, P> pageRetriever;

    private final PageRetrieverSync<C, P> pageRetrieverSync;

    private final C continuationToken;
    private final Predicate<C> continuationPredicate;
    private final Integer preferredPageSize;

    ContinuablePagedByPageIterable(PageRetriever<C, P> pageRetriever, C continuationToken,
        Predicate<C> continuationPredicate, Integer preferredPageSize) {
        this.pageRetriever = pageRetriever;
        this.continuationToken = continuationToken;
        this.continuationPredicate = continuationPredicate;
        this.preferredPageSize = preferredPageSize;
        this.pageRetrieverSync = null;
    }

    ContinuablePagedByPageIterable(PageRetrieverSync<C, P> pageRetrieverSync, C continuationToken,
                                   Predicate<C> continuationPredicate, Integer preferredPageSize) {
        this.pageRetrieverSync = pageRetrieverSync;
        this.continuationToken = continuationToken;
        this.continuationPredicate = continuationPredicate;
        this.preferredPageSize = preferredPageSize;
        this.pageRetriever = null;
    }

    @Override
    public Iterator<P> iterator() {
        if (pageRetriever != null) {
            return new ContinuablePagedByPageIterator<>(pageRetriever, continuationToken, continuationPredicate,
                preferredPageSize);
        }
        return new ContinuablePagedByPageIterator<>(pageRetrieverSync, continuationToken, continuationPredicate,
            preferredPageSize);
    }

    private static final class ContinuablePagedByPageIterator<C, T, P extends ContinuablePage<C, T>>
        extends ContinuablePagedByIteratorBase<C, T, P, P> {
        // ContinuablePagedByPageIterator is a commonly used class, use static logger.
        private static final ClientLogger LOGGER = new ClientLogger(ContinuablePagedByPageIterator.class);

        private volatile Queue<P> pages = new ConcurrentLinkedQueue<>();

        ContinuablePagedByPageIterator(PageRetriever<C, P> pageRetriever, C continuationToken,
            Predicate<C> continuationPredicate, Integer preferredPageSize) {
            super(pageRetriever, new ContinuationState<>(continuationToken, continuationPredicate), preferredPageSize,
                LOGGER);

            requestPage();
        }

        ContinuablePagedByPageIterator(PageRetrieverSync<C, P> pageRetrieverSync, C continuationToken,
                                       Predicate<C> continuationPredicate, Integer preferredPageSize) {
            super(pageRetrieverSync, new ContinuationState<>(continuationToken, continuationPredicate),
                preferredPageSize, LOGGER);

            requestPage();
        }

        @Override
        boolean needToRequestPage() {
            return pages.peek() == null;
        }

        @Override
        public boolean isNextAvailable() {
            return pages.peek() != null;
        }

        @Override
        P getNext() {
            return pages.poll();
        }

        @Override
        void addPage(P page) {
            this.pages.add(page);
        }
    }
}
