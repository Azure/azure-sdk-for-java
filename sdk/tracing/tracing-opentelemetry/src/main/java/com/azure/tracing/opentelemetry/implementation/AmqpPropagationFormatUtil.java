// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.tracing.opentelemetry.implementation;

import com.azure.core.implementation.tracing.Tracer;
import com.azure.core.util.Context;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracestate;


public class AmqpPropagationFormatUtil {
    private static final String SPAN_CONTEXT = Tracer.OPENTELEMETRY_AMQP_EVENT_SPAN_CONTEXT;

    private AmqpPropagationFormatUtil() { }

    /**
     * This method is called to extract the Span Context information from the received event's diagnostic Id.
     *
     * @param diagnosticId The diagnostic Id for the event.
     * @return {@link Context}
     */
    public static Context extractContext(String diagnosticId) {
        return new Context(SPAN_CONTEXT, fromDiagnosticId(diagnosticId));
    }

    /**
     * Parse OpenTelemetry Status from HTTP response status code.
     *
     * <p>This method serves a default routine to map HTTP status code to Open Census Status. The
     * mapping is defined in <a
     * href="https://github.com/googleapis/googleapis/blob/master/google/rpc/code.proto">Google API
     * canonical error code</a>, and the behavior is defined in <a
     * href="https://github.com/census-instrumentation/opencensus-specs/blob/master/trace/HTTP.md">OpenTelemetry
     * Specs</a>.
     *
     * @param spanContext the span context.
     * @return the corresponding OpenTelemetry {@code Status}.
     */
    public static String getDiagnosticId(SpanContext spanContext) {
        char[] chars = new char[55];
        chars[0] = '0';
        chars[1] = '0';
        chars[2] = '-';
        spanContext.getTraceId().copyLowerBase16To(chars, 3);
        chars[35] = '-';
        spanContext.getSpanId().copyLowerBase16To(chars, 36);
        chars[52] = '-';
        spanContext.getTraceOptions().copyLowerBase16To(chars, 53);
        return new String(chars);
    }

    private static SpanContext fromDiagnosticId(String diagnosticId) {
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
