// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.telemetry.otel.tracing;

import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.telemetry.otel.OTelInitializer;
import io.clientcore.core.telemetry.LibraryTelemetryOptions;
import io.clientcore.core.telemetry.tracing.SpanBuilder;
import io.clientcore.core.telemetry.tracing.SpanKind;
import io.clientcore.core.telemetry.tracing.Tracer;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.Context;

import java.lang.invoke.MethodHandles;

import static io.clientcore.core.implementation.ReflectionUtils.getMethodInvoker;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.TRACER_BUILDER_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.TRACER_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.TRACER_PROVIDER_CLASS;

public final class OTelTracer implements Tracer {
    public static final OTelTracer NOOP = new OTelTracer();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();
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
                spanBuilderInvoker
                    = getMethodInvoker(TRACER_CLASS, TRACER_CLASS.getMethod("spanBuilder", String.class));

                setInstrumentationVersionInvoker = getMethodInvoker(TRACER_BUILDER_CLASS,
                    TRACER_BUILDER_CLASS.getMethod("setInstrumentationVersion", String.class));

                setSchemaUrlInvoker = getMethodInvoker(TRACER_BUILDER_CLASS,
                    TRACER_BUILDER_CLASS.getMethod("setSchemaUrl", String.class));

                buildInvoker = getMethodInvoker(TRACER_BUILDER_CLASS, TRACER_BUILDER_CLASS.getMethod("build"));

                getTracerBuilderInvoker = getMethodInvoker(TRACER_PROVIDER_CLASS,
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

    private OTelTracer() {
        this.otelTracer = null;
        this.libraryOptions = null;
    }

    public OTelTracer(Object otelTracerProvider, LibraryTelemetryOptions libraryOptions) throws Throwable {
        assert TRACER_PROVIDER_CLASS.isInstance(otelTracerProvider);

        Object tracerBuilder = GET_TRACER_BUILDER_INVOKER.invoke(otelTracerProvider, libraryOptions.getLibraryName());

        SET_INSTRUMENTATION_VERSION_INVOKER.invoke(tracerBuilder, libraryOptions.getLibraryVersion());
        SET_SCHEMA_URL_INVOKER.invoke(tracerBuilder, libraryOptions.getSchemaUrl());

        this.otelTracer = BUILD_INVOKER.invoke(tracerBuilder);
        this.libraryOptions = libraryOptions;
    }

    @Override
    public SpanBuilder spanBuilder(String spanName, SpanKind spanKind, RequestOptions options) {
        if (isEnabled()) {
            try {
                Context parent = options == null ? Context.none() : options.getContext();
                return new OTelSpanBuilder(SPAN_BUILDER_INVOKER.invoke(otelTracer, spanName), spanKind, parent,
                    libraryOptions);
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
