// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import java.util.stream.Stream;

/**
 *  This class provides utility to iterate over {@link PagedResponse}.
 * @param  <T> value
 */
public class PagedIterable<T> extends IterableResponse<T>  {
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
     * @return {@link Stream} of {@link PagedResponse}
     */
    public Stream<PagedResponse<T>> streamByPage() {
        return pagedFlux.byPage().toStream();
    }

    /**
     * Provides iterable API for{@link PagedResponse}.
     * @return iterable interface
     */
    public Iterable<PagedResponse<T>> iterableByPage() {
        return pagedFlux.byPage().toIterable();
    }

}
