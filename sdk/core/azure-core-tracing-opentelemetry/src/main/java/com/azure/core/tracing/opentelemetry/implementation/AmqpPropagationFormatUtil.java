// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.tracing.opentelemetry.implementation;

import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;

import com.azure.core.util.Context;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.Tracestate;

import java.util.Objects;

public final class AmqpPropagationFormatUtil {

    private AmqpPropagationFormatUtil() { }

    /**
     * This method is called to extract the Span Context information from the received event's diagnostic Id.
     *
     * @param diagnosticId The diagnostic Id providing an unique identifier for individual traces and requests
     * @return {@link com.azure.core.util.Context} which contains the trace context propagation data
     */
    public static Context extractContext(String diagnosticId, Context context) {
        return context.addData(SPAN_CONTEXT_KEY, fromDiagnosticId(diagnosticId));
    }

    /**
     * The trace-parent HTTP header field identifies the incoming request in a tracing system with four fields:
     * version, trace-id, parent-id, trace-flags.
     *
     * Please refer to the <a href=https://www.w3.org/TR/trace-context/#traceparent-header>Trace-parent Header</a>
     * for more information on the conversion of these fields to Span Context format.
     *
     * @param spanContext is a specification defines an agreed-upon format for the exchange of trace context propagation
     * data.
     * @return  The diagnostic Id providing an unique identifier for individual traces and requests,
     * allowing trace data of multiple providers to be linked together.
     * @throws NullPointerException if {@code spanContext} is {@code null}.
     */
    public static String getDiagnosticId(SpanContext spanContext) {
        Objects.requireNonNull(spanContext, "'spanContext' cannot be null.");
        final char[] chars = new char[55];
        chars[0] = '0';
        chars[1] = '0';
        chars[2] = '-';
        spanContext.getTraceId().copyLowerBase16To(chars, 3);
        chars[35] = '-';
        spanContext.getSpanId().copyLowerBase16To(chars, 36);
        chars[52] = '-';
        spanContext.getTraceFlags().copyLowerBase16To(chars, 53);
        return new String(chars);
    }

    /**
     * The trace-parent HTTP header field identifies the incoming request in a tracing system with four fields:
     * version, trace-id, parent-id, trace-flags.
     *
     * Please refer to the <a href=https://www.w3.org/TR/trace-context/#traceparent-header>Trace-parent Header</a>
     * for more information on the conversion of these fields to Span Context format.
     *
     * @param diagnosticId provides a unique identifier for individual traces and requests,
     * @return SpanContext is a specification defines an agreed-upon format for the exchange of trace context
     * propagation data
     */
    private static SpanContext fromDiagnosticId(String diagnosticId) {
        if (diagnosticId == null || diagnosticId.length() < 55 || !diagnosticId.startsWith("00")) {
            return SpanContext.create(
                TraceId.getInvalid(),
                SpanId.getInvalid(),
                TraceFlags.getDefault(),
                Tracestate.getDefault()
            );
        }
        return SpanContext.create(
            TraceId.fromLowerBase16(diagnosticId, 3),
            SpanId.fromLowerBase16(diagnosticId, 36),
            TraceFlags.fromLowerBase16(diagnosticId, 53),
            Tracestate.builder().build());
    }
}
