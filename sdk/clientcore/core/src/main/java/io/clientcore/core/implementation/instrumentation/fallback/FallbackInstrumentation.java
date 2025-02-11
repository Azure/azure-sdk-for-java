// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.fallback;

import io.clientcore.core.implementation.instrumentation.LibraryInstrumentationOptionsAccessHelper;
import io.clientcore.core.implementation.instrumentation.NoopMeter;
import io.clientcore.core.instrumentation.Instrumentation;
import io.clientcore.core.instrumentation.InstrumentationAttributes;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.InstrumentationOptions;
import io.clientcore.core.instrumentation.LibraryInstrumentationOptions;
import io.clientcore.core.instrumentation.metrics.Meter;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.clientcore.core.instrumentation.tracing.TraceContextPropagator;
import io.clientcore.core.instrumentation.tracing.Tracer;

import java.util.Map;

/**
 * Fallback implementation of {@link Instrumentation} which implements basic correlation and context propagation
 * and, when enabled, records traces as logs.
 */
public class FallbackInstrumentation implements Instrumentation {
    public static final FallbackInstrumentation DEFAULT_INSTANCE = new FallbackInstrumentation(null, null);

    private final InstrumentationOptions instrumentationOptions;
    private final LibraryInstrumentationOptions libraryOptions;
    private final boolean allowNestedSpans;
    private final boolean isTracingEnabled;

    /**
     * Creates a new instance of {@link FallbackInstrumentation}.
     * @param instrumentationOptions the application instrumentation options
     * @param libraryOptions the library instrumentation options
     */
    public FallbackInstrumentation(InstrumentationOptions instrumentationOptions,
        LibraryInstrumentationOptions libraryOptions) {
        this.instrumentationOptions = instrumentationOptions;
        this.libraryOptions = libraryOptions;
        this.allowNestedSpans = libraryOptions != null
            && LibraryInstrumentationOptionsAccessHelper.isSpanSuppressionDisabled(libraryOptions);
        this.isTracingEnabled = instrumentationOptions == null || instrumentationOptions.isTracingEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tracer createTracer() {
        return new FallbackTracer(instrumentationOptions, libraryOptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Meter createMeter() {
        // We don't provide fallback metrics support. This might change in the future.
        // Some challenges:
        // - metric aggregation is complicated
        // - having metrics reported in logs is not very useful
        return NoopMeter.INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InstrumentationAttributes createAttributes(Map<String, Object> attributes) {
        return new FallbackAttributes(attributes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TraceContextPropagator getW3CTraceContextPropagator() {
        return FallbackContextPropagator.W3C_TRACE_CONTEXT_PROPAGATOR;
    }

    /**
     * Creates a new instance of {@link InstrumentationContext} from the given object.
     * It recognizes {@link FallbackSpanContext}, {@link FallbackSpan}, and generic {@link InstrumentationContext}
     * as a source and converts them to {@link FallbackSpanContext}.
     * @param context the context object to convert
     * @return the instance of {@link InstrumentationContext} which is invalid if the context is not recognized
     * @param <T> the type of the context object
     */
    public <T> InstrumentationContext createInstrumentationContext(T context) {
        if (context instanceof InstrumentationContext) {
            return FallbackSpanContext.fromInstrumentationContext((InstrumentationContext) context);
        } else if (context instanceof FallbackSpan) {
            return ((FallbackSpan) context).getInstrumentationContext();
        } else {
            return FallbackSpanContext.INVALID;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shouldInstrument(SpanKind spanKind, InstrumentationContext context) {
        if (!isTracingEnabled) {
            return false;
        }

        if (allowNestedSpans) {
            return true;
        }

        return spanKind != tryGetSpanKind(context);
    }

    /**
     * Retrieves the span kind from the given context if and only if the context is a {@link FallbackSpanContext}
     * i.e. was created by this instrumentation.
     * @param context the context to get the span kind from
     * @return the span kind or {@code null} if the context is not recognized
     */
    private SpanKind tryGetSpanKind(InstrumentationContext context) {
        if (context instanceof FallbackSpanContext) {
            Span span = context.getSpan();
            if (span instanceof FallbackSpan) {
                return ((FallbackSpan) span).getSpanKind();
            }
        }
        return null;
    }
}
