// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.tracing.opentelemetry.implementation;

import com.azure.core.util.Context;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;

import java.util.Objects;

import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;

public final class AmqpPropagationFormatUtil {

    private static final String VERSION = "00";
    private static final int VERSION_SIZE = 2;
    private static final char TRACEPARENT_DELIMITER = '-';
    private static final int TRACEPARENT_DELIMITER_SIZE = 1;
    private static final int TRACE_ID_HEX_SIZE = TraceId.getLength();
    private static final int SPAN_ID_HEX_SIZE = SpanId.getLength();
    private static final int TRACE_OPTION_HEX_SIZE = TraceFlags.getLength();
    private static final int TRACE_ID_OFFSET = VERSION_SIZE + TRACEPARENT_DELIMITER_SIZE;
    private static final int SPAN_ID_OFFSET =
        TRACE_ID_OFFSET + TRACE_ID_HEX_SIZE + TRACEPARENT_DELIMITER_SIZE;
    private static final int TRACE_OPTION_OFFSET =
        SPAN_ID_OFFSET + SPAN_ID_HEX_SIZE + TRACEPARENT_DELIMITER_SIZE;
    private static final int TRACEPARENT_HEADER_SIZE = TRACE_OPTION_OFFSET + TRACE_OPTION_HEX_SIZE;

    private AmqpPropagationFormatUtil() {
    }

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
     * <p>
     * Please refer to the <a href=https://www.w3.org/TR/trace-context/#traceparent-header>Trace-parent Header</a>
     * for more information on the conversion of these fields to Span Context format.
     *
     * @param spanContext is a specification defines an agreed-upon format for the exchange of trace context propagation
     * data.
     * @return The diagnostic Id providing an unique identifier for individual traces and requests,
     * allowing trace data of multiple providers to be linked together.
     * @throws NullPointerException if {@code spanContext} is {@code null}.
     */
    public static String getDiagnosticId(SpanContext spanContext) {
        Objects.requireNonNull(spanContext, "'spanContext' cannot be null.");
        if (!spanContext.isValid()) {
            return null;
        }

        char[] chars = new char[TRACEPARENT_HEADER_SIZE];
        chars[0] = VERSION.charAt(0);
        chars[1] = VERSION.charAt(1);
        chars[2] = TRACEPARENT_DELIMITER;

        String traceId = spanContext.getTraceId();
        for (int i = 0; i < traceId.length(); i++) {
            chars[TRACE_ID_OFFSET + i] = traceId.charAt(i);
        }

        chars[SPAN_ID_OFFSET - 1] = TRACEPARENT_DELIMITER;

        String spanId = spanContext.getSpanId();
        for (int i = 0; i < spanId.length(); i++) {
            chars[SPAN_ID_OFFSET + i] = spanId.charAt(i);
        }

        chars[TRACE_OPTION_OFFSET - 1] = TRACEPARENT_DELIMITER;
        String traceFlagsHex = spanContext.getTraceFlags().asHex();
        chars[TRACE_OPTION_OFFSET] = traceFlagsHex.charAt(0);
        chars[TRACE_OPTION_OFFSET + 1] = traceFlagsHex.charAt(1);
        return new String(chars, 0, TRACEPARENT_HEADER_SIZE);
    }

    /**
     * The trace-parent HTTP header field identifies the incoming request in a tracing system with four fields:
     * version, trace-id, parent-id, trace-flags.
     * <p>
     * Please refer to the <a href=https://www.w3.org/TR/trace-context/#traceparent-header>Trace-parent Header</a>
     * for more information on the conversion of these fields to Span Context format.
     *
     * @param traceparent provides a unique identifier for individual traces and requests,
     * @return SpanContext is a specification defines an agreed-upon format for the exchange of trace context
     * propagation data
     */
    private static SpanContext fromDiagnosticId(String traceparent) {
        if (traceparent == null || traceparent.length() < 55 || !traceparent.startsWith(VERSION)) {
            return SpanContext.create(
                TraceId.getInvalid(),
                SpanId.getInvalid(),
                TraceFlags.getDefault(),
                TraceState.getDefault()
            );
        }

        String traceId =
            traceparent.substring(TRACE_ID_OFFSET, TRACE_ID_OFFSET + TraceId.getLength());
        String spanId = traceparent.substring(SPAN_ID_OFFSET, SPAN_ID_OFFSET + SpanId.getLength());

        TraceFlags traceFlags = TraceFlags.fromHex(traceparent, TRACE_OPTION_OFFSET);

        return SpanContext.create(
            traceId,
            spanId,
            traceFlags,
            TraceState.builder().build());
    }
}
