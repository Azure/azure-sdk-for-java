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
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link OpenTelemetryHttpPolicy}.
 */
public class OpenTelemetryHttpPolicyTests {

    private static final String X_MS_REQUEST_ID = "response id";
    private TestExporter exporter;
    private Tracer tracer;

    @BeforeEach
    public void setUp() {
        exporter = new TestExporter();
        SdkTracerProvider otelProvider = SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(exporter)).build();

        tracer = OpenTelemetrySdk.builder().setTracerProvider(otelProvider).build().getTracer("TracerSdkTest");
    }

    @Test
    public void addAfterPolicyTest() {
        // Arrange & Act
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        HttpPolicyProviders.addAfterRetryPolicies(policies);

        // Assert
        assertEquals(1, policies.size());
        assertEquals(OpenTelemetryHttpPolicy.class, policies.get(0).getClass());
    }

    @Test
    public void openTelemetryHttpPolicyTest() {
        // Start user parent span.
        Span parentSpan = tracer.spanBuilder(PARENT_SPAN_KEY).startSpan();

        // Add parent span to tracingContext
        Context tracingContext = new Context(PARENT_SPAN_KEY, parentSpan)
            .addData(AZ_TRACING_NAMESPACE_KEY, "foo");

        // Act
        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://httpbin.org/hello?there#otel");
        request.setHeader("User-Agent", "user-agent");
        HttpResponse response =  createHttpPipeline(tracer).send(request, tracingContext).block();

        // Assert
        List<SpanData> exportedSpans = exporter.getSpans();
        // rest proxy span is not exported as global otel is not configured
        assertEquals(1, exportedSpans.size());

        SpanData httpSpan = exportedSpans.get(0);

        assertEquals(request.getHeaders().getValue("Traceparent"), String.format("00-%s-%s-01", httpSpan.getTraceId(), httpSpan.getSpanId()));
        assertEquals(((ReadableSpan)parentSpan).getSpanContext().getSpanId(), httpSpan.getParentSpanId());
        assertEquals("/hello", httpSpan.getName());

        Map<String, Object> httpAttributes = getAttributes(httpSpan);

        assertEquals(6, httpAttributes.size());
        assertEquals("https://httpbin.org/hello?there#otel", httpAttributes.get("http.url"));
        assertEquals("GET", httpAttributes.get("http.method"));
        assertEquals("user-agent", httpAttributes.get("http.user_agent"));
        assertEquals("foo", httpAttributes.get(AZ_TRACING_NAMESPACE_KEY));
        assertEquals( 201l, httpAttributes.get("http.status_code"));
        assertEquals(X_MS_REQUEST_ID, httpAttributes.get("x-ms-request-id"));
    }

    private Map<String, Object> getAttributes(SpanData span) {
        Map<String, Object> attributes = new HashMap<>();
        span.getAttributes().forEach((k, v) -> attributes.put(k.getKey(), v));

        return attributes;
    }

    private static HttpPipeline createHttpPipeline(Tracer tracer) {
        final HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .httpClient(new SimpleMockHttpClient())
            .policies(new OpenTelemetryHttpPolicy(tracer))
            .build();
        return httpPipeline;
    }

    private static class SimpleMockHttpClient implements HttpClient {

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            HttpHeaders headers = new HttpHeaders()
                .set("Content-Type", "application/json")
                .set("x-ms-request-id", X_MS_REQUEST_ID);

            HttpResponse response = new MockHttpResponse(request, headers);
            return Mono.just(response);
        }
    }

    private static class MockHttpResponse extends HttpResponse {
        private final HttpHeaders headers;
        private final Flux<ByteBuffer> body = Flux.just(ByteBuffer.wrap(new byte[0]));

        MockHttpResponse(HttpRequest request, HttpHeaders headers) {
            super(request);
            this.headers = headers;
        }

        @Override
        public int getStatusCode() {
            return 201;
        }

        @Override
        public String getHeaderValue(String name) {
            return headers.getValue(name);
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            return body;
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return FluxUtil.collectBytesInByteBufferStream(body);
        }

        @Override
        public Mono<String> getBodyAsString() {
            return getBodyAsString(StandardCharsets.UTF_8);
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
        }
    }

    static class TestExporter implements SpanExporter {

        private final List<SpanData> exportedSpans = new ArrayList<>();
        @Override
        public CompletableResultCode export(Collection<SpanData> spans) {
            exportedSpans.addAll(spans);
            return CompletableResultCode.ofSuccess();
        }

        @Override
        public CompletableResultCode flush() {
            return CompletableResultCode.ofSuccess();
        }

        @Override
        public CompletableResultCode shutdown() {
            return CompletableResultCode.ofSuccess();
        }

        public List<SpanData> getSpans() {
            return exportedSpans;
        }
    }

}
