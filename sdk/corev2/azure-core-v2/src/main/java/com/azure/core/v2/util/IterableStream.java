// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This class provides utility to iterate over values using standard 'for-each' style loops or to convert them into a
 * {@link Stream} and operate in that fashion.
 *
 * <p>
 * <strong>Code sample using Stream</strong>
 * </p>
 *
 * <!-- src_embed com.azure.core.util.iterableStream.stream -->
 * <!-- end com.azure.core.util.iterableStream.stream -->
 *
 * <p>
 * <strong>Code sample using Iterator</strong>
 * </p>
 *
 * <!-- src_embed com.azure.core.util.iterableStream.iterator.while -->
 * <!-- end com.azure.core.util.iterableStream.iterator.while -->
 *
 * <p>
 * <strong>Code sample using Stream and filter</strong>
 * </p>
 *
 * <!-- src_embed com.azure.core.util.iterableStream.stream.filter -->
 * <!-- end com.azure.core.util.iterableStream.stream.filter -->
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
    private static final IterableStream<Object> EMPTY = new IterableStream<>(new ArrayList<>());

    private final Iterable<T> iterable;

    /**
     * Creates an instance with the given {@link Iterable}.
     *
     * @param iterable Collection of items to iterate over.
     * @throws NullPointerException If {@code iterable} is {@code null}.
     */
    public IterableStream(Iterable<T> iterable) {
        this.iterable = Objects.requireNonNull(iterable, "'iterable' cannot be null.");
    }

    /**
     * Utility function to provide {@link Stream} of value {@code T}.
     *
     * @return {@link Stream} of value {@code T}.
     */
    public Stream<T> stream() {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    /**
     * Utility function to provide {@link Iterator} of value {@code T}.
     *
     * @return {@link Iterator} of value {@code T}.
     */
    @Override
    public Iterator<T> iterator() {
        return iterable.iterator();
    }

    /**
     * Creates an {@link IterableStream} from an {@link Iterable}.
     * <p>
     * An empty {@link IterableStream} will be returned if the input iterable is {@code null}.
     *
     * @param iterable Collection of items to iterate over.
     * @param <T> The type of value in this {@link Iterable}.
     * @return An {@link IterableStream} based on the passed collection.
     */
    @SuppressWarnings("unchecked")
    public static <T> IterableStream<T> of(Iterable<T> iterable) {
        if (iterable == null) {
            return (IterableStream<T>) EMPTY;
        } else {
            return new IterableStream<T>(iterable);
        }
    }
}
