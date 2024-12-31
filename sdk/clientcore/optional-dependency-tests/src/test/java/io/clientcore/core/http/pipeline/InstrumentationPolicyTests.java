// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.MockHttpResponse;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpLogOptions;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.telemetry.LibraryTelemetryOptions;
import io.clientcore.core.telemetry.TelemetryOptions;
import io.clientcore.core.telemetry.TelemetryProvider;
import io.clientcore.core.util.Context;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.IdGenerator;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static io.clientcore.core.http.models.HttpHeaderName.TRACEPARENT;
import static io.clientcore.core.telemetry.TelemetryProvider.DISABLE_TRACING_KEY;
import static io.clientcore.core.telemetry.tracing.SpanKind.INTERNAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InstrumentationPolicyTests {
    private static final AttributeKey<String> ERROR_TYPE = AttributeKey.stringKey("error.type");
    private static final AttributeKey<Long> HTTP_REQUEST_RESEND_COUNT
        = AttributeKey.longKey("http.request.resend_count");
    private static final AttributeKey<String> USER_AGENT_ORIGINAL = AttributeKey.stringKey("user_agent.original");
    private static final AttributeKey<String> HTTP_REQUEST_METHOD = AttributeKey.stringKey("http.request.method");
    private static final AttributeKey<String> URL_FULL = AttributeKey.stringKey("url.full");
    private static final AttributeKey<String> SERVER_ADDRESS = AttributeKey.stringKey("server.address");
    private static final AttributeKey<Long> SERVER_PORT = AttributeKey.longKey("server.port");
    private static final AttributeKey<Long> HTTP_RESPONSE_STATUS_CODE
        = AttributeKey.longKey("http.response.status_code");
    private static final HttpHeaderName TRACESTATE = HttpHeaderName.fromString("tracestate");

    private InMemorySpanExporter exporter;
    private SdkTracerProvider tracerProvider;
    private OpenTelemetry openTelemetry;
    private TelemetryOptions<OpenTelemetry> otelOptions;

    @BeforeEach
    public void setUp() {
        exporter = InMemorySpanExporter.create();
        tracerProvider = SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(exporter)).build();

        openTelemetry = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();
        otelOptions = new TelemetryOptions<OpenTelemetry>().setProvider(openTelemetry);
    }

    @AfterEach
    public void tearDown() {
        exporter.reset();
        tracerProvider.close();
    }

    @ParameterizedTest
    @ValueSource(ints = { 200, 201, 206, 302 })
    public void simpleRequestIsRecorded(int statusCode) throws IOException {
        AtomicReference<Span> current = new AtomicReference<>();

        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new InstrumentationPolicy(otelOptions, null)).httpClient(request -> {
                assertStartAttributes((ReadableSpan) Span.current(), request.getHttpMethod(), request.getUri());
                assertNull(request.getHeaders().get(TRACESTATE));
                assertEquals(traceparent(Span.current()), request.getHeaders().get(TRACEPARENT).getValue());
                current.set(Span.current());
                return new MockHttpResponse(request, statusCode);
            }).build();

        pipeline.send(new HttpRequest(HttpMethod.GET, "https://localhost/")).close();
        assertNotNull(exporter.getFinishedSpanItems());
        assertEquals(1, exporter.getFinishedSpanItems().size());

        assertNotNull(current.get());
        SpanData exportedSpan = exporter.getFinishedSpanItems().get(0);
        assertEquals(exportedSpan.getSpanId(), current.get().getSpanContext().getSpanId());
        assertEquals(exportedSpan.getTraceId(), current.get().getSpanContext().getTraceId());
        assertHttpSpan(exportedSpan, HttpMethod.GET, "https://localhost/", statusCode);

        assertNull(exportedSpan.getAttributes().get(HTTP_REQUEST_RESEND_COUNT));
        assertNull(exportedSpan.getAttributes().get(ERROR_TYPE));
        assertNull(exportedSpan.getAttributes().get(USER_AGENT_ORIGINAL));
        assertEquals(StatusCode.UNSET, exportedSpan.getStatus().getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(ints = { 400, 404, 500, 503 })
    public void errorResponseIsRecorded(int statusCode) throws IOException {
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new InstrumentationPolicy(otelOptions, null))
            .httpClient(request -> new MockHttpResponse(request, statusCode))
            .build();

        pipeline.send(new HttpRequest(HttpMethod.GET, "https://localhost:8080/path/to/resource?query=param")).close();
        assertNotNull(exporter.getFinishedSpanItems());
        assertEquals(1, exporter.getFinishedSpanItems().size());

        SpanData exportedSpan = exporter.getFinishedSpanItems().get(0);
        assertHttpSpan(exportedSpan, HttpMethod.GET, "https://localhost:8080/path/to/resource?query=REDACTED",
            statusCode);
        assertNull(exportedSpan.getAttributes().get(HTTP_REQUEST_RESEND_COUNT));
        assertEquals(String.valueOf(statusCode), exportedSpan.getAttributes().get(ERROR_TYPE));
        assertEquals(StatusCode.ERROR, exportedSpan.getStatus().getStatusCode());
        assertEquals("", exportedSpan.getStatus().getDescription());
    }

    @SuppressWarnings("try")
    @Test
    public void tracingWithRetries() throws IOException {
        Tracer testTracer = tracerProvider.get("test");
        Span testSpan = testTracer.spanBuilder("test").startSpan();
        try (Scope scope = testSpan.makeCurrent()) {
            AtomicInteger count = new AtomicInteger(0);

            HttpPipeline pipeline = new HttpPipelineBuilder()
                .policies(new HttpRetryPolicy(), new InstrumentationPolicy(otelOptions, null))
                .httpClient(request -> {
                    assertEquals(traceparent(Span.current()), request.getHeaders().get(TRACEPARENT).getValue());
                    if (count.getAndIncrement() == 0) {
                        throw new UnknownHostException("test exception");
                    } else {
                        return new MockHttpResponse(request, 200);
                    }
                })
                .build();

            pipeline.send(new HttpRequest(HttpMethod.GET, "https://localhost:8080/path/to/resource?query=param"))
                .close();

            assertEquals(2, count.get());
            assertNotNull(exporter.getFinishedSpanItems());
            assertEquals(2, exporter.getFinishedSpanItems().size());

            SpanData failedTry = exporter.getFinishedSpanItems().get(0);
            assertHttpSpan(failedTry, HttpMethod.GET, "https://localhost:8080/path/to/resource?query=REDACTED", null);
            assertNull(failedTry.getAttributes().get(HTTP_REQUEST_RESEND_COUNT));
            assertEquals(UnknownHostException.class.getCanonicalName(), failedTry.getAttributes().get(ERROR_TYPE));
            assertEquals(StatusCode.ERROR, failedTry.getStatus().getStatusCode());
            assertEquals("test exception", failedTry.getStatus().getDescription());

            SpanData successfulTry = exporter.getFinishedSpanItems().get(1);
            assertHttpSpan(successfulTry, HttpMethod.GET, "https://localhost:8080/path/to/resource?query=REDACTED",
                200);
            assertEquals(1L, successfulTry.getAttributes().get(HTTP_REQUEST_RESEND_COUNT));
            assertNull(successfulTry.getAttributes().get(ERROR_TYPE));
        } finally {
            testSpan.end();
        }
    }

    @Test
    public void unsampledSpan() throws IOException {
        SdkTracerProvider sampleNone = SdkTracerProvider.builder()
            .setSampler(Sampler.alwaysOff())
            .addSpanProcessor(SimpleSpanProcessor.create(exporter))
            .build();
        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder().setTracerProvider(sampleNone).build();
        TelemetryOptions<OpenTelemetry> otelOptions = new TelemetryOptions<OpenTelemetry>().setProvider(openTelemetry);

        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new InstrumentationPolicy(otelOptions, null)).httpClient(request -> {
                assertTrue(Span.current().getSpanContext().isValid());
                assertEquals(traceparent(Span.current()), request.getHeaders().get(TRACEPARENT).getValue());
                return new MockHttpResponse(request, 200);
            }).build();

        pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/")).close();
        assertNotNull(exporter.getFinishedSpanItems());
        assertEquals(0, exporter.getFinishedSpanItems().size());
    }

    @Test
    @SuppressWarnings("try")
    public void tracestateIsPropagated() throws IOException {
        SpanContext parentContext
            = SpanContext.create(IdGenerator.random().generateTraceId(), IdGenerator.random().generateSpanId(),
                TraceFlags.getSampled(), TraceState.builder().put("key", "value").build());

        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new InstrumentationPolicy(otelOptions, null)).httpClient(request -> {
                assertEquals("key=value", request.getHeaders().get(TRACESTATE).getValue());
                assertEquals(traceparent(Span.current()), request.getHeaders().get(TRACEPARENT).getValue());
                return new MockHttpResponse(request, 200);
            }).build();

        try (Scope scope = Span.wrap(parentContext).makeCurrent()) {
            pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/")).close();
        }

        assertNotNull(exporter.getFinishedSpanItems());
        assertEquals(1, exporter.getFinishedSpanItems().size());
    }

    @Test
    public void otelPropagatorIsIgnored() throws IOException {
        TextMapPropagator testPropagator = new TextMapPropagator() {
            @Override
            public Collection<String> fields() {
                return Collections.singleton("foo");
            }

            @Override
            public <C> void inject(io.opentelemetry.context.Context context, C carrier, TextMapSetter<C> setter) {
                setter.set(carrier, "foo", "bar");
            }

            @Override
            public <C> io.opentelemetry.context.Context extract(io.opentelemetry.context.Context context, C carrier,
                TextMapGetter<C> getter) {
                return context;
            }
        };

        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setPropagators(ContextPropagators.create(testPropagator))
            .build();

        TelemetryOptions<OpenTelemetry> otelOptions = new TelemetryOptions<OpenTelemetry>().setProvider(openTelemetry);

        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new InstrumentationPolicy(otelOptions, null)).httpClient(request -> {
                assertEquals(traceparent(Span.current()), request.getHeaders().get(TRACEPARENT).getValue());
                return new MockHttpResponse(request, 200);
            }).build();

        pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/")).close();
    }

    @Test
    public void exceptionIsRecorded() {
        SocketException exception = new SocketException("test exception");
        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new InstrumentationPolicy(otelOptions, null)).httpClient(request -> {
                throw exception;
            }).build();

        assertThrows(UncheckedIOException.class,
            () -> pipeline.send(new HttpRequest(HttpMethod.GET, "https://localhost/")).close());
        assertNotNull(exporter.getFinishedSpanItems());
        assertEquals(1, exporter.getFinishedSpanItems().size());

        SpanData exportedSpan = exporter.getFinishedSpanItems().get(0);
        assertHttpSpan(exportedSpan, HttpMethod.GET, "https://localhost/", null);
        assertEquals(exception.getClass().getCanonicalName(), exportedSpan.getAttributes().get(ERROR_TYPE));
        assertEquals(StatusCode.ERROR, exportedSpan.getStatus().getStatusCode());
        assertEquals(exception.getMessage(), exportedSpan.getStatus().getDescription());
    }

    @Test
    public void tracingIsDisabledOnInstance() throws IOException {
        TelemetryOptions<OpenTelemetry> options
            = new TelemetryOptions<OpenTelemetry>().setTracingEnabled(false).setProvider(openTelemetry);
        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new InstrumentationPolicy(options, null)).httpClient(request -> {
                assertFalse(Span.current().getSpanContext().isValid());
                assertFalse(Span.current().isRecording());
                assertNull(request.getHeaders().get(TRACEPARENT));
                return new MockHttpResponse(request, 200);
            }).build();

        URI url = URI.create("http://localhost/");
        pipeline.send(new HttpRequest(HttpMethod.GET, url)).close();
        assertNotNull(exporter.getFinishedSpanItems());
        assertEquals(0, exporter.getFinishedSpanItems().size());
    }

    @Test
    public void tracingIsDisabledOnRequest() throws IOException {
        TelemetryOptions<OpenTelemetry> options = new TelemetryOptions<OpenTelemetry>().setProvider(openTelemetry);
        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new InstrumentationPolicy(options, null)).httpClient(request -> {
                assertFalse(Span.current().getSpanContext().isValid());
                assertFalse(Span.current().isRecording());
                assertNull(request.getHeaders().get(TRACEPARENT));
                return new MockHttpResponse(request, 200);
            }).build();

        URI url = URI.create("http://localhost/");

        RequestOptions requestOptions = new RequestOptions().setContext(Context.of(DISABLE_TRACING_KEY, true));

        pipeline.send(new HttpRequest(HttpMethod.GET, url).setRequestOptions(requestOptions)).close();
        assertNotNull(exporter.getFinishedSpanItems());
        assertEquals(0, exporter.getFinishedSpanItems().size());
    }

    @Test
    public void userAgentIsRecorded() throws IOException {
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new InstrumentationPolicy(otelOptions, null))
            .httpClient(request -> new MockHttpResponse(request, 200))
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://localhost/");
        request.getHeaders().set(HttpHeaderName.USER_AGENT, "test-user-agent");
        pipeline.send(request).close();

        assertNotNull(exporter.getFinishedSpanItems());
        assertEquals(1, exporter.getFinishedSpanItems().size());

        SpanData exportedSpan = exporter.getFinishedSpanItems().get(0);
        assertHttpSpan(exportedSpan, HttpMethod.GET, "https://localhost/", 200);

        assertEquals("test-user-agent", exportedSpan.getAttributes().get(USER_AGENT_ORIGINAL));
    }

    @Test
    public void recordRequestAndResponseHeaders() throws IOException {
        Map<HttpHeaderName, String> requestHeaders
            = Collections.singletonMap(HttpHeaderName.CLIENT_REQUEST_ID, "custom.request.id");
        Map<HttpHeaderName, String> responseHeaders
            = Collections.singletonMap(HttpHeaderName.REQUEST_ID, "custom.response.id");
        InstrumentationPolicy instrumentationPolicy
            = new InstrumentationPolicy(otelOptions, null, requestHeaders, responseHeaders);
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(instrumentationPolicy).httpClient(request -> {
            MockHttpResponse response = new MockHttpResponse(request, 200);
            response.getHeaders().set(HttpHeaderName.REQUEST_ID, "response-id");
            return response;
        }).build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://localhost/");
        request.getHeaders().set(HttpHeaderName.CLIENT_REQUEST_ID, "request-id");
        pipeline.send(request).close();

        assertNotNull(exporter.getFinishedSpanItems());
        assertEquals(1, exporter.getFinishedSpanItems().size());

        SpanData exportedSpan = exporter.getFinishedSpanItems().get(0);
        assertHttpSpan(exportedSpan, HttpMethod.GET, "https://localhost/", 200);

        assertEquals("request-id", exportedSpan.getAttributes().get(AttributeKey.stringKey("custom.request.id")));
        assertEquals("response-id", exportedSpan.getAttributes().get(AttributeKey.stringKey("custom.response.id")));
    }

    @Test
    public void missingRequestAndResponseHeaders() throws IOException {
        Map<HttpHeaderName, String> requestHeaders = new HashMap<>();
        requestHeaders.put(HttpHeaderName.CLIENT_REQUEST_ID, "custom.request.id");
        requestHeaders.put(HttpHeaderName.fromString("x-request-foo-id"), "custom.request.foo.id");

        Map<HttpHeaderName, String> responseHeaders = new HashMap<>();
        responseHeaders.put(HttpHeaderName.fromString("x-response-foo-id"), "custom.response.foo.id");

        InstrumentationPolicy instrumentationPolicy
            = new InstrumentationPolicy(otelOptions, null, requestHeaders, responseHeaders);

        HttpPipeline pipeline = new HttpPipelineBuilder().policies(instrumentationPolicy)
            .httpClient(request -> new MockHttpResponse(request, 200))
            .build();

        URI url = URI.create("https://localhost/");
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        request.getHeaders().set(HttpHeaderName.CLIENT_REQUEST_ID, "request-id");

        // should not throw
        pipeline.send(request).close();

        assertNotNull(exporter.getFinishedSpanItems());
        assertEquals(1, exporter.getFinishedSpanItems().size());

        SpanData exportedSpan = exporter.getFinishedSpanItems().get(0);
        assertHttpSpan(exportedSpan, HttpMethod.GET, "https://localhost/", 200);

        assertEquals("request-id", exportedSpan.getAttributes().get(AttributeKey.stringKey("custom.request.id")));
        assertNull(exportedSpan.getAttributes().get(AttributeKey.stringKey("custom.request.foo.id")));
        assertNull(exportedSpan.getAttributes().get(AttributeKey.stringKey("custom.response.foo.id")));
    }

    @SuppressWarnings("try")
    @Test
    public void implicitParent() throws IOException {
        Tracer testTracer = tracerProvider.get("test");
        Span testSpan = testTracer.spanBuilder("test").startSpan();

        try (Scope scope = testSpan.makeCurrent()) {
            HttpPipeline pipeline = new HttpPipelineBuilder().policies(new InstrumentationPolicy(otelOptions, null))
                .httpClient(request -> new MockHttpResponse(request, 200))
                .build();

            pipeline.send(new HttpRequest(HttpMethod.GET, "https://localhost:8080/path/to/resource?query=param"))
                .close();
        } finally {
            testSpan.end();
        }

        assertNotNull(exporter.getFinishedSpanItems());
        assertEquals(2, exporter.getFinishedSpanItems().size());

        SpanData httpSpan = exporter.getFinishedSpanItems().get(0);
        assertHttpSpan(httpSpan, HttpMethod.GET, "https://localhost:8080/path/to/resource?query=REDACTED", 200);
        assertEquals(testSpan.getSpanContext().getSpanId(), httpSpan.getParentSpanContext().getSpanId());
        assertEquals(testSpan.getSpanContext().getTraceId(), httpSpan.getSpanContext().getTraceId());
    }

    @Test
    public void explicitParent() throws IOException {
        Tracer testTracer = tracerProvider.get("test");
        Span testSpan = testTracer.spanBuilder("test").startSpan();

        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new InstrumentationPolicy(otelOptions, null))
            .httpClient(request -> new MockHttpResponse(request, 200))
            .build();

        RequestOptions requestOptions = new RequestOptions().setContext(
            Context.of(TelemetryProvider.TRACE_CONTEXT_KEY, io.opentelemetry.context.Context.current().with(testSpan)));

        pipeline.send(new HttpRequest(HttpMethod.GET, "https://localhost:8080/path/to/resource?query=param")
            .setRequestOptions(requestOptions)).close();
        testSpan.end();

        assertNotNull(exporter.getFinishedSpanItems());
        assertEquals(2, exporter.getFinishedSpanItems().size());

        SpanData httpSpan = exporter.getFinishedSpanItems().get(0);
        assertHttpSpan(httpSpan, HttpMethod.GET, "https://localhost:8080/path/to/resource?query=REDACTED", 200);
        assertEquals(testSpan.getSpanContext().getSpanId(), httpSpan.getParentSpanContext().getSpanId());
        assertEquals(testSpan.getSpanContext().getTraceId(), httpSpan.getSpanContext().getTraceId());
    }

    @Test
    public void customUrlRedaction() throws IOException {
        HttpLogOptions logOptions = new HttpLogOptions().setAllowedQueryParamNames(Collections.singleton("key1"));
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new InstrumentationPolicy(otelOptions, logOptions))
            .httpClient(request -> new MockHttpResponse(request, 200))
            .build();

        pipeline
            .send(new HttpRequest(HttpMethod.GET, "https://localhost:8080/path/to/resource?query=param&key1=value1"))
            .close();

        assertNotNull(exporter.getFinishedSpanItems());
        assertEquals(1, exporter.getFinishedSpanItems().size());

        SpanData httpSpan = exporter.getFinishedSpanItems().get(0);
        assertHttpSpan(httpSpan, HttpMethod.GET, "https://localhost:8080/path/to/resource?query=REDACTED&key1=value1",
            200);
    }

    @Test
    public void explicitLibraryCallParent() throws IOException {
        io.clientcore.core.telemetry.tracing.Tracer tracer
            = TelemetryProvider.create(otelOptions, new LibraryTelemetryOptions("test-library")).getTracer();

        RequestOptions requestOptions = new RequestOptions();
        io.clientcore.core.telemetry.tracing.Span parent
            = tracer.spanBuilder("parent", INTERNAL, requestOptions).startSpan();

        requestOptions.setContext(Context.of(TelemetryProvider.TRACE_CONTEXT_KEY, parent));

        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new InstrumentationPolicy(otelOptions, null))
            .httpClient(request -> new MockHttpResponse(request, 200))
            .build();

        pipeline.send(new HttpRequest(HttpMethod.GET, "https://localhost:8080/path/to/resource?query=param")
            .setRequestOptions(requestOptions)).close();

        parent.end();

        assertNotNull(exporter.getFinishedSpanItems());
        assertEquals(2, exporter.getFinishedSpanItems().size());

        SpanData httpSpan = exporter.getFinishedSpanItems().get(0);
        assertHttpSpan(httpSpan, HttpMethod.GET, "https://localhost:8080/path/to/resource?query=REDACTED", 200);
        assertEquals(parent.getSpanContext().getSpanId(), httpSpan.getParentSpanContext().getSpanId());
        assertEquals(parent.getSpanContext().getTraceId(), httpSpan.getSpanContext().getTraceId());
    }

    private void assertStartAttributes(ReadableSpan span, HttpMethod method, URI url) {
        assertEquals(url.toString(), span.getAttributes().get(URL_FULL));
        assertEquals(url.getHost(), span.getAttributes().get(SERVER_ADDRESS));
        assertEquals(url.getPort() == -1 ? 443L : url.getPort(), span.getAttributes().get(SERVER_PORT));
        assertEquals(method.toString(), span.getAttributes().get(HTTP_REQUEST_METHOD));
    }

    private void assertHttpSpan(SpanData span, HttpMethod method, String urlStr, Integer statusCode) {
        URI url = URI.create(urlStr);
        assertEquals(method.toString(), span.getName());
        assertEquals(url.toString(), span.getAttributes().get(URL_FULL));
        assertEquals(url.getHost(), span.getAttributes().get(SERVER_ADDRESS));
        assertEquals(url.getPort() == -1 ? 443L : url.getPort(), span.getAttributes().get(SERVER_PORT));
        assertEquals(method.toString(), span.getAttributes().get(HTTP_REQUEST_METHOD));
        if (statusCode != null) {
            assertEquals(statusCode.longValue(), span.getAttributes().get(HTTP_RESPONSE_STATUS_CODE));
        } else {
            assertNull(span.getAttributes().get(HTTP_RESPONSE_STATUS_CODE));
        }

        assertEquals("core", span.getInstrumentationScopeInfo().getName());
        assertNotNull(span.getInstrumentationScopeInfo().getVersion());
        assertEquals("https://opentelemetry.io/schemas/1.29.0", span.getInstrumentationScopeInfo().getSchemaUrl());
    }

    private String traceparent(Span span) {
        return String.format("00-%s-%s-%s", span.getSpanContext().getTraceId(), span.getSpanContext().getSpanId(),
            span.getSpanContext().getTraceFlags());
    }
}
