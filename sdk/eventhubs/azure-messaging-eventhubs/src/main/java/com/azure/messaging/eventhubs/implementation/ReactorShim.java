// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.time.Duration;

/**
 * A shim that proxies Reactor operator calls, for example, if the loaded Reactor has an optimized variant
 * of a standard operator, then shim uses it else fallback to the standard variant, if there are breaking
 * changes in operators among Reactor versions that SDK supports, then shim may expose a unified API for
 * those operators.
 */
public final class ReactorShim {
    private static final ClientLogger LOGGER = new ClientLogger(ReactorShim.class);

    /* Reactor Operator names */
    private static final String  WINDOW_TIMEOUT_OPERATOR = "windowTimeout";
    /* Reactor Operator handles */
    private static final MethodHandle BACKPRESSURE_WINDOW_TIMEOUT_OPERATOR_HANDLE;

    static {
        BACKPRESSURE_WINDOW_TIMEOUT_OPERATOR_HANDLE = lookupBackpressureWindowTimeoutOperator();
    }

    /**
     * Split the {@code source} {@link Flux} sequence into multiple {@link Flux} windows containing
     * {@code maxSize} elements (or less for the final window) and starting from the first item.
     * Each {@link Flux} window will onComplete once it contains {@code maxSize} elements
     * OR it has been open for the given {@link Duration} (as measured on the {@link Schedulers#parallel() parallel}
     * Scheduler).
     *
     * <p>
     * If the loaded Reactor library has a backpressure-aware window-timeout operator then it will be used,
     * which caps requests to the source by {@code maxSize} (i.e. prefetch), otherwise, the regular variant
     * of window-timeout operator requesting unbounded demand will be used.
     *
     * @param maxSize the maximum number of items to emit in the window before closing it
     * @param maxTime the maximum {@link Duration} since the window was opened before closing it
     *
     * @param <T> the element type of the source {@link Flux}.
     * @return a {@link Flux} of {@link Flux} windows based on element count and duration.
     */
    public static <T> Flux<Flux<T>> windowTimeout(Flux<T> source, int maxSize, Duration maxTime) {
        if (BACKPRESSURE_WINDOW_TIMEOUT_OPERATOR_HANDLE == null) {
            // optimized (backpressure) aware windowTimeout operator not available use standard variant.
            return source.windowTimeout(maxSize, maxTime);
        }

        try {
            return ((Flux<Flux<T>>) (BACKPRESSURE_WINDOW_TIMEOUT_OPERATOR_HANDLE.invoke(source, maxSize, maxTime, true)));
        } catch (Throwable err) {
            // 'java.lang.invoke' throws Throwable. Given 'Error' category represents a serious
            // abnormal thread state throw it immediately else throw via standard azure-core Logger.
            if (err instanceof Error) {
                throw (Error) err;
            } else if (err instanceof RuntimeException) {
                throw LOGGER.logExceptionAsError((RuntimeException) err);
            } else {
                throw LOGGER.logExceptionAsError(new RuntimeException(err));
            }
        }
    }

    /**
     * Try to obtain {@link MethodHandle} for backpressure aware windowTimeout Reactor operator.
     *
     * @return if the backpressure aware windowTimeout Reactor operator is available then return
     * operator {@link MethodHandle} else null.
     */
    private static MethodHandle lookupBackpressureWindowTimeoutOperator() {
        try {
            return MethodHandles.publicLookup().findVirtual(Flux.class, WINDOW_TIMEOUT_OPERATOR,
                MethodType.methodType(Flux.class, int.class, Duration.class, boolean.class));
        } catch (IllegalAccessException | NoSuchMethodException err) {
            LOGGER.verbose("Failed to retrieve MethodHandle for backpressure aware windowTimeout Reactor operator.", err);
        }
        return null;
    }
}
