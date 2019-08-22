package com.azure.tracing.opentelemetry;

import com.azure.core.implementation.tracing.Tracer;
import com.azure.core.util.Context;
import io.opencensus.trace.*;

public class AmqpPropagationFormat
{
    private static final String spanContext = Tracer.OPENTELEMETRY_AMQP_EVENT_SPAN_CONTEXT;

    public static Context extractContext(String diagnosticId)
    {
        return new Context(spanContext, fromDiagnosticId(diagnosticId));
    }

    static final SpanContext fromDiagnosticId(String diagnosticId) {
        if (diagnosticId == null || diagnosticId.length() < 55 || !diagnosticId.startsWith("00")) {
            return SpanContext.create(TraceId.INVALID, SpanId.INVALID, TraceOptions.DEFAULT, Tracestate.builder().build());
        }
        return SpanContext.create(
            TraceId.fromLowerBase16(diagnosticId, 3),
            SpanId.fromLowerBase16(diagnosticId, 36),
            TraceOptions.fromLowerBase16(diagnosticId, 53),
            Tracestate.builder().build());
    }
}
