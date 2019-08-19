// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tracing.opentelemetry;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Context;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Span;
import io.opencensus.trace.Span.Options;
import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;

import java.util.Optional;

/**
 * OpenTelemetry span
 */
public class OpenTelemetryTracer implements com.azure.core.implementation.tracing.Tracer {
    // Singleton OpenTelemetry tracer capable of starting and exporting spans.
    private static final Tracer TRACER = Tracing.getTracer();
    private static final String OPENTELEMETRY_SPAN_KEY = com.azure.core.implementation.tracing.Tracer.OPENTELEMETRY_SPAN_KEY;
    private static final String OPENTELEMETRY_SPAN_NAME_KEY = com.azure.core.implementation.tracing.Tracer.OPENTELEMETRY_SPAN_NAME_KEY;

    private final ClientLogger logger = new ClientLogger(OpenTelemetryTracer.class);

    @Override
    public Context start(String methodName, Context context) {
        Span parentSpan = (Span) context.getData(OPENTELEMETRY_SPAN_KEY).orElse(TRACER.getCurrentSpan());
        String spanName = (String) context.getData(OPENTELEMETRY_SPAN_NAME_KEY).orElse(methodName);

        SpanBuilder spanBuilder = TRACER.spanBuilderWithExplicitParent(spanName, parentSpan);
        Span span = spanBuilder.startSpan();

        return context.addData(OPENTELEMETRY_SPAN_KEY, span);
    }

    @Override
    public void end(int responseCode, Throwable throwable, Context context) {
        Optional<Object> spanOptional = context.getData(OPENTELEMETRY_SPAN_KEY);
        if (spanOptional.isPresent()) {
            Span span = (Span) spanOptional.get();

            if (span.getOptions().contains(Options.RECORD_EVENTS)) {
                span.setStatus(HttpTraceUtil.parseResponseStatus(responseCode, throwable));
            }

            span.end();
        } else {
            logger.warning("Failed to find span to end it.");
        }
    }

    @Override
    public void setAttribute(String key, String value, Context context) {
        if (ImplUtils.isNullOrEmpty(value)) {
            logger.info("Failed to set span attribute since value is null or empty.");
            return;
        }

        Optional<Object> spanOptional = context.getData(OPENTELEMETRY_SPAN_KEY);
        if (spanOptional.isPresent()) {
            Span span = (Span) spanOptional.get();
            span.putAttribute(key, AttributeValue.stringAttributeValue(value));
        } else {
            logger.warning("Failed to find span to add attribute.");
        }
    }

    @Override
    public Context setSpanName(String spanName, Context context) {
        return context.addData(OPENTELEMETRY_SPAN_NAME_KEY, spanName);
    }
}
