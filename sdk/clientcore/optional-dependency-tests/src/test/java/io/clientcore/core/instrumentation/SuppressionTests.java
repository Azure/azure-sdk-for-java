// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.clientcore.core.instrumentation.tracing.Tracer;
import io.clientcore.core.instrumentation.tracing.TracingScope;
import io.clientcore.core.models.binarydata.BinaryData;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.testing.time.TestClock;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.stream.Stream;

import static io.clientcore.core.instrumentation.tracing.SpanKind.CLIENT;
import static io.clientcore.core.instrumentation.tracing.SpanKind.INTERNAL;
import static io.clientcore.core.instrumentation.tracing.SpanKind.PRODUCER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SuppressionTests {
    private static final long SECOND_NANOS = 1_000_000_000;

    private InMemorySpanExporter exporter;
    private SdkTracerProvider tracerProvider;
    private SdkMeterProvider meterProvider;
    private InMemoryMetricReader sdkMeterReader;
    private TestClock testClock;

    private InstrumentationOptions otelOptions;
    private final InstrumentationOptions otelOptionsWithExperimentalFeatures
        = new InstrumentationOptions().setExperimentalFeaturesEnabled(true);
    private Tracer tracer;
    private final SdkInstrumentationOptions sdkOptions = new SdkInstrumentationOptions("test-library");

    @BeforeEach
    public void setUp() {
        exporter = InMemorySpanExporter.create();
        tracerProvider = SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(exporter)).build();
        testClock = TestClock.create();
        sdkMeterReader = InMemoryMetricReader.create();

        meterProvider = SdkMeterProvider.builder().setClock(testClock).registerMetricReader(sdkMeterReader).build();

        OpenTelemetry openTelemetry
            = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).setMeterProvider(meterProvider).build();
        otelOptions = new InstrumentationOptions().setTelemetryProvider(openTelemetry);
        otelOptionsWithExperimentalFeatures.setTelemetryProvider(openTelemetry);
        tracer = Instrumentation.create(otelOptions, sdkOptions).getTracer();
    }

    @AfterEach
    public void tearDown() {
        exporter.reset();
        tracerProvider.close();
    }

    @Test
    public void testNoSuppressionForSimpleMethod() {
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(request -> new Response<>(request, 200, new HttpHeaders(), BinaryData.empty()))
            .build();
        SampleClientTracing client = new SampleClientTracing(pipeline, otelOptions);

        client.protocolMethod(RequestContext.none());

        // test that one span is created for simple method
        assertEquals(1, exporter.getFinishedSpanItems().size());
        SpanData span = exporter.getFinishedSpanItems().get(0);
        assertEquals("protocolMethod", span.getName());
    }

    @Test
    public void testNestedInternalSpanSuppression() {
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(request -> new Response<>(request, 200, new HttpHeaders(), BinaryData.empty()))
            .build();
        SampleClientTracing client = new SampleClientTracing(pipeline, otelOptions);

        client.convenienceMethod(RequestContext.none());
        assertEquals(1, exporter.getFinishedSpanItems().size());
        SpanData span = exporter.getFinishedSpanItems().get(0);
        assertEquals("convenienceMethod", span.getName());
    }

    @Test
    public void testNestedInternalScopeSuppression() {
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(request -> new Response<>(request, 200, new HttpHeaders(), BinaryData.empty()))
            .build();

        SdkInstrumentationOptions sdkInstrumentationOptions
            = new SdkInstrumentationOptions("test-library").setEndpoint("https://localhost");

        SampleClientCallInstrumentation client = new SampleClientCallInstrumentation(pipeline,
            otelOptionsWithExperimentalFeatures, sdkInstrumentationOptions);

        client.convenienceMethod(RequestContext.none());
        assertEquals(1, exporter.getFinishedSpanItems().size());
        SpanData span = exporter.getFinishedSpanItems().get(0);
        assertEquals("convenience", span.getName());

        testClock.advance(Duration.ofNanos(SECOND_NANOS));
        assertThat(sdkMeterReader.collectAllMetrics())
            .satisfiesExactly(metric -> OpenTelemetryAssertions.assertThat(metric)
                .hasName("test.library.client.operation.duration")
                .hasHistogramSatisfying(h -> h.isCumulative().hasPointsSatisfying(point -> point.hasCount(1))));
    }

    @Test
    public void testNestedInternalScopeDisabledSuppression() {
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(request -> new Response<>(request, 200, new HttpHeaders(), BinaryData.empty()))
            .build();
        SdkInstrumentationOptions sdkOptions
            = new SdkInstrumentationOptions("test-library").disableSpanSuppression(true);
        SampleClientCallInstrumentation client
            = new SampleClientCallInstrumentation(pipeline, otelOptionsWithExperimentalFeatures, sdkOptions);

        client.convenienceMethod(RequestContext.none());
        assertEquals(2, exporter.getFinishedSpanItems().size());
        assertEquals("protocol", exporter.getFinishedSpanItems().get(0).getName());
        assertEquals("convenience", exporter.getFinishedSpanItems().get(1).getName());

        testClock.advance(Duration.ofNanos(SECOND_NANOS));
        assertThat(sdkMeterReader.collectAllMetrics())
            .satisfiesExactly(metric -> OpenTelemetryAssertions.assertThat(metric)
                .hasName("test.library.client.operation.duration")
                .hasHistogramSatisfying(
                    h -> h.isCumulative().hasPointsSatisfying(point -> point.hasCount(1), point -> point.hasCount(1))));
    }

    @Test
    public void testDisabledSuppression() {
        Tracer outerTracer = tracer;
        Tracer innerTracer = Instrumentation.create(otelOptions, sdkOptions.disableSpanSuppression(true)).getTracer();

        Span outerSpan = outerTracer.spanBuilder("outerSpan", CLIENT, null).startSpan();

        Span innerSpan
            = innerTracer.spanBuilder("innerSpan", CLIENT, outerSpan.getInstrumentationContext()).startSpan();
        innerSpan.end();
        outerSpan.end();

        assertEquals(2, exporter.getFinishedSpanItems().size());
        SpanData outerSpanData = exporter.getFinishedSpanItems().get(1);
        SpanData innerSpanData = exporter.getFinishedSpanItems().get(0);

        assertEquals("outerSpan", outerSpanData.getName());
        assertEquals("innerSpan", innerSpanData.getName());
        assertIsParentOf(outerSpanData, innerSpanData);
    }

    @Test
    public void disabledSuppressionDoesNotAffectChildren() {
        Tracer outerTracer = Instrumentation
            .create(otelOptions, new SdkInstrumentationOptions("test-library").disableSpanSuppression(true))
            .getTracer();
        Tracer innerTracer = tracer;

        Span outerSpan = outerTracer.spanBuilder("outerSpan", CLIENT, null).startSpan();

        Span innerSpan
            = innerTracer.spanBuilder("innerSpan", CLIENT, outerSpan.getInstrumentationContext()).startSpan();
        innerSpan.end();
        outerSpan.end();

        assertEquals(1, exporter.getFinishedSpanItems().size());
        SpanData outerSpanData = exporter.getFinishedSpanItems().get(0);

        assertEquals("outerSpan", outerSpanData.getName());
    }

    @Test
    @SuppressWarnings("try")
    public void noSuppressionForSiblings() {
        Span first = tracer.spanBuilder("first", CLIENT, null).startSpan();
        try (TracingScope outerScope = first.makeCurrent()) {
            first.setAttribute("key", "valueOuter");
        } finally {
            first.end();
        }

        tracer.spanBuilder("second", CLIENT, null).startSpan().end();
        assertEquals(2, exporter.getFinishedSpanItems().size());
    }

    @Test
    public void multipleLayers() {
        Tracer tracer = Instrumentation.create(otelOptions, sdkOptions).getTracer();

        Span outer = tracer.spanBuilder("outer", PRODUCER, null).startSpan();
        Span inner = tracer.spanBuilder("inner", CLIENT, outer.getInstrumentationContext()).startSpan();
        Span suppressed = tracer.spanBuilder("suppressed", CLIENT, inner.getInstrumentationContext()).startSpan();
        suppressed.end();
        inner.end();
        outer.end();

        assertEquals(2, exporter.getFinishedSpanItems().size());
        SpanData firstSpanData = exporter.getFinishedSpanItems().get(1);
        SpanData secondSpanData = exporter.getFinishedSpanItems().get(0);

        assertEquals("outer", firstSpanData.getName());
        assertEquals("inner", secondSpanData.getName());
        assertIsParentOf(firstSpanData, secondSpanData);
        assertSpanContextEquals(inner, suppressed);
    }

    @ParameterizedTest
    @MethodSource("suppressionTestCases")
    @SuppressWarnings("try")
    public void testSuppressionExplicitContext(SpanKind outerKind, SpanKind innerKind, int expectedSpanCount) {
        Span outerSpan = tracer.spanBuilder("outerSpan", outerKind, null).setAttribute("key", "valueOuter").startSpan();

        Span innerSpan = tracer.spanBuilder("innerSpan", innerKind, outerSpan.getInstrumentationContext())
            .setAttribute("key", "valueInner")
            .startSpan();
        // sanity check - this should not throw
        innerSpan.setAttribute("anotherKey", "anotherValue");

        if (expectedSpanCount == 1) {
            // suppressed span should carry the original context
            assertSpanContextEquals(outerSpan, innerSpan);

            // but should not be recording
            assertFalse(innerSpan.isRecording());
        }
        innerSpan.end();
        outerSpan.end();

        assertEquals(expectedSpanCount, exporter.getFinishedSpanItems().size());
        SpanData outerSpanData = exporter.getFinishedSpanItems().get(expectedSpanCount - 1);
        assertEquals("outerSpan", outerSpanData.getName());
        assertEquals("valueOuter", outerSpanData.getAttributes().get(AttributeKey.stringKey("key")));
        assertNull(outerSpanData.getAttributes().get(AttributeKey.stringKey("anotherKey")));

        assertNotNull(innerSpan);
        if (expectedSpanCount == 2) {
            SpanData innerSpanData = exporter.getFinishedSpanItems().get(0);
            assertEquals("innerSpan", innerSpanData.getName());
            assertEquals("valueInner", innerSpanData.getAttributes().get(AttributeKey.stringKey("key")));

            assertIsParentOf(outerSpanData, innerSpanData);
        } else {
            assertSpanContextEquals(outerSpan, innerSpan);
        }
    }

    @ParameterizedTest
    @MethodSource("suppressionTestCases")
    @SuppressWarnings("try")
    public void testSuppressionImplicitContext(SpanKind outerKind, SpanKind innerKind, int expectedSpanCount) {
        Span outerSpan = tracer.spanBuilder("outerSpan", outerKind, null).setAttribute("key", "valueOuter").startSpan();
        Span innerSpan = null;
        try (TracingScope outerScope = outerSpan.makeCurrent()) {
            innerSpan = tracer.spanBuilder("innerSpan", innerKind, null).setAttribute("key", "valueInner").startSpan();
            io.opentelemetry.api.trace.Span outerCurrentSpan = io.opentelemetry.api.trace.Span.current();
            try (TracingScope innerScope = innerSpan.makeCurrent()) {
                // sanity check - this should not throw
                innerSpan.setAttribute("anotherKey", "anotherValue");

                if (expectedSpanCount == 1) {
                    // suppressed span should carry the original context
                    io.opentelemetry.api.trace.Span innerCurrentSpan = io.opentelemetry.api.trace.Span.current();
                    assertTrue(innerCurrentSpan.getSpanContext().isValid());

                    assertSpanContextEquals(outerCurrentSpan, innerCurrentSpan);
                    assertSpanContextEquals(outerSpan, innerSpan);

                    // but should not be recording
                    assertFalse(innerCurrentSpan.isRecording());
                    assertFalse(innerSpan.isRecording());
                }
            } finally {
                innerSpan.end();
            }
        } finally {
            outerSpan.end();
        }

        assertEquals(expectedSpanCount, exporter.getFinishedSpanItems().size());
        SpanData outerSpanData = exporter.getFinishedSpanItems().get(expectedSpanCount - 1);
        assertEquals("outerSpan", outerSpanData.getName());
        assertEquals("valueOuter", outerSpanData.getAttributes().get(AttributeKey.stringKey("key")));
        assertNull(outerSpanData.getAttributes().get(AttributeKey.stringKey("anotherKey")));

        assertNotNull(innerSpan);
        if (expectedSpanCount == 2) {
            SpanData innerSpanData = exporter.getFinishedSpanItems().get(0);
            assertEquals("innerSpan", innerSpanData.getName());
            assertEquals("valueInner", innerSpanData.getAttributes().get(AttributeKey.stringKey("key")));

            assertIsParentOf(outerSpanData, innerSpanData);
        } else {
            assertSpanContextEquals(outerSpan, innerSpan);
        }
    }

    private static void assertIsParentOf(SpanData parent, SpanData child) {
        assertEquals(parent.getSpanContext().getSpanId(), child.getParentSpanContext().getSpanId());
        assertEquals(parent.getSpanContext().getTraceId(), child.getSpanContext().getTraceId());
    }

    private static void assertSpanContextEquals(Span first, Span second) {
        InstrumentationContext firstContext = first.getInstrumentationContext();
        InstrumentationContext secondContext = second.getInstrumentationContext();
        assertEquals(firstContext.getTraceId(), secondContext.getTraceId());
        assertEquals(firstContext.getSpanId(), secondContext.getSpanId());
        assertSame(firstContext.getTraceFlags(), secondContext.getTraceFlags());
    }

    private static void assertSpanContextEquals(io.opentelemetry.api.trace.Span first,
        io.opentelemetry.api.trace.Span second) {
        assertEquals(first.getSpanContext().getTraceId(), second.getSpanContext().getTraceId());
        assertEquals(first.getSpanContext().getSpanId(), second.getSpanContext().getSpanId());
        assertSame(first.getSpanContext().getTraceFlags(), second.getSpanContext().getTraceFlags());
    }

    public static Stream<Arguments> suppressionTestCases() {
        return Stream.of(Arguments.of(CLIENT, CLIENT, 1), Arguments.of(CLIENT, INTERNAL, 2),
            Arguments.of(INTERNAL, CLIENT, 2), Arguments.of(INTERNAL, INTERNAL, 1),
            Arguments.of(SpanKind.SERVER, CLIENT, 2), Arguments.of(SpanKind.SERVER, INTERNAL, 2),
            Arguments.of(PRODUCER, CLIENT, 2), Arguments.of(INTERNAL, PRODUCER, 2),
            Arguments.of(SpanKind.CONSUMER, CLIENT, 2), Arguments.of(SpanKind.CONSUMER, INTERNAL, 2));
    }

    static class SampleClientTracing {
        private static final String SDK_NAME = "test-library";
        private final HttpPipeline pipeline;
        private final Tracer tracer;

        SampleClientTracing(HttpPipeline pipeline, InstrumentationOptions options) {
            this.pipeline = pipeline;

            SdkInstrumentationOptions sdkInstrumentationOptions
                = new SdkInstrumentationOptions(SDK_NAME).setEndpoint("https://localhost");
            this.tracer = Instrumentation.create(options, sdkInstrumentationOptions).getTracer();
        }

        @SuppressWarnings("try")
        public void protocolMethod(RequestContext context) {
            context = context == null ? RequestContext.none() : context;
            Span span = tracer.spanBuilder("protocolMethod", INTERNAL, context.getInstrumentationContext()).startSpan();

            try (TracingScope scope = span.makeCurrent()) {
                Response<?> response = pipeline.send(new HttpRequest().setMethod(HttpMethod.GET)
                    .setUri("https://localhost")
                    .setContext(
                        context.toBuilder().setInstrumentationContext(span.getInstrumentationContext()).build()));
                response.close();
            } finally {
                span.end();
            }
        }

        @SuppressWarnings("try")
        public void convenienceMethod(RequestContext context) {
            context = context == null ? RequestContext.none() : context;
            Span span
                = tracer
                    .spanBuilder("convenienceMethod", INTERNAL,
                        context == null ? null : context.getInstrumentationContext())
                    .startSpan();

            try (TracingScope scope = span.makeCurrent()) {
                protocolMethod(context.toBuilder().setInstrumentationContext(span.getInstrumentationContext()).build());
            } finally {
                span.end();
            }
        }
    }

    static class SampleClientCallInstrumentation {
        private final HttpPipeline pipeline;
        private final Instrumentation instrumentation;

        SampleClientCallInstrumentation(HttpPipeline pipeline, InstrumentationOptions options,
            SdkInstrumentationOptions sdkInstrumentationOptions) {
            this.pipeline = pipeline;
            this.instrumentation = Instrumentation.create(options, sdkInstrumentationOptions);
        }

        public Response<?> protocolMethod(RequestContext context) {
            return instrumentation.instrumentWithResponse("protocol", context, updatedContext -> pipeline.send(
                new HttpRequest().setMethod(HttpMethod.GET).setUri("https://localhost").setContext(updatedContext)));
        }

        public Response<?> convenienceMethod(RequestContext context) {
            return instrumentation.instrumentWithResponse("convenience", context, this::protocolMethod);
        }
    }
}
