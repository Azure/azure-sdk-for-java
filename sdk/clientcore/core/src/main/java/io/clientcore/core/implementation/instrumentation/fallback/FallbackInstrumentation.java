// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.fallback;

import io.clientcore.core.instrumentation.Instrumentation;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.InstrumentationOptions;
import io.clientcore.core.instrumentation.LibraryInstrumentationOptions;
import io.clientcore.core.instrumentation.tracing.TraceContextPropagator;
import io.clientcore.core.instrumentation.tracing.Tracer;

/**
 * Fallback implementation of {@link Instrumentation} which implements basic correlation and context propagation
 * and, when enabled, records traces as logs.
 */
public class FallbackInstrumentation implements Instrumentation {
    public static final FallbackInstrumentation DEFAULT_INSTANCE = new FallbackInstrumentation(null, null);

    private final InstrumentationOptions instrumentationOptions;
    private final LibraryInstrumentationOptions libraryOptions;

    /**
     * Creates a new instance of {@link FallbackInstrumentation}.
     * @param instrumentationOptions the application instrumentation options
     * @param libraryOptions the library instrumentation options
     */
    public FallbackInstrumentation(InstrumentationOptions instrumentationOptions,
        LibraryInstrumentationOptions libraryOptions) {
        this.instrumentationOptions = instrumentationOptions;
        this.libraryOptions = libraryOptions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tracer getTracer() {
        return new FallbackTracer(instrumentationOptions, libraryOptions);
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
}
