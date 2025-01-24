// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.Tracer;
import io.clientcore.core.instrumentation.tracing.TracingScope;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;

import static io.clientcore.core.instrumentation.logging.InstrumentationTestUtils.createInvalidInstrumentationContext;
import static io.clientcore.core.instrumentation.logging.InstrumentationTestUtils.createRandomInstrumentationContext;
import static io.clientcore.core.instrumentation.tracing.SpanKind.INTERNAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Isolated
@Execution(ExecutionMode.SAME_THREAD)
public class InstrumentationTests {
    private static final LibraryInstrumentationOptions DEFAULT_LIB_OPTIONS
        = new LibraryInstrumentationOptions("test-library");
    private InMemorySpanExporter exporter;
    private SdkTracerProvider tracerProvider;

    @BeforeEach
    public void setup() {
        GlobalOpenTelemetry.resetForTest();
        exporter = InMemorySpanExporter.create();
        tracerProvider = SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(exporter)).build();
    }

    @AfterEach
    public void teardown() {
        GlobalOpenTelemetry.resetForTest();
    }

    @Test
    public void createTracerOTelNotConfigured() {
        Tracer tracer = Instrumentation.create(null, DEFAULT_LIB_OPTIONS).getTracer();
        assertFalse(tracer.isEnabled());
    }

    @Test
    public void createTracerTracingDisabled() {
        OpenTelemetry otel = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).buildAndRegisterGlobal();

        InstrumentationOptions options
            = new InstrumentationOptions().setTracingEnabled(false).setTelemetryProvider(otel);

        Tracer tracer = Instrumentation.create(options, DEFAULT_LIB_OPTIONS).getTracer();
        assertFalse(tracer.isEnabled());
        tracer.spanBuilder("test", INTERNAL, null).startSpan().end();

        assertEquals(0, exporter.getFinishedSpanItems().size());
    }

    @SuppressWarnings("try")
    @Test
    public void createTracerGlobalOTel() throws Exception {
        try (AutoCloseable otel
            = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).buildAndRegisterGlobal()) {

            Tracer tracer = Instrumentation.create(null, DEFAULT_LIB_OPTIONS).getTracer();
            assertTrue(tracer.isEnabled());

            tracer.spanBuilder("test", INTERNAL, null).startSpan().end();

            assertEquals(1, exporter.getFinishedSpanItems().size());
            SpanData span = exporter.getFinishedSpanItems().get(0);
            assertEquals("test", span.getName());
            assertEquals("test-library", span.getInstrumentationScopeInfo().getName());
            assertNull(span.getInstrumentationScopeInfo().getVersion());
            assertNull(span.getInstrumentationScopeInfo().getSchemaUrl());
        }
    }

    @SuppressWarnings("try")
    @Test
    public void createTracerExplicitOTel() throws Exception {
        try (AutoCloseable global
            = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).buildAndRegisterGlobal()) {

            InMemorySpanExporter localExporter = InMemorySpanExporter.create();
            SdkTracerProvider localTracerProvider
                = SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(localExporter)).build();

            OpenTelemetry localOTel = OpenTelemetrySdk.builder().setTracerProvider(localTracerProvider).build();

            Tracer tracer = Instrumentation
                .create(new InstrumentationOptions().setTelemetryProvider(localOTel), DEFAULT_LIB_OPTIONS)
                .getTracer();
            assertTrue(tracer.isEnabled());

            tracer.spanBuilder("test", INTERNAL, null).startSpan().end();

            assertTrue(exporter.getFinishedSpanItems().isEmpty());
            assertEquals(1, localExporter.getFinishedSpanItems().size());
            assertEquals("test", localExporter.getFinishedSpanItems().get(0).getName());
        }
    }

    @Test
    public void createTracerBadArguments() {
        InstrumentationOptions options = new InstrumentationOptions().setTelemetryProvider(tracerProvider);

        assertThrows(IllegalArgumentException.class,
            () -> Instrumentation.create(options, DEFAULT_LIB_OPTIONS).getTracer());
        assertThrows(NullPointerException.class, () -> Instrumentation.create(null, null).getTracer());
    }

    @SuppressWarnings("try")
    @Test
    public void createTracerWithLibInfo() throws Exception {
        try (AutoCloseable otel
            = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).buildAndRegisterGlobal()) {

            LibraryInstrumentationOptions libOptions
                = new LibraryInstrumentationOptions("test-library").setLibraryVersion("1.0.0")
                    .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0");

            Tracer tracer = Instrumentation.create(null, libOptions).getTracer();
            assertTrue(tracer.isEnabled());

            tracer.spanBuilder("test", INTERNAL, null).startSpan().end();

            SpanData span = exporter.getFinishedSpanItems().get(0);
            assertEquals("test-library", span.getInstrumentationScopeInfo().getName());
            assertEquals("1.0.0", span.getInstrumentationScopeInfo().getVersion());
            assertEquals("https://opentelemetry.io/schemas/1.29.0", span.getInstrumentationScopeInfo().getSchemaUrl());
        }
    }

    @Test
    public void createInstrumentationContextNull() {
        assertNotNull(Instrumentation.createInstrumentationContext(null));
        assertFalse(Instrumentation.createInstrumentationContext(null).isValid());
    }

    @Test
    @SuppressWarnings("try")
    public void createInstrumentationContextFromOTelSpan() {
        OpenTelemetry otel = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();
        io.opentelemetry.api.trace.Tracer otelTracer = otel.getTracer("test");
        io.opentelemetry.api.trace.Span otelSpan = otelTracer.spanBuilder("test").startSpan();

        InstrumentationContext context = Instrumentation.createInstrumentationContext(otelSpan);
        assertNotNull(context);
        assertTrue(context.isValid());
        assertNotNull(context.getSpan());
        assertEquals(otelSpan.getSpanContext().getSpanId(), context.getSpanId());
        assertEquals(otelSpan.getSpanContext().getTraceId(), context.getTraceId());
        assertEquals(otelSpan.getSpanContext().getTraceFlags().asHex(), context.getTraceFlags());

        Tracer tracer
            = Instrumentation.create(new InstrumentationOptions().setTelemetryProvider(otel), DEFAULT_LIB_OPTIONS)
                .getTracer();
        Span span = tracer.spanBuilder("test", INTERNAL, context).startSpan();
        assertEquals(otelSpan.getSpanContext().getTraceId(), span.getInstrumentationContext().getTraceId());

        try (TracingScope scope = span.makeCurrent()) {
            assertEquals(otelSpan.getSpanContext().getSpanId(),
                ((ReadableSpan) io.opentelemetry.api.trace.Span.current()).getParentSpanContext().getSpanId());
        }
    }

    @Test
    public void createInstrumentationContextFromOTelContext() {
        OpenTelemetry otel = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();
        io.opentelemetry.api.trace.Tracer otelTracer = otel.getTracer("test");
        io.opentelemetry.api.trace.Span otelSpan = otelTracer.spanBuilder("test").startSpan();
        Context otelContext = Context.current().with(otelSpan);

        InstrumentationContext context = Instrumentation.createInstrumentationContext(otelContext);
        assertNotNull(context);
        assertTrue(context.isValid());
        assertNotNull(context.getSpan());
        assertEquals(otelSpan.getSpanContext().getSpanId(), context.getSpanId());
        assertEquals(otelSpan.getSpanContext().getTraceId(), context.getTraceId());
        assertEquals(otelSpan.getSpanContext().getTraceFlags().asHex(), context.getTraceFlags());
    }

    @Test
    @SuppressWarnings("try")
    public void createInstrumentationContextFromOTelSpanContext() {
        OpenTelemetry otel = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();

        SpanContext otelSpanContext = SpanContext.create("0123456789abcdef0123456789abcdef", "0123456789abcdef",
            TraceFlags.getSampled(), TraceState.builder().put("key", "value").build());

        InstrumentationContext context = Instrumentation.createInstrumentationContext(otelSpanContext);
        assertNotNull(context);
        assertTrue(context.isValid());
        assertNotNull(context.getSpan());
        assertEquals(otelSpanContext.getSpanId(), context.getSpanId());
        assertEquals(otelSpanContext.getTraceId(), context.getTraceId());
        assertEquals(otelSpanContext.getTraceFlags().asHex(), context.getTraceFlags());

        Tracer tracer
            = Instrumentation.create(new InstrumentationOptions().setTelemetryProvider(otel), DEFAULT_LIB_OPTIONS)
                .getTracer();
        Span span = tracer.spanBuilder("test", INTERNAL, context).startSpan();
        assertEquals(otelSpanContext.getTraceId(), span.getInstrumentationContext().getTraceId());

        try (TracingScope scope = span.makeCurrent()) {
            ReadableSpan readableSpan = (ReadableSpan) io.opentelemetry.api.trace.Span.current();
            assertEquals(otelSpanContext.getSpanId(), readableSpan.getParentSpanContext().getSpanId());

            TraceState traceState = readableSpan.getSpanContext().getTraceState();
            assertEquals("value", traceState.get("key"));
            assertEquals(1, traceState.size());
        }
    }

    @Test
    public void createInstrumentationContextFromCustomContext() {
        InstrumentationContext customContext = createRandomInstrumentationContext();

        InstrumentationContext context = Instrumentation.createInstrumentationContext(customContext);
        assertNotNull(context);
        assertTrue(context.isValid());
        assertNotNull(context.getSpan());
        assertEquals(customContext.getSpanId(), context.getSpanId());
        assertEquals(customContext.getTraceId(), context.getTraceId());
        assertEquals(customContext.getTraceFlags(), context.getTraceFlags());
    }

    @Test
    public void createInstrumentationContextFromInvalidContext() {
        InstrumentationContext customContext = createInvalidInstrumentationContext();

        InstrumentationContext context = Instrumentation.createInstrumentationContext(customContext);
        assertNotNull(context);
        assertFalse(context.isValid());
        assertNotNull(context.getSpan());
    }
}
