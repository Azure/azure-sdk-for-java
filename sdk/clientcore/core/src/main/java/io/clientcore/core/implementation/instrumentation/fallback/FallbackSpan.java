// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.fallback;

import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.TracingScope;

final class FallbackSpan implements Span {
    private static final String SPAN_END_EVENT = "span.ended";

    private final ClientLogger.LoggingEventBuilder log;
    private final long startTime;
    private final FallbackSpanContext spanContext;
    private String errorType;

    FallbackSpan(ClientLogger.LoggingEventBuilder log, FallbackSpanContext parentSpanContext, boolean isRecording) {
        this.log = log;
        this.startTime = isRecording ? System.nanoTime() : 0;
        this.spanContext = FallbackSpanContext.fromParent(parentSpanContext, isRecording, this);
        if (log != null && log.isEnabled()) {
            this.log.addKeyValue("trace.id", spanContext.getTraceId()).addKeyValue("span.id", spanContext.getSpanId());
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
        log.addKeyValue("span.duration.ms", durationMs);
        if (error != null || errorType != null) {
            setAttribute("error.type", errorType != null ? errorType : error.getClass().getCanonicalName());
        }

        log.setEventName(SPAN_END_EVENT);
        if (error != null) {
            log.log(null, error);
        } else {
            log.log(null);
        }
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
