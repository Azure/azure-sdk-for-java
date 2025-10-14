// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.fallback;

import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.implementation.AccessibleByteArrayOutputStream;
import io.clientcore.core.instrumentation.Instrumentation;
import io.clientcore.core.instrumentation.InstrumentationAttributes;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.InstrumentationOptions;
import io.clientcore.core.instrumentation.SdkInstrumentationOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.logging.LogLevel;
import io.clientcore.core.instrumentation.metrics.DoubleHistogram;
import io.clientcore.core.instrumentation.metrics.LongCounter;
import io.clientcore.core.instrumentation.metrics.Meter;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.TraceContextGetter;
import io.clientcore.core.instrumentation.tracing.TraceContextPropagator;
import io.clientcore.core.instrumentation.tracing.Tracer;
import io.clientcore.core.instrumentation.tracing.TracingScope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.clientcore.core.instrumentation.logging.InstrumentationTestUtils.assertValidSpanId;
import static io.clientcore.core.instrumentation.logging.InstrumentationTestUtils.assertValidTraceId;
import static io.clientcore.core.instrumentation.logging.InstrumentationTestUtils.createRandomInstrumentationContext;
import static io.clientcore.core.instrumentation.logging.InstrumentationTestUtils.parseLogMessages;
import static io.clientcore.core.instrumentation.logging.InstrumentationTestUtils.setupLogLevelAndGetLogger;
import static io.clientcore.core.instrumentation.tracing.SpanKind.CLIENT;
import static io.clientcore.core.instrumentation.tracing.SpanKind.CONSUMER;
import static io.clientcore.core.instrumentation.tracing.SpanKind.INTERNAL;
import static io.clientcore.core.instrumentation.tracing.SpanKind.PRODUCER;
import static io.clientcore.core.instrumentation.tracing.SpanKind.SERVER;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FallbackInstrumentationTests {
    private static final SdkInstrumentationOptions DEFAULT_LIB_OPTIONS = new SdkInstrumentationOptions("test-library");
    private static final Instrumentation DEFAULT_INSTRUMENTATION = Instrumentation.create(null, DEFAULT_LIB_OPTIONS);
    private final AccessibleByteArrayOutputStream logCaptureStream;

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

    public FallbackInstrumentationTests() {
        logCaptureStream = new AccessibleByteArrayOutputStream();
    }

    @Test
    public void basicTracing() {
        Tracer tracer = DEFAULT_INSTRUMENTATION.getTracer();
        assertTrue(tracer.isEnabled());

        Span span = tracer.spanBuilder("test-span", INTERNAL, null).startSpan();

        assertValidSpan(span, false);

        testContextInjection(span.getInstrumentationContext(), DEFAULT_INSTRUMENTATION.getW3CTraceContextPropagator());

        span.end();
        assertEquals(0, parseLogMessages(logCaptureStream).size());
    }

    @Test
    public void basicTracingExplicitParentSpan() {
        Tracer tracer = DEFAULT_INSTRUMENTATION.getTracer();

        Span parent = tracer.spanBuilder("parent", INTERNAL, null).startSpan();
        Span child = tracer.spanBuilder("child", INTERNAL, parent.getInstrumentationContext()).startSpan();

        assertValidSpan(child, false);
        assertEquals(parent.getInstrumentationContext().getTraceId(), child.getInstrumentationContext().getTraceId());

        testContextInjection(child.getInstrumentationContext(), DEFAULT_INSTRUMENTATION.getW3CTraceContextPropagator());

        child.end();
        parent.end();
        assertEquals(0, parseLogMessages(logCaptureStream).size());
    }

    @Test
    @SuppressWarnings("try")
    public void basicTracingImplicitParentSpan() {
        Tracer tracer = DEFAULT_INSTRUMENTATION.getTracer();

        assertSame(Span.noop(), FallbackScope.getCurrentSpan());
        Span parent = tracer.spanBuilder("parent", INTERNAL, null).startSpan();
        try (TracingScope scope = parent.makeCurrent()) {
            Span child = tracer.spanBuilder("child", CLIENT, null).startSpan();

            assertValidSpan(child, false);
            assertEquals(parent.getInstrumentationContext().getTraceId(),
                child.getInstrumentationContext().getTraceId());
            assertSame(parent, FallbackScope.getCurrentSpan());
            assertNotSame(Span.noop(), FallbackScope.getCurrentSpan());
            child.end();
        }
        parent.end();
        assertSame(Span.noop(), FallbackScope.getCurrentSpan());
        assertEquals(0, parseLogMessages(logCaptureStream).size());
    }

    @Test
    @SuppressWarnings("try")
    public void basicTracingExplicitAndImplicitParentSpan() {
        Tracer tracer = DEFAULT_INSTRUMENTATION.getTracer();

        Span span = tracer.spanBuilder("span", INTERNAL, null).startSpan();
        try (TracingScope scope = span.makeCurrent()) {
            InstrumentationContext parentContext = createRandomInstrumentationContext();

            Span child = tracer.spanBuilder("child", CLIENT, parentContext).startSpan();
            assertValidSpan(child, false);
            try (TracingScope childScope = child.makeCurrent()) {
                assertSame(child, FallbackScope.getCurrentSpan());
            }
            assertSame(span, FallbackScope.getCurrentSpan());

            assertEquals(parentContext.getTraceId(), child.getInstrumentationContext().getTraceId());
            assertNotEquals(span.getInstrumentationContext().getTraceId(),
                child.getInstrumentationContext().getTraceId());

            child.end();
        }
        span.end();

        assertSame(Span.noop(), FallbackScope.getCurrentSpan());
        assertEquals(0, parseLogMessages(logCaptureStream).size());
    }

    @Test
    @SuppressWarnings("try")
    public void tracingImplicitParentSpan() {
        Tracer tracer = DEFAULT_INSTRUMENTATION.getTracer();

        Span parent = tracer.spanBuilder("parent", INTERNAL, null).startSpan();
        try (TracingScope scope = parent.makeCurrent()) {
            Span child1 = tracer.spanBuilder("child1", CLIENT, null).startSpan();
            try (TracingScope childScope1 = child1.makeCurrent()) {
                assertSame(child1, FallbackScope.getCurrentSpan());
            }

            assertSame(parent, FallbackScope.getCurrentSpan());
            try (TracingScope childScope1 = child1.makeCurrent()) {
                assertSame(child1, FallbackScope.getCurrentSpan());
            }

            Span child2 = tracer.spanBuilder("child2", CLIENT, null).startSpan();
            try (TracingScope childScope2 = child2.makeCurrent()) {
                assertSame(child2, FallbackScope.getCurrentSpan());
                Span grandChild = tracer.spanBuilder("grandChild", CLIENT, null).startSpan();
                try (TracingScope grandChildScope = grandChild.makeCurrent()) {
                    assertSame(grandChild, FallbackScope.getCurrentSpan());
                }
                assertSame(child2, FallbackScope.getCurrentSpan());
            }
            assertSame(parent, FallbackScope.getCurrentSpan());
        }
        parent.end();
        assertEquals(0, parseLogMessages(logCaptureStream).size());
    }

    @Test
    public void testWrongScopeClosure() {
        Tracer tracer = DEFAULT_INSTRUMENTATION.getTracer();

        Span span1 = tracer.spanBuilder("span1", INTERNAL, null).startSpan();
        TracingScope scope1 = span1.makeCurrent();

        Span span2 = tracer.spanBuilder("span2", INTERNAL, null).startSpan();
        TracingScope scope2 = span2.makeCurrent();

        assertSame(span2, FallbackScope.getCurrentSpan());

        // should be noop - this span is not current on this thread
        scope1.close();
        assertSame(span2, FallbackScope.getCurrentSpan());

        scope2.close();
        assertSame(span1, FallbackScope.getCurrentSpan());

        scope1.close();
        assertSame(Span.noop(), FallbackScope.getCurrentSpan());
    }

    @Test
    public void basicTracingExplicitParentContext() {
        Tracer tracer = DEFAULT_INSTRUMENTATION.getTracer();

        InstrumentationContext parentContext = createRandomInstrumentationContext();
        Span child = tracer.spanBuilder("parent", INTERNAL, parentContext).startSpan();

        assertValidSpan(child, false);
        assertEquals(parentContext.getTraceId(), child.getInstrumentationContext().getTraceId());

        testContextInjection(child.getInstrumentationContext(), DEFAULT_INSTRUMENTATION.getW3CTraceContextPropagator());

        child.end();
        assertEquals(0, parseLogMessages(logCaptureStream).size());
    }

    @Test
    public void testEmptyContextExtraction() {
        TraceContextPropagator propagator = DEFAULT_INSTRUMENTATION.getW3CTraceContextPropagator();

        Map<String, String> carrier = new HashMap<>();
        carrier.put("random-key", "random-value");

        InstrumentationContext context = propagator.extract(null, carrier, GETTER);

        assertNotNull(context);
        assertFalse(context.isValid());
        assertEquals("00", context.getTraceFlags());
        assertEquals(RandomIdUtils.INVALID_SPAN_ID, context.getSpanId());
        assertEquals(RandomIdUtils.INVALID_TRACE_ID, context.getTraceId());

        assertArrayEquals(new String[] { "random-key" }, carrier.keySet().toArray());
    }

    @Test
    public void testValidContextExtraction() {
        TraceContextPropagator propagator = DEFAULT_INSTRUMENTATION.getW3CTraceContextPropagator();

        Map<String, String> carrier = new HashMap<>();
        carrier.put("random-key", "random-value");
        carrier.put("traceparent", "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01");

        InstrumentationContext context = propagator.extract(null, carrier, GETTER);

        assertNotNull(context);
        assertTrue(context.isValid());
        assertEquals("01", context.getTraceFlags());
        assertEquals("00f067aa0ba902b7", context.getSpanId());
        assertEquals("4bf92f3577b34da6a3ce929d0e0e4736", context.getTraceId());

        assertArrayEquals(new String[] { "random-key", "traceparent" }, carrier.keySet().toArray());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "",
            "a random string",
            "4bf92f3577b34da6a3ce929d0e0e4736",
            "4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7",
            "01-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01",
            "0z-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01",
            "00--00f067aa0ba902b7-01",
            "00-29d0e0e4736-00f067aa0ba902b7-01",
            "00-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx-00f067aa0ba902b7-01",
            "00-00000000000000000000000000000000-00f067aa0ba902b7-01",
            "00-000004bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01",
            "00-4bf92f3577b34da6a3ce929d0e0e4736--01",
            "00-4bf92f3577b34da6a3ce929d0e0e4736-902b7-01",
            "00-4bf92f3577b34da6a3ce929d0e0e4736-zzzzzzzzzzzzzzzz-01",
            "00-4bf92f3577b34da6a3ce929d0e0e4736-0000000000000000-01",
            "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7--",
            "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-0y",
            "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-0000", })
    public void testInvalidContextExtraction(String invalidTraceparent) {
        TraceContextPropagator propagator = DEFAULT_INSTRUMENTATION.getW3CTraceContextPropagator();

        Map<String, String> carrier = new HashMap<>();
        carrier.put("traceparent", invalidTraceparent);

        InstrumentationContext context = propagator.extract(null, carrier, GETTER);

        assertNotNull(context);
        assertFalse(context.isValid());
        assertEquals("00", context.getTraceFlags());
        assertEquals(RandomIdUtils.INVALID_SPAN_ID, context.getSpanId());
        assertEquals(RandomIdUtils.INVALID_TRACE_ID, context.getTraceId());
    }

    @ParameterizedTest
    @MethodSource("instrumentationContextSource")
    public void testIncomingContextIsIgnored(InstrumentationContext source) {
        TraceContextPropagator propagator = DEFAULT_INSTRUMENTATION.getW3CTraceContextPropagator();

        Map<String, String> carrier = new HashMap<>();
        carrier.put("traceparent", "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01");

        InstrumentationContext context = propagator.extract(source, carrier, GETTER);

        assertNotNull(context);
        assertTrue(context.isValid());
        assertEquals("01", context.getTraceFlags());
        assertEquals("00f067aa0ba902b7", context.getSpanId());
        assertEquals("4bf92f3577b34da6a3ce929d0e0e4736", context.getTraceId());
    }

    public static Stream<InstrumentationContext> instrumentationContextSource() {
        return Stream.of(createRandomInstrumentationContext(), FallbackSpanContext.INVALID,
            new FallbackSpanContext("4000000577b34da6a3ce9000000e4736", "00f0611111a902b7", "00", true, Span.noop()),
            new FallbackSpanContext("", "", "42", true, Span.noop()));
    }

    @Test
    @SuppressWarnings("try")
    public void basicTracingDisabledTests() {
        InstrumentationOptions options = new InstrumentationOptions().setTracingEnabled(false);
        Instrumentation instrumentation = Instrumentation.create(options, DEFAULT_LIB_OPTIONS);

        Tracer tracer = instrumentation.getTracer();
        assertFalse(tracer.isEnabled());

        // should not throw
        Span span = tracer.spanBuilder("test-span", INTERNAL, null).setAttribute("test-key", "test-value").startSpan();

        span.setAttribute("test-key2", "test-value2");
        span.setError("test-error");

        try (TracingScope scope = span.makeCurrent()) {
            assertSame(Span.noop(), FallbackScope.getCurrentSpan());
        }

        assertNotNull(span);
        assertNotNull(span.getInstrumentationContext());
        assertFalse(span.getInstrumentationContext().isValid());

        assertSame(Span.noop(), span);
        assertFalse(span.isRecording());
        testContextInjection(span.getInstrumentationContext(), instrumentation.getW3CTraceContextPropagator());

        span.end();
    }

    @Test
    public void createTracerUnknownProvider() {
        // should not throw
        InstrumentationOptions options
            = new InstrumentationOptions().setTelemetryProvider("this is not a valid provider");
        Instrumentation instrumentation = Instrumentation.create(options, DEFAULT_LIB_OPTIONS);
        Tracer tracer = instrumentation.getTracer();
        assertTrue(tracer.isEnabled());
    }

    @Test
    public void createInstrumentationBadOptions() {
        assertThrows(NullPointerException.class, () -> Instrumentation.create(new InstrumentationOptions(), null));
    }

    @ParameterizedTest
    @MethodSource("logLevels")
    public void basicTracingLogsLevel(LogLevel logLevel, boolean expectLogs) {
        ClientLogger logger = setupLogLevelAndGetLogger(logLevel, logCaptureStream);
        InstrumentationOptions options = new InstrumentationOptions().setTelemetryProvider(logger);
        Instrumentation instrumentation = Instrumentation.create(options, DEFAULT_LIB_OPTIONS);
        Tracer tracer = instrumentation.getTracer();

        Span span = tracer.spanBuilder("test-span", INTERNAL, null).startSpan();
        assertEquals(expectLogs, span.isRecording());
        span.end();

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        assertEquals(expectLogs ? 1 : 0, logMessages.size());
        if (expectLogs) {
            assertSpanLog(logMessages.get(0), "test-span", "INTERNAL", span.getInstrumentationContext(), null);
        }
    }

    @Test
    public void testSetAllAttributes() {
        Map<String, Object> start = new HashMap<>();
        start.put("string", "value");
        start.put("int", 42);
        start.put("double", 0.42);
        start.put("float", 4.2f);
        start.put("boolean", true);
        start.put("long", Long.MAX_VALUE);

        ClientLogger logger = setupLogLevelAndGetLogger(LogLevel.VERBOSE, logCaptureStream);
        InstrumentationOptions options = new InstrumentationOptions().setTelemetryProvider(logger);
        InstrumentationAttributes startAttributes = DEFAULT_INSTRUMENTATION.createAttributes(start);
        Instrumentation instrumentation = Instrumentation.create(options, DEFAULT_LIB_OPTIONS);
        Tracer tracer = instrumentation.getTracer();

        Span span = tracer.spanBuilder("test-span", INTERNAL, null).setAllAttributes(startAttributes).startSpan();
        span.end();

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);

        assertEquals(1, logMessages.size());
        Map<String, Object> loggedSpan = logMessages.get(0);
        assertEquals(12, loggedSpan.size());
        assertEquals("value", loggedSpan.get("string"));
        assertEquals(42, loggedSpan.get("int"));
        assertEquals(Long.MAX_VALUE, loggedSpan.get("long"));
        assertEquals(0.42, loggedSpan.get("double"));
        assertEquals(4.2d, (Double) loggedSpan.get("float"), 0.1);
        assertEquals(true, loggedSpan.get("boolean"));
    }

    public static Stream<Arguments> logLevels() {
        return Stream.of(Arguments.of(LogLevel.ERROR, false), Arguments.of(LogLevel.WARNING, false),
            Arguments.of(LogLevel.INFORMATIONAL, false), Arguments.of(LogLevel.VERBOSE, true));
    }

    @Test
    public void basicTracingLogsEnabled() {
        ClientLogger logger = setupLogLevelAndGetLogger(LogLevel.VERBOSE, logCaptureStream);
        InstrumentationOptions options = new InstrumentationOptions().setTelemetryProvider(logger);
        Instrumentation instrumentation = Instrumentation.create(options, DEFAULT_LIB_OPTIONS);
        Tracer tracer = instrumentation.getTracer();

        long startTime = System.nanoTime();
        Span span = tracer.spanBuilder("test-span", INTERNAL, null).startSpan();

        assertValidSpan(span, true);
        testContextInjection(span.getInstrumentationContext(), instrumentation.getW3CTraceContextPropagator());

        span.end();
        Duration duration = Duration.ofNanos(System.nanoTime() - startTime);

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        assertEquals(1, logMessages.size());
        Map<String, Object> loggedSpan = logMessages.get(0);
        assertSpanLog(loggedSpan, "test-span", "INTERNAL", span.getInstrumentationContext(), null);
        assertTrue((Double) loggedSpan.get("span.duration") <= duration.toNanos() / 1_000_000.0);

        // lib info is null since custom logger is provided, we can't add global context.
        // we'll add it in user app in common case
        assertNull(loggedSpan.get("library.name"));
        assertNull(loggedSpan.get("library.version"));
    }

    @Test
    public void tracingWithAttributesLogsEnabled() {
        ClientLogger logger = setupLogLevelAndGetLogger(LogLevel.VERBOSE, logCaptureStream);
        InstrumentationOptions options = new InstrumentationOptions().setTelemetryProvider(logger);
        Tracer tracer = Instrumentation.create(options, DEFAULT_LIB_OPTIONS).getTracer();

        Span span = tracer.spanBuilder("test-span", PRODUCER, null)
            .setAttribute("builder-string-key", "builder-value")
            .setAttribute("builder-int-key", 42)
            .setAttribute("builder-long-key", 420L)
            .setAttribute("builder-double-key", 4.2)
            .setAttribute("builder-boolean-key", true)
            .startSpan();
        span.setAttribute("span-string-key", "span-value")
            .setAttribute("span-int-key", 42)
            .setAttribute("span-long-key", 420L)
            .setAttribute("span-double-key", 4.2)
            .setAttribute("span-boolean-key", false)
            .setError("test-error");

        span.end();

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        assertEquals(1, logMessages.size());
        Map<String, Object> loggedSpan = logMessages.get(0);
        assertSpanLog(loggedSpan, "test-span", "PRODUCER", span.getInstrumentationContext(), "test-error");
        assertEquals("builder-value", loggedSpan.get("builder-string-key"));
        assertEquals(42, loggedSpan.get("builder-int-key"));
        assertEquals(420, loggedSpan.get("builder-long-key"));
        assertEquals(4.2, loggedSpan.get("builder-double-key"));
        assertEquals(true, loggedSpan.get("builder-boolean-key"));
        assertEquals("span-value", loggedSpan.get("span-string-key"));
        assertEquals(42, loggedSpan.get("span-int-key"));
        assertEquals(420, loggedSpan.get("span-long-key"));
        assertEquals(4.2, loggedSpan.get("span-double-key"));
        assertEquals(false, loggedSpan.get("span-boolean-key"));
    }

    @Test
    public void tracingWithExceptionLogsEnabled() {
        ClientLogger logger = setupLogLevelAndGetLogger(LogLevel.VERBOSE, logCaptureStream);
        InstrumentationOptions options = new InstrumentationOptions().setTelemetryProvider(logger);
        Tracer tracer = Instrumentation.create(options, DEFAULT_LIB_OPTIONS).getTracer();

        Span span = tracer.spanBuilder("test-span", SERVER, null).startSpan();

        IOException exception = new IOException("test-exception");
        span.end(exception);

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        assertEquals(1, logMessages.size());
        Map<String, Object> loggedSpan = logMessages.get(0);
        assertSpanLog(loggedSpan, "test-span", "SERVER", span.getInstrumentationContext(),
            exception.getClass().getCanonicalName());
    }

    @Test
    public void tracingLogsEnabledParent() {
        ClientLogger logger = setupLogLevelAndGetLogger(LogLevel.VERBOSE, logCaptureStream);
        InstrumentationOptions options = new InstrumentationOptions().setTelemetryProvider(logger);
        Tracer tracer = Instrumentation.create(options, DEFAULT_LIB_OPTIONS).getTracer();

        Span parent = tracer.spanBuilder("parent", CONSUMER, null).startSpan();
        Span child = tracer.spanBuilder("child", CLIENT, parent.getInstrumentationContext()).startSpan();
        parent.end();
        child.end();

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        assertEquals(2, logMessages.size());
        Map<String, Object> parentLog = logMessages.get(0);
        Map<String, Object> childLog = logMessages.get(1);
        assertSpanLog(parentLog, "parent", "CONSUMER", parent.getInstrumentationContext(), null);
        assertSpanLog(childLog, "child", "CLIENT", child.getInstrumentationContext(), null);
        assertEquals(childLog.get("span.parent.id"), parentLog.get("span.id"));
        assertEquals(childLog.get("trace.id"), parentLog.get("trace.id"));
    }

    @Test
    public void testCreateInstrumentationContextFromSpan() {
        Tracer tracer = DEFAULT_INSTRUMENTATION.getTracer();

        Span span = tracer.spanBuilder("span", CONSUMER, null).startSpan();
        InstrumentationContext fromSpan = Instrumentation.createInstrumentationContext(span);
        assertEquals(span.getInstrumentationContext().getTraceId(), fromSpan.getTraceId());
        assertEquals(span.getInstrumentationContext().getSpanId(), fromSpan.getSpanId());
        assertEquals(span.getInstrumentationContext().getTraceFlags(), fromSpan.getTraceFlags());
        assertEquals(span.getInstrumentationContext().isValid(), fromSpan.isValid());
        assertSame(span, fromSpan.getSpan());
    }

    @Test
    public void testCreateInstrumentationContextFromAnotherContext() {
        InstrumentationContext testContext = createRandomInstrumentationContext();
        InstrumentationContext fromTestContext = Instrumentation.createInstrumentationContext(testContext);
        assertEquals(testContext.getTraceId(), fromTestContext.getTraceId());
        assertEquals(testContext.getSpanId(), fromTestContext.getSpanId());
        assertEquals(testContext.getTraceFlags(), fromTestContext.getTraceFlags());
        assertEquals(testContext.isValid(), fromTestContext.isValid());
        assertSame(Span.noop(), fromTestContext.getSpan());
    }

    @ParameterizedTest
    @MethodSource("notSupportedContexts")
    public void testCreateInstrumentationContextNotSupported(Object context) {
        InstrumentationContext fromNull = Instrumentation.createInstrumentationContext(context);
        assertEquals(RandomIdUtils.INVALID_TRACE_ID, fromNull.getTraceId());
        assertEquals(RandomIdUtils.INVALID_SPAN_ID, fromNull.getSpanId());
        assertEquals("00", fromNull.getTraceFlags());
        assertFalse(fromNull.isValid());
        assertSame(Span.noop(), fromNull.getSpan());
    }

    @Test
    public void testCreateMeterAndInstruments() {
        Meter meter = DEFAULT_INSTRUMENTATION.getMeter();
        assertFalse(meter.isEnabled());

        InstrumentationAttributes attributes = DEFAULT_INSTRUMENTATION.createAttributes(Collections.emptyMap());
        DoubleHistogram histogram = meter.createDoubleHistogram("test", "description", "1", null);
        histogram.record(42.0, attributes, null);
        assertFalse(histogram.isEnabled());

        LongCounter counter = meter.createLongCounter("test", "description", "1");
        counter.add(42, attributes, null);
        assertFalse(counter.isEnabled());

        LongCounter upDownCounter = meter.createLongUpDownCounter("test", "description", "1");
        upDownCounter.add(42, attributes, null);
        assertFalse(upDownCounter.isEnabled());
    }

    @Test
    public void testInvalidParams() {
        Meter meter = DEFAULT_INSTRUMENTATION.getMeter();

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
    public void testGetAttributes() {
        InstrumentationAttributes attributes = DEFAULT_INSTRUMENTATION.createAttributes(Collections.emptyMap());
        assertNotNull(attributes);

        // does not throw
        attributes.put("key", "value1");
        attributes.put("key", "value2");
        DEFAULT_INSTRUMENTATION.createAttributes(null);
    }

    @Test
    public void testAttributesInvalidParams() {
        assertThrows(NullPointerException.class,
            () -> DEFAULT_INSTRUMENTATION.createAttributes(Collections.singletonMap(null, "value")));
        assertThrows(NullPointerException.class,
            () -> DEFAULT_INSTRUMENTATION.createAttributes(Collections.singletonMap("key", null)));
        InstrumentationAttributes attributes = DEFAULT_INSTRUMENTATION.createAttributes(null);
        assertThrows(NullPointerException.class, () -> attributes.put(null, "value"));
        assertThrows(NullPointerException.class, () -> attributes.put("key", null));
    }

    @Test
    public void testCreateAttributes() {
        Instrumentation instrumentation = Instrumentation.create(null, DEFAULT_LIB_OPTIONS);
        InstrumentationAttributes attributes = instrumentation.createAttributes(Collections.emptyMap());
        assertInstanceOf(FallbackAttributes.class, attributes);

        // does not throw
        attributes.put("key", "value1");
        attributes.put("key", "value2");
        instrumentation.createAttributes(null);
    }

    @Test
    public void testAttributes() {
        Instrumentation instrumentation = Instrumentation.create(null, DEFAULT_LIB_OPTIONS);
        Map<String, Object> start = new HashMap<>();
        start.put("string", "value");
        start.put("int", 42);
        start.put("double", 0.42);
        start.put("float", 4.2f);
        start.put("boolean", true);
        start.put("long", 420L);

        InstrumentationAttributes attributes = instrumentation.createAttributes(start);
        assertInstanceOf(FallbackAttributes.class, attributes);

        Map<String, Object> attrs = ((FallbackAttributes) attributes).getAttributes();
        assertEquals(6, attrs.size());
        assertEquals("value", attrs.get("string"));
        assertEquals(42, attrs.get("int"));
        assertEquals(420L, attrs.get("long"));
        assertEquals(0.42, attrs.get("double"));
        assertEquals(4.2f, (Float) attrs.get("float"), 0.1);
        assertEquals(true, attrs.get("boolean"));

        InstrumentationAttributes attributes2 = attributes.put("string2", "value2");

        assertNotSame(attributes, attributes2);
        assertNull(attrs.get("string2"));
        assertEquals(6, attrs.size());

        attributes2 = attributes2.put("int2", 24)
            .put("double2", 0.24)
            .put("float2", 2.4f)
            .put("boolean2", false)
            .put("long2", 240L);

        attrs = ((FallbackAttributes) attributes2).getAttributes();
        assertEquals(12, attrs.size());
        assertEquals("value2", attrs.get("string2"));
        assertEquals(24, attrs.get("int2"));
        assertEquals(240L, attrs.get("long2"));
        assertEquals(0.24, attrs.get("double2"));
        assertEquals(2.4f, (Float) attrs.get("float2"), 0.1);
        assertEquals(false, attrs.get("boolean2"));
    }

    @Test
    public void testDuplicates() {
        Instrumentation instrumentation = Instrumentation.create(null, DEFAULT_LIB_OPTIONS);
        Map<String, Object> start = new HashMap<>();
        start.put("string", "value1");
        start.put("string", "value2");

        InstrumentationAttributes attributes
            = instrumentation.createAttributes(start).put("string", "value3").put("string", "value4");

        Map<String, Object> attrs = ((FallbackAttributes) attributes).getAttributes();
        assertEquals(1, attrs.size());
        assertEquals("value4", attrs.get("string"));
    }

    public static Stream<Object> notSupportedContexts() {
        return Stream.of(null, new Object(), "this is not a valid context", RequestContext.none());
    }

    private static void assertValidSpan(Span span, boolean isRecording) {
        assertNotNull(span.getInstrumentationContext());
        assertTrue(span.getInstrumentationContext().isValid());
        assertValidSpanId(span.getInstrumentationContext().getSpanId());
        assertValidTraceId(span.getInstrumentationContext().getTraceId());
        assertEquals(isRecording ? "01" : "00", span.getInstrumentationContext().getTraceFlags());
        assertEquals(isRecording, span.isRecording());
    }

    private static void assertSpanLog(Map<String, Object> loggedSpan, String spanName, String spanKind,
        InstrumentationContext context, String errorType) {
        assertEquals("span.ended", loggedSpan.get("event.name"));
        assertEquals(spanName, loggedSpan.get("span.name"));
        assertEquals(spanKind, loggedSpan.get("span.kind"));
        assertEquals(context.getTraceId(), loggedSpan.get("trace.id"));
        assertEquals(context.getSpanId(), loggedSpan.get("span.id"));

        assertInstanceOf(Double.class, loggedSpan.get("span.duration"));
        double durationMs = (Double) loggedSpan.get("span.duration");
        assertTrue(durationMs > 0);
        assertEquals(errorType, loggedSpan.get("error.type"));
    }

    private void testContextInjection(InstrumentationContext context, TraceContextPropagator propagator) {
        Map<String, String> carrier = new HashMap<>();
        propagator.inject(context, carrier, Map::put);

        if (context.isValid()) {
            assertFalse(carrier.isEmpty());
            assertEquals("00-" + context.getTraceId() + "-" + context.getSpanId() + "-" + context.getTraceFlags(),
                carrier.get("traceparent"));
        } else {
            assertTrue(carrier.isEmpty());
        }
    }
}
