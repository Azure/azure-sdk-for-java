// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.util.concurrent;

import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Implementation of {@link Future} that provides additional functionality on top of the wrapped future.
 *
 * @param <V> The type of value returned by the future.
 */
public class AzureFuture<V> implements Future<V> {
    private final Future<V> wrappedFuture;

    /**
     * Creates a new instance of {@link AzureFuture}.
     *
     * @param wrappedFuture The {@link Future} being wrapped.
     * @throws NullPointerException If {@code wrappedFuture} is null.
     */
    public AzureFuture(Future<V> wrappedFuture) {
        this.wrappedFuture = Objects.requireNonNull(wrappedFuture, "'wrappedFuture' cannot be null.");
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return wrappedFuture.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return wrappedFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return wrappedFuture.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return wrappedFuture.get();
    }

    /**
     * Waits if necessary for at most the given time for the computation to complete, and then retrieves its result,
     * if available.
     * <p>
     * If the computation doesn't complete before timing out it will be cancelled upon timing out using
     * {@link #cancel(boolean) cancel(true)}.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return the computed result
     * @throws CancellationException if the computation was cancelled
     * @throws ExecutionException if the computation threw an exception
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws TimeoutException if the wait timed out
     */
    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            return wrappedFuture.get(timeout, unit);
        } catch (TimeoutException ex) {
            wrappedFuture.cancel(true);
            throw ex;
        }
    }
}
