// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.paging;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This class provides utility to iterate over {@link PagedResponse} using {@link Stream} and {@link Iterable}
 * interfaces.
 *
 * @param <T> The type of items in the page.
 * @param <C> The type of the continuation token.
 */
@Metadata(properties = MetadataProperties.IMMUTABLE)
public final class PagedIterable<T, C> implements Iterable<T> {

    private final Function<PagingContext<C>, PagedResponse<T, C>> pageRetriever;
    private final Predicate<C> continuationPredicate;

    /**
     * Creates a token-based {@link PagedIterable} using the default continuation predicate.
     *
     * <p>The retriever is called with the token returned by the previous page. The default predicate stops paging for
     * a {@code null} token or an empty {@link CharSequence}. All other tokens continue paging. Use
     * {@link #PagedIterable(Function, Predicate)} when a token has another terminal representation.</p>
     *
     * <p>This overload doesn't follow next links. Use {@link #PagedIterable(Function, BiFunction)} when pages return a
     * next link.</p>
     *
     * @param pageRetriever Function that retrieves a page given paging options.
     * @throws NullPointerException If {@code pageRetriever} is {@code null}.
     */
    public PagedIterable(Function<PagingOptions<C>, PagedResponse<T, C>> pageRetriever) {
        this(pageRetriever, ((pagingOptions, nextLink) -> null));
    }

    /**
     * Creates a token-based {@link PagedIterable} using a custom continuation predicate.
     *
     * <p>The retriever is called with the token returned by the previous page. The predicate is evaluated after each
     * page, including terminal values such as {@code null}, and another page is requested only when it returns
     * {@code true}. This overload doesn't follow next links.</p>
     *
     * @param pageRetriever Function that retrieves a page given paging options.
     * @param continuationPredicate Predicate that determines whether another page should be requested for a token.
     * @throws NullPointerException If either argument is {@code null}.
     */
    public PagedIterable(Function<PagingOptions<C>, PagedResponse<T, C>> pageRetriever,
        Predicate<C> continuationPredicate) {
        this(pageRetriever, ((pagingOptions, nextLink) -> null), continuationPredicate);
    }

    /**
     * Creates a {@link PagedIterable} that supports next links and token-based paging using the default continuation
     * predicate.
     *
     * <p>The next-page retriever is used when a page has a next link. Otherwise, the first-page retriever is called
     * with the continuation token returned by the previous page. The default predicate stops paging for a {@code null}
     * token or an empty {@link CharSequence}.</p>
     *
     * @param firstPageRetriever Function that retrieves the first page or a page identified by paging options.
     * @param nextPageRetriever Function that retrieves a page identified by a next link.
     * @throws NullPointerException If either argument is {@code null}.
     */
    public PagedIterable(Function<PagingOptions<C>, PagedResponse<T, C>> firstPageRetriever,
        BiFunction<PagingOptions<C>, String, PagedResponse<T, C>> nextPageRetriever) {
        this(firstPageRetriever, nextPageRetriever, PagedIterable::hasContinuationToken);
    }

    /**
     * Creates an instance of {@link PagedIterable} supporting continuation tokens and next links.
     *
     * @param firstPageRetriever Function that retrieves the first page or a page identified by paging options.
     * @param nextPageRetriever Function that retrieves a page identified by a next link.
    * @param continuationPredicate Predicate that determines whether another page should be requested when no next
    * link is present. The predicate receives terminal values such as {@code null}.
     * @throws NullPointerException If any argument is {@code null}.
     */
    public PagedIterable(Function<PagingOptions<C>, PagedResponse<T, C>> firstPageRetriever,
        BiFunction<PagingOptions<C>, String, PagedResponse<T, C>> nextPageRetriever,
        Predicate<C> continuationPredicate) {
        Objects.requireNonNull(firstPageRetriever, "'firstPageRetriever' cannot be null.");
        Objects.requireNonNull(nextPageRetriever, "'nextPageRetriever' cannot be null.");
        this.continuationPredicate
            = Objects.requireNonNull(continuationPredicate, "'continuationPredicate' cannot be null.");
        this.pageRetriever = context -> (context.getNextLink() == null || context.getNextLink().isEmpty())
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
    public Iterable<PagedResponse<T, C>> iterableByPage() {
        return iterableByPageInternal(null);
    }

    /**
     * Retrieve the {@link Iterable}, one page at a time. It will provide same {@link Iterable} of pages from
     * starting if called multiple times.
     *
     * @param pagingOptions the paging options
     * @return {@link Iterable} of a pages
     */
    public Iterable<PagedResponse<T, C>> iterableByPage(PagingOptions<C> pagingOptions) {
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
    public Stream<PagedResponse<T, C>> streamByPage() {
        return StreamSupport.stream(iterableByPage().spliterator(), false);
    }

    /**
     * Retrieve the {@link Stream}, one page at a time. It will provide same {@link Stream} of T values from starting if
     * called multiple times.
     *
     * @param pagingOptions the paging options
     * @return {@link Stream} of a pages
     */
    public Stream<PagedResponse<T, C>> streamByPage(PagingOptions<C> pagingOptions) {
        return StreamSupport.stream(iterableByPage(pagingOptions).spliterator(), false);
    }

    private static final class PagingContext<C> {
        private final PagingOptions<C> pagingOptions;
        private final String nextLink;

        private PagingContext(PagingOptions<C> pagingOptions, String nextLink) {
            this.pagingOptions = pagingOptions;
            this.nextLink = nextLink;
        }

        private PagingOptions<C> getPagingOptions() {
            return pagingOptions;
        }

        private String getNextLink() {
            return nextLink;
        }
    }

    private Iterable<T> iterableByItemInternal(PagingOptions<C> pagingOptions) {
        return () -> new PagedIterator<T, C, T>(pageRetriever, pagingOptions, continuationPredicate) {

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
            void addPage(PagedResponse<T, C> page) {
                Iterator<T> pageValues = page.getValue().iterator();
                if (pageValues.hasNext()) {
                    nextPage = pageValues;
                }
            }
        };
    }

    private Iterable<PagedResponse<T, C>> iterableByPageInternal(PagingOptions<C> pagingOptions) {
        return () -> new PagedIterator<T, C, PagedResponse<T, C>>(pageRetriever, pagingOptions, continuationPredicate) {

            private PagedResponse<T, C> nextPage;

            @Override
            boolean needToRequestPage() {
                return nextPage == null;
            }

            @Override
            boolean isNextAvailable() {
                return nextPage != null;
            }

            @Override
            PagedResponse<T, C> getNext() {
                PagedResponse<T, C> currentPage = nextPage;
                nextPage = null;
                return currentPage;
            }

            @Override
            void addPage(PagedResponse<T, C> page) {
                nextPage = page;
            }
        };
    }

    private abstract static class PagedIterator<T, C, E> implements Iterator<E> {
        private static final ClientLogger LOGGER = new ClientLogger(PagedIterator.class);

        private final Function<PagingContext<C>, PagedResponse<T, C>> pageRetriever;
        private final Predicate<C> continuationPredicate;
        private final Long pageSize;
        private C continuationToken;
        private String nextLink;
        private boolean done;

        PagedIterator(Function<PagingContext<C>, PagedResponse<T, C>> pageRetriever, PagingOptions<C> pagingOptions,
            Predicate<C> continuationPredicate) {
            this.pageRetriever = pageRetriever;
            this.continuationPredicate = continuationPredicate;
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
            PagingOptions<C> pagingOptions = new PagingOptions<>();
            pagingOptions.setPageSize(pageSize);
            pagingOptions.setContinuationToken(continuationToken);
            PagedResponse<T, C> page = pageRetriever.apply(new PagingContext<>(pagingOptions, nextLink));
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

        abstract void addPage(PagedResponse<T, C> page);

        private void receivePage(PagedResponse<T, C> page) {
            addPage(page);

            nextLink = page.getNextLink();
            continuationToken = page.getContinuationToken();
            this.done = (nextLink == null || nextLink.isEmpty()) && !continuationPredicate.test(continuationToken);
        }
    }

    private static <C> boolean hasContinuationToken(C continuationToken) {
        return continuationToken != null
            && (!(continuationToken instanceof CharSequence) || ((CharSequence) continuationToken).length() > 0);
    }
}
