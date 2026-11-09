// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.otel;

import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.util.function.Supplier;

/**
 * Runs OpenTelemetry operations with the existing disable-on-error behavior.
 */
public final class FallbackInvoker {
    private FallbackInvoker() {
    }

    /**
     * Runs an OpenTelemetry operation and returns the fallback if telemetry is disabled or fails.
     *
     * @param logger the logger
     * @param fallback the fallback value
     * @param operation the operation
     * @param <T> the result type
     * @return the result or fallback
     */
    public static <T> T call(ClientLogger logger, T fallback, Supplier<? extends T> operation) {
        if (OTelInitializer.isInitialized()) {
            try {
                return operation.get();
            } catch (Throwable t) {
                OTelInitializer.runtimeError(logger, t);
            }
        }
        return fallback;
    }

    /**
     * Runs an OpenTelemetry operation that has no result.
     *
     * @param logger the logger
     * @param operation the operation
     */
    public static void run(ClientLogger logger, Runnable operation) {
        if (OTelInitializer.isInitialized()) {
            try {
                operation.run();
            } catch (Throwable t) {
                OTelInitializer.runtimeError(logger, t);
            }
        }
    }
}
