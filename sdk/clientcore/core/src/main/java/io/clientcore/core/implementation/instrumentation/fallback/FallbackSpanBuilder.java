// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.fallback;

import io.clientcore.core.instrumentation.InstrumentationAttributes;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.logging.LoggingEvent;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanBuilder;
import io.clientcore.core.instrumentation.tracing.SpanKind;

import java.util.Map;

import static io.clientcore.core.implementation.instrumentation.AttributeKeys.SPAN_KIND_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.SPAN_NAME_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.SPAN_PARENT_ID_KEY;

final class FallbackSpanBuilder implements SpanBuilder {
    static final FallbackSpanBuilder NOOP = new FallbackSpanBuilder();
    private final LoggingEvent log;
    private final FallbackSpanContext parentSpanContext;
    private final SpanKind spanKind;

    private FallbackSpanBuilder() {
        this.log = null;
        this.parentSpanContext = FallbackSpanContext.INVALID;
        this.spanKind = null;
    }

    FallbackSpanBuilder(ClientLogger logger, String spanName, SpanKind spanKind,
        InstrumentationContext instrumentationContext) {
        this.parentSpanContext = FallbackSpanContext.fromInstrumentationContext(instrumentationContext);
        this.spanKind = spanKind;
        this.log = logger.atVerbose();
        if (log.isEnabled()) {
            log.addKeyValue(SPAN_NAME_KEY, spanName).addKeyValue(SPAN_KIND_KEY, spanKind.name());
            if (parentSpanContext.isValid()) {
                log.addKeyValue(SPAN_PARENT_ID_KEY, parentSpanContext.getSpanId());
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
    public SpanBuilder setAllAttributes(InstrumentationAttributes attributes) {
        if (log != null && attributes instanceof FallbackAttributes) {
            for (Map.Entry<String, Object> entry : ((FallbackAttributes) attributes).getAttributes().entrySet()) {
                log.addKeyValue(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Span startSpan() {
        if (log != null) {
            return new FallbackSpan(log, spanKind, parentSpanContext, log.isEnabled());
        }

        return Span.noop();
    }
}
