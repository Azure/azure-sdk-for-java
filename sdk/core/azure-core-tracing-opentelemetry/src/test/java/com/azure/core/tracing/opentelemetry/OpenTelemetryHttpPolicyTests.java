// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Context;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {com.azure.core.implementation.http.policy.InstrumentationPolicy.
 */
@SuppressWarnings("try")
public class OpenTelemetryHttpPolicyTests {

    private static final String X_MS_REQUEST_ID_1 = "response id 1";
    private static final String X_MS_REQUEST_ID_2 = "response id 2";
    private static final int RESPONSE_STATUS_CODE = 201;
    private InMemorySpanExporter exporter;
    private SdkTracerProvider tracerProvider;
    private Tracer tracer;
    private com.azure.core.util.tracing.Tracer azTracer;
    private static final String SPAN_NAME = "foo";
    private static final HttpHeaderName TRACE_PARENT = HttpHeaderName.fromString("traceparent");
    private static final HttpHeaderName X_MS_REQUEST_ID = HttpHeaderName.fromString("x-ms-request-id");

    @BeforeEach
    public void setUp(TestInfo testInfo) {
        exporter = InMemorySpanExporter.create();
        tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(exporter)).build();

        azTracer = new OpenTelemetryTracer("test", null, null, new OpenTelemetryTracingOptions().setProvider(tracerProvider));
        tracer = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build().getTracer(testInfo.getDisplayName());
    }

    @Test
    public void openTelemetryHttpPolicyTest() {
        // Start parent span.
        Span parentSpan = tracer.spanBuilder(SPAN_NAME).startSpan();

        // Add parent span to tracingContext
        Context tracingContext = new Context(PARENT_TRACE_CONTEXT_KEY, io.opentelemetry.context.Context.root().with(parentSpan))
            .addData("az.namespace", "foo");

        // Act
        HttpRequest request = new HttpRequest(HttpMethod.POST, "https://httpbin.org/hello?there#otel");
        request.setHeader(HttpHeaderName.USER_AGENT, "user-agent");

        try (Scope scope = parentSpan.makeCurrent()) {
            createHttpPipeline(azTracer).send(request, tracingContext).block();
        }
        // Assert
        List<SpanData> exportedSpans = exporter.getFinishedSpanItems();
        // rest proxy span is not exported as global otel is not configured
        assertEquals(1, exportedSpans.size());

        SpanData httpSpan = exportedSpans.get(0);

        assertEquals(request.getHeaders().getValue(TRACE_PARENT), String.format("00-%s-%s-01", httpSpan.getTraceId(), httpSpan.getSpanId()));
        assertEquals(((ReadableSpan) parentSpan).getSpanContext().getSpanId(), httpSpan.getParentSpanId());
        assertEquals("HTTP POST", httpSpan.getName());

        Map<String, Object> httpAttributes = getAttributes(httpSpan);

        assertEquals(6, httpAttributes.size());
        assertEquals("https://httpbin.org/hello?there#otel", httpAttributes.get("http.url"));
        assertEquals("POST", httpAttributes.get("http.method"));
        assertEquals("user-agent", httpAttributes.get("http.user_agent"));
        assertEquals("foo", httpAttributes.get("az.namespace"));
        assertEquals((long) RESPONSE_STATUS_CODE, httpAttributes.get("http.status_code"));
        assertEquals(X_MS_REQUEST_ID_1, httpAttributes.get("az.service_request_id"));
    }

    @Test
    public void presamplingAttributesArePopulatedBeforeSpanStarts() {
        AtomicBoolean samplerCalled = new AtomicBoolean();
        SdkTracerProvider providerWithSampler = SdkTracerProvider.builder()
            .setSampler(new Sampler() {
                @Override
                public SamplingResult shouldSample(io.opentelemetry.context.Context parentContext, String traceId, String name, SpanKind spanKind, Attributes attributes, List<LinkData> parentLinks) {
                    samplerCalled.set(true);
                    assertEquals(2, attributes.size());
                    assertEquals("HTTP DELETE", name);
                    attributes.forEach((k, v) -> {
                        if ("http.url".equals(k.getKey())) {
                            assertEquals("https://httpbin.org/hello?there#otel", v);
                        } else {
                            assertEquals("http.method", k.getKey());
                            assertEquals("DELETE", v);
                        }
                    });

                    return SamplingResult.create(SamplingDecision.DROP);
                }

                @Override
                public String getDescription() {
                    return "test";
                }
            })
            .addSpanProcessor(SimpleSpanProcessor.create(exporter)).build();

        // Act
        HttpRequest request = new HttpRequest(HttpMethod.DELETE, "https://httpbin.org/hello?there#otel");
        try (Scope scope = tracer.spanBuilder("test").startSpan().makeCurrent()) {
            createHttpPipeline(new OpenTelemetryTracer("test", null, null,
                new OpenTelemetryTracingOptions().setProvider(providerWithSampler)))
                .send(request)
                .block();
        }
        // Assert
        List<SpanData> exportedSpans = exporter.getFinishedSpanItems();
        assertEquals(0, exportedSpans.size());
        assertTrue(samplerCalled.get());
    }

    @Test
    public void clientRequestIdIsStamped() {
        try (Scope scope = tracer.spanBuilder("test").startSpan().makeCurrent()) {
            HttpRequest request = new HttpRequest(HttpMethod.PUT, "https://httpbin.org/hello?there#otel");
            HttpResponse response = createHttpPipeline(azTracer, new RequestIdPolicy()).send(request).block();

            // Assert
            List<SpanData> exportedSpans = exporter.getFinishedSpanItems();
            assertEquals(1, exportedSpans.size());

            assertEquals("HTTP PUT", exportedSpans.get(0).getName());

            Map<String, Object> httpAttributes = getAttributes(exportedSpans.get(0));
            assertEquals(5, httpAttributes.size());

            assertEquals(response.getRequest().getHeaders().getValue(HttpHeaderName.X_MS_CLIENT_REQUEST_ID), httpAttributes.get("az.client_request_id"));
            assertEquals(X_MS_REQUEST_ID_1, httpAttributes.get("az.service_request_id"));

            assertEquals("https://httpbin.org/hello?there#otel", httpAttributes.get("http.url"));
            assertEquals("PUT", httpAttributes.get("http.method"));
            assertEquals((long) RESPONSE_STATUS_CODE, httpAttributes.get("http.status_code"));
        }
    }

    @Test
    public void everyTryIsTraced() {
        AtomicInteger attemptCount = new AtomicInteger();
        AtomicReference<String> traceparentTry503 = new AtomicReference<>();
        AtomicReference<String> traceparentTry200 = new AtomicReference<>();

        OpenTelemetryTracingOptions options = new OpenTelemetryTracingOptions().setProvider(tracerProvider);

        com.azure.core.util.tracing.Tracer azTracer = new OpenTelemetryTracer("test", null, null, options);

        List<HttpPipelinePolicy> policies = new ArrayList<>(Arrays.asList(new RetryPolicy()));
        HttpPolicyProviders.addAfterRetryPolicies(policies);

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(request -> {
                HttpHeaders headers = new HttpHeaders();

                int count = attemptCount.getAndIncrement();
                if (count == 0) {
                    traceparentTry503.set(request.getHeaders().getValue(TRACE_PARENT));
                    headers.set(X_MS_REQUEST_ID, X_MS_REQUEST_ID_1);
                    return Mono.just(new MockHttpResponse(request, 503, headers));
                } else if (count == 1) {
                    traceparentTry200.set(request.getHeaders().getValue(TRACE_PARENT));
                    headers.set(X_MS_REQUEST_ID, X_MS_REQUEST_ID_2);
                    return Mono.just(new MockHttpResponse(request, 200, headers));
                } else {
                    // Too many requests have been made.
                    return Mono.just(new MockHttpResponse(request, 400, headers));
                }
            })
            .tracer(azTracer)
            .build();

        // Start user parent span and populate context.
        Span parentSpan = tracer.spanBuilder("test").startSpan();

        Context tracingContext = new Context(PARENT_TRACE_CONTEXT_KEY, io.opentelemetry.context.Context.root().with(parentSpan))
            .addData("az.namespace", "foo");

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/hello"), tracingContext))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();

        List<SpanData> exportedSpans = exporter.getFinishedSpanItems();
        assertEquals(2, exportedSpans.size());

        SpanData try503 = exportedSpans.get(0);
        SpanData try200 = exportedSpans.get(1);

        assertEquals(traceparentTry503.get(), String.format("00-%s-%s-01", try503.getTraceId(), try503.getSpanId()));
        assertEquals(traceparentTry200.get(), String.format("00-%s-%s-01", try200.getTraceId(), try200.getSpanId()));

        assertEquals("HTTP GET", try503.getName());
        Map<String, Object> httpAttributes503 = getAttributes(try503);
        assertEquals(5, httpAttributes503.size());
        assertEquals(503L, httpAttributes503.get("http.status_code"));
        assertEquals(X_MS_REQUEST_ID_1, httpAttributes503.get("az.service_request_id"));

        assertEquals("HTTP GET", try503.getName());
        Map<String, Object> httpAttributes200 = getAttributes(try200);
        assertEquals(5, httpAttributes200.size());
        assertEquals(200L, httpAttributes200.get("http.status_code"));
        assertEquals(X_MS_REQUEST_ID_2, httpAttributes200.get("az.service_request_id"));
    }

    @ParameterizedTest
    @MethodSource("getStatusCodes")
    public void endStatusDependingOnStatusCode(int statusCode, StatusCode status) {

        OpenTelemetryTracingOptions options = new OpenTelemetryTracingOptions().setProvider(tracerProvider);

        com.azure.core.util.tracing.Tracer azTracer = new OpenTelemetryTracer("test", null, null, options);

        List<HttpPipelinePolicy> policies = new ArrayList<>();
        HttpPolicyProviders.addAfterRetryPolicies(policies);

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(request -> Mono.just(new MockHttpResponse(request, statusCode)))
            .tracer(azTracer)
            .build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/hello")))
            .assertNext(response -> assertEquals(statusCode, response.getStatusCode()))
            .verifyComplete();

        List<SpanData> exportedSpans = exporter.getFinishedSpanItems();
        assertEquals(1, exportedSpans.size());

        SpanData span = exportedSpans.get(0);
        assertEquals(Long.valueOf(statusCode), span.getAttributes().get(AttributeKey.longKey("http.status_code")));
        assertEquals(status, span.getStatus().getStatusCode());
    }

    @Test
    public void exceptionEventIsRecorded() {
        OpenTelemetryTracingOptions options = new OpenTelemetryTracingOptions().setProvider(tracerProvider);

        com.azure.core.util.tracing.Tracer azTracer = new OpenTelemetryTracer("test", null, null, options);

        List<HttpPipelinePolicy> policies = new ArrayList<>();
        HttpPolicyProviders.addAfterRetryPolicies(policies);

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(request -> Mono.error(new Exception("foo")))
            .tracer(azTracer)
            .build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/hello")))
            .expectErrorMessage("foo")
            .verify();

        List<SpanData> exportedSpans = exporter.getFinishedSpanItems();
        assertEquals(1, exportedSpans.size());

        SpanData span = exportedSpans.get(0);
        assertNull(span.getAttributes().get(AttributeKey.longKey("http.status_code")));
        assertEquals(StatusCode.ERROR, span.getStatus().getStatusCode());

        List<EventData> events = span.getEvents();
        assertEquals(1, events.size());
        assertEquals("exception", events.get(0).getName());
        assertEquals(Exception.class.getName(), events.get(0).getAttributes().get(AttributeKey.stringKey("exception.type")));
        assertEquals("foo", events.get(0).getAttributes().get(AttributeKey.stringKey("exception.message")));
    }

    @Test
    public void timeoutIsTraced() {
        AtomicInteger attemptCount = new AtomicInteger();
        OpenTelemetryTracingOptions options = new OpenTelemetryTracingOptions().setProvider(tracerProvider);

        com.azure.core.util.tracing.Tracer azTracer = new OpenTelemetryTracer("test", null, null, options);

        List<HttpPipelinePolicy> policies = new ArrayList<>(Arrays.asList(new RetryPolicy()));
        HttpPolicyProviders.addAfterRetryPolicies(policies);

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(request -> {
                HttpHeaders headers = new HttpHeaders();
                int count = attemptCount.getAndIncrement();
                if (count == 0) {
                    return Mono.error(new TimeoutException("timeout"));
                } else if (count == 1) {
                    return Mono.just(new MockHttpResponse(request, 200, headers));
                } else {
                    // Too many requests have been made.
                    return Mono.just(new MockHttpResponse(request, 400, headers));
                }
            })
            .tracer(azTracer)
            .build();

        // Start user parent span and populate context.
        Span parentSpan = tracer.spanBuilder("test").startSpan();

        Context tracingContext = new Context(PARENT_TRACE_CONTEXT_KEY, io.opentelemetry.context.Context.root().with(parentSpan))
            .addData("az.namespace", "foo");

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/hello"), tracingContext))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();

        List<SpanData> exportedSpans = exporter.getFinishedSpanItems();
        assertEquals(2, exportedSpans.size());

        SpanData tryTimeout = exportedSpans.get(0);
        Map<String, Object> httpAttributesTimeout = getAttributes(tryTimeout);
        assertNull(httpAttributesTimeout.get("http.status_code"));
        assertEquals(StatusCode.ERROR, tryTimeout.getStatus().getStatusCode());
        assertEquals("", tryTimeout.getStatus().getDescription());

        List<EventData> events = tryTimeout.getEvents();
        assertEquals(1, events.size());
        assertEquals("exception", events.get(0).getName());
        assertEquals(TimeoutException.class.getName(), events.get(0).getAttributes().get(AttributeKey.stringKey("exception.type")));
    }

    @Test
    public void cancelIsTraced() {
        OpenTelemetryTracingOptions options = new OpenTelemetryTracingOptions().setProvider(tracerProvider);

        com.azure.core.util.tracing.Tracer azTracer = new OpenTelemetryTracer("test", null, null, options);

        List<HttpPipelinePolicy> policies = new ArrayList<>(Arrays.asList(new RetryPolicy()));
        HttpPolicyProviders.addAfterRetryPolicies(policies);

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(request ->
                Mono.delay(Duration.ofSeconds(10)).map(l -> new MockHttpResponse(request, 200)))
            .tracer(azTracer)
            .build();

        pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/hello"), Context.NONE)
                .toFuture()
            .cancel(true);

        List<SpanData> exportedSpans = exporter.getFinishedSpanItems();
        assertEquals(1, exportedSpans.size());

        SpanData cancelled = exportedSpans.get(0);
        Map<String, Object> httpAttributesTimeout = getAttributes(cancelled);
        assertNull(httpAttributesTimeout.get("http.status_code"));
        assertEquals(StatusCode.ERROR, cancelled.getStatus().getStatusCode());
        assertEquals("cancel", cancelled.getStatus().getDescription());
    }

    private Map<String, Object> getAttributes(SpanData span) {
        Map<String, Object> attributes = new HashMap<>();
        span.getAttributes().forEach((k, v) -> attributes.put(k.getKey(), v));

        return attributes;
    }

    private static HttpPipeline createHttpPipeline(com.azure.core.util.tracing.Tracer azTracer, HttpPipelinePolicy... beforeRetryPolicies) {
        List<HttpPipelinePolicy> policies = new ArrayList<>(Arrays.asList(beforeRetryPolicies));
        HttpPolicyProviders.addAfterRetryPolicies(policies);

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(new SimpleMockHttpClient())
            .tracer(azTracer)
            .build();
    }

    private static Stream<Arguments> getStatusCodes() {
        return Stream.of(
            Arguments.of(100, StatusCode.UNSET),
            Arguments.of(200, StatusCode.UNSET),
            Arguments.of(201, StatusCode.UNSET),
            Arguments.of(302, StatusCode.UNSET),
            Arguments.of(307, StatusCode.UNSET),
            Arguments.of(400, StatusCode.ERROR),
            Arguments.of(404, StatusCode.ERROR),
            Arguments.of(500, StatusCode.ERROR),
            Arguments.of(503, StatusCode.ERROR)
        );
    }

    private static class SimpleMockHttpClient implements HttpClient {

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            HttpHeaders headers = new HttpHeaders()
                .set(X_MS_REQUEST_ID, X_MS_REQUEST_ID_1);

            // parent span
            SpanContext currentContext = Span.current().getSpanContext();
            assertTrue(currentContext.isValid());
            assertEquals(currentContext.getTraceId(), request.getHeaders().getValue(TRACE_PARENT).substring(3, 35));

            return Mono.just(new MockHttpResponse(request, RESPONSE_STATUS_CODE, headers));
        }
    }
}
