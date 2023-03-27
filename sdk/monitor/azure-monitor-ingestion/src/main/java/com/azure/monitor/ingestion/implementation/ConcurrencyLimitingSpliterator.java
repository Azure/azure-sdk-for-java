// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion.implementation;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Splits list of items into a given number of sub-lists allowing to process
 * sub-lists concurrently.
 * <p>
 * Follows example here: https://docs.oracle.com/javase/8/docs/api/java/util/Spliterator.html
 */
class ConcurrencyLimitingSpliterator<T> implements Spliterator<T> {
    private final AtomicInteger concurrency;
    private final Iterator<T> iterator;

    /**
     * Creates spliterator.
     *
     * @param concurrency Number of sub-lists to split items to. When processing items concurrently,
     *                    indicates number of threads to process items with.
     */
    ConcurrencyLimitingSpliterator(Iterator<T> iterator, int concurrency) {
        Objects.requireNonNull(iterator, "'iterator' cannot be null");
        if (concurrency == 0) {
            throw new IllegalArgumentException("'concurrency' must be a positive number.");
        }

        this.concurrency = new AtomicInteger(concurrency);
        this.iterator = iterator;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        // this method is called on individual spliterators concurrently
        // it synchronizes access to logs iterator while requesting the next batch.
        T request = null;
        synchronized (iterator) {
            if (iterator.hasNext()) {
                request = iterator.next();
            }
        }

        if (request != null) {
            action.accept(request);
            return true;
        }

        return false;
    }

    @Override
    public Spliterator<T> trySplit() {
        // here we split the stream, creating multiple spliterators that will be executed concurrently
        return concurrency.getAndDecrement() > 1 ? new ConcurrencyLimitingSpliterator<>(iterator, 1) : null;
    }

    @Override
    public long estimateSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return NONNULL | ORDERED & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
    }
}
