// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import com.azure.core.http.HttpClient;
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
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {com.azure.core.implementation.http.policy.InstrumentationPolicy.
 */
public class OpenTelemetryHttpPolicyTests {

    private static final String X_MS_REQUEST_ID_1 = "response id 1";
    private static final String X_MS_REQUEST_ID_2 = "response id 2";
    private static final int RESPONSE_STATUS_CODE = 201;
    private InMemorySpanExporter exporter;
    private SdkTracerProvider tracerProvider;
    private Tracer tracer;
    private com.azure.core.util.tracing.Tracer azTracer;
    private static final String SPAN_NAME = "foo";

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
        // Start user parent span.
        Span parentSpan = tracer.spanBuilder(SPAN_NAME).startSpan();

        // Add parent span to tracingContext
        Context tracingContext = new Context(PARENT_TRACE_CONTEXT_KEY, io.opentelemetry.context.Context.root().with(parentSpan))
            .addData("az.namespace", "foo");

        // Act
        HttpRequest request = new HttpRequest(HttpMethod.POST, "https://httpbin.org/hello?there#otel");
        request.setHeader("User-Agent", "user-agent");
        HttpResponse response =  createHttpPipeline(azTracer).send(request, tracingContext).block();

        // Assert
        List<SpanData> exportedSpans = exporter.getFinishedSpanItems();
        // rest proxy span is not exported as global otel is not configured
        assertEquals(1, exportedSpans.size());

        SpanData httpSpan = exportedSpans.get(0);

        assertEquals(request.getHeaders().getValue("traceparent"), String.format("00-%s-%s-01", httpSpan.getTraceId(), httpSpan.getSpanId()));
        assertEquals(((ReadableSpan) parentSpan).getSpanContext().getSpanId(), httpSpan.getParentSpanId());
        assertEquals("HTTP POST", httpSpan.getName());

        Map<String, Object> httpAttributes = getAttributes(httpSpan);

        assertEquals(6, httpAttributes.size());
        assertEquals("https://httpbin.org/hello?there#otel", httpAttributes.get("http.url"));
        assertEquals("POST", httpAttributes.get("http.method"));
        assertEquals("user-agent", httpAttributes.get("http.user_agent"));
        assertEquals("foo", httpAttributes.get("az.namespace"));
        assertEquals(Long.valueOf(RESPONSE_STATUS_CODE), httpAttributes.get("http.status_code"));
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
                        if (k.getKey() == "http.url") {
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
        HttpResponse response =  createHttpPipeline(new OpenTelemetryTracer("test", null, null,
                new OpenTelemetryTracingOptions().setProvider(providerWithSampler)))
            .send(request).block();

        // Assert
        List<SpanData> exportedSpans = exporter.getFinishedSpanItems();
        assertEquals(0, exportedSpans.size());
        assertTrue(samplerCalled.get());
    }

    @Test
    public void clientRequestIdIsStamped() {
        HttpRequest request = new HttpRequest(HttpMethod.PUT, "https://httpbin.org/hello?there#otel");
        HttpResponse response =  createHttpPipeline(azTracer, new RequestIdPolicy()).send(request).block();

        // Assert
        List<SpanData> exportedSpans = exporter.getFinishedSpanItems();
        assertEquals(1, exportedSpans.size());

        assertEquals("HTTP PUT", exportedSpans.get(0).getName());

        Map<String, Object> httpAttributes = getAttributes(exportedSpans.get(0));
        assertEquals(5, httpAttributes.size());

        assertEquals(response.getRequest().getHeaders().getValue("x-ms-client-request-id"), httpAttributes.get("az.client_request_id"));
        assertEquals(X_MS_REQUEST_ID_1, httpAttributes.get("az.service_request_id"));

        assertEquals("https://httpbin.org/hello?there#otel", httpAttributes.get("http.url"));
        assertEquals("PUT", httpAttributes.get("http.method"));
        assertEquals(Long.valueOf(RESPONSE_STATUS_CODE), httpAttributes.get("http.status_code"));
    }

    @Test
    public void everyTryIsTraced() {
        AtomicInteger attemptCount = new AtomicInteger();
        AtomicReference<String> traceparentTry503 = new AtomicReference<>();
        AtomicReference<String> traceparentTry200 = new AtomicReference<>();
        AtomicReference<Span> currentSpanTry503 = new AtomicReference<>();
        AtomicReference<Span> currentSpanTry200 = new AtomicReference<>();

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
                    traceparentTry503.set(request.getHeaders().getValue("traceparent"));
                    currentSpanTry503.set(Span.current());
                    headers.set("x-ms-request-id", X_MS_REQUEST_ID_1);
                    return Mono.just(new MockHttpResponse(request, 503, headers));
                } else if (count == 1) {
                    traceparentTry200.set(request.getHeaders().getValue("traceparent"));
                    currentSpanTry200.set(Span.current());
                    headers.set("x-ms-request-id", X_MS_REQUEST_ID_2);
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

        assertEquals(currentSpanTry503.get().getSpanContext().getSpanId(), try503.getSpanId());
        assertEquals(currentSpanTry200.get().getSpanContext().getSpanId(), try200.getSpanId());

        assertEquals("HTTP GET", try503.getName());
        Map<String, Object> httpAttributes503 = getAttributes(try503);
        assertEquals(5, httpAttributes503.size());
        assertEquals(Long.valueOf(503), httpAttributes503.get("http.status_code"));
        assertEquals(X_MS_REQUEST_ID_1, httpAttributes503.get("az.service_request_id"));

        assertEquals("HTTP GET", try503.getName());
        Map<String, Object> httpAttributes200 = getAttributes(try200);
        assertEquals(5, httpAttributes200.size());
        assertEquals(Long.valueOf(200), httpAttributes200.get("http.status_code"));
        assertEquals(X_MS_REQUEST_ID_2, httpAttributes200.get("az.service_request_id"));
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
    }

    private Map<String, Object> getAttributes(SpanData span) {
        Map<String, Object> attributes = new HashMap<>();
        span.getAttributes().forEach((k, v) -> attributes.put(k.getKey(), v));

        return attributes;
    }

    private static HttpPipeline createHttpPipeline(com.azure.core.util.tracing.Tracer azTracer, HttpPipelinePolicy... beforeRetryPolicies) {
        List<HttpPipelinePolicy> policies = new ArrayList<>(Arrays.asList(beforeRetryPolicies));
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        final HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(new SimpleMockHttpClient())
            .tracer(azTracer)
            .build();


        return httpPipeline;
    }

    private static class SimpleMockHttpClient implements HttpClient {

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            HttpHeaders headers = new HttpHeaders()
                .set("x-ms-request-id", X_MS_REQUEST_ID_1);

            SpanContext currentContext = Span.current().getSpanContext();
            String expectedTraceparent = String.format("00-%s-%s-%s", currentContext.getTraceId(), currentContext.getSpanId(), currentContext.getTraceFlags().toString());
            if (currentContext.isValid()) {
                assertEquals(expectedTraceparent, request.getHeaders().getValue("traceparent"));
            } else {
                assertNull(request.getHeaders().getValue("traceparent"));
            }

            return Mono.just(new MockHttpResponse(request, RESPONSE_STATUS_CODE, headers));
        }
    }

}
