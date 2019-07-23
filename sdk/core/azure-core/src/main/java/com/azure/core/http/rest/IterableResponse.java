// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import reactor.core.publisher.Flux;

import java.util.Iterator;
import java.util.stream.Stream;


/**
 * This class provides utility to iterate over values.
 * @param <T> vslue
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
     * Retrieve the {@link Stream} from response.
     * @return stream of T
     */
    public Stream<T> stream() {
        return flux.toStream();
    }

    /**
     *  Provides iterator API.
     * @return iterator of T
     */
    @Override
    public Iterator<T> iterator() {
        return flux.toIterable().iterator();
    }
}
