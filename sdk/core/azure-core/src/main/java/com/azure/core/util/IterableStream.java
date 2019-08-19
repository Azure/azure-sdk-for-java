// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import reactor.core.publisher.Flux;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * This class provides utility to iterate over values using standard 'for-each' style loops, or to convert them into a
 * Stream and operate in that fashion. All the values are preserved even if they are traversed multiple times.
 *
 * <p><strong>Code sample using Stream</strong></p>
 *
 * {@codesnippet com.azure.core.util.iterableStream.stream}
 *
 * <p><strong>Code sample using Iterator</strong></p>
 *
 * {@codesnippet com.azure.core.util.iterableStream.iterator.while}
 *
 * <p><strong>Code sample using Stream and filter</strong></p>
 *
 * {@codesnippet com.azure.core.util.iterableStream.stream.filter}
 *
 * @param <T> The type of value in this {@link Iterable}.
 * @see Iterable
 */
public class IterableStream<T> implements Iterable<T> {
    private final Flux<T> flux;

    /**
     * Creates instance given {@link Flux}.
     * @param flux to iterate over
     */
    public IterableStream(Flux<T> flux) {
        this.flux = flux;
    }

    /**
     * Utility function to provide {@link Stream} of value T.
     * It will provide same stream of T values if called multiple times.
     * @return {@link Stream} of value T.
     */
    public Stream<T> stream() {
        return flux.toStream();
    }

    /**
     * Utility function to provide {@link Iterator} of value T.
     * It will provide same collection of T values if called multiple times.
     * @return {@link Iterator} of value T.
     */
    @Override
    public Iterator<T> iterator() {
        return flux.toIterable().iterator();
    }

}
