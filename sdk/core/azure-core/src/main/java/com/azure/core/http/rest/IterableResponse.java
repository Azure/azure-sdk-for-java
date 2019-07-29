// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import reactor.core.publisher.Flux;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * This class provides utility to iterate over values.
 * @param <T> value
 */
public class IterableResponse<T> implements Iterable<T> {
    private final Flux<T> flux;

    /**
     * Creates instance given {@link Flux}.
     * @param flux to iterate over
     */

    public IterableResponse(Flux<T> flux) {
        this.flux = flux;
    }

    /**
     * Utility function to provide {@link Stream} of value T.
     * It will provide same stream of T values every time if called multiple times.
     * @return {@link Stream} of value T.
     */
    public Stream<T> stream() {
        return flux.toStream();
    }

    /**
     * Utility function to provide {@link Iterator} of value T.
     * It will provide same collection of T values every time if called multiple times.
     * @return {@link Iterator} of value T.
     */
    @Override
    public Iterator<T> iterator() {
        return flux.toIterable().iterator();
    }

}
