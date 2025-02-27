// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.otel;

import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.instrumentation.LibraryInstrumentationOptionsAccessHelper;
import io.clientcore.core.implementation.instrumentation.NoopAttributes;
import io.clientcore.core.implementation.instrumentation.NoopMeter;
import io.clientcore.core.implementation.instrumentation.otel.metrics.OTelMeter;
import io.clientcore.core.implementation.instrumentation.otel.tracing.OTelSpan;
import io.clientcore.core.implementation.instrumentation.otel.tracing.OTelSpanContext;
import io.clientcore.core.implementation.instrumentation.otel.tracing.OTelTraceContextPropagator;
import io.clientcore.core.implementation.instrumentation.otel.tracing.OTelTracer;
import io.clientcore.core.instrumentation.Instrumentation;
import io.clientcore.core.instrumentation.InstrumentationAttributes;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.LibraryInstrumentationOptions;
import io.clientcore.core.instrumentation.InstrumentationOptions;
import io.clientcore.core.instrumentation.metrics.Meter;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.clientcore.core.instrumentation.tracing.TraceContextPropagator;
import io.clientcore.core.instrumentation.tracing.Tracer;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.util.Map;

import static io.clientcore.core.implementation.ReflectionUtils.getMethodInvoker;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.CONTEXT_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.GLOBAL_OTEL_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.OTEL_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.SPAN_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.SPAN_CONTEXT_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.TRACER_PROVIDER_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.W3C_PROPAGATOR_CLASS;

/**
 * A {@link Instrumentation} implementation that uses OpenTelemetry.
 */
public class OTelInstrumentation implements Instrumentation {
    private static final FallbackInvoker GET_TRACER_PROVIDER_INVOKER;
    private static final FallbackInvoker GET_METER_PROVIDER_INVOKER;
    private static final FallbackInvoker GET_GLOBAL_OTEL_INVOKER;

    private static final Object NOOP_PROVIDER;
    private static final OTelTraceContextPropagator W3C_PROPAGATOR_INSTANCE;
    private static final ClientLogger LOGGER = new ClientLogger(OTelInstrumentation.class);

    static {
        ReflectiveInvoker getTracerProviderInvoker = null;
        ReflectiveInvoker getMeterProviderInvoker = null;
        ReflectiveInvoker getGlobalOtelInvoker = null;

        Object noopProvider = null;
        Object w3cPropagatorInstance = null;

        if (OTelInitializer.isInitialized()) {
            try {
                getTracerProviderInvoker = getMethodInvoker(OTEL_CLASS, OTEL_CLASS.getMethod("getTracerProvider"));
                getMeterProviderInvoker = getMethodInvoker(OTEL_CLASS, OTEL_CLASS.getMethod("getMeterProvider"));
                getGlobalOtelInvoker = getMethodInvoker(GLOBAL_OTEL_CLASS, GLOBAL_OTEL_CLASS.getMethod("get"));

                ReflectiveInvoker noopProviderInvoker
                    = getMethodInvoker(TRACER_PROVIDER_CLASS, TRACER_PROVIDER_CLASS.getMethod("noop"));
                noopProvider = noopProviderInvoker.invoke();

                ReflectiveInvoker w3cPropagatorInvoker
                    = getMethodInvoker(W3C_PROPAGATOR_CLASS, W3C_PROPAGATOR_CLASS.getMethod("getInstance"));
                w3cPropagatorInstance = w3cPropagatorInvoker.invoke();

            } catch (Throwable t) {
                OTelInitializer.initError(LOGGER, t);
            }
        }

        GET_TRACER_PROVIDER_INVOKER = new FallbackInvoker(getTracerProviderInvoker, LOGGER);
        GET_METER_PROVIDER_INVOKER = new FallbackInvoker(getMeterProviderInvoker, LOGGER);
        GET_GLOBAL_OTEL_INVOKER = new FallbackInvoker(getGlobalOtelInvoker, LOGGER);
        NOOP_PROVIDER = noopProvider;

        W3C_PROPAGATOR_INSTANCE = new OTelTraceContextPropagator(w3cPropagatorInstance);
    }

    public static final OTelInstrumentation DEFAULT_INSTANCE = new OTelInstrumentation(null, null);

    private final Object otelInstance;
    private final LibraryInstrumentationOptions libraryOptions;
    private final boolean isTracingEnabled;
    private final boolean isMetricsEnabled;
    private final boolean allowNestedSpans;

    /**
     * Creates a new instance of {@link OTelInstrumentation}.
     *
     * @param applicationOptions the application options
     * @param libraryOptions the library options
     */
    public OTelInstrumentation(InstrumentationOptions applicationOptions,
        LibraryInstrumentationOptions libraryOptions) {
        Object explicitOTel = applicationOptions == null ? null : applicationOptions.getTelemetryProvider();
        if (explicitOTel != null && !OTEL_CLASS.isInstance(explicitOTel)) {
            throw LOGGER.atError()
                .addKeyValue("expectedProvider", OTEL_CLASS.getName())
                .addKeyValue("actualProvider", explicitOTel.getClass().getName())
                .log("Unexpected telemetry provider type.",
                    new IllegalArgumentException("Telemetry provider is not an instance of " + OTEL_CLASS.getName()));
        }

        this.otelInstance = explicitOTel;
        this.libraryOptions = libraryOptions;
        this.isTracingEnabled = applicationOptions == null || applicationOptions.isTracingEnabled();
        this.isMetricsEnabled = applicationOptions == null || applicationOptions.isMetricsEnabled();
        this.allowNestedSpans = libraryOptions != null
            && LibraryInstrumentationOptionsAccessHelper.isSpanSuppressionDisabled(libraryOptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tracer createTracer() {
        if (isTracingEnabled && OTelInitializer.isInitialized()) {
            Object otelTracerProvider = GET_TRACER_PROVIDER_INVOKER.invoke(getOtelInstance());

            if (otelTracerProvider != null && otelTracerProvider != NOOP_PROVIDER) {
                return new OTelTracer(otelTracerProvider, libraryOptions);
            }
        }

        return OTelTracer.NOOP;
    }

    @Override
    public Meter createMeter() {
        if (isMetricsEnabled && OTelInitializer.isInitialized()) {
            Object otelMeterProvider = GET_METER_PROVIDER_INVOKER.invoke(getOtelInstance());

            if (otelMeterProvider != null && otelMeterProvider != NOOP_PROVIDER) {
                return new OTelMeter(otelMeterProvider, libraryOptions);
            }
        }

        return NoopMeter.INSTANCE;
    }

    @Override
    public InstrumentationAttributes createAttributes(Map<String, Object> attributes) {
        return OTelInitializer.isInitialized() ? OTelAttributes.create(attributes) : NoopAttributes.INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TraceContextPropagator getW3CTraceContextPropagator() {
        return OTelInitializer.isInitialized() ? W3C_PROPAGATOR_INSTANCE : OTelTraceContextPropagator.NOOP;
    }

    /**
     * Creates a new instance of {@link InstrumentationContext} from the given object.
     * It recognizes {@code io.opentelemetry.api.trace.Span}, {@code io.opentelemetry.api.trace.SpanContext},
     * {@code io.opentelemetry.context.Context} and generic {@link InstrumentationContext}
     * as a source and converts them to {@link InstrumentationContext}.
     * @param context the context object to convert
     * @return the instance of {@link InstrumentationContext} which is invalid if the context is not recognized
     * @param <T> the type of the context object
     */
    public <T> InstrumentationContext createInstrumentationContext(T context) {
        if (context instanceof InstrumentationContext) {
            return (InstrumentationContext) context;
        } else if (context instanceof OTelSpan) {
            return ((OTelSpan) context).getInstrumentationContext();
        } else if (SPAN_CLASS.isInstance(context)) {
            return OTelSpanContext.fromOTelSpan(context);
        } else if (CONTEXT_CLASS.isInstance(context)) {
            return OTelSpanContext.fromOTelContext(context);
        } else if (SPAN_CONTEXT_CLASS.isInstance(context)) {
            return new OTelSpanContext(context, null);
        }

        return OTelSpanContext.getInvalid();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shouldInstrument(SpanKind spanKind, InstrumentationContext context) {
        if (!isTracingEnabled && !isMetricsEnabled) {
            return false;
        }

        if (allowNestedSpans) {
            return true;
        }

        return spanKind != tryGetSpanKind(context);
    }

    private Object getOtelInstance() {
        // not caching global to prevent caching instance that was not setup yet at the start time.
        return otelInstance != null ? otelInstance : GET_GLOBAL_OTEL_INVOKER.invoke();
    }

    /**
     * Retrieves the span kind from the given context if and only if the context is a {@link OTelSpanContext}
     * i.e. was created by this instrumentation.
     * @param context the context to get the span kind from
     * @return the span kind or {@code null} if the context is not recognized
     */
    private static SpanKind tryGetSpanKind(InstrumentationContext context) {
        if (context instanceof OTelSpanContext) {
            Span span = context.getSpan();
            if (span instanceof OTelSpan) {
                return ((OTelSpan) span).getSpanKind();
            }
        }
        return null;
    }
}
