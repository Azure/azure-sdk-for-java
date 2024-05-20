// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.tracing.opentelemetry;

import com.azure.core.util.Context;
import com.azure.core.util.tracing.Tracer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("deprecation")
public class AmqpPropagationFormatUtilTest {

    private static final SdkTracerProvider TRACER_PROVIDER = SdkTracerProvider.builder().build();
    private static final OpenTelemetry OPEN_TELEMETRY
        = OpenTelemetrySdk.builder().setTracerProvider(TRACER_PROVIDER).build();
    private static final Tracer TRACER = new OpenTelemetryTracer("test", null, null,
        new OpenTelemetryTracingOptions().setOpenTelemetry(OPEN_TELEMETRY));

    @Test
    public void extractContextReturnsSpanContext() {

        // Act
        Context context = TRACER.extractContext("", Context.NONE);

        // Assert
        assertNotNull(context);
        assertTrue(context.getData(SPAN_CONTEXT_KEY).isPresent());
    }

    @Test
    public void getInvalidSpanContext() {
        // Act
        Context context = TRACER.extractContext("", Context.NONE);

        // Assert
        assertNotNull(context);
        assertFalse(((SpanContext) context.getData(SPAN_CONTEXT_KEY).get()).isValid(),
            "Invalid diagnostic Id, returns invalid SpanContext ");
    }

    @Test
    public void getValidSpanContext() {
        // Act
        Context context
            = TRACER.extractContext("00-0af7651916cd43dd8448eb211c80319c-b9c7c989f97918e1-01", Context.NONE);

        // Assert
        assertNotNull(context);
        assertTrue(((SpanContext) context.getData(SPAN_CONTEXT_KEY).get()).isValid(),
            "Valid diagnostic Id, returns valid SpanContext ");
    }

    @Test
    public void getValidDiagnosticId() {
        // Arrange

        final Context contextWithSpan = TRACER.start("test", Context.NONE);
        Span span = Span
            .fromContext((io.opentelemetry.context.Context) contextWithSpan.getData(PARENT_TRACE_CONTEXT_KEY).get());

        AtomicReference<String> diagnosticId = new AtomicReference<>();
        TRACER.injectContext((name, value) -> {
            if (name.equals("traceparent")) {
                diagnosticId.set(value);
            }
        }, contextWithSpan);

        // Assert
        String expectedTraceparent = "00-" + span.getSpanContext().getTraceId() + "-"
            + span.getSpanContext().getSpanId() + "-" + span.getSpanContext().getTraceFlags().asHex();
        assertNotNull(diagnosticId.get());
        assertEquals(expectedTraceparent, diagnosticId.get());
    }
}
