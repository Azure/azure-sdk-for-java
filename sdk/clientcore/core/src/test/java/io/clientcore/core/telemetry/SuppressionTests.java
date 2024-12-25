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
import io.clientcore.core.telemetry.tracing.Tracer;
import io.clientcore.core.util.Context;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    public void testSuppression() {
        HttpPipeline pipeline
            = new HttpPipelineBuilder().httpClient(request -> new MockHttpResponse(request, 200)).build();
        SampleClient client = new SampleClient(pipeline, otelOptions);

        client.doSomething(null);

        // test that one span is created for simple method
        assertEquals(1, exporter.getFinishedSpanItems().size());
        SpanData span = exporter.getFinishedSpanItems().get(0);
        assertEquals("doSomething", span.getName());

        exporter.reset();

        // now test that one span is created for complicated method
        // and the inner span is suppressed
        client.doSomethingComplicated(null);
        assertEquals(1, exporter.getFinishedSpanItems().size());
        span = exporter.getFinishedSpanItems().get(0);
        assertEquals("doSomethingComplicated", span.getName());
    }

    static class SampleClient {
        private final HttpPipeline pipeline;
        private final Tracer tracer;

        SampleClient(HttpPipeline pipeline, TelemetryOptions<?> options) {
            this.pipeline = pipeline;
            this.tracer = TelemetryProvider.getInstance().getTracer(options, DEFAULT_LIB_OPTIONS);
        }

        @SuppressWarnings("try")
        public void doSomething(RequestOptions options) {
            Span span = tracer.spanBuilder("doSomething", options == null ? Context.none() : options.getContext())
                .startSpan();

            if (tracer.isEnabled()) {
                if (options == null) {
                    options = new RequestOptions();
                }

                // TODO: we should put span on the RequestOptions directly
                options.setContext(span.storeInContext(options.getContext()));
            }

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
        public void doSomethingComplicated(RequestOptions options) {
            Span span
                = tracer.spanBuilder("doSomethingComplicated", options == null ? Context.none() : options.getContext())
                    .startSpan();

            if (tracer.isEnabled()) {
                if (options == null) {
                    options = new RequestOptions();
                }

                options.setContext(span.storeInContext(options.getContext()));
            }

            try (Scope scope = span.makeCurrent()) {
                doSomething(options);
            } finally {
                span.end();
            }
        }
    }
}
