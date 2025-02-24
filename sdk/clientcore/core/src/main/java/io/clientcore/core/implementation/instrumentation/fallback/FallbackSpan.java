// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.fallback;

import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.logging.LoggingEvent;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.clientcore.core.instrumentation.tracing.TracingScope;

import static io.clientcore.core.implementation.instrumentation.AttributeKeys.ERROR_TYPE_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.SPAN_DURATION_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.SPAN_ID_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.TRACE_ID_KEY;
import static io.clientcore.core.implementation.instrumentation.LoggingEventNames.SPAN_ENDED_EVENT_NAME;

final class FallbackSpan implements Span {
    private final LoggingEvent log;
    private final long startTime;
    private final FallbackSpanContext spanContext;
    private final SpanKind kind;
    private String errorType;

    FallbackSpan(LoggingEvent log, SpanKind spanKind, FallbackSpanContext parentSpanContext, boolean isRecording) {
        this.log = log;
        this.startTime = isRecording ? System.nanoTime() : 0;
        this.kind = spanKind;
        this.spanContext = FallbackSpanContext.fromParent(parentSpanContext, isRecording, this);
        if (log != null && log.isEnabled()) {
            this.log.addKeyValue(TRACE_ID_KEY, spanContext.getTraceId())
                .addKeyValue(SPAN_ID_KEY, spanContext.getSpanId());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Span setAttribute(String key, Object value) {
        if (log != null) {
            log.addKeyValue(key, value);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Span setError(String errorType) {
        this.errorType = errorType;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void end() {
        end(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void end(Throwable error) {
        if (log == null || !log.isEnabled()) {
            return;
        }

        double durationMs = (System.nanoTime() - startTime) / 1_000_000.0;
        log.addKeyValue(SPAN_DURATION_KEY, durationMs);
        if (error != null || errorType != null) {
            setAttribute(ERROR_TYPE_KEY, errorType != null ? errorType : error.getClass().getCanonicalName());
        }

        log.setEventName(SPAN_ENDED_EVENT_NAME);
        log.log();
    }

    public SpanKind getSpanKind() {
        return kind;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRecording() {
        return log != null && log.isEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TracingScope makeCurrent() {
        return new FallbackScope(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InstrumentationContext getInstrumentationContext() {
        return spanContext;
    }
}
