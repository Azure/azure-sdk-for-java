// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.paging;

import com.azure.core.util.IterableStream;

import java.util.stream.Stream;

/**
 * This class provides utility to iterate over {@link ContinuablePage} using {@link Stream}
 * {@link Iterable} interfaces.
 *
 * @param <C> the type of the continuation token
 * @param <T> The type of elements in a {@link ContinuablePage}
 * @param <P> The {@link ContinuablePage} holding items of type {@code T}.
 *
 * @see IterableStream
 * @see ContinuablePagedFlux
 */
public abstract class ContinuablePagedIterable<C, T, P extends ContinuablePage<C, T>> extends IterableStream<T> {
    private final ContinuablePagedFlux<C, T, P> pagedFlux;
    private final int batchSize;

    /**
     * Creates instance with the given {@link ContinuablePagedFlux}.
     *
     * @param pagedFlux the paged flux use as iterable
     */
    public ContinuablePagedIterable(ContinuablePagedFlux<C, T, P> pagedFlux) {
        this(pagedFlux, 1);
    }

    /**
     * Creates instance with the given {@link ContinuablePagedFlux}.
     *
     * @param pagedFlux the paged flux use as iterable
     * @param batchSize the bounded capacity to prefetch from the {@link ContinuablePagedFlux}
     */
    public ContinuablePagedIterable(ContinuablePagedFlux<C, T, P> pagedFlux, int batchSize) {
        super(pagedFlux);
        this.pagedFlux = pagedFlux;
        this.batchSize = batchSize;
    }

    /**
     * Retrieve the {@link Stream}, one page at a time.
     * It will provide same {@link Stream} of T values from starting if called multiple times.
     *
     * @return {@link Stream} of a pages
     */
    public Stream<P> streamByPage() {
        return pagedFlux.byPage().toStream(this.batchSize);
    }

    /**
     * Retrieve the {@link Stream}, one page at a time, starting from the next page associated with the given
     * continuation token. To start from first page, use {@link #streamByPage()} instead.
     *
     * @param continuationToken The continuation token used to fetch the next page
     * @return {@link Stream} of a pages
     */
    public Stream<P> streamByPage(C continuationToken) {
        return this.pagedFlux.byPage(continuationToken).toStream(this.batchSize);
    }

    /**
     * Retrieve the {@link Stream}, one page at a time, with each page containing {@code preferredPageSize}
     * items.
     *
     * It will provide same {@link Stream} of T values from starting if called multiple times.
     *
     * @param preferredPageSize the preferred page size, service may or may not honor the page
     *                          size preference hence client MUST be prepared to handle pages
     *                          with different page size.
     * @return {@link Stream} of a pages
     */
    public Stream<P> streamByPage(int preferredPageSize) {
        return this.pagedFlux.byPage(null, preferredPageSize).toStream(this.batchSize);
    }

    /**
     * Retrieve the {@link Stream}, one page at a time, with each page containing {@code preferredPageSize}
     * items, starting from the next page associated with the given continuation token.
     * To start from first page, use {@link #streamByPage()} or {@link #streamByPage(int)} instead.
     *
     * @param preferredPageSize the preferred page size, service may or may not honor the page
     *                          size preference hence client MUST be prepared to handle pages
     *                          with different page size.
     * @param continuationToken The continuation token used to fetch the next page
     * @return {@link Stream} of a pages
     */
    public Stream<P> streamByPage(C continuationToken, int preferredPageSize) {
        return this.pagedFlux.byPage(continuationToken, preferredPageSize).toStream(this.batchSize);
    }

    /**
     * Retrieve the {@link Iterable}, one page at a time.
     * It will provide same {@link Iterable} of T values from starting if called multiple times.
     *
     * @return {@link Stream} of a pages
     */
    public Iterable<P> iterableByPage() {
        return this.pagedFlux.byPage().toIterable(this.batchSize);
    }

    /**
     * Retrieve the {@link Iterable}, one page at a time, starting from the next page associated with the given
     * continuation token. To start from first page, use {@link #iterableByPage()} instead.
     *
     * @param continuationToken The continuation token used to fetch the next page
     * @return {@link Iterable} of a pages
     */
    public Iterable<P> iterableByPage(C continuationToken) {
        return this.pagedFlux.byPage(continuationToken).toIterable(this.batchSize);
    }

    /**
     * Retrieve the {@link Iterable}, one page at a time, with each page containing {@code preferredPageSize}
     * items.
     *
     * It will provide same {@link Iterable} of T values from starting if called multiple times.
     *
     * @param preferredPageSize the preferred page size, service may or may not honor the page
     *                          size preference hence client MUST be prepared to handle pages
     *                          with different page size.
     * @return {@link Iterable} of a pages
     */
    public Iterable<P> iterableByPage(int preferredPageSize) {
        return this.pagedFlux.byPage(null, preferredPageSize).toIterable(this.batchSize);
    }

    /**
     * Retrieve the {@link Iterable}, one page at a time, with each page containing {@code preferredPageSize}
     * items, starting from the next page associated with the given continuation token.
     * To start from first page, use {@link #iterableByPage()} or {@link #iterableByPage(int)} instead.
     *
     * @param preferredPageSize the preferred page size, service may or may not honor the page
     *                          size preference hence client MUST be prepared to handle pages
     *                          with different page size.
     * @param continuationToken The continuation token used to fetch the next page
     * @return {@link Iterable} of a pages
     */
    public Iterable<P> iterableByPage(C continuationToken, int preferredPageSize) {
        return this.pagedFlux.byPage(continuationToken, preferredPageSize).toIterable(this.batchSize);
    }
}
