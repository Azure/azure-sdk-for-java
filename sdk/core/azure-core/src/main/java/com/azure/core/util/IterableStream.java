// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
    /*
     * This is the default batch size that will be requested when using stream or iterable by page, this will indicate
     * to Reactor how many elements should be prefetched before another batch is requested.
     */
    private static final int DEFAULT_BATCH_SIZE = 1;

    private final ClientLogger logger = new ClientLogger(IterableStream.class);
    private final Flux<T> flux;
    private final Iterable<T> iterable;

    /**
     * Creates an instance with the given {@link Flux}.
     *
     * @param flux Flux of items to iterate over.
     * @throws NullPointerException if {@code flux} is {@code null}.
     */
    public IterableStream(Flux<T> flux) {
        this.flux = Objects.requireNonNull(flux, "'flux' cannot be null.");
        this.iterable = null;
    }

    /**
     * Creates an instance with the given {@link Iterable}.
     *
     * @param iterable Collection of items to iterate over.
     * @throws NullPointerException if {@code iterable} is {@code null}.
     */
    public IterableStream(Iterable<T> iterable) {
        this.iterable = Objects.requireNonNull(iterable, "'iterable' cannot be null.");
        this.flux = null;
    }

    /**
     * Utility function to provide {@link Stream} of value {@code T}.
     * It will provide the same stream of {@code T} values if called multiple times.
     *
     * @return {@link Stream} of value {@code T}.
     */
    public Stream<T> stream() {
        if (flux != null) {
            return flux.toStream(DEFAULT_BATCH_SIZE);
        } else if (iterable != null) {
            return StreamSupport.stream(iterable.spliterator(), false);
        } else {
            logger.warning("IterableStream was not initialized with Iterable or Flux, returning empty stream.");
            return Stream.empty();
        }
    }

    /**
     * Utility function to provide {@link Iterator} of value {@code T}.
     * It will provide same collection of {@code T} values if called multiple times.
     *
     * @return {@link Iterator} of value {@code T}.
     */
    @Override
    public Iterator<T> iterator() {
        if (flux != null) {
            return flux.toIterable(DEFAULT_BATCH_SIZE).iterator();
        } else if (iterable != null) {
            return iterable.iterator();
        } else {
            logger.warning("IterableStream was not initialized with Iterable or Flux, returning empty iterator.");
            return Collections.emptyIterator();
        }
    }
}
