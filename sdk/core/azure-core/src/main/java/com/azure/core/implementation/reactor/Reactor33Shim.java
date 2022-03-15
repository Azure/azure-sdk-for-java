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

public final class Reactor33Shim implements ReactorShim {
    private static final ClientLogger LOGGER = new ClientLogger(Reactor33Shim.class);

    static final boolean INITIALIZED;
    private static final MethodHandle MONO_DEFER_WITH_CONTEXT;
    private static final MethodHandle FLUX_DEFER_WITH_CONTEXT;

    static {
        MethodHandle monoDeferWithContext;
        MethodHandle fluxDeferWithContext;
        boolean initialized;
        try {
            // Use a public lookup as non-public APIs shouldn't be used.
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();

            monoDeferWithContext = lookup.unreflect(Mono.class.getDeclaredMethod("deferWithContext", Function.class));
            fluxDeferWithContext = lookup.unreflect(Flux.class.getDeclaredMethod("deferWithContext", Function.class));

            initialized = true;
        } catch (ReflectiveOperationException ex) {
            LOGGER.log(LogLevel.VERBOSE, () -> "Failed to initialize shim for Reactor 3.3.x.", ex);

            monoDeferWithContext = null;
            fluxDeferWithContext = null;

            initialized = false;
        }

        MONO_DEFER_WITH_CONTEXT = monoDeferWithContext;
        FLUX_DEFER_WITH_CONTEXT = fluxDeferWithContext;
        INITIALIZED = initialized;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Mono<T> withMonoContext(Function<Context, Mono<T>> contextFunction) throws Exception {
        try {
            return (Mono<T>) MONO_DEFER_WITH_CONTEXT.invokeWithArguments(contextFunction);
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
            return (Flux<T>) FLUX_DEFER_WITH_CONTEXT.invoke(contextFunction);
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
