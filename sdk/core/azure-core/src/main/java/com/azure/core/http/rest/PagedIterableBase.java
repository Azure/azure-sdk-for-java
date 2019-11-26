// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.util.IterableStream;
import java.util.stream.Stream;

/**
 * This class provides utility to iterate over responses that extend {@link PagedResponse} using {@link Stream} and
 * {@link Iterable} interfaces.
 *
 * <p><strong>Code sample using {@link Stream} by page</strong></p>
 *
 * {@codesnippet com.azure.core.http.rest.pagedIterableBase.streamByPage}
 *
 * <p><strong>Code sample using {@link Iterable} by page</strong></p>
 *
 * {@codesnippet com.azure.core.http.rest.pagedIterableBase.iterableByPage}
 *
 * <p><strong>Code sample using {@link Iterable} by page and while loop</strong></p>
 *
 * {@codesnippet com.azure.core.http.rest.pagedIterableBase.iterableByPage.while}
 *
 * @param <T> The type of value contained in this {@link IterableStream}.
 * @param <P> The response extending from {@link PagedResponse}
 * @see PagedResponse
 * @see IterableStream
 */
public class PagedIterableBase<T, P extends PagedResponse<T>> extends IterableStream<T> {
    /*
     * This is the default batch size that will be requested when using stream or iterable by page, this will indicate
     * to Reactor how many elements should be prefetched before another batch is requested.
     */
    private static final int DEFAULT_BATCH_SIZE = 1;

    private final PagedFluxBase<T, P> pagedFluxBase;

    /**
     * Creates instance given {@link PagedFluxBase}.
     * @param pagedFluxBase to use as iterable
     */
    public PagedIterableBase(PagedFluxBase<T, P> pagedFluxBase) {
        super(pagedFluxBase);
        this.pagedFluxBase = pagedFluxBase;
    }

    /**
     * Retrieve the {@link Stream}, one page at a time.
     * It will provide same {@link Stream} of T values from starting if called multiple times.
     *
     * @return {@link Stream} of a Response that extends {@link PagedResponse}
     */
    public Stream<P> streamByPage() {
        return pagedFluxBase.byPage().toStream(DEFAULT_BATCH_SIZE);
    }

    /**
     * Retrieve the {@link Stream}, one page at a time, starting from the next page associated with the given
     * continuation token. To start from first page, use {@link #streamByPage()} instead.
     *
     * @param continuationToken The continuation token used to fetch the next page
     * @return {@link Stream} of a Response that extends {@link PagedResponse}, starting from the page associated
     * with the continuation token
     */
    public Stream<P> streamByPage(String continuationToken) {
        return pagedFluxBase.byPage(continuationToken).toStream(DEFAULT_BATCH_SIZE);
    }

    /**
     * Provides {@link Iterable} API for{ @link PagedResponse}
     * It will provide same collection of {@code T} values from starting if called multiple times.
     *
     * @return {@link Iterable} interface
     */
    public Iterable<P> iterableByPage() {
        return pagedFluxBase.byPage().toIterable(DEFAULT_BATCH_SIZE);
    }

    /**
     * Provides {@link Iterable} API for {@link PagedResponse}, starting from the next page associated with the given
     * continuation token. To start from first page, use {@link #streamByPage()} instead.
     * It will provide same collection of T values from starting if called multiple times.
     *
     * @param continuationToken The continuation token used to fetch the next page
     * @return {@link Iterable} interface
     */
    public Iterable<P> iterableByPage(String continuationToken) {
        return pagedFluxBase.byPage(continuationToken).toIterable(DEFAULT_BATCH_SIZE);
    }
}
