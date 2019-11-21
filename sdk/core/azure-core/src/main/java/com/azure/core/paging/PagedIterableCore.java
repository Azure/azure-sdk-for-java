// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.paging;

import com.azure.core.http.rest.Page;
import com.azure.core.util.IterableStream;

import java.util.stream.Stream;

/**
 * This class provides utility to iterate over pages that extend {@link Page} using {@link Stream} and
 * {@link Iterable} interfaces.
 *
 * @param <T> The type of value contained in this {@link IterableStream}.
 * @param <P> The page extending from {@link Page}
 * @see Page
 * @see IterableStream
 */
public class PagedIterableCore<T, P extends Page<T>> extends IterableStream<T> {
    private final PagedFluxCore<T, P> pagedFluxCore;

    /**
     * Creates instance given {@link PagedIterableCore}.
     * @param pagedFlux to use as iterable
     */
    public PagedIterableCore(PagedFluxCore<T, P> pagedFlux) {
        super(pagedFlux);
        this.pagedFluxCore = pagedFlux;
    }

    /**
     * Retrieve the {@link Stream}, one page at a time.
     * It will provide same {@link Stream} of T values from starting if called multiple times.
     *
     * @return {@link Stream} of a page that extends {@link Page}
     */
    public Stream<P> streamByPage() {
        return pagedFluxCore.byPage().toStream();
    }

    /**
     * Retrieve the {@link Stream}, one page at a time, starting from the next page associated with the given
     * continuation token. To start from first page, use {@link #streamByPage()} instead.
     *
     * @param continuationToken The continuation token used to fetch the next page
     * @return {@link Stream} of a Response that extends {@link Page}, starting from the page associated
     * with the continuation token
     */
    public Stream<P> streamByPage(String continuationToken) {
        return pagedFluxCore.byPage(continuationToken).toStream();
    }

    /**
     * Provides {@link Iterable} API for{ @link PagedResponse}
     * It will provide same collection of {@code T} values from starting if called multiple times.
     *
     * @return {@link Iterable} interface
     */
    public Iterable<P> iterableByPage() {
        return pagedFluxCore.byPage().toIterable();
    }

    /**
     * Provides {@link Iterable} API for {@link Page}, starting from the next page associated with the given
     * continuation token. To start from first page, use {@link #streamByPage()} instead.
     * It will provide same collection of T values from starting if called multiple times.
     *
     * @param continuationToken The continuation token used to fetch the next page
     * @return {@link Iterable} interface
     */
    public Iterable<P> iterableByPage(String continuationToken) {
        return pagedFluxCore.byPage(continuationToken).toIterable();
    }
}
