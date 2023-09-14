// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Implementation of {@link ExecutorService} that returns {@link AzureFuture AzureFutures}.
 */
public final class AzureExecutorService implements ExecutorService {
    private final ExecutorService executorService;

    /**
     * Creates an instance of {@link AzureExecutorService}.
     *
     * @param executorService The {@link ExecutorService} to wrap.
     * @throws NullPointerException If {@code executorService} is null.
     */
    public AzureExecutorService(ExecutorService executorService) {
        this.executorService = Objects.requireNonNull(executorService, "'executorService' cannot be null.");
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return executorService.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return executorService.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return executorService.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return new AzureFuture<>(executorService.submit(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return new AzureFuture<>(executorService.submit(task, result));
    }

    @Override
    public Future<?> submit(Runnable task) {
        return new AzureFuture<>(executorService.submit(task));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        List<Future<T>> azureFutures = new ArrayList<>(tasks.size());
        executorService.invokeAll(tasks).forEach(future -> azureFutures.add(new AzureFuture<>(future)));

        return azureFutures;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
        throws InterruptedException {
        List<Future<T>> azureFutures = new ArrayList<>(tasks.size());
        executorService.invokeAll(tasks, timeout, unit).forEach(future -> azureFutures.add(new AzureFuture<>(future)));

        return azureFutures;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return executorService.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        return executorService.invokeAny(tasks, timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        executorService.execute(command);
    }
}
