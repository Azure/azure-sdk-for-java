// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import io.clientcore.core.util.ClientLogger;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This class provides utility to iterate over {@link PagedResponse} using {@link Stream} and {@link Iterable}
 * interfaces.
 *
 * @param <T> The type of items in the page.
 */
public final class PagedIterable<T> implements Iterable<T> {

    private final Function<String, PagedResponse<T>> pageRetriever;

    /**
     * Creates an instance of {@link PagedIterable} that consists of only a single page. This constructor takes a {@code
     * Supplier} that return the single page of {@code T}.
     *
     * @param firstPageRetriever Supplier that retrieves the first page.
     */
    public PagedIterable(Supplier<PagedResponse<T>> firstPageRetriever) {
        this(firstPageRetriever, null);
    }

    /**
     * Creates an instance of {@link PagedIterable}. The constructor takes a {@code Supplier} and {@code Function}. The
     * {@code Supplier} returns the first page of {@code T}, the {@code Function} retrieves subsequent pages of {@code
     * T}.
     *
     * @param firstPageRetriever Supplier that retrieves the first page.
     * @param nextPageRetriever Function that retrieves the next page given a continuation token
     */
    public PagedIterable(Supplier<PagedResponse<T>> firstPageRetriever,
        Function<String, PagedResponse<T>> nextPageRetriever) {
        this.pageRetriever = (continuationToken) -> (continuationToken == null)
            ? firstPageRetriever.get()
            : nextPageRetriever.apply(continuationToken);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<T> iterator() {
        return iterableByItemInternal().iterator();
    }

    /**
     * Retrieve the {@link Iterable}, one page at a time. It will provide same {@link Iterable} of T values from
     * starting if called multiple times.
     *
     * @return {@link Iterable} of a pages
     */
    public Iterable<PagedResponse<T>> iterableByPage() {
        return iterableByPageInternal();
    }

    /**
     * Utility function to provide {@link Stream} of value {@code T}.
     *
     * @return {@link Stream} of value {@code T}.
     */
    public Stream<T> stream() {
        return StreamSupport.stream(iterableByItemInternal().spliterator(), false);
    }

    /**
     * Retrieve the {@link Stream}, one page at a time. It will provide same {@link Stream} of T values from starting if
     * called multiple times.
     *
     * @return {@link Stream} of a pages
     */
    public Stream<PagedResponse<T>> streamByPage() {
        return StreamSupport.stream(iterableByPage().spliterator(), false);
    }

    private Iterable<T> iterableByItemInternal() {
        return () -> new PagedByIterator<>(pageRetriever) {

            private final Queue<Iterator<T>> pages = new ConcurrentLinkedQueue<>();
            private volatile Iterator<T> currentPage;

            @Override
            boolean needToRequestPage() {
                return (currentPage == null || !currentPage.hasNext()) && pages.peek() == null;
            }

            @Override
            boolean isNextAvailable() {
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
            void addPage(PagedResponse<T> page) {
                Iterator<T> pageValues = page.getValue().iterator();
                if (pageValues.hasNext()) {
                    this.pages.add(pageValues);
                }
            }
        };
    }

    private Iterable<PagedResponse<T>> iterableByPageInternal() {
        return () -> new PagedByIterator<T, PagedResponse<T>>(pageRetriever) {

            private final Queue<PagedResponse<T>> pages = new ConcurrentLinkedQueue<>();

            @Override
            boolean needToRequestPage() {
                return pages.peek() == null;
            }

            @Override
            boolean isNextAvailable() {
                return pages.peek() != null;
            }

            @Override
            PagedResponse<T> getNext() {
                return pages.poll();
            }

            @Override
            void addPage(PagedResponse<T> page) {
                this.pages.add(page);
            }
        };
    }

    private abstract static class PagedByIterator<T, E> implements Iterator<E> {
        private static final ClientLogger LOGGER = new ClientLogger(PagedByIterator.class);

        private final Function<String, PagedResponse<T>> pageRetriever;
        private volatile String continuationToken;
        private volatile boolean done;

        PagedByIterator(Function<String, PagedResponse<T>> pageRetriever) {
            this.pageRetriever = pageRetriever;
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw LOGGER.logThrowableAsError(new NoSuchElementException("Iterator contains no more elements."));
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

        abstract boolean needToRequestPage();

        abstract boolean isNextAvailable();

        abstract E getNext();

        synchronized void requestPage() {
            AtomicBoolean receivedPages = new AtomicBoolean(false);
            PagedResponse<T> page = pageRetriever.apply(continuationToken);
            if (page != null) {
                receivePage(receivedPages, page);
            }
        }

        abstract void addPage(PagedResponse<T> page);

        private void receivePage(AtomicBoolean receivedPages, PagedResponse<T> page) {
            receivedPages.set(true);
            addPage(page);

            continuationToken = page.getNextLink();
            this.done = continuationToken == null || continuationToken.isEmpty();
        }
    }
}
