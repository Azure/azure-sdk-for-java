// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.implementation.instrumentation.otel.tracing.OTelSpanContext;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.clientcore.core.instrumentation.tracing.Tracer;
import io.clientcore.core.instrumentation.tracing.TracingScope;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static io.clientcore.core.instrumentation.tracing.SpanKind.INTERNAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TracerTests {
    private static final SdkInstrumentationOptions DEFAULT_LIB_OPTIONS = new SdkInstrumentationOptions("test-library");

    private InMemorySpanExporter exporter;
    private SdkTracerProvider tracerProvider;
    private InstrumentationOptions otelOptions;
    private OpenTelemetry openTelemetry;
    private Instrumentation instrumentation;
    private Tracer tracer;

    @BeforeEach
    public void setUp() {
        exporter = InMemorySpanExporter.create();
        tracerProvider = SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(exporter)).build();

        openTelemetry = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();
        otelOptions = new InstrumentationOptions().setTelemetryProvider(openTelemetry);
        instrumentation = Instrumentation.create(otelOptions, DEFAULT_LIB_OPTIONS);
        tracer = instrumentation.getTracer();
    }

    @AfterEach
    public void tearDown() {
        exporter.reset();
        tracerProvider.close();
    }

    @SuppressWarnings("try")
    @Test
    public void testSpan() {
        Span span = tracer.spanBuilder("test-span", INTERNAL, null)
            .setAttribute("builder-string-attribute", "string")
            .setAttribute("builder-int-attribute", 42)
            .setAttribute("builder-long-attribute", 4242L)
            .setAttribute("builder-double-attribute", 42.42)
            .setAttribute("builder-boolean-attribute", true)
            .startSpan();

        assertTrue(span.isRecording());

        try (TracingScope scope = span.makeCurrent()) {
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

        assertEquals(io.opentelemetry.api.trace.StatusCode.UNSET, spanData.getStatus().getStatusCode());
    }

    @Test
    public void testSetAllAttributes() {
        Map<String, Object> start = new HashMap<>();
        start.put("string", "value");
        start.put("int", 42);
        start.put("double", 0.42);
        start.put("float", 4.2f);
        start.put("boolean", true);
        start.put("long", 420L);
        InstrumentationAttributes startAttributes = instrumentation.createAttributes(start);

        Span span = tracer.spanBuilder("test-span", INTERNAL, null).setAllAttributes(startAttributes).startSpan();
        span.end();

        assertEquals(1, exporter.getFinishedSpanItems().size());
        SpanData spanData = exporter.getFinishedSpanItems().get(0);
        Attributes attrs = spanData.getAttributes();
        assertEquals(6, attrs.size());
        assertEquals("value", attrs.get(AttributeKey.stringKey("string")));
        assertEquals(42L, attrs.get(AttributeKey.longKey("int")));
        assertEquals(420L, attrs.get(AttributeKey.longKey("long")));
        assertEquals(0.42, attrs.get(AttributeKey.doubleKey("double")));
        assertEquals(4.2f, attrs.get(AttributeKey.doubleKey("float")), 0.1);
        assertEquals(true, attrs.get(AttributeKey.booleanKey("boolean")));
    }

    @Test
    public void testEndWithErrorString() {
        Span span = tracer.spanBuilder("test-span", INTERNAL, null).startSpan();

        span.setError("cancelled");
        span.end();

        assertEquals(1, exporter.getFinishedSpanItems().size());
        SpanData spanData = exporter.getFinishedSpanItems().get(0);
        assertEquals("test-span", spanData.getName());

        assertEquals("cancelled", spanData.getAttributes().get(AttributeKey.stringKey("error.type")));
        assertEquals(io.opentelemetry.api.trace.StatusCode.ERROR, spanData.getStatus().getStatusCode());
        assertEquals("", spanData.getStatus().getDescription());
    }

    @Test
    public void testEndWithException() {
        Span span = tracer.spanBuilder("test-span", INTERNAL, null).startSpan();

        IOException exception = new IOException("test");
        span.end(exception);

        assertEquals(1, exporter.getFinishedSpanItems().size());
        SpanData spanData = exporter.getFinishedSpanItems().get(0);
        assertEquals("test-span", spanData.getName());

        assertEquals(IOException.class.getCanonicalName(),
            spanData.getAttributes().get(AttributeKey.stringKey("error.type")));
        assertEquals(io.opentelemetry.api.trace.StatusCode.ERROR, spanData.getStatus().getStatusCode());
        assertEquals(exception.getMessage(), spanData.getStatus().getDescription());
    }

    @ParameterizedTest
    @MethodSource("kindSource")
    public void testKinds(SpanKind kind, io.opentelemetry.api.trace.SpanKind expectedKind) {
        Span span = tracer.spanBuilder("test-span", kind, null).startSpan();

        span.end();

        assertEquals(1, exporter.getFinishedSpanItems().size());
        SpanData spanData = exporter.getFinishedSpanItems().get(0);

        assertEquals(expectedKind, spanData.getKind());
    }

    @SuppressWarnings("try")
    @Test
    public void implicitParent() throws Exception {
        io.opentelemetry.api.trace.Tracer otelTracer = openTelemetry.getTracer("test");
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
    public void explicitParent() {
        io.opentelemetry.api.trace.Tracer otelTracer = openTelemetry.getTracer("test");
        io.opentelemetry.api.trace.Span parent = otelTracer.spanBuilder("parent").startSpan();

        Span child = tracer.spanBuilder("child", INTERNAL, OTelSpanContext.fromOTelContext(Context.root().with(parent)))
            .startSpan();
        child.end();
        parent.end();

        assertEquals(2, exporter.getFinishedSpanItems().size());
        SpanData childData = exporter.getFinishedSpanItems().get(0);
        SpanData parentData = exporter.getFinishedSpanItems().get(1);

        assertEquals("child", childData.getName());
        assertEquals(parentData.getTraceId(), childData.getTraceId());
        assertEquals(parentData.getSpanId(), childData.getParentSpanId());
    }

    public static Stream<Arguments> kindSource() {
        return Stream.of(Arguments.of(SpanKind.INTERNAL, io.opentelemetry.api.trace.SpanKind.INTERNAL),
            Arguments.of(SpanKind.CLIENT, io.opentelemetry.api.trace.SpanKind.CLIENT),
            Arguments.of(SpanKind.PRODUCER, io.opentelemetry.api.trace.SpanKind.PRODUCER),
            Arguments.of(SpanKind.CONSUMER, io.opentelemetry.api.trace.SpanKind.CONSUMER),
            Arguments.of(SpanKind.SERVER, io.opentelemetry.api.trace.SpanKind.SERVER));
    }
}
