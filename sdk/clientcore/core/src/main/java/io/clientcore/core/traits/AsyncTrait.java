// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.traits;

import io.clientcore.core.utils.SharedExecutorService;

import java.util.concurrent.ExecutorService;

/**
 * A {@link io.clientcore.core.traits trait} providing a consistent interface for configuration of asynchronous-specific
 * settings.
 *
 * @param <T> The concrete type that implements the trait. This is required so that fluent operations can continue to
 * return the concrete type, rather than the trait type.
 */
public interface AsyncTrait<T extends AsyncTrait<T>> {
    /**
     * Sets the {@link ExecutorService} to use when making asynchronous calls with the service client.
     * <p>
     * The service client won't assume ownership of the passed {@link ExecutorService}. It is left to the application to
     * maintain ownership of the {@link ExecutorService} and shut it down appropriately when the JVM is shutting down.
     * <p>
     * The passed {@link ExecutorService} may be null. This will clear any previously set value and will indicate to the
     * service client to use {@link SharedExecutorService} when making asynchronous calls.
     * <p>
     * The passed {@link ExecutorService} must not be {@link ExecutorService#isShutdown()} or
     * {@link ExecutorService#isTerminated()}. If passed in that state an {@link IllegalStateException} will be thrown.
     * If the {@link ExecutorService} enters those states while the service client is still being actively used attempts
     * to make asynchronous calls will fail.
     *
     * @param executorService The {@link ExecutorService} to use when making asynchronous calls.
     * @return The same concrete type with the appropriate properties updated, to allow for fluent chaining of
     * operations.
     * @throws IllegalStateException If the passed {@link ExecutorService} is {@link ExecutorService#isShutdown()} or
     * {@link ExecutorService#isTerminated()}.
     */
    T executorService(ExecutorService executorService);
}
