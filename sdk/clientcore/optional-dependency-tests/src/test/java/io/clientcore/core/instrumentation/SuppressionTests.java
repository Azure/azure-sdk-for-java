// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.http.MockHttpResponse;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.clientcore.core.instrumentation.tracing.Tracer;
import io.clientcore.core.instrumentation.tracing.TracingScope;
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

import java.io.IOException;
import java.util.stream.Stream;

import static io.clientcore.core.instrumentation.tracing.SpanKind.CLIENT;
import static io.clientcore.core.instrumentation.tracing.SpanKind.INTERNAL;
import static io.clientcore.core.instrumentation.tracing.SpanKind.PRODUCER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class SuppressionTests {

    private static final LibraryInstrumentationOptions DEFAULT_LIB_OPTIONS
        = new LibraryInstrumentationOptions("test-library");

    private InMemorySpanExporter exporter;
    private SdkTracerProvider tracerProvider;
    private InstrumentationOptions otelOptions;
    private Tracer tracer;

    @BeforeEach
    public void setUp() {
        exporter = InMemorySpanExporter.create();
        tracerProvider = SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(exporter)).build();
        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();
        otelOptions = new InstrumentationOptions().setTelemetryProvider(openTelemetry);
        tracer = Instrumentation.create(otelOptions, DEFAULT_LIB_OPTIONS).getTracer();
    }

    @AfterEach
    public void tearDown() {
        exporter.reset();
        tracerProvider.close();
    }

    @Test
    public void testNoSuppressionForSimpleMethod() {
        HttpPipeline pipeline
            = new HttpPipelineBuilder().httpClient(request -> new MockHttpResponse(request, 200)).build();
        SampleClient client = new SampleClient(pipeline, otelOptions);

        client.protocolMethod(new RequestOptions());

        // test that one span is created for simple method
        assertEquals(1, exporter.getFinishedSpanItems().size());
        SpanData span = exporter.getFinishedSpanItems().get(0);
        assertEquals("protocolMethod", span.getName());
    }

    @Test
    public void testNestedInternalSpanSuppression() {
        HttpPipeline pipeline
            = new HttpPipelineBuilder().httpClient(request -> new MockHttpResponse(request, 200)).build();
        SampleClient client = new SampleClient(pipeline, otelOptions);

        client.convenienceMethod(new RequestOptions());
        assertEquals(1, exporter.getFinishedSpanItems().size());
        SpanData span = exporter.getFinishedSpanItems().get(0);
        assertEquals("convenienceMethod", span.getName());
    }

    @Test
    public void testDisabledSuppression() {
        Tracer outerTracer = tracer;
        Tracer innerTracer = Instrumentation
            .create(otelOptions, new LibraryInstrumentationOptions("test-library").disableSpanSuppression(true))
            .getTracer();

        RequestOptions options = new RequestOptions();
        Span outerSpan = outerTracer.spanBuilder("outerSpan", CLIENT, options.getInstrumentationContext()).startSpan();

        options.setInstrumentationContext(outerSpan.getInstrumentationContext());

        Span innerSpan = innerTracer.spanBuilder("innerSpan", CLIENT, options.getInstrumentationContext()).startSpan();
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
            .create(otelOptions, new LibraryInstrumentationOptions("test-library").disableSpanSuppression(true))
            .getTracer();
        Tracer innerTracer = tracer;

        RequestOptions options = new RequestOptions();
        Span outerSpan = outerTracer.spanBuilder("outerSpan", CLIENT, options.getInstrumentationContext()).startSpan();

        options.setInstrumentationContext(outerSpan.getInstrumentationContext());
        Span innerSpan = innerTracer.spanBuilder("innerSpan", CLIENT, options.getInstrumentationContext()).startSpan();
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
        Tracer tracer = Instrumentation.create(otelOptions, DEFAULT_LIB_OPTIONS).getTracer();

        RequestOptions options = new RequestOptions();

        Span outer = tracer.spanBuilder("outer", PRODUCER, options.getInstrumentationContext()).startSpan();
        options.setInstrumentationContext(outer.getInstrumentationContext());

        Span inner = tracer.spanBuilder("inner", CLIENT, options.getInstrumentationContext()).startSpan();
        options.setInstrumentationContext(inner.getInstrumentationContext());

        Span suppressed = tracer.spanBuilder("suppressed", CLIENT, options.getInstrumentationContext()).startSpan();
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
        RequestOptions options = new RequestOptions();
        Span outerSpan = tracer.spanBuilder("outerSpan", outerKind, options.getInstrumentationContext())
            .setAttribute("key", "valueOuter")
            .startSpan();

        options.setInstrumentationContext(outerSpan.getInstrumentationContext());

        Span innerSpan = tracer.spanBuilder("innerSpan", innerKind, options.getInstrumentationContext())
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
        return Stream.of(Arguments.of(CLIENT, CLIENT, 1), Arguments.of(CLIENT, INTERNAL, 1),
            Arguments.of(INTERNAL, CLIENT, 1), Arguments.of(INTERNAL, INTERNAL, 1),
            Arguments.of(SpanKind.SERVER, CLIENT, 2), Arguments.of(SpanKind.SERVER, INTERNAL, 2),
            Arguments.of(PRODUCER, CLIENT, 2), Arguments.of(INTERNAL, PRODUCER, 2),
            Arguments.of(SpanKind.CONSUMER, CLIENT, 2), Arguments.of(SpanKind.CONSUMER, INTERNAL, 2));
    }

    static class SampleClient {
        private final HttpPipeline pipeline;
        private final Tracer tracer;

        SampleClient(HttpPipeline pipeline, InstrumentationOptions options) {
            this.pipeline = pipeline;
            this.tracer = Instrumentation.create(options, DEFAULT_LIB_OPTIONS).getTracer();
        }

        @SuppressWarnings("try")
        public void protocolMethod(RequestOptions options) {
            Span span = tracer.spanBuilder("protocolMethod", INTERNAL, options.getInstrumentationContext()).startSpan();

            options.setInstrumentationContext(span.getInstrumentationContext());

            try (TracingScope scope = span.makeCurrent()) {
                Response<?> response = pipeline.send(new HttpRequest(HttpMethod.GET, "https://localhost"));
                try {
                    response.close();
                } catch (IOException e) {
                    fail(e);
                }
            } finally {
                span.end();
            }
        }

        @SuppressWarnings("try")
        public void convenienceMethod(RequestOptions options) {
            Span span
                = tracer.spanBuilder("convenienceMethod", INTERNAL, options.getInstrumentationContext()).startSpan();

            options.setInstrumentationContext(span.getInstrumentationContext());

            try (TracingScope scope = span.makeCurrent()) {
                protocolMethod(options);
            } finally {
                span.end();
            }
        }
    }
}
