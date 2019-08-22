// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.tracing.opentelemetry;

import com.azure.core.implementation.tracing.Tracer;
import com.azure.core.util.Context;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracestate;


public class AmqpPropagationFormat {
    private static final String SPAN_CONTEXT = Tracer.OPENTELEMETRY_AMQP_EVENT_SPAN_CONTEXT;

    /**
     * This method is called to extract the Span Context information from the received event's diagnostic Id.
     *
     * @param diagnosticId The diagnostic Id for the event.
     * @return {@link Context}
     */
    public static Context extractContext(String diagnosticId) {
        return new Context(SPAN_CONTEXT, fromDiagnosticId(diagnosticId));
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
