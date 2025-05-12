// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.paging;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This class provides utility to iterate over {@link PagedResponse} using {@link Stream} and {@link Iterable}
 * interfaces.
 *
 * @param <T> The type of items in the page.
 */
@Metadata(properties = MetadataProperties.IMMUTABLE)
public final class PagedIterable<T> implements Iterable<T> {

    private final Function<PagingContext, PagedResponse<T>> pageRetriever;

    /**
     * Creates an instance of {@link PagedIterable} that consists of only a single page. This constructor takes a {@code
     * Supplier} that return the single page of {@code T}.
     *
     * @param firstPageRetriever Function that retrieves the first page, given paging options.
     */
    public PagedIterable(Function<PagingOptions, PagedResponse<T>> firstPageRetriever) {
        this(firstPageRetriever, ((pagingOptions, nextLink) -> null));
    }

    /**
     * Creates an instance of {@link PagedIterable}. The constructor takes a {@code Supplier} and {@code Function}. The
     * {@code Supplier} returns the first page of {@code T}, the {@code Function} retrieves subsequent pages of {@code
     * T}.
     *
     * @param firstPageRetriever Function that retrieves the first page, given paging options.
     * @param nextPageRetriever Function that retrieves the next page, given paging options and next link.
     */
    public PagedIterable(Function<PagingOptions, PagedResponse<T>> firstPageRetriever,
        BiFunction<PagingOptions, String, PagedResponse<T>> nextPageRetriever) {
        this.pageRetriever = context -> (context.getNextLink() == null)
            ? firstPageRetriever.apply(context.getPagingOptions())
            : nextPageRetriever.apply(context.getPagingOptions(), context.getNextLink());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<T> iterator() {
        return iterableByItemInternal(null).iterator();
    }

    /**
     * Retrieve the {@link Iterable}, one page at a time. It will provide same {@link Iterable} of T values from
     * starting if called multiple times.
     *
     * @return {@link Iterable} of a pages
     */
    public Iterable<PagedResponse<T>> iterableByPage() {
        return iterableByPageInternal(null);
    }

    /**
     * Retrieve the {@link Iterable}, one page at a time. It will provide same {@link Iterable} of pages from
     * starting if called multiple times.
     *
     * @param pagingOptions the paging options
     * @return {@link Iterable} of a pages
     */
    public Iterable<PagedResponse<T>> iterableByPage(PagingOptions pagingOptions) {
        return iterableByPageInternal(pagingOptions);
    }

    /**
     * Retrieve the {@link Stream} of value {@code T}. It will provide same {@link Stream} of T values from
     * starting if called multiple times.
     *
     * @return {@link Stream} of value {@code T}
     */
    public Stream<T> stream() {
        return StreamSupport.stream(iterableByItemInternal(null).spliterator(), false);
    }

    /**
     * Retrieve the {@link Stream}, one page at a time. It will provide same {@link Stream} of pages from starting if
     * called multiple times.
     *
     * @return {@link Stream} of a pages
     */
    public Stream<PagedResponse<T>> streamByPage() {
        return StreamSupport.stream(iterableByPage().spliterator(), false);
    }

    /**
     * Retrieve the {@link Stream}, one page at a time. It will provide same {@link Stream} of T values from starting if
     * called multiple times.
     *
     * @param pagingOptions the paging options
     * @return {@link Stream} of a pages
     */
    public Stream<PagedResponse<T>> streamByPage(PagingOptions pagingOptions) {
        return StreamSupport.stream(iterableByPage(pagingOptions).spliterator(), false);
    }

    private static final class PagingContext {
        private final PagingOptions pagingOptions;
        private final String nextLink;

        private PagingContext(PagingOptions pagingOptions, String nextLink) {
            this.pagingOptions = pagingOptions;
            this.nextLink = nextLink;
        }

        private PagingOptions getPagingOptions() {
            return pagingOptions;
        }

        private String getNextLink() {
            return nextLink;
        }
    }

    private Iterable<T> iterableByItemInternal(PagingOptions pagingOptions) {
        return () -> new PagedIterator<T, T>(pageRetriever, pagingOptions) {

            private Iterator<T> nextPage;
            private Iterator<T> currentPage;

            @Override
            boolean needToRequestPage() {
                return (currentPage == null || !currentPage.hasNext()) && nextPage == null;
            }

            @Override
            boolean isNextAvailable() {
                return (currentPage != null && currentPage.hasNext()) || nextPage != null;
            }

            @Override
            T getNext() {
                if ((currentPage == null || !currentPage.hasNext()) && nextPage != null) {
                    currentPage = nextPage;
                    nextPage = null;
                }

                return currentPage.next();
            }

            @Override
            void addPage(PagedResponse<T> page) {
                Iterator<T> pageValues = page.getValue().iterator();
                if (pageValues.hasNext()) {
                    nextPage = pageValues;
                }
            }
        };
    }

    private Iterable<PagedResponse<T>> iterableByPageInternal(PagingOptions pagingOptions) {
        return () -> new PagedIterator<T, PagedResponse<T>>(pageRetriever, pagingOptions) {

            private PagedResponse<T> nextPage;

            @Override
            boolean needToRequestPage() {
                return nextPage == null;
            }

            @Override
            boolean isNextAvailable() {
                return nextPage != null;
            }

            @Override
            PagedResponse<T> getNext() {
                PagedResponse<T> currentPage = nextPage;
                nextPage = null;
                return currentPage;
            }

            @Override
            void addPage(PagedResponse<T> page) {
                nextPage = page;
            }
        };
    }

    private abstract static class PagedIterator<T, E> implements Iterator<E> {
        private static final ClientLogger LOGGER = new ClientLogger(PagedIterator.class);

        private final Function<PagingContext, PagedResponse<T>> pageRetriever;
        private final Long pageSize;
        private String continuationToken;
        private String nextLink;
        private boolean done;

        PagedIterator(Function<PagingContext, PagedResponse<T>> pageRetriever, PagingOptions pagingOptions) {
            this.pageRetriever = pageRetriever;
            this.pageSize = pagingOptions == null ? null : pagingOptions.getPageSize();
            this.continuationToken = pagingOptions == null ? null : pagingOptions.getContinuationToken();
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw LOGGER.throwableAtError().log("Iterator contains no more elements.", NoSuchElementException::new);
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

        void requestPage() {
            boolean receivedPages = false;
            PagingOptions pagingOptions = new PagingOptions();
            pagingOptions.setPageSize(pageSize);
            pagingOptions.setContinuationToken(continuationToken);
            PagedResponse<T> page = pageRetriever.apply(new PagingContext(pagingOptions, nextLink));
            if (page != null) {
                receivePage(page);
                receivedPages = true;
            }

            /*
             * In the scenario when the subscription completes without emitting an element indicate we are done by checking
             * if we have any additional elements to return.
             */
            this.done = done || (!receivedPages && !isNextAvailable());
        }

        abstract void addPage(PagedResponse<T> page);

        private void receivePage(PagedResponse<T> page) {
            addPage(page);

            nextLink = page.getNextLink();
            continuationToken = page.getContinuationToken();
            this.done = (nextLink == null || nextLink.isEmpty())
                && (continuationToken == null || continuationToken.isEmpty());
        }
    }
}
