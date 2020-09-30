// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.paging;

import com.azure.core.util.logging.ClientLogger;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

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
    private final C continuationToken;
    private final Integer preferredPageSize;

    ContinuablePagedByPageIterable(PageRetriever<C, P> pageRetriever, C continuationToken, Integer preferredPageSize) {
        this.pageRetriever = pageRetriever;
        this.continuationToken = continuationToken;
        this.preferredPageSize = preferredPageSize;
    }

    @Override
    public Iterator<P> iterator() {
        return new ContinuablePagedByPageIterator<>(pageRetriever, continuationToken, preferredPageSize);
    }

    private static final class ContinuablePagedByPageIterator<C, T, P extends ContinuablePage<C, T>>
        extends ContinuablePagedByIteratorBase<C, T, P, P> {
        private volatile Queue<P> pages = new ConcurrentLinkedQueue<>();

        ContinuablePagedByPageIterator(PageRetriever<C, P> pageRetriever, C continuationToken,
            Integer preferredPageSize) {
            super(pageRetriever, new ContinuationState<>(continuationToken), preferredPageSize,
                new ClientLogger(ContinuablePagedByPageIterator.class));

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
