// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.telemetry;

import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.telemetry.tracing.Span;
import io.clientcore.core.telemetry.tracing.SpanKind;
import io.clientcore.core.telemetry.tracing.Tracer;
import io.clientcore.core.util.Context;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.clientcore.core.telemetry.tracing.SpanKind.INTERNAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TracerTests {
    private static final LibraryTelemetryOptions DEFAULT_LIB_OPTIONS = new LibraryTelemetryOptions("test-library");

    private InMemorySpanExporter exporter;
    private SdkTracerProvider tracerProvider;
    private TelemetryOptions<OpenTelemetry> otelOptions;

    @BeforeEach
    public void setUp() {
        exporter = InMemorySpanExporter.create();
        tracerProvider = SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(exporter)).build();

        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();
        otelOptions = new TelemetryOptions<OpenTelemetry>().setProvider(openTelemetry);
    }

    @AfterEach
    public void tearDown() {
        exporter.reset();
        tracerProvider.close();
    }

    @SuppressWarnings("try")
    @Test
    public void testSpan() {
        Tracer tracer = TelemetryProvider.getInstance().getTracer(otelOptions, DEFAULT_LIB_OPTIONS);
        Span span = tracer.spanBuilder("test-span", INTERNAL, null)
            .setAttribute("builder-string-attribute", "string")
            .setAttribute("builder-int-attribute", 42)
            .setAttribute("builder-long-attribute", 4242L)
            .setAttribute("builder-double-attribute", 42.42)
            .setAttribute("builder-boolean-attribute", true)
            .startSpan();

        assertTrue(span.isRecording());

        try (Scope scope = span.makeCurrent()) {
            assertTrue(io.opentelemetry.api.trace.Span.current().getSpanContext().isValid());
        }

        span.setAttribute("span-string-attribute", "string")
            .setAttribute("span-int-attribute", 42)
            .setAttribute("span-long-attribute", 4242L)
            .setAttribute("span-double-attribute", 42.42)
            .setAttribute("span-boolean-attribute", true);

        span.end();

        assertEquals(1, exporter.getFinishedSpanItems().size());
        SpanData spanData = exporter.getFinishedSpanItems().get(0);
        assertEquals("test-span", spanData.getName());
        assertEquals(io.opentelemetry.api.trace.SpanKind.INTERNAL, spanData.getKind());

        assertEquals("string", spanData.getAttributes().get(AttributeKey.stringKey("builder-string-attribute")));
        assertEquals("string", spanData.getAttributes().get(AttributeKey.stringKey("span-string-attribute")));

        assertEquals(42, spanData.getAttributes().get(AttributeKey.longKey("builder-int-attribute")));
        assertEquals(42, spanData.getAttributes().get(AttributeKey.longKey("span-int-attribute")));

        assertEquals(4242L, spanData.getAttributes().get(AttributeKey.longKey("builder-long-attribute")));
        assertEquals(4242L, spanData.getAttributes().get(AttributeKey.longKey("span-long-attribute")));

        assertEquals(42.42, spanData.getAttributes().get(AttributeKey.doubleKey("span-double-attribute")));
        assertEquals(42.42, spanData.getAttributes().get(AttributeKey.doubleKey("builder-double-attribute")));

        assertEquals(true, spanData.getAttributes().get(AttributeKey.booleanKey("builder-boolean-attribute")));
        assertEquals(true, spanData.getAttributes().get(AttributeKey.booleanKey("span-boolean-attribute")));
    }

    @ParameterizedTest
    @MethodSource("kindSource")
    public void testKinds(SpanKind kind, io.opentelemetry.api.trace.SpanKind expectedKind) {
        Tracer tracer = TelemetryProvider.getInstance().getTracer(otelOptions, DEFAULT_LIB_OPTIONS);
        Span span = tracer.spanBuilder("test-span", kind, null).startSpan();

        span.end();

        assertEquals(1, exporter.getFinishedSpanItems().size());
        SpanData spanData = exporter.getFinishedSpanItems().get(0);

        assertEquals(expectedKind, spanData.getKind());
    }

    @SuppressWarnings("try")
    @Test
    public void implicitParent() throws Exception {
        Tracer tracer = TelemetryProvider.getInstance().getTracer(otelOptions, DEFAULT_LIB_OPTIONS);

        io.opentelemetry.api.trace.Tracer otelTracer = otelOptions.getProvider().getTracer("test");
        io.opentelemetry.api.trace.Span parent = otelTracer.spanBuilder("parent").startSpan();
        try (AutoCloseable scope = parent.makeCurrent()) {
            Span child = tracer.spanBuilder("child", INTERNAL, null).startSpan();
            child.end();
        }

        parent.end();

        assertEquals(2, exporter.getFinishedSpanItems().size());
        SpanData childData = exporter.getFinishedSpanItems().get(0);
        SpanData parentData = exporter.getFinishedSpanItems().get(1);

        assertEquals("child", childData.getName());
        assertEquals(parentData.getTraceId(), childData.getTraceId());
        assertEquals(parentData.getSpanId(), childData.getParentSpanId());
    }

    @Test
    public void explicitParent() throws Exception {
        Tracer tracer = TelemetryProvider.getInstance().getTracer(otelOptions, DEFAULT_LIB_OPTIONS);

        io.opentelemetry.api.trace.Tracer otelTracer = otelOptions.getProvider().getTracer("test");
        io.opentelemetry.api.trace.Span parent = otelTracer.spanBuilder("parent").startSpan();

        RequestOptions requestOptions = new RequestOptions().setContext(Context.of(TelemetryProvider.TRACE_CONTEXT_KEY,
            parent.storeInContext(io.opentelemetry.context.Context.current())));
        Span child = tracer.spanBuilder("child", INTERNAL, requestOptions).startSpan();
        child.end();
        parent.end();

        assertEquals(2, exporter.getFinishedSpanItems().size());
        SpanData childData = exporter.getFinishedSpanItems().get(0);
        SpanData parentData = exporter.getFinishedSpanItems().get(1);

        assertEquals("child", childData.getName());
        assertEquals(parentData.getTraceId(), childData.getTraceId());
        assertEquals(parentData.getSpanId(), childData.getParentSpanId());
    }

    @Test
    public void explicitParentWrongType() {
        Tracer tracer = TelemetryProvider.getInstance().getTracer(otelOptions, DEFAULT_LIB_OPTIONS);

        RequestOptions requestOptions = new RequestOptions()
            .setContext(Context.of(TelemetryProvider.TRACE_CONTEXT_KEY, "This is not a valid trace context"));
        Span child = tracer
            .spanBuilder("child", INTERNAL, requestOptions)
            .startSpan();
        child.end();

        assertEquals(1, exporter.getFinishedSpanItems().size());
        SpanData childData = exporter.getFinishedSpanItems().get(0);

        assertEquals("child", childData.getName());
        assertFalse(childData.getParentSpanContext().isValid());
    }

    public static Stream<Arguments> kindSource() {
        return Stream.of(Arguments.of(SpanKind.INTERNAL, io.opentelemetry.api.trace.SpanKind.INTERNAL),
            Arguments.of(SpanKind.CLIENT, io.opentelemetry.api.trace.SpanKind.CLIENT),
            Arguments.of(SpanKind.PRODUCER, io.opentelemetry.api.trace.SpanKind.PRODUCER),
            Arguments.of(SpanKind.CONSUMER, io.opentelemetry.api.trace.SpanKind.CONSUMER),
            Arguments.of(SpanKind.SERVER, io.opentelemetry.api.trace.SpanKind.SERVER));
    }
}
