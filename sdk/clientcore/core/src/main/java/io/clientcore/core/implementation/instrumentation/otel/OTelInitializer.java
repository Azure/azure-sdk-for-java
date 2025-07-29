// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.otel;

import io.clientcore.core.instrumentation.logging.ClientLogger;

/**
 * This class is used to initialize OpenTelemetry.
 */
public final class OTelInitializer {
    private static final ClientLogger LOGGER = new ClientLogger(OTelInitializer.class);
    private static final OTelInitializer INSTANCE;

    public static final Class<?> ATTRIBUTE_KEY_CLASS;
    public static final Class<?> ATTRIBUTES_CLASS;
    public static final Class<?> ATTRIBUTES_BUILDER_CLASS;

    public static final Class<?> CONTEXT_CLASS;
    public static final Class<?> CONTEXT_KEY_CLASS;
    public static final Class<?> CONTEXT_PROPAGATORS_CLASS;
    public static final Class<?> OTEL_CLASS;
    public static final Class<?> GLOBAL_OTEL_CLASS;

    public static final Class<?> DOUBLE_HISTOGRAM_CLASS;
    public static final Class<?> DOUBLE_HISTOGRAM_BUILDER_CLASS;
    public static final Class<?> LONG_COUNTER_CLASS;
    public static final Class<?> LONG_COUNTER_BUILDER_CLASS;
    public static final Class<?> LONG_UP_DOWN_COUNTER_CLASS;
    public static final Class<?> LONG_UP_DOWN_COUNTER_BUILDER_CLASS;

    public static final Class<?> METER_CLASS;
    public static final Class<?> METER_BUILDER_CLASS;
    public static final Class<?> METER_PROVIDER_CLASS;

    public static final Class<?> SCOPE_CLASS;
    public static final Class<?> SPAN_BUILDER_CLASS;
    public static final Class<?> SPAN_CONTEXT_CLASS;
    public static final Class<?> SPAN_KIND_CLASS;
    public static final Class<?> SPAN_CLASS;

    public static final Class<?> STATUS_CODE_CLASS;

    public static final Class<?> TEXT_MAP_PROPAGATOR_CLASS;
    public static final Class<?> TEXT_MAP_GETTER_CLASS;
    public static final Class<?> TEXT_MAP_SETTER_CLASS;

    public static final Class<?> TRACE_FLAGS_CLASS;
    public static final Class<?> TRACE_STATE_CLASS;
    public static final Class<?> TRACER_CLASS;
    public static final Class<?> TRACER_BUILDER_CLASS;
    public static final Class<?> TRACER_PROVIDER_CLASS;

    public static final Class<?> W3C_PROPAGATOR_CLASS;

    private volatile boolean initialized;

    static {
        Class<?> attributeKeyClass = null;
        Class<?> attributesClass = null;
        Class<?> attributesBuilderClass = null;

        Class<?> contextClass = null;
        Class<?> contextKeyClass = null;
        Class<?> contextPropagatorsClass = null;

        Class<?> otelClass = null;
        Class<?> globalOtelClass = null;

        Class<?> doubleHistogramClass = null;
        Class<?> doubleHistogramBuilderClass = null;

        Class<?> longCounterClass = null;
        Class<?> longCounterBuilderClass = null;
        Class<?> longUpDownCounterClass = null;
        Class<?> longUpDownCounterBuilderClass = null;
        Class<?> meterClass = null;
        Class<?> meterBuilderClass = null;
        Class<?> meterProviderClass = null;

        Class<?> scopeClass = null;
        Class<?> spanClass = null;
        Class<?> spanBuilderClass = null;
        Class<?> spanContextClass = null;
        Class<?> spanKindClass = null;
        Class<?> statusCodeClass = null;

        Class<?> textMapPropagatorClass = null;
        Class<?> textMapGetterClass = null;
        Class<?> textMapSetterClass = null;

        Class<?> traceFlagsClass = null;
        Class<?> traceStateClass = null;
        Class<?> tracerClass = null;
        Class<?> tracerBuilderClass = null;
        Class<?> tracerProviderClass = null;

        Class<?> w3cPropagatorClass = null;

        OTelInitializer instance = null;
        try {
            ClassLoader classLoader = OTelInitializer.class.getClassLoader();
            attributeKeyClass = Class.forName("io.opentelemetry.api.common.AttributeKey", true, classLoader);
            attributesClass = Class.forName("io.opentelemetry.api.common.Attributes", true, classLoader);
            attributesBuilderClass = Class.forName("io.opentelemetry.api.common.AttributesBuilder", true, classLoader);

            contextClass = Class.forName("io.opentelemetry.context.Context", true, classLoader);
            contextKeyClass = Class.forName("io.opentelemetry.context.ContextKey", true, classLoader);
            contextPropagatorsClass
                = Class.forName("io.opentelemetry.context.propagation.ContextPropagators", true, classLoader);

            otelClass = Class.forName("io.opentelemetry.api.OpenTelemetry", true, classLoader);
            globalOtelClass = Class.forName("io.opentelemetry.api.GlobalOpenTelemetry", true, classLoader);

            doubleHistogramClass = Class.forName("io.opentelemetry.api.metrics.DoubleHistogram", true, classLoader);
            doubleHistogramBuilderClass
                = Class.forName("io.opentelemetry.api.metrics.DoubleHistogramBuilder", true, classLoader);

            longCounterClass = Class.forName("io.opentelemetry.api.metrics.LongCounter", true, classLoader);
            longCounterBuilderClass
                = Class.forName("io.opentelemetry.api.metrics.LongCounterBuilder", true, classLoader);

            longUpDownCounterClass = Class.forName("io.opentelemetry.api.metrics.LongUpDownCounter", true, classLoader);
            longUpDownCounterBuilderClass
                = Class.forName("io.opentelemetry.api.metrics.LongUpDownCounterBuilder", true, classLoader);

            meterClass = Class.forName("io.opentelemetry.api.metrics.Meter", true, classLoader);
            meterBuilderClass = Class.forName("io.opentelemetry.api.metrics.MeterBuilder", true, classLoader);
            meterProviderClass = Class.forName("io.opentelemetry.api.metrics.MeterProvider", true, classLoader);

            scopeClass = Class.forName("io.opentelemetry.context.Scope", true, classLoader);

            spanClass = Class.forName("io.opentelemetry.api.trace.Span", true, classLoader);
            spanBuilderClass = Class.forName("io.opentelemetry.api.trace.SpanBuilder", true, classLoader);
            spanContextClass = Class.forName("io.opentelemetry.api.trace.SpanContext", true, classLoader);
            spanKindClass = Class.forName("io.opentelemetry.api.trace.SpanKind", true, classLoader);
            statusCodeClass = Class.forName("io.opentelemetry.api.trace.StatusCode", true, classLoader);

            textMapPropagatorClass
                = Class.forName("io.opentelemetry.context.propagation.TextMapPropagator", true, classLoader);
            textMapGetterClass = Class.forName("io.opentelemetry.context.propagation.TextMapGetter", true, classLoader);
            textMapSetterClass = Class.forName("io.opentelemetry.context.propagation.TextMapSetter", true, classLoader);

            traceFlagsClass = Class.forName("io.opentelemetry.api.trace.TraceFlags", true, classLoader);
            traceStateClass = Class.forName("io.opentelemetry.api.trace.TraceState", true, classLoader);
            tracerClass = Class.forName("io.opentelemetry.api.trace.Tracer", true, classLoader);
            tracerBuilderClass = Class.forName("io.opentelemetry.api.trace.TracerBuilder", true, classLoader);
            tracerProviderClass = Class.forName("io.opentelemetry.api.trace.TracerProvider", true, classLoader);
            w3cPropagatorClass
                = Class.forName("io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator", true, classLoader);

            instance = new OTelInitializer(true);
        } catch (Throwable t) {
            LOGGER.atVerbose().setThrowable(t).log("OpenTelemetry was not initialized.");
            instance = new OTelInitializer(false);
        }

        ATTRIBUTE_KEY_CLASS = attributeKeyClass;
        ATTRIBUTES_CLASS = attributesClass;
        ATTRIBUTES_BUILDER_CLASS = attributesBuilderClass;

        CONTEXT_CLASS = contextClass;
        CONTEXT_KEY_CLASS = contextKeyClass;
        CONTEXT_PROPAGATORS_CLASS = contextPropagatorsClass;

        OTEL_CLASS = otelClass;
        GLOBAL_OTEL_CLASS = globalOtelClass;

        DOUBLE_HISTOGRAM_CLASS = doubleHistogramClass;
        DOUBLE_HISTOGRAM_BUILDER_CLASS = doubleHistogramBuilderClass;

        LONG_COUNTER_CLASS = longCounterClass;
        LONG_COUNTER_BUILDER_CLASS = longCounterBuilderClass;

        LONG_UP_DOWN_COUNTER_CLASS = longUpDownCounterClass;
        LONG_UP_DOWN_COUNTER_BUILDER_CLASS = longUpDownCounterBuilderClass;

        METER_CLASS = meterClass;
        METER_BUILDER_CLASS = meterBuilderClass;
        METER_PROVIDER_CLASS = meterProviderClass;

        SCOPE_CLASS = scopeClass;
        SPAN_CLASS = spanClass;
        SPAN_BUILDER_CLASS = spanBuilderClass;
        SPAN_CONTEXT_CLASS = spanContextClass;
        SPAN_KIND_CLASS = spanKindClass;
        STATUS_CODE_CLASS = statusCodeClass;

        TEXT_MAP_PROPAGATOR_CLASS = textMapPropagatorClass;
        TEXT_MAP_GETTER_CLASS = textMapGetterClass;
        TEXT_MAP_SETTER_CLASS = textMapSetterClass;

        TRACE_FLAGS_CLASS = traceFlagsClass;
        TRACE_STATE_CLASS = traceStateClass;
        TRACER_CLASS = tracerClass;
        TRACER_BUILDER_CLASS = tracerBuilderClass;
        TRACER_PROVIDER_CLASS = tracerProviderClass;

        W3C_PROPAGATOR_CLASS = w3cPropagatorClass;

        INSTANCE = instance;
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
