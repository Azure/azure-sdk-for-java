// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.fallback;

import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.tracing.Span;

import static io.clientcore.core.implementation.instrumentation.fallback.RandomIdUtils.INVALID_SPAN_ID;
import static io.clientcore.core.implementation.instrumentation.fallback.RandomIdUtils.INVALID_TRACE_ID;
import static io.clientcore.core.implementation.instrumentation.fallback.RandomIdUtils.generateSpanId;
import static io.clientcore.core.implementation.instrumentation.fallback.RandomIdUtils.generateTraceId;

final class FallbackSpanContext implements InstrumentationContext {
    static final FallbackSpanContext INVALID
        = new FallbackSpanContext(INVALID_TRACE_ID, INVALID_SPAN_ID, "00", false, Span.noop());
    private final String traceId;
    private final String spanId;
    private final String traceFlags;
    private final boolean isValid;
    private final Span span;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTraceId() {
        return traceId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSpanId() {
        return spanId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        return isValid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Span getSpan() {
        return this.span;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTraceFlags() {
        return traceFlags;
    }

    FallbackSpanContext(String traceId, String spanId, String traceFlags, boolean isValid, Span span) {
        this.traceId = traceId;
        this.spanId = spanId;
        this.traceFlags = traceFlags;
        this.isValid = isValid;
        this.span = span;
    }

    static FallbackSpanContext fromParent(InstrumentationContext parent, boolean isSampled, FallbackSpan span) {
        return parent.isValid()
            ? new FallbackSpanContext(parent.getTraceId(), generateSpanId(), isSampled ? "01" : "00", true, span)
            : new FallbackSpanContext(generateTraceId(), generateSpanId(), isSampled ? "01" : "00", true, span);
    }

    static FallbackSpanContext fromInstrumentationContext(InstrumentationContext instrumentationContext) {
        if (instrumentationContext instanceof FallbackSpanContext) {
            return (FallbackSpanContext) instrumentationContext;
        }

        if (instrumentationContext != null) {
            return new FallbackSpanContext(instrumentationContext.getTraceId(), instrumentationContext.getSpanId(),
                instrumentationContext.getTraceFlags(), instrumentationContext.isValid(),
                instrumentationContext.getSpan());
        }

        Span currentSpan = FallbackScope.getCurrentSpan();
        if (currentSpan != Span.noop()) {
            return (FallbackSpanContext) currentSpan.getInstrumentationContext();
        }

        return INVALID;
    }
}
