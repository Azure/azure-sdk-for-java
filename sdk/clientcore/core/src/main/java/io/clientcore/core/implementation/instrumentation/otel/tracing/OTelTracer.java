// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.otel.tracing;

import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.instrumentation.otel.FallbackInvoker;
import io.clientcore.core.implementation.instrumentation.otel.OTelInitializer;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.SdkInstrumentationOptions;
import io.clientcore.core.instrumentation.tracing.SpanBuilder;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.clientcore.core.instrumentation.tracing.Tracer;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import static io.clientcore.core.implementation.ReflectionUtils.getMethodInvoker;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.TRACER_BUILDER_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.TRACER_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.TRACER_PROVIDER_CLASS;

/**
 * OpenTelemetry implementation of {@link Tracer}.
 */
public final class OTelTracer implements Tracer {
    public static final OTelTracer NOOP = new OTelTracer();
    private static final ClientLogger LOGGER = new ClientLogger(OTelTracer.class);
    private static final FallbackInvoker SPAN_BUILDER_INVOKER;
    private static final FallbackInvoker SET_INSTRUMENTATION_VERSION_INVOKER;
    private static final FallbackInvoker BUILD_INVOKER;
    private static final FallbackInvoker SET_SCHEMA_URL_INVOKER;
    private static final FallbackInvoker GET_TRACER_BUILDER_INVOKER;

    private final Object otelTracer;
    private final SdkInstrumentationOptions sdkOptions;

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

        SPAN_BUILDER_INVOKER = new FallbackInvoker(spanBuilderInvoker, LOGGER);
        SET_INSTRUMENTATION_VERSION_INVOKER = new FallbackInvoker(setInstrumentationVersionInvoker, LOGGER);
        SET_SCHEMA_URL_INVOKER = new FallbackInvoker(setSchemaUrlInvoker, LOGGER);
        BUILD_INVOKER = new FallbackInvoker(buildInvoker, LOGGER);
        GET_TRACER_BUILDER_INVOKER = new FallbackInvoker(getTracerBuilderInvoker, LOGGER);
    }

    private OTelTracer() {
        this.otelTracer = null;
        this.sdkOptions = null;
    }

    /**
     * Creates a new instance of {@link OTelTracer}.
     * @param otelTracerProvider the OpenTelemetry tracer provider
     * @param sdkOptions the sdk instrumentation options
     */
    public OTelTracer(Object otelTracerProvider, SdkInstrumentationOptions sdkOptions) {
        Object tracerBuilder = GET_TRACER_BUILDER_INVOKER.invoke(otelTracerProvider, sdkOptions.getSdkName());
        if (tracerBuilder != null) {
            SET_INSTRUMENTATION_VERSION_INVOKER.invoke(tracerBuilder, sdkOptions.getSdkVersion());
            SET_SCHEMA_URL_INVOKER.invoke(tracerBuilder, sdkOptions.getSchemaUrl());
            this.otelTracer = BUILD_INVOKER.invoke(tracerBuilder);
        } else {
            this.otelTracer = null;
        }
        this.sdkOptions = sdkOptions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SpanBuilder spanBuilder(String spanName, SpanKind spanKind, InstrumentationContext parentContext) {
        if (isEnabled()) {
            Object otelSpanBuilder = SPAN_BUILDER_INVOKER.invoke(otelTracer, spanName);
            if (otelSpanBuilder != null) {
                return new OTelSpanBuilder(otelSpanBuilder, spanKind, parentContext, sdkOptions);
            }
        }

        return OTelSpanBuilder.NOOP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return otelTracer != null && OTelInitializer.isInitialized();
    }
}
