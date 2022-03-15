// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.reactor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.function.Function;

/**
 * Shim interface for spanning multiple minor versions of Reactor.
 */
public interface ReactorShim {
    /**
     * Creates a {@link Mono} with {@link Context}.
     *
     * @param contextFunction The function that mutates the Reactor {@link Context}.
     * @param <T> The type of the {@link Mono}.
     * @return The {@link Mono} with {@link Context}.
     * @throws Exception If an exception occurs while creating the {@link Mono} with {@link Context}.
     */
    <T> Mono<T> withMonoContext(Function<Context, Mono<T>> contextFunction) throws Exception;

    /**
     * Creates a {@link Flux} with {@link Context}.
     *
     * @param contextFunction The function that mutates the Reactor {@link Context}.
     * @param <T> The type of the {@link Flux}.
     * @return The {@link Flux} with {@link Context}.
     * @throws Exception If an exception occurs while creating the {@link Flux} with {@link Context}.
     */
    <T> Flux<T> withFluxContext(Function<Context, Flux<T>> contextFunction) throws Exception;
}
