// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.fallback;

import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanBuilder;
import io.clientcore.core.instrumentation.tracing.SpanKind;

final class FallbackSpanBuilder implements SpanBuilder {
    static final FallbackSpanBuilder NOOP = new FallbackSpanBuilder();
    private final ClientLogger.LoggingEventBuilder log;
    private final FallbackSpanContext parentSpanContext;

    private FallbackSpanBuilder() {
        this.log = null;
        this.parentSpanContext = FallbackSpanContext.INVALID;
    }

    FallbackSpanBuilder(ClientLogger logger, String spanName, SpanKind spanKind,
        InstrumentationContext instrumentationContext) {
        this.parentSpanContext = FallbackSpanContext.fromInstrumentationContext(instrumentationContext);
        this.log = logger.atInfo();
        if (log.isEnabled()) {
            log.addKeyValue("span.name", spanName).addKeyValue("span.kind", spanKind.name());
            if (parentSpanContext.isValid()) {
                log.addKeyValue("span.parent.id", parentSpanContext.getSpanId());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SpanBuilder setAttribute(String key, Object value) {
        if (log != null) {
            log.addKeyValue(key, value);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Span startSpan() {
        if (log != null) {
            return new FallbackSpan(log, parentSpanContext, log.isEnabled());
        }

        return Span.noop();
    }
}
