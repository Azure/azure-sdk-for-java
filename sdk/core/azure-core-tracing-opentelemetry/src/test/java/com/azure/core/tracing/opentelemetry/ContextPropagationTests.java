// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import com.azure.core.util.Context;
import com.azure.core.util.tracing.Tracer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.IdGenerator;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("deprecation")
public class ContextPropagationTests {

    private static final SdkTracerProvider TRACER_PROVIDER = SdkTracerProvider.builder().build();
    private static final OpenTelemetry OPEN_TELEMETRY
        = OpenTelemetrySdk.builder().setTracerProvider(TRACER_PROVIDER).build();
    private static final Tracer TRACER = new OpenTelemetryTracer("test", null, null,
        new OpenTelemetryTracingOptions().setOpenTelemetry(OPEN_TELEMETRY));

    @Test
    public void extractContextEmpty() {
        // Act
        Context context = TRACER.extractContext(name -> null);

        // Assert
        assertNotNull(context);
        assertTrue(context.getData(SPAN_CONTEXT_KEY).isPresent());
        assertFalse(((SpanContext) context.getData(SPAN_CONTEXT_KEY).get()).isValid());
    }

    @Test
    public void extractContextInvalid() {
        // Act
        Context context = TRACER.extractContext(name -> "traceparent".equals(name) ? "00-abc-123-01" : null);

        // Assert
        assertNotNull(context);
        assertTrue(context.getData(SPAN_CONTEXT_KEY).isPresent());
        assertFalse(((SpanContext) context.getData(SPAN_CONTEXT_KEY).get()).isValid());
    }

    @Test
    public void extractContextValid() {
        // Act
        Context context = TRACER.extractContext(
            name -> "traceparent".equals(name) ? "00-0af7651916cd43dd8448eb211c80319c-b9c7c989f97918e1-01" : null);

        // Assert
        assertNotNull(context);
        assertTrue(context.getData(SPAN_CONTEXT_KEY).isPresent());
        SpanContext spanContext = (SpanContext) context.getData(SPAN_CONTEXT_KEY).get();
        assertTrue(spanContext.isValid());
        assertEquals(spanContext.getTraceId(), "0af7651916cd43dd8448eb211c80319c");
        assertEquals(spanContext.getSpanId(), "b9c7c989f97918e1");
        assertEquals(spanContext.getTraceFlags(), TraceFlags.getSampled());
    }

    @Test
    public void extractContextTracestate() {
        Map<String, String> headers = new HashMap<>();
        headers.put("traceparent", "00-0af7651916cd43dd8448eb211c80319c-b9c7c989f97918e1-01");
        headers.put("tracestate", "foo=bar,baz=42");

        // Act
        Context context = TRACER.extractContext(name -> headers.get(name));

        // Assert
        assertNotNull(context);
        assertTrue(context.getData(SPAN_CONTEXT_KEY).isPresent());
        SpanContext spanContext = (SpanContext) context.getData(SPAN_CONTEXT_KEY).get();
        assertTrue(spanContext.isValid());
        TraceState state = spanContext.getTraceState();

        assertEquals(2, state.size());
        assertEquals("bar", state.get("foo"));
        assertEquals("42", state.get("baz"));
    }

    @Test
    public void extractTraceparentAndDiagnosticId() {
        Map<String, String> headers = new HashMap<>();
        headers.put("traceparent", "00-0af7651916cd43dd8448eb211c80319c-b9c7c989f97918e1-00");
        headers.put("Diagnostic-Id", "00-1af7651916cd43dd8448eb211c80319c-c9c7c989f97918e1-01");

        // Act
        Context context = TRACER.extractContext(name -> headers.get(name));

        // Assert
        assertNotNull(context);
        assertTrue(context.getData(SPAN_CONTEXT_KEY).isPresent());
        SpanContext spanContext = (SpanContext) context.getData(SPAN_CONTEXT_KEY).get();
        assertTrue(spanContext.isValid());
        assertEquals(spanContext.getTraceId(), "0af7651916cd43dd8448eb211c80319c");
        assertEquals(spanContext.getSpanId(), "b9c7c989f97918e1");
        assertEquals(spanContext.getTraceFlags(), TraceFlags.getDefault());
    }

    @Test
    public void extractDiagnosticId() {
        // Act
        Context context = TRACER.extractContext(
            name -> "Diagnostic-Id".equals(name) ? "00-0af7651916cd43dd8448eb211c80319c-b9c7c989f97918e1-01" : null);

        // Assert
        assertNotNull(context);
        assertTrue(context.getData(SPAN_CONTEXT_KEY).isPresent());
        SpanContext spanContext = (SpanContext) context.getData(SPAN_CONTEXT_KEY).get();
        assertTrue(spanContext.isValid());
        assertEquals(spanContext.getTraceId(), "0af7651916cd43dd8448eb211c80319c");
        assertEquals(spanContext.getSpanId(), "b9c7c989f97918e1");
        assertEquals(spanContext.getTraceFlags(), TraceFlags.getSampled());
    }

    @Test
    public void injectTraceparent() {
        // Arrange

        final Context contextWithSpan = TRACER.start("test", Context.NONE);
        Span span = Span
            .fromContext((io.opentelemetry.context.Context) contextWithSpan.getData(PARENT_TRACE_CONTEXT_KEY).get());

        Map<String, String> headers = new HashMap<>();
        TRACER.injectContext((name, value) -> headers.put(name, value), contextWithSpan);

        // Assert
        String expectedTraceparent = "00-" + span.getSpanContext().getTraceId() + "-"
            + span.getSpanContext().getSpanId() + "-" + span.getSpanContext().getTraceFlags().asHex();
        assertEquals(expectedTraceparent, headers.get("traceparent"));
        assertNull(headers.get("Diagnostic-Id"));
        assertNull(headers.get("tracestate"));
        assertNull(headers.get("baggage"));
    }

    @Test
    @SuppressWarnings("try")
    public void injectTraceparentAndState() {
        // Arrange
        io.opentelemetry.api.trace.Tracer otelTracer = TRACER_PROVIDER.get("test");

        TraceState state = TraceState.builder().put("foo", "bar").put("baz", "42").build();
        SpanContext withTraceState = SpanContext.create(IdGenerator.random().generateTraceId(),
            IdGenerator.random().generateSpanId(), TraceFlags.getSampled(), state);

        Span span = otelTracer.spanBuilder("span")
            .setParent(io.opentelemetry.context.Context.root().with(Span.wrap(withTraceState)))
            .startSpan();
        Context contextWithSpan
            = new Context(PARENT_TRACE_CONTEXT_KEY, io.opentelemetry.context.Context.root().with(span));

        Map<String, String> headers = new HashMap<>();

        Baggage baggage = Baggage.builder().put("baggage-foo", "bar").build();
        try (Scope s = baggage.makeCurrent()) {
            TRACER.injectContext((name, value) -> headers.put(name, value), contextWithSpan);
        }

        // Assert
        String expectedTraceparent = "00-" + span.getSpanContext().getTraceId() + "-"
            + span.getSpanContext().getSpanId() + "-" + span.getSpanContext().getTraceFlags().asHex();
        assertEquals(expectedTraceparent, headers.get("traceparent"));
        assertEquals("baz=42,foo=bar", headers.get("tracestate"));
        assertNull(headers.get("baggage"));
    }
}
