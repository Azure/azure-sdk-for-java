// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.util.IterableStream;

import java.util.stream.Stream;

/**
 * This class provides utility to iterate over {@link PagedResponse} using {@link Stream} and {@link Iterable}
 * interfaces.
 *
 * <p><strong>Code sample using {@link Stream} by page</strong></p>
 *
 * {@codesnippet com.azure.core.http.rest.pagedIterable.streamByPage}
 *
 * <p><strong>Code sample using {@link Iterable} by page</strong></p>
 *
 * {@codesnippet com.azure.core.http.rest.pagedIterable.iterableByPage}
 *
 * <p><strong>Code sample using {@link Iterable} by page and while loop</strong></p>
 *
 * {@codesnippet com.azure.core.http.rest.pagedIterable.iterableByPage.while}
 *
 * @param <T> The type of value contained in this {@link IterableStream}.
 * @see PagedResponse
 * @see IterableStream
 */
public class PagedIterable<T> extends IterableStream<T> {
    private final PagedFluxBase<T, ? extends PagedResponse<T>> pagedFluxBase;

    /**
     * Creates instance given {@link PagedFluxBase}.
     * @param pagedFluxBase to use as iterable
     */
    public <P extends PagedResponse<T>> PagedIterable(PagedFluxBase<T, P> pagedFluxBase) {
        super(pagedFluxBase);
        this.pagedFluxBase = pagedFluxBase;
    }

    /**
     * Retrieve the {@link Stream}, one page at a time.
     * It will provide same {@link Stream} of T values from starting if called multiple times.
     * @return {@link Stream} of a Response that extends {@link PagedResponse}
     */
    @SuppressWarnings("unchecked")
    public <P extends PagedResponse<T>> Stream<P> streamByPage() {
        return (Stream<P>) pagedFluxBase.byPage().toStream();
    }

    /**
     * Retrieve the {@link Stream}, one page at a time, starting from the next page associated with the given
     * continuation token. To start from first page, use {@link #streamByPage()} instead.
     *
     * @param continuationToken The continuation token used to fetch the next page
     *
     * @return {@link Stream} of a Response that extends {@link PagedResponse}, starting from the page associated
     * with the continuation token
     */
    @SuppressWarnings("unchecked")
    public <P extends PagedResponse<T>> Stream<P> streamByPage(String continuationToken) {
        return (Stream<P>) pagedFluxBase.byPage(continuationToken).toStream();
    }

    /**
     * Provides {@link Iterable} API for{ @link PagedResponse}
     * It will provide same collection of T values from starting if called multiple times.
     * @return {@link Iterable} interface
     */
    @SuppressWarnings("unchecked")
    public <P extends  PagedResponse<T>> Iterable<P> iterableByPage() {
        return (Iterable<P>) pagedFluxBase.byPage().toIterable();
    }

    /**
     * Provides {@link Iterable} API for {@link PagedResponse}, starting from the next page associated with the given
     * continuation token. To start from first page, use {@link #streamByPage()} instead.
     * It will provide same collection of T values from starting if called multiple times.
     *
     * @param continuationToken The continuation token used to fetch the next page
     *
     * @return {@link Iterable} interface
     */
    @SuppressWarnings("unchecked")
    public <P extends  PagedResponse<T>> Iterable<P> iterableByPage(String continuationToken) {
        return (Iterable<P>) pagedFluxBase.byPage(continuationToken).toIterable();
    }
}
