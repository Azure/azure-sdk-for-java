// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.telemetry.otel;

import io.clientcore.core.util.ClientLogger;

public final class OTelInitializer {
    private static final ClientLogger LOGGER = new ClientLogger(OTelInitializer.class);
    private static final OTelInitializer INSTANCE;

    public static final Class<?> ATTRIBUTE_KEY_CLASS;
    public static final Class<?> ATTRIBUTES_CLASS;
    public static final Class<?> ATTRIBUTES_BUILDER_CLASS;

    public static final Class<?> CONTEXT_CLASS;
    public static final Class<?> CONTEXT_KEY_CLASS;
    public static final Class<?> OTEL_CLASS;
    public static final Class<?> GLOBAL_OTEL_CLASS;

    public static final Class<?> SCOPE_CLASS;
    public static final Class<?> SPAN_BUILDER_CLASS;
    public static final Class<?> SPAN_CONTEXT_CLASS;
    public static final Class<?> SPAN_KIND_CLASS;
    public static final Class<?> SPAN_CLASS;

    public static final Class<?> STATUS_CODE_CLASS;

    public static final Class<?> TRACE_FLAGS_CLASS;
    public static final Class<?> TRACE_STATE_CLASS;
    public static final Class<?> TRACER_CLASS;
    public static final Class<?> TRACER_BUILDER_CLASS;
    public static final Class<?> TRACER_PROVIDER_CLASS;

    private volatile boolean initialized;

    static {
        Class<?> attributeKeyClass = null;
        Class<?> attributesClass = null;
        Class<?> attributesBuilderClass = null;

        Class<?> contextClass = null;
        Class<?> contextKeyClass = null;

        Class<?> otelClass = null;
        Class<?> globalOtelClass = null;

        Class<?> scopeClass = null;
        Class<?> spanClass = null;
        Class<?> spanBuilderClass = null;
        Class<?> spanContextClass = null;
        Class<?> spanKindClass = null;
        Class<?> statusCodeClass = null;

        Class<?> traceFlagsClass = null;
        Class<?> traceStateClass = null;
        Class<?> tracerClass = null;
        Class<?> tracerBuilderClass = null;
        Class<?> tracerProviderClass = null;

        OTelInitializer instance = null;
        try {
            ClassLoader classLoader = OTelInitializer.class.getClassLoader();
            attributeKeyClass = Class.forName("io.opentelemetry.api.common.AttributeKey", true, classLoader);
            attributesClass = Class.forName("io.opentelemetry.api.common.Attributes", true, classLoader);
            attributesBuilderClass = Class.forName("io.opentelemetry.api.common.AttributesBuilder", true, classLoader);

            contextClass = Class.forName("io.opentelemetry.context.Context", true, classLoader);
            contextKeyClass = Class.forName("io.opentelemetry.context.ContextKey", true, classLoader);

            otelClass = Class.forName("io.opentelemetry.api.OpenTelemetry", true, classLoader);
            globalOtelClass = Class.forName("io.opentelemetry.api.GlobalOpenTelemetry", true, classLoader);

            scopeClass = Class.forName("io.opentelemetry.context.Scope", true, classLoader);

            spanClass = Class.forName("io.opentelemetry.api.trace.Span", true, classLoader);
            spanBuilderClass = Class.forName("io.opentelemetry.api.trace.SpanBuilder", true, classLoader);
            spanContextClass = Class.forName("io.opentelemetry.api.trace.SpanContext", true, classLoader);
            spanKindClass = Class.forName("io.opentelemetry.api.trace.SpanKind", true, classLoader);
            statusCodeClass = Class.forName("io.opentelemetry.api.trace.StatusCode", true, classLoader);

            traceFlagsClass = Class.forName("io.opentelemetry.api.trace.TraceFlags", true, classLoader);
            traceStateClass = Class.forName("io.opentelemetry.api.trace.TraceState", true, classLoader);
            tracerClass = Class.forName("io.opentelemetry.api.trace.Tracer", true, classLoader);
            tracerBuilderClass = Class.forName("io.opentelemetry.api.trace.TracerBuilder", true, classLoader);
            tracerProviderClass = Class.forName("io.opentelemetry.api.trace.TracerProvider", true, classLoader);

            instance = new OTelInitializer(true);
        } catch (Throwable t) {
            LOGGER.atVerbose().log("OpenTelemetry is not detected.", t);
            instance = new OTelInitializer(false);
        }

        ATTRIBUTE_KEY_CLASS = attributeKeyClass;
        ATTRIBUTES_CLASS = attributesClass;
        ATTRIBUTES_BUILDER_CLASS = attributesBuilderClass;

        CONTEXT_CLASS = contextClass;
        CONTEXT_KEY_CLASS = contextKeyClass;

        OTEL_CLASS = otelClass;
        GLOBAL_OTEL_CLASS = globalOtelClass;

        SCOPE_CLASS = scopeClass;
        SPAN_CLASS = spanClass;
        SPAN_BUILDER_CLASS = spanBuilderClass;
        SPAN_CONTEXT_CLASS = spanContextClass;
        SPAN_KIND_CLASS = spanKindClass;
        STATUS_CODE_CLASS = statusCodeClass;

        TRACE_FLAGS_CLASS = traceFlagsClass;
        TRACE_STATE_CLASS = traceStateClass;
        TRACER_CLASS = tracerClass;
        TRACER_BUILDER_CLASS = tracerBuilderClass;
        TRACER_PROVIDER_CLASS = tracerProviderClass;

        INSTANCE = instance;
    }

    private OTelInitializer(boolean initialized) {
        this.initialized = initialized;
    }

    public static void initError(ClientLogger logger, Throwable t) {
        logger.atVerbose().log("OpenTelemetry version is incompatible.", t);
        INSTANCE.initialized = false;
    }

    public static void runtimeError(ClientLogger logger, Throwable t) {
        if (INSTANCE.initialized) {
            logger.atWarning().log("Unexpected error when invoking OpenTelemetry, turning tracing off.", t);
        }

        INSTANCE.initialized = false;
    }

    public static boolean isInitialized() {
        return INSTANCE.initialized;
    }
}
