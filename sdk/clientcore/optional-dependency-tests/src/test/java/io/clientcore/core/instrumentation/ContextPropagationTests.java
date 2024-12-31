// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.implementation.instrumentation.otel.tracing.OTelSpan;
import io.clientcore.core.implementation.instrumentation.otel.tracing.OTelSpanContext;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.TraceContextGetter;
import io.clientcore.core.instrumentation.tracing.TraceContextPropagator;
import io.clientcore.core.instrumentation.tracing.Tracer;
import io.clientcore.core.util.Context;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.IdGenerator;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static io.clientcore.core.instrumentation.InstrumentationProvider.TRACE_CONTEXT_KEY;
import static io.clientcore.core.instrumentation.tracing.SpanKind.INTERNAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContextPropagationTests {
    private static final LibraryInstrumentationOptions DEFAULT_LIB_OPTIONS
        = new LibraryInstrumentationOptions("test-library");
    private static final TraceContextGetter<Map<String, String>> GETTER
        = new TraceContextGetter<Map<String, String>>() {
            @Override
            public Iterable<String> keys(Map<String, String> carrier) {
                return carrier.keySet();
            }

            @Override
            public String get(Map<String, String> carrier, String key) {
                return carrier.get(key);
            }
        };

    private InMemorySpanExporter exporter;
    private SdkTracerProvider tracerProvider;
    private InstrumentationOptions<OpenTelemetry> otelOptions;
    private Tracer tracer;
    private TraceContextPropagator contextPropagator;
    private InstrumentationProvider instrumentationProvider;

    @BeforeEach
    public void setUp() {
        exporter = InMemorySpanExporter.create();
        tracerProvider = SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(exporter)).build();

        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();
        otelOptions = new InstrumentationOptions<OpenTelemetry>().setProvider(openTelemetry);
        instrumentationProvider = InstrumentationProvider.create(otelOptions, DEFAULT_LIB_OPTIONS);
        tracer = instrumentationProvider.getTracer();
        contextPropagator = instrumentationProvider.getW3CTraceContextPropagator();
    }

    @AfterEach
    public void tearDown() {
        exporter.reset();
        tracerProvider.close();
    }

    @Test
    public void testInject() {
        Span span = tracer.spanBuilder("test-span", INTERNAL, null).startSpan();

        Map<String, String> carrier = new HashMap<>();
        contextPropagator.inject(Context.of(TRACE_CONTEXT_KEY, span), carrier, Map::put);

        assertEquals(getTraceparent(span), carrier.get("traceparent"));
        assertEquals(1, carrier.size());
    }

    @Test
    public void testInjectReplaces() {
        Span span = tracer.spanBuilder("test-span", INTERNAL, null).startSpan();

        Map<String, String> carrier = new HashMap<>();
        carrier.put("traceparent", "00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01");
        contextPropagator.inject(Context.of(TRACE_CONTEXT_KEY, span), carrier, Map::put);

        assertEquals(getTraceparent(span), carrier.get("traceparent"));
        assertEquals(1, carrier.size());
    }

    @Test
    public void testInjectNoContext() {
        Map<String, String> carrier = new HashMap<>();
        contextPropagator.inject(Context.none(), carrier, Map::put);

        assertNull(carrier.get("traceparent"));
        assertNull(carrier.get("tracestate"));
        assertEquals(0, carrier.size());
    }

    @Test
    public void testInjectWithTracestate() {
        TraceState traceState = TraceState.builder().put("k1", "v1").put("k2", "v2").build();
        SpanContext otelSpanContext = SpanContext.create(IdGenerator.random().generateTraceId(),
            IdGenerator.random().generateSpanId(), TraceFlags.getSampled(), traceState);

        io.opentelemetry.context.Context otelContext
            = io.opentelemetry.context.Context.root().with(io.opentelemetry.api.trace.Span.wrap(otelSpanContext));

        Map<String, String> carrier = new HashMap<>();
        contextPropagator.inject(Context.of(TRACE_CONTEXT_KEY, otelContext), carrier, Map::put);

        assertEquals(getTraceparent(otelSpanContext), carrier.get("traceparent"));
        assertEquals("k2=v2,k1=v1", carrier.get("tracestate"));
        assertEquals(2, carrier.size());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void testExtract(boolean isSampled) {
        Map<String, String> carrier = new HashMap<>();
        carrier.put("traceparent", "00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-" + (isSampled ? "01" : "00"));

        Context updated = contextPropagator.extract(Context.none(), carrier, GETTER);

        assertInstanceOf(io.opentelemetry.context.Context.class, updated.get(TRACE_CONTEXT_KEY));
        io.opentelemetry.context.Context otelContext
            = (io.opentelemetry.context.Context) updated.get(TRACE_CONTEXT_KEY);
        SpanContext extracted = io.opentelemetry.api.trace.Span.fromContext(otelContext).getSpanContext();
        assertTrue(extracted.isValid());
        assertEquals("0af7651916cd43dd8448eb211c80319c", extracted.getTraceId());
        assertEquals("b7ad6b7169203331", extracted.getSpanId());
        assertEquals(isSampled, extracted.isSampled());
    }

    @Test
    public void testExtractEmpty() {
        Map<String, String> carrier = new HashMap<>();

        Context updated = contextPropagator.extract(Context.none(), carrier, GETTER);

        assertInstanceOf(io.opentelemetry.context.Context.class, updated.get(TRACE_CONTEXT_KEY));

        io.opentelemetry.context.Context otelContext
            = (io.opentelemetry.context.Context) updated.get(TRACE_CONTEXT_KEY);
        SpanContext extracted = io.opentelemetry.api.trace.Span.fromContext(otelContext).getSpanContext();
        assertFalse(extracted.isValid());
    }

    @Test
    public void testExtractInvalid() {
        Map<String, String> carrier = new HashMap<>();
        carrier.put("traceparent", "00-traceId-spanId-01");

        Context updated = contextPropagator.extract(Context.none(), carrier, GETTER);

        assertInstanceOf(io.opentelemetry.context.Context.class, updated.get(TRACE_CONTEXT_KEY));

        io.opentelemetry.context.Context otelContext
            = (io.opentelemetry.context.Context) updated.get(TRACE_CONTEXT_KEY);
        assertFalse(io.opentelemetry.api.trace.Span.fromContext(otelContext).getSpanContext().isValid());
    }

    @Test
    public void testExtractPreservesContext() {
        Map<String, String> carrier = new HashMap<>();
        carrier.put("traceparent", "00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01");

        Context original = Context.of("key", "value");
        Context updated = contextPropagator.extract(original, carrier, GETTER);

        io.opentelemetry.context.Context otelContext
            = (io.opentelemetry.context.Context) updated.get(TRACE_CONTEXT_KEY);
        assertTrue(io.opentelemetry.api.trace.Span.fromContext(otelContext).getSpanContext().isValid());

        assertEquals("value", updated.get("key"));
    }

    private String getTraceparent(Span span) {
        OTelSpanContext spanContext = ((OTelSpan) span).getSpanContext();
        return "00-" + spanContext.getTraceId() + "-" + spanContext.getSpanId() + "-01";
    }

    private String getTraceparent(SpanContext spanContext) {
        return "00-" + spanContext.getTraceId() + "-" + spanContext.getSpanId() + "-01";
    }
}
