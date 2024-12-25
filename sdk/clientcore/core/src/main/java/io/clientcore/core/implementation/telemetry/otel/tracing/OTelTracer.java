// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.telemetry.otel.tracing;

import io.clientcore.core.implementation.ReflectionUtils;
import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.telemetry.otel.OTelInitializer;
import io.clientcore.core.telemetry.LibraryTelemetryOptions;
import io.clientcore.core.telemetry.tracing.SpanBuilder;
import io.clientcore.core.telemetry.tracing.Tracer;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.Context;

import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.TRACER_BUILDER_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.TRACER_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.TRACER_PROVIDER_CLASS;

public final class OTelTracer implements Tracer {
    public static final OTelTracer NOOP = new OTelTracer(null, null);
    private static final ClientLogger LOGGER = new ClientLogger(OTelTracer.class);
    private static final ReflectiveInvoker SPAN_BUILDER_INVOKER;
    private static final ReflectiveInvoker SET_INSTRUMENTATION_VERSION_INVOKER;
    private static final ReflectiveInvoker BUILD_INVOKER;
    private static final ReflectiveInvoker SET_SCHEMA_URL_INVOKER;
    private static final ReflectiveInvoker GET_TRACER_BUILDER_INVOKER;

    private final Object otelTracer;
    private final LibraryTelemetryOptions libraryOptions;

    static {
        ReflectiveInvoker spanBuilderInvoker = null;
        ReflectiveInvoker setInstrumentationVersionInvoker = null;
        ReflectiveInvoker buildInvoker = null;
        ReflectiveInvoker setSchemaUrlInvoker = null;
        ReflectiveInvoker getTracerBuilderInvoker = null;

        if (OTelInitializer.isInitialized()) {
            try {
                spanBuilderInvoker = ReflectionUtils.getMethodInvoker(TRACER_CLASS,
                    TRACER_CLASS.getMethod("spanBuilder", String.class));

                setInstrumentationVersionInvoker = ReflectionUtils.getMethodInvoker(TRACER_BUILDER_CLASS,
                    TRACER_BUILDER_CLASS.getMethod("setInstrumentationVersion", String.class));
                setSchemaUrlInvoker = ReflectionUtils.getMethodInvoker(TRACER_BUILDER_CLASS,
                    TRACER_BUILDER_CLASS.getMethod("setSchemaUrl", String.class));
                buildInvoker
                    = ReflectionUtils.getMethodInvoker(TRACER_BUILDER_CLASS, TRACER_BUILDER_CLASS.getMethod("build"));

                getTracerBuilderInvoker = ReflectionUtils.getMethodInvoker(TRACER_PROVIDER_CLASS,
                    TRACER_PROVIDER_CLASS.getMethod("tracerBuilder", String.class));
            } catch (Throwable t) {
                OTelInitializer.initError(LOGGER, t);
            }
        }

        SPAN_BUILDER_INVOKER = spanBuilderInvoker;
        SET_INSTRUMENTATION_VERSION_INVOKER = setInstrumentationVersionInvoker;
        SET_SCHEMA_URL_INVOKER = setSchemaUrlInvoker;
        BUILD_INVOKER = buildInvoker;
        GET_TRACER_BUILDER_INVOKER = getTracerBuilderInvoker;
    }

    public static OTelTracer createTracer(Object otelTracerProvider, LibraryTelemetryOptions libraryOptions) {
        if (OTelInitializer.isInitialized() && otelTracerProvider != null) {
            assert TRACER_PROVIDER_CLASS.isInstance(otelTracerProvider);
            try {
                Object tracerBuilder = GET_TRACER_BUILDER_INVOKER.invokeWithArguments(otelTracerProvider,
                    libraryOptions.getLibraryName());

                SET_INSTRUMENTATION_VERSION_INVOKER.invokeWithArguments(tracerBuilder,
                    libraryOptions.getLibraryVersion());
                SET_SCHEMA_URL_INVOKER.invokeWithArguments(tracerBuilder, libraryOptions.getSchemaUrl());
                return new OTelTracer(BUILD_INVOKER.invokeWithArguments(tracerBuilder), libraryOptions);
            } catch (Throwable t) {
                OTelInitializer.runtimeError(LOGGER, t);
            }
        }
        return OTelTracer.NOOP;
    }

    private OTelTracer(Object otelTracer, LibraryTelemetryOptions libraryOptions) {
        this.otelTracer = otelTracer;
        this.libraryOptions = libraryOptions;
    }

    @Override
    public SpanBuilder spanBuilder(String spanName, Context context) {
        if (isEnabled()) {
            try {
                return new OTelSpanBuilder(SPAN_BUILDER_INVOKER.invokeWithArguments(otelTracer, spanName),
                    libraryOptions, context);
            } catch (Throwable t) {
                OTelInitializer.runtimeError(LOGGER, t);
            }
        }

        return OTelSpanBuilder.NOOP;
    }

    @Override
    public boolean isEnabled() {
        return OTelInitializer.isInitialized() && otelTracer != null;
    }
}
