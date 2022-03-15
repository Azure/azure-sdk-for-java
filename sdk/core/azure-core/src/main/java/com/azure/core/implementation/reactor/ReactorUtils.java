// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.reactor;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.function.Function;

public final class ReactorUtils {
    private static final ClientLogger LOGGER = new ClientLogger(ReactorUtils.class);

    private static final ReactorShim SHIM;

    static {
        ReactorShim shim = new Reactor34Shim();

        if (!Reactor34Shim.INITIALIZED) {
            shim = new Reactor33Shim();
        }

        if (!Reactor33Shim.INITIALIZED) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("Unable to initialize ReactorShim. "
                + "Unsupported version of reactor-core is being used."));
        }

        SHIM = shim;
    }

    /**
     * Creates a {@link Mono} with {@link Context}.
     *
     * @param contextFunction The function that mutates the Reactor {@link Context}.
     * @param <T> The type of the {@link Mono}.
     * @return The {@link Mono} with {@link Context}.
     * @throws RuntimeException If an exception occurs while creating the {@link Mono} with {@link Context}.
     */
    public static <T> Mono<T> withMonoContext(Function<Context, Mono<T>> contextFunction) {
        try {
            return SHIM.withMonoContext(contextFunction);
        } catch (Exception ex) {
            if (ex instanceof RuntimeException) {
                throw LOGGER.logExceptionAsError((RuntimeException) ex);
            }

            throw LOGGER.logExceptionAsError(new RuntimeException(ex));
        }
    }

    /**
     * Creates a {@link Flux} with {@link Context}.
     *
     * @param contextFunction The function that mutates the Reactor {@link Context}.
     * @param <T> The type of the {@link Flux}.
     * @return The {@link Flux} with {@link Context}.
     * @throws RuntimeException If an exception occurs while creating the {@link Flux} with {@link Context}.
     */
    <T> Flux<T> withFluxContext(Function<Context, Flux<T>> contextFunction) {
        try {
            return SHIM.withFluxContext(contextFunction);
        } catch (Exception ex) {
            if (ex instanceof RuntimeException) {
                throw LOGGER.logExceptionAsError((RuntimeException) ex);
            }

            throw LOGGER.logExceptionAsError(new RuntimeException(ex));
        }
    }

    private ReactorUtils() {
    }
}
