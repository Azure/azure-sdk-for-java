// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.implementation.instrumentation.otel.OTelAttributes;
import io.clientcore.core.instrumentation.metrics.DoubleHistogram;
import io.clientcore.core.instrumentation.metrics.LongCounter;
import io.clientcore.core.instrumentation.metrics.Meter;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.Tracer;
import io.clientcore.core.instrumentation.tracing.TracingScope;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.clientcore.core.instrumentation.logging.InstrumentationTestUtils.createInvalidInstrumentationContext;
import static io.clientcore.core.instrumentation.logging.InstrumentationTestUtils.createRandomInstrumentationContext;
import static io.clientcore.core.instrumentation.tracing.SpanKind.INTERNAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Isolated
@Execution(ExecutionMode.SAME_THREAD)
public class InstrumentationTests {
    private static final SdkInstrumentationOptions DEFAULT_SDK_OPTIONS = new SdkInstrumentationOptions("test-library");
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
        Tracer tracer = Instrumentation.create(null, DEFAULT_SDK_OPTIONS).getTracer();
        assertFalse(tracer.isEnabled());
    }

    @Test
    public void createTracerTracingDisabled() {
        OpenTelemetry otel = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).buildAndRegisterGlobal();

        InstrumentationOptions options
            = new InstrumentationOptions().setTracingEnabled(false).setTelemetryProvider(otel);

        Tracer tracer = Instrumentation.create(options, DEFAULT_SDK_OPTIONS).getTracer();
        assertFalse(tracer.isEnabled());
        tracer.spanBuilder("test", INTERNAL, null).startSpan().end();

        assertEquals(0, exporter.getFinishedSpanItems().size());
    }

    @SuppressWarnings("try")
    @Test
    public void createTracerGlobalOTel() throws Exception {
        try (AutoCloseable otel
            = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).buildAndRegisterGlobal()) {

            Instrumentation instrumentation = Instrumentation.create(null, DEFAULT_SDK_OPTIONS);
            Tracer tracer = instrumentation.getTracer();
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
                .create(new InstrumentationOptions().setTelemetryProvider(localOTel), DEFAULT_SDK_OPTIONS)
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
            () -> Instrumentation.create(options, DEFAULT_SDK_OPTIONS).getTracer());
        assertThrows(NullPointerException.class, () -> Instrumentation.create(null, null).getTracer());
    }

    @SuppressWarnings("try")
    @Test
    public void createTracerWithLibInfo() throws Exception {
        try (AutoCloseable otel
            = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).buildAndRegisterGlobal()) {

            SdkInstrumentationOptions sdkOptions = new SdkInstrumentationOptions("test-library").setSdkVersion("1.0.0")
                .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0");

            Tracer tracer = Instrumentation.create(null, sdkOptions).getTracer();
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
            = Instrumentation.create(new InstrumentationOptions().setTelemetryProvider(otel), DEFAULT_SDK_OPTIONS)
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
            = Instrumentation.create(new InstrumentationOptions().setTelemetryProvider(otel), DEFAULT_SDK_OPTIONS)
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

    @Test
    public void testCreateMeterAndInstruments() {
        Instrumentation instrumentation = Instrumentation.create(null, DEFAULT_SDK_OPTIONS);
        Meter meter = instrumentation.getMeter();
        assertTrue(meter.isEnabled());

        InstrumentationAttributes attributes = instrumentation.createAttributes(Collections.emptyMap());
        DoubleHistogram histogram = meter.createDoubleHistogram("test", "description", "By", null);
        histogram.record(42.0, attributes, null);
        assertTrue(histogram.isEnabled());

        LongCounter counter = meter.createLongCounter("test", "description", "1");
        counter.add(42, attributes, null);
        assertTrue(counter.isEnabled());

        LongCounter upDownCounter = meter.createLongUpDownCounter("test", "description", "1");
        upDownCounter.add(42, attributes, null);
        assertTrue(upDownCounter.isEnabled());
    }

    @Test
    public void testInvalidParams() {
        Instrumentation instrumentation = Instrumentation.create(null, DEFAULT_SDK_OPTIONS);
        Meter meter = instrumentation.getMeter();

        assertThrows(NullPointerException.class, () -> meter.createDoubleHistogram("test", null, "1", null));
        assertThrows(NullPointerException.class, () -> meter.createLongCounter("test", null, "1"));
        assertThrows(NullPointerException.class, () -> meter.createLongUpDownCounter("test", null, "1"));
        assertThrows(NullPointerException.class, () -> meter.createDoubleHistogram(null, "description", "1", null));
        assertThrows(NullPointerException.class, () -> meter.createLongCounter(null, "description", "1"));
        assertThrows(NullPointerException.class, () -> meter.createLongUpDownCounter(null, "description", "1"));
        assertThrows(NullPointerException.class, () -> meter.createDoubleHistogram("test", "description", null, null));
        assertThrows(NullPointerException.class, () -> meter.createLongCounter("test", "description", null));
        assertThrows(NullPointerException.class, () -> meter.createLongUpDownCounter("test", "description", null));

        DoubleHistogram histogram = meter.createDoubleHistogram("test", "description", "1", null);
        assertThrows(NullPointerException.class, () -> histogram.record(42.0, null, null));

        LongCounter counter = meter.createLongCounter("test", "description", "1");
        assertThrows(NullPointerException.class, () -> counter.add(42, null, null));

        LongCounter upDownCounter = meter.createLongUpDownCounter("test", "description", "1");
        assertThrows(NullPointerException.class, () -> upDownCounter.add(42, null, null));
    }

    @Test
    public void testCreateAttributes() {
        Instrumentation instrumentation = Instrumentation.create(null, DEFAULT_SDK_OPTIONS);
        InstrumentationAttributes attributes = instrumentation.createAttributes(Collections.emptyMap());
        assertInstanceOf(OTelAttributes.class, attributes);

        // does not throw
        attributes.put("key", "value1");
        attributes.put("key", "value2");
        instrumentation.createAttributes(null);
    }

    @Test
    public void testAttributesInvalidParams() {
        Instrumentation instrumentation = Instrumentation.create(null, DEFAULT_SDK_OPTIONS);
        assertThrows(NullPointerException.class,
            () -> instrumentation.createAttributes(Collections.singletonMap(null, "value")));
        assertThrows(NullPointerException.class,
            () -> instrumentation.createAttributes(Collections.singletonMap("key", null)));
        InstrumentationAttributes attributes = instrumentation.createAttributes(null);
        assertThrows(NullPointerException.class, () -> attributes.put(null, "value"));
        assertThrows(NullPointerException.class, () -> attributes.put("key", null));
    }

    @Test
    public void testAttributes() {
        Instrumentation instrumentation = Instrumentation.create(null, DEFAULT_SDK_OPTIONS);
        Map<String, Object> start = new HashMap<>();
        start.put("string", "value");
        start.put("int", 42);
        start.put("double", 0.42);
        start.put("float", 4.2f);
        start.put("boolean", true);
        start.put("long", 420L);

        InstrumentationAttributes attributes = instrumentation.createAttributes(start);
        OTelAttributes otelAttributes = (OTelAttributes) attributes;
        Object otelAttrs = otelAttributes.getOTelAttributes();
        assertInstanceOf(Attributes.class, otelAttrs);

        Attributes attrs = (Attributes) otelAttrs;
        assertEquals(6, attrs.size());
        assertEquals("value", attrs.get(AttributeKey.stringKey("string")));
        assertEquals(42L, attrs.get(AttributeKey.longKey("int")));
        assertEquals(420L, attrs.get(AttributeKey.longKey("long")));
        assertEquals(0.42, attrs.get(AttributeKey.doubleKey("double")));
        assertEquals(4.2f, attrs.get(AttributeKey.doubleKey("float")), 0.1);
        assertEquals(true, attrs.get(AttributeKey.booleanKey("boolean")));

        InstrumentationAttributes attributes2 = attributes.put("string2", "value2");

        assertNotSame(attributes, attributes2);
        assertNull(attrs.get(AttributeKey.stringKey("string2")));
        assertEquals(6, attrs.size());

        attributes2 = attributes2.put("int2", 24)
            .put("double2", 0.24)
            .put("float2", 2.4f)
            .put("boolean2", false)
            .put("long2", 240L);

        attrs = (Attributes) ((OTelAttributes) attributes2).getOTelAttributes();
        assertEquals(12, attrs.size());
        assertEquals("value2", attrs.get(AttributeKey.stringKey("string2")));
        assertEquals(24L, attrs.get(AttributeKey.longKey("int2")));
        assertEquals(240L, attrs.get(AttributeKey.longKey("long2")));
        assertEquals(0.24, attrs.get(AttributeKey.doubleKey("double2")));
        assertEquals(2.4f, attrs.get(AttributeKey.doubleKey("float2")), 0.1);
        assertEquals(false, attrs.get(AttributeKey.booleanKey("boolean2")));
    }

    @Test
    public void testDuplicates() {
        Instrumentation instrumentation = Instrumentation.create(null, DEFAULT_SDK_OPTIONS);
        Map<String, Object> start = new HashMap<>();
        start.put("string", "value1");
        start.put("string", "value2");

        InstrumentationAttributes attributes
            = instrumentation.createAttributes(start).put("string", "value3").put("string", "value4");
        Object otelAttrs = ((OTelAttributes) attributes).getOTelAttributes();

        Attributes attrs = (Attributes) otelAttrs;
        assertEquals(1, attrs.size());
        assertEquals("value4", attrs.get(AttributeKey.stringKey("string")));
    }
}
