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
 * @param  <T> The type of value contained in this {@link IterableStream}.
 * @see PagedResponse
 * @see IterableStream
 */
public class PagedIterable<T> extends IterableStream<T> {
    private final PagedFlux<T> pagedFlux;

    /**
     * Creates instance given {@link PagedFlux}.
     * @param pagedFlux to use as iterable
     */
    public PagedIterable(PagedFlux<T> pagedFlux) {
        super(pagedFlux);
        this.pagedFlux = pagedFlux;
    }

    /**
     * Retrieve the {@link Stream}, one page at a time.
     * It will provide same {@link Stream} of T values from starting if called multiple times.
     * @return {@link Stream} of {@link PagedResponse}
     */
    public Stream<PagedResponse<T>> streamByPage() {
        return pagedFlux.byPage().toStream();
    }

    /**
     * Retrieve the {@link Stream}, one page at a time, starting from the next page associated with the given
     * continuation token. To start from first page, use {@link #streamByPage()} instead.
     *
     * @param continuationToken The continuation token used to fetch the next page
     *
     * @return {@link Stream} of {@link PagedResponse}, starting from the page associated with the continuation token
     */
    public Stream<PagedResponse<T>> streamByPage(String continuationToken) {
        return pagedFlux.byPage(continuationToken).toStream();
    }

    /**
     * Provides {@link Iterable} API for{ @link PagedResponse}
     * It will provide same collection of T values from starting if called multiple times.
     * @return {@link Iterable} interface
     */
    public Iterable<PagedResponse<T>> iterableByPage() {
        return pagedFlux.byPage().toIterable();
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
    public Iterable<PagedResponse<T>> iterableByPage(String continuationToken) {
        return pagedFlux.byPage(continuationToken).toIterable();
    }
}
