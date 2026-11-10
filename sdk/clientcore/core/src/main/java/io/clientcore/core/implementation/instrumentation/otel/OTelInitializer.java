// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.otel;

import io.clientcore.core.implementation.instrumentation.OptionalDependency;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;

/**
 * This class is used to initialize OpenTelemetry.
 */
public final class OTelInitializer {
    private static final ClientLogger LOGGER = new ClientLogger(OTelInitializer.class);
    private static final OTelInitializer INSTANCE = new OTelInitializer(isAvailable());

    private volatile boolean initialized;

    private static boolean isAvailable() {
        try {
            ClassLoader loader = OTelInitializer.class.getClassLoader();
            OptionalDependency.load("io.opentelemetry.api.OpenTelemetry");
            OptionalDependency.load("io.opentelemetry.context.Context");
            for (String className : new String[] {
                "io.opentelemetry.api.common.AttributeKey",
                "io.opentelemetry.api.common.Attributes",
                "io.opentelemetry.api.common.AttributesBuilder",
                "io.opentelemetry.context.ContextKey",
                "io.opentelemetry.context.propagation.ContextPropagators",
                "io.opentelemetry.api.GlobalOpenTelemetry",
                "io.opentelemetry.api.metrics.DoubleHistogram",
                "io.opentelemetry.api.metrics.DoubleHistogramBuilder",
                "io.opentelemetry.api.metrics.LongCounter",
                "io.opentelemetry.api.metrics.LongCounterBuilder",
                "io.opentelemetry.api.metrics.LongUpDownCounter",
                "io.opentelemetry.api.metrics.LongUpDownCounterBuilder",
                "io.opentelemetry.api.metrics.Meter",
                "io.opentelemetry.api.metrics.MeterBuilder",
                "io.opentelemetry.api.metrics.MeterProvider",
                "io.opentelemetry.context.Scope",
                "io.opentelemetry.api.trace.Span",
                "io.opentelemetry.api.trace.SpanBuilder",
                "io.opentelemetry.api.trace.SpanContext",
                "io.opentelemetry.api.trace.SpanKind",
                "io.opentelemetry.api.trace.StatusCode",
                "io.opentelemetry.context.propagation.TextMapPropagator",
                "io.opentelemetry.context.propagation.TextMapGetter",
                "io.opentelemetry.context.propagation.TextMapSetter",
                "io.opentelemetry.api.trace.TraceFlags",
                "io.opentelemetry.api.trace.TraceState",
                "io.opentelemetry.api.trace.Tracer",
                "io.opentelemetry.api.trace.TracerBuilder",
                "io.opentelemetry.api.trace.TracerProvider",
                "io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator" }) {
                Class.forName(className, false, loader);
            }
            // Class literals verify JPMS access without initializing the OpenTelemetry APIs.
            return OpenTelemetry.class.isInterface() && Context.class.isInterface();
        } catch (ReflectiveOperationException | LinkageError | SecurityException e) {
            LOGGER.atVerbose().setThrowable(e).log("OpenTelemetry was not initialized.");
            return false;
        }
    }

    private OTelInitializer(boolean initialized) {
        this.initialized = initialized;
    }

    /**
     * Disables OTel and logs OTel initialization error.
     * @param logger the logger
     * @param t the error
     */
    public static void initError(ClientLogger logger, Throwable t) {
        logger.atVerbose().setThrowable(t).log("OpenTelemetry version is incompatible.");
        INSTANCE.initialized = false;
    }

    /**
     * Disables OTel and logs runtime error when using OTel.
     *
     * @param logger the logger
     * @param t the error
     */
    public static void runtimeError(ClientLogger logger, Throwable t) {
        if (INSTANCE.initialized) {
            logger.atWarning()
                .setThrowable(t)
                .log("Unexpected error when invoking OpenTelemetry, turning tracing off.");
        }

        INSTANCE.initialized = false;
    }

    /**
     * Checks if OTel initialization was successful.
     *
     * @return true if OTel is initialized successfully, false otherwise
     */
    public static boolean isInitialized() {
        return INSTANCE.initialized;
    }

}
