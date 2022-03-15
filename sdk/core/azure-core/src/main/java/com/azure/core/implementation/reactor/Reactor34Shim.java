// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.reactor;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.function.Function;

public final class Reactor34Shim implements ReactorShim {
    private static final ClientLogger LOGGER = new ClientLogger(Reactor33Shim.class);

    static final boolean INITIALIZED;
    private static final MethodHandle MONO_DEFER_CONTEXTUAL;
    private static final MethodHandle FLUX_DEFER_CONTEXTUAL;

    static {
        MethodHandle monoDeferContextual;
        MethodHandle fluxDeferContextual;
        boolean initialized;
        try {
            // Use a public lookup as non-public APIs shouldn't be used.
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();

            monoDeferContextual = lookup.unreflect(Mono.class.getDeclaredMethod("deferContextual", Function.class));
            fluxDeferContextual = lookup.unreflect(Flux.class.getDeclaredMethod("deferContextual", Function.class));

            initialized = true;
        } catch (ReflectiveOperationException ex) {
            LOGGER.log(LogLevel.VERBOSE, () -> "Failed to initialize shim for Reactor 3.3.x.", ex);

            monoDeferContextual = null;
            fluxDeferContextual = null;

            initialized = false;
        }

        MONO_DEFER_CONTEXTUAL = monoDeferContextual;
        FLUX_DEFER_CONTEXTUAL = fluxDeferContextual;
        INITIALIZED = initialized;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Mono<T> withMonoContext(Function<Context, Mono<T>> contextFunction) throws Exception {
        try {
            return (Mono<T>) MONO_DEFER_CONTEXTUAL.invokeWithArguments(contextFunction);
        } catch (Throwable t) {
            if (t instanceof Error) {
                throw (Error) t;
            }

            if (t instanceof Exception) {
                throw (Exception) t;
            }

            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Flux<T> withFluxContext(Function<Context, Flux<T>> contextFunction) throws Exception {
        try {
            return (Flux<T>) FLUX_DEFER_CONTEXTUAL.invokeWithArguments(contextFunction);
        } catch (Throwable t) {
            if (t instanceof Error) {
                throw (Error) t;
            }

            if (t instanceof Exception) {
                throw (Exception) t;
            }

            throw new RuntimeException(t);
        }
    }
}
