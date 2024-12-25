// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.telemetry;

import io.clientcore.core.http.MockHttpResponse;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
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

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class SuppressionTests {

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
        Tracer outerTracer = TelemetryProvider.getInstance().getTracer(otelOptions, DEFAULT_LIB_OPTIONS);
        Tracer innerTracer = TelemetryProvider.getInstance()
            .getTracer(otelOptions, new LibraryTelemetryOptions("test-library").disableSpanSuppression(true));
        Context context = Context.none();

        // TODO: context propagation sucks
        Span outerSpan = outerTracer.spanBuilder("outerSpan", context).setSpanKind(SpanKind.CLIENT).startSpan();
        Span innerSpan = innerTracer.spanBuilder("innerSpan", outerSpan.storeInContext(context))
            .setSpanKind(SpanKind.CLIENT)
            .startSpan();
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
        Tracer outerTracer = TelemetryProvider.getInstance()
            .getTracer(otelOptions, new LibraryTelemetryOptions("test-library").disableSpanSuppression(true));
        Tracer innerTracer = TelemetryProvider.getInstance().getTracer(otelOptions, DEFAULT_LIB_OPTIONS);

        Context context = Context.none();

        // TODO: context propagation sucks
        Span outerSpan = outerTracer.spanBuilder("outerSpan", context).setSpanKind(SpanKind.CLIENT).startSpan();
        Span innerSpan = innerTracer.spanBuilder("innerSpan", outerSpan.storeInContext(context))
            .setSpanKind(SpanKind.CLIENT)
            .startSpan();
        innerSpan.end();
        outerSpan.end();

        assertEquals(1, exporter.getFinishedSpanItems().size());
        SpanData outerSpanData = exporter.getFinishedSpanItems().get(0);

        assertEquals("outerSpan", outerSpanData.getName());
    }

    @Test
    @SuppressWarnings("try")
    public void noSuppressionForSiblings() {
        Tracer tracer = TelemetryProvider.getInstance().getTracer(otelOptions, DEFAULT_LIB_OPTIONS);
        Context parentContext = Context.none();

        Span first = tracer.spanBuilder("first", parentContext).setSpanKind(SpanKind.CLIENT).startSpan();
        try (Scope outerScope = first.makeCurrent()) {
            first.setAttribute("key", "valueOuter");
        } finally {
            first.end();
        }

        tracer.spanBuilder("second", parentContext).setSpanKind(SpanKind.CLIENT).startSpan().end();
        assertEquals(2, exporter.getFinishedSpanItems().size());
    }

    @Test
    public void multipleLayers() {
        Tracer tracer = TelemetryProvider.getInstance().getTracer(otelOptions, DEFAULT_LIB_OPTIONS);
        Context parentContext = Context.none();

        Span outer = tracer.spanBuilder("outer", parentContext).setSpanKind(SpanKind.CLIENT).startSpan();
        Context outerContext = outer.storeInContext(parentContext);
        Span inner = tracer.spanBuilder("inner", outerContext).setSpanKind(SpanKind.PRODUCER).startSpan();
        Span suppressed = tracer.spanBuilder("suppressed", inner.storeInContext(outerContext))
            .setSpanKind(SpanKind.CLIENT)
            .startSpan();
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
        Tracer tracer = TelemetryProvider.getInstance().getTracer(otelOptions, DEFAULT_LIB_OPTIONS);
        Context context = Context.none();

        // TODO: context propagation sucks
        Span outerSpan = tracer.spanBuilder("outerSpan", context)
            .setSpanKind(outerKind)
            .setAttribute("key", "valueOuter")
            .startSpan();
        Span innerSpan = tracer.spanBuilder("innerSpan", outerSpan.storeInContext(context))
            .setAttribute("key", "valueInner")
            .setSpanKind(innerKind)
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
        Tracer tracer = TelemetryProvider.getInstance().getTracer(otelOptions, DEFAULT_LIB_OPTIONS);

        // TODO: context propagation sucks
        Span outerSpan = tracer.spanBuilder("outerSpan", Context.none())
            .setSpanKind(outerKind)
            .setAttribute("key", "valueOuter")
            .startSpan();
        Span innerSpan = null;
        try (Scope outerScope = outerSpan.makeCurrent()) {
            innerSpan = tracer.spanBuilder("innerSpan", Context.none())
                .setAttribute("key", "valueInner")
                .setSpanKind(innerKind)
                .startSpan();
            io.opentelemetry.api.trace.Span outerCurrentSpan = io.opentelemetry.api.trace.Span.current();
            try (Scope innerScope = innerSpan.makeCurrent()) {
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
        assertEquals(first.getSpanContext().getTraceId(), second.getSpanContext().getTraceId());
        assertEquals(first.getSpanContext().getSpanId(), second.getSpanContext().getSpanId());
        assertSame(first.getSpanContext().getTraceFlags(), second.getSpanContext().getTraceFlags());
    }

    private static void assertSpanContextEquals(io.opentelemetry.api.trace.Span first,
        io.opentelemetry.api.trace.Span second) {
        assertEquals(first.getSpanContext().getTraceId(), second.getSpanContext().getTraceId());
        assertEquals(first.getSpanContext().getSpanId(), second.getSpanContext().getSpanId());
        assertSame(first.getSpanContext().getTraceFlags(), second.getSpanContext().getTraceFlags());
    }

    public static Stream<Arguments> suppressionTestCases() {
        return Stream.of(Arguments.of(SpanKind.CLIENT, SpanKind.CLIENT, 1),
            Arguments.of(SpanKind.CLIENT, SpanKind.INTERNAL, 1), Arguments.of(SpanKind.INTERNAL, SpanKind.CLIENT, 1),
            Arguments.of(SpanKind.INTERNAL, SpanKind.INTERNAL, 1), Arguments.of(SpanKind.SERVER, SpanKind.CLIENT, 2),
            Arguments.of(SpanKind.SERVER, SpanKind.INTERNAL, 2), Arguments.of(SpanKind.PRODUCER, SpanKind.CLIENT, 2),
            Arguments.of(SpanKind.INTERNAL, SpanKind.PRODUCER, 2), Arguments.of(SpanKind.CONSUMER, SpanKind.CLIENT, 2),
            Arguments.of(SpanKind.CONSUMER, SpanKind.INTERNAL, 2));
    }

    static class SampleClient {
        private final HttpPipeline pipeline;
        private final Tracer tracer;

        SampleClient(HttpPipeline pipeline, TelemetryOptions<?> options) {
            this.pipeline = pipeline;
            this.tracer = TelemetryProvider.getInstance().getTracer(options, DEFAULT_LIB_OPTIONS);
        }

        @SuppressWarnings("try")
        public void protocolMethod(RequestOptions options) {
            Span span = tracer.spanBuilder("protocolMethod", options == null ? Context.none() : options.getContext())
                .startSpan();

            // TODO: we should put span on the RequestOptions directly
            options.setContext(span.storeInContext(options.getContext()));

            try (Scope scope = span.makeCurrent()) {
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
            Span span = tracer.spanBuilder("convenienceMethod", options == null ? Context.none() : options.getContext())
                .startSpan();

            options.setContext(span.storeInContext(options.getContext()));

            try (Scope scope = span.makeCurrent()) {
                protocolMethod(options);
            } finally {
                span.end();
            }
        }
    }
}
