// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.instrumentation.Instrumentation;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.SdkInstrumentationOptions;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.models.binarydata.BinaryData;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
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
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.HistogramData;
import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
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
import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static io.clientcore.core.http.models.HttpHeaderName.TRACEPARENT;
import static io.clientcore.core.instrumentation.tracing.SpanKind.INTERNAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpInstrumentationPolicyTests {
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
    private static final HttpHeaderName CUSTOM_REQUEST_ID = HttpHeaderName.fromString("custom-request-id");

    private InMemorySpanExporter exporter;
    private SdkTracerProvider tracerProvider;
    private SdkMeterProvider meterProvider;
    private InMemoryMetricReader meterReader;
    private OpenTelemetry openTelemetry;
    private HttpInstrumentationOptions otelOptions;

    @BeforeEach
    public void setUp() {
        exporter = InMemorySpanExporter.create();
        tracerProvider = SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(exporter)).build();
        meterReader = InMemoryMetricReader.create();
        meterProvider = SdkMeterProvider.builder().registerMetricReader(meterReader).build();
        openTelemetry
            = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).setMeterProvider(meterProvider).build();
        otelOptions = new HttpInstrumentationOptions().setTelemetryProvider(openTelemetry);
    }

    @AfterEach
    public void tearDown() {
        exporter.reset();
        tracerProvider.close();
        meterProvider.close();
    }

    @ParameterizedTest
    @ValueSource(ints = { 200, 201, 206, 302 })
    public void simpleRequestIsRecorded(int statusCode) throws IOException {
        AtomicReference<Span> current = new AtomicReference<>();

        HttpPipeline pipeline
            = new HttpPipelineBuilder().addPolicy(new HttpInstrumentationPolicy(otelOptions)).httpClient(request -> {
                assertStartAttributes((ReadableSpan) Span.current(), request.getHttpMethod(), request.getUri());
                assertNull(request.getHeaders().get(TRACESTATE));
                assertEquals(traceparent(Span.current().getSpanContext()),
                    request.getHeaders().get(TRACEPARENT).getValue());
                current.set(Span.current());
                return new Response<>(request, statusCode, new HttpHeaders(), BinaryData.empty());
            }).build();

        long start = System.nanoTime();
        pipeline.send(new HttpRequest().setMethod(HttpMethod.GET).setUri("https://localhost/")).close();
        long duration = System.nanoTime() - start;

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

        HistogramData durationHistogram
            = assertDurationMetric(HttpMethod.GET, "localhost", 443, statusCode, null, exportedSpan.getSpanContext());
        List<HistogramPointData> points = new ArrayList<>(durationHistogram.getPoints());
        assertEquals(1, points.size());
        assertDuration(duration, points.get(0));
    }

    @ParameterizedTest
    @ValueSource(ints = { 400, 404, 500, 503 })
    public void errorResponseIsRecorded(int statusCode) throws IOException {
        HttpPipeline pipeline = new HttpPipelineBuilder().addPolicy(new HttpInstrumentationPolicy(otelOptions))
            .httpClient(request -> new Response<>(request, statusCode, new HttpHeaders(), BinaryData.empty()))
            .build();

        pipeline
            .send(new HttpRequest().setMethod(HttpMethod.GET)
                .setUri("https://localhost:8080/path/to/resource?query=param"))
            .close();
        assertNotNull(exporter.getFinishedSpanItems());
        assertEquals(1, exporter.getFinishedSpanItems().size());

        SpanData exportedSpan = exporter.getFinishedSpanItems().get(0);
        assertHttpSpan(exportedSpan, HttpMethod.GET, "https://localhost:8080/path/to/resource?query=REDACTED",
            statusCode);
        assertNull(exportedSpan.getAttributes().get(HTTP_REQUEST_RESEND_COUNT));
        assertEquals(String.valueOf(statusCode), exportedSpan.getAttributes().get(ERROR_TYPE));
        assertEquals(StatusCode.ERROR, exportedSpan.getStatus().getStatusCode());
        assertEquals("", exportedSpan.getStatus().getDescription());

        assertDurationMetric(HttpMethod.GET, "localhost", 8080, statusCode, String.valueOf(statusCode),
            exportedSpan.getSpanContext());
    }

    @SuppressWarnings("try")
    @Test
    public void tracingWithRetries() throws IOException {
        Tracer testTracer = tracerProvider.get("test");
        Span testSpan = testTracer.spanBuilder("test").startSpan();
        try (Scope scope = testSpan.makeCurrent()) {
            AtomicInteger count = new AtomicInteger(0);

            HttpPipeline pipeline = new HttpPipelineBuilder().addPolicy(new HttpRetryPolicy())
                .addPolicy(new HttpInstrumentationPolicy(otelOptions))
                .httpClient(request -> {
                    assertEquals(traceparent(Span.current().getSpanContext()),
                        request.getHeaders().get(TRACEPARENT).getValue());
                    if (count.getAndIncrement() == 0) {
                        throw CoreException.from(new UnknownHostException("test exception"));
                    } else {
                        return new Response<>(request, 200, new HttpHeaders(), BinaryData.empty());
                    }
                })
                .build();

            long start = System.nanoTime();
            pipeline.send(new HttpRequest().setMethod(HttpMethod.GET)
                .setUri("https://localhost:8080/path/to/resource?query=param")).close();
            long duration = System.nanoTime() - start;

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

            List<MetricData> metrics = new ArrayList<>(meterReader.collectAllMetrics());
            assertEquals(1, metrics.size());
            assertEquals("http.client.request.duration", metrics.get(0).getName());

            HistogramData histogram = (HistogramData) metrics.get(0).getData();
            List<HistogramPointData> points = new ArrayList<>(histogram.getPoints());
            assertEquals(2, points.size());
            assertDuration(duration, points.get(0));
            assertDuration(duration, points.get(1));

            HistogramPointData errorPoint = points.stream()
                .filter(p -> p.getAttributes().get(ERROR_TYPE) != null)
                .collect(Collectors.toList())
                .get(0);

            HistogramPointData successPoint = points.stream()
                .filter(p -> p.getAttributes().get(ERROR_TYPE) == null)
                .collect(Collectors.toList())
                .get(0);

            assertNull(errorPoint.getAttributes().get(HTTP_RESPONSE_STATUS_CODE));
            assertEquals(200, successPoint.getAttributes().get(HTTP_RESPONSE_STATUS_CODE));

            assertNull(successPoint.getAttributes().get(ERROR_TYPE));
            assertEquals(UnknownHostException.class.getCanonicalName(), errorPoint.getAttributes().get(ERROR_TYPE));
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
        OpenTelemetry openTelemetry
            = OpenTelemetrySdk.builder().setTracerProvider(sampleNone).setMeterProvider(meterProvider).build();
        HttpInstrumentationOptions otelOptions = new HttpInstrumentationOptions().setTelemetryProvider(openTelemetry);

        AtomicReference<SpanContext> spanContext = new AtomicReference<>();
        HttpPipeline pipeline
            = new HttpPipelineBuilder().addPolicy(new HttpInstrumentationPolicy(otelOptions)).httpClient(request -> {
                spanContext.set(Span.current().getSpanContext());
                return new Response<>(request, 200, new HttpHeaders(), BinaryData.empty());
            }).build();

        HttpRequest request = new HttpRequest().setMethod(HttpMethod.GET).setUri("http://localhost/");
        pipeline.send(request).close();

        assertTrue(spanContext.get().isValid());
        assertEquals(traceparent(spanContext.get()), request.getHeaders().get(TRACEPARENT).getValue());

        assertNotNull(exporter.getFinishedSpanItems());
        assertEquals(0, exporter.getFinishedSpanItems().size());

        assertDurationMetric(HttpMethod.GET, "localhost", 80, 200, null, spanContext.get());
    }

    @Test
    @SuppressWarnings("try")
    public void tracestateIsPropagated() throws IOException {
        SpanContext parentContext
            = SpanContext.create(IdGenerator.random().generateTraceId(), IdGenerator.random().generateSpanId(),
                TraceFlags.getSampled(), TraceState.builder().put("key", "value").build());

        HttpPipeline pipeline
            = new HttpPipelineBuilder().addPolicy(new HttpInstrumentationPolicy(otelOptions)).httpClient(request -> {
                assertEquals("key=value", request.getHeaders().get(TRACESTATE).getValue());
                assertEquals(traceparent(Span.current().getSpanContext()),
                    request.getHeaders().get(TRACEPARENT).getValue());
                return new Response<>(request, 200, new HttpHeaders(), BinaryData.empty());
            }).build();

        try (Scope scope = Span.wrap(parentContext).makeCurrent()) {
            pipeline.send(new HttpRequest().setMethod(HttpMethod.POST).setUri("http://localhost/")).close();
        }

        assertNotNull(exporter.getFinishedSpanItems());
        assertEquals(1, exporter.getFinishedSpanItems().size());
        assertDurationMetric(HttpMethod.POST, "localhost", 80, 200, null,
            exporter.getFinishedSpanItems().get(0).getSpanContext());
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

        HttpInstrumentationOptions otelOptions = new HttpInstrumentationOptions().setTelemetryProvider(openTelemetry);

        HttpPipeline pipeline
            = new HttpPipelineBuilder().addPolicy(new HttpInstrumentationPolicy(otelOptions)).httpClient(request -> {
                assertEquals(traceparent(Span.current().getSpanContext()),
                    request.getHeaders().get(TRACEPARENT).getValue());
                return new Response<>(request, 200, new HttpHeaders(), BinaryData.empty());
            }).build();

        pipeline.send(new HttpRequest().setMethod(HttpMethod.GET).setUri("http://localhost/")).close();
    }

    @Test
    public void exceptionIsRecorded() {
        SocketException exception = new SocketException("test exception");
        HttpPipeline pipeline
            = new HttpPipelineBuilder().addPolicy(new HttpInstrumentationPolicy(otelOptions)).httpClient(request -> {
                throw CoreException.from(exception);
            }).build();

        assertThrows(CoreException.class,
            () -> pipeline.send(new HttpRequest().setMethod(HttpMethod.GET).setUri("https://localhost/")).close());
        assertNotNull(exporter.getFinishedSpanItems());
        assertEquals(1, exporter.getFinishedSpanItems().size());

        SpanData exportedSpan = exporter.getFinishedSpanItems().get(0);
        assertHttpSpan(exportedSpan, HttpMethod.GET, "https://localhost/", null);
        assertEquals(exception.getClass().getCanonicalName(), exportedSpan.getAttributes().get(ERROR_TYPE));
        assertEquals(StatusCode.ERROR, exportedSpan.getStatus().getStatusCode());
        assertEquals(exception.getMessage(), exportedSpan.getStatus().getDescription());
        assertDurationMetric(HttpMethod.GET, "localhost", 443, -1, exception.getClass().getCanonicalName(),
            exportedSpan.getSpanContext());
    }

    @Test
    public void tracingIsDisabledOnInstance() throws IOException {
        HttpInstrumentationOptions options
            = new HttpInstrumentationOptions().setTracingEnabled(false).setTelemetryProvider(openTelemetry);
        HttpPipeline pipeline
            = new HttpPipelineBuilder().addPolicy(new HttpInstrumentationPolicy(options)).httpClient(request -> {
                assertFalse(Span.current().getSpanContext().isValid());
                assertFalse(Span.current().isRecording());
                assertNull(request.getHeaders().get(TRACEPARENT));
                return new Response<>(request, 200, new HttpHeaders(), BinaryData.empty());
            }).build();

        URI url = URI.create("http://localhost/");
        pipeline.send(new HttpRequest().setMethod(HttpMethod.GET).setUri(url)).close();
        assertNotNull(exporter.getFinishedSpanItems());
        assertEquals(0, exporter.getFinishedSpanItems().size());

        assertDurationMetric(HttpMethod.GET, "localhost", 80, 200, null, SpanContext.getInvalid());
    }

    @Test
    public void metricsDisabledOnInstance() throws IOException {
        HttpInstrumentationOptions options
            = new HttpInstrumentationOptions().setMetricsEnabled(false).setTelemetryProvider(openTelemetry);
        HttpPipeline pipeline
            = new HttpPipelineBuilder().addPolicy(new HttpInstrumentationPolicy(options)).httpClient(request -> {
                assertTrue(Span.current().getSpanContext().isValid());
                assertTrue(Span.current().isRecording());
                assertNotNull(request.getHeaders().get(TRACEPARENT));
                return new Response<>(request, 200, new HttpHeaders(), BinaryData.empty());
            }).build();

        URI url = URI.create("http://localhost/");
        pipeline.send(new HttpRequest().setMethod(HttpMethod.GET).setUri(url)).close();
        assertNotNull(exporter.getFinishedSpanItems());
        assertEquals(1, exporter.getFinishedSpanItems().size());
        assertEquals(0, meterReader.collectAllMetrics().size());
    }

    @Test
    public void userAgentIsRecorded() throws IOException {
        HttpPipeline pipeline = new HttpPipelineBuilder().addPolicy(new HttpInstrumentationPolicy(otelOptions))
            .httpClient(request -> new Response<>(request, 200, new HttpHeaders(), BinaryData.empty()))
            .build();

        HttpRequest request = new HttpRequest().setMethod(HttpMethod.GET).setUri("https://localhost/");
        request.getHeaders().set(HttpHeaderName.USER_AGENT, "test-user-agent");
        pipeline.send(request).close();

        assertNotNull(exporter.getFinishedSpanItems());
        assertEquals(1, exporter.getFinishedSpanItems().size());

        SpanData exportedSpan = exporter.getFinishedSpanItems().get(0);
        assertHttpSpan(exportedSpan, HttpMethod.GET, "https://localhost/", 200);

        assertEquals("test-user-agent", exportedSpan.getAttributes().get(USER_AGENT_ORIGINAL));
    }

    @Test
    public void enrichSpans() throws IOException {
        HttpInstrumentationPolicy httpInstrumentationPolicy = new HttpInstrumentationPolicy(
            otelOptions.setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.HEADERS));

        HttpPipelinePolicy enrichingPolicy = new HttpPipelinePolicy() {
            @Override
            public Response<BinaryData> process(HttpRequest request, HttpPipelineNextPolicy next) {
                io.clientcore.core.instrumentation.tracing.Span span
                    = request.getContext().getInstrumentationContext().getSpan();
                if (span.isRecording()) {
                    span.setAttribute("custom.request.id", request.getHeaders().getValue(CUSTOM_REQUEST_ID));
                }

                return next.process();
            }

            @Override
            public HttpPipelinePosition getPipelinePosition() {
                return HttpPipelinePosition.AFTER_INSTRUMENTATION;
            }
        };

        HttpPipeline pipeline = new HttpPipelineBuilder().addPolicy(httpInstrumentationPolicy)
            .addPolicy(enrichingPolicy)
            .httpClient(request -> new Response<>(request, 200, new HttpHeaders(), BinaryData.empty()))
            .build();

        HttpRequest request = new HttpRequest().setMethod(HttpMethod.GET).setUri("https://localhost/");
        request.getHeaders().set(CUSTOM_REQUEST_ID, "42");

        pipeline.send(request).close();

        assertNotNull(exporter.getFinishedSpanItems());
        assertEquals(1, exporter.getFinishedSpanItems().size());

        SpanData exportedSpan = exporter.getFinishedSpanItems().get(0);
        assertHttpSpan(exportedSpan, HttpMethod.GET, "https://localhost/", 200);

        assertEquals("42", exportedSpan.getAttributes().get(AttributeKey.stringKey("custom.request.id")));
    }

    @SuppressWarnings("try")
    @Test
    public void implicitParent() throws IOException {
        Tracer testTracer = tracerProvider.get("test");
        Span testSpan = testTracer.spanBuilder("test").startSpan();

        try (Scope scope = testSpan.makeCurrent()) {
            HttpPipeline pipeline = new HttpPipelineBuilder().addPolicy(new HttpInstrumentationPolicy(otelOptions))
                .httpClient(request -> new Response<>(request, 200, new HttpHeaders(), BinaryData.empty()))
                .build();

            pipeline.send(new HttpRequest().setMethod(HttpMethod.GET)
                .setUri("https://localhost:8080/path/to/resource?query=param")).close();
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

        HttpPipeline pipeline = new HttpPipelineBuilder().addPolicy(new HttpInstrumentationPolicy(otelOptions))
            .httpClient(request -> new Response<>(request, 200, new HttpHeaders(), BinaryData.empty()))
            .build();

        RequestContext context = RequestContext.builder()
            .setInstrumentationContext(Instrumentation.createInstrumentationContext(testSpan))
            .build();

        pipeline.send(new HttpRequest().setMethod(HttpMethod.GET)
            .setUri("https://localhost:8080/path/to/resource?query=param")
            .setContext(context)).close();
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
        otelOptions.setAllowedQueryParamNames(Collections.singleton("key1"));
        HttpPipeline pipeline = new HttpPipelineBuilder().addPolicy(new HttpInstrumentationPolicy(otelOptions))
            .httpClient(request -> new Response<>(request, 200, new HttpHeaders(), BinaryData.empty()))
            .build();

        pipeline.send(new HttpRequest().setMethod(HttpMethod.GET)
            .setUri("https://localhost:8080/path/to/resource?query=param&key1=value1")).close();

        assertNotNull(exporter.getFinishedSpanItems());
        assertEquals(1, exporter.getFinishedSpanItems().size());

        SpanData httpSpan = exporter.getFinishedSpanItems().get(0);
        assertHttpSpan(httpSpan, HttpMethod.GET, "https://localhost:8080/path/to/resource?query=REDACTED&key1=value1",
            200);
    }

    @Test
    public void explicitLibraryCallParent() throws IOException {
        io.clientcore.core.instrumentation.tracing.Tracer tracer
            = Instrumentation.create(otelOptions, new SdkInstrumentationOptions("test-library")).getTracer();

        io.clientcore.core.instrumentation.tracing.Span parent
            = tracer.spanBuilder("parent", INTERNAL, null).startSpan();

        RequestContext context
            = RequestContext.builder().setInstrumentationContext(parent.getInstrumentationContext()).build();

        HttpPipeline pipeline = new HttpPipelineBuilder().addPolicy(new HttpInstrumentationPolicy(otelOptions))
            .httpClient(request -> new Response<>(request, 200, new HttpHeaders(), BinaryData.empty()))
            .build();

        pipeline.send(new HttpRequest().setMethod(HttpMethod.GET)
            .setUri("https://localhost:8080/path/to/resource?query=param")
            .setContext(context)).close();

        parent.end();

        assertNotNull(exporter.getFinishedSpanItems());
        assertEquals(2, exporter.getFinishedSpanItems().size());

        SpanData httpSpan = exporter.getFinishedSpanItems().get(0);
        assertHttpSpan(httpSpan, HttpMethod.GET, "https://localhost:8080/path/to/resource?query=REDACTED", 200);

        InstrumentationContext parentContext = parent.getInstrumentationContext();
        assertEquals(parentContext.getSpanId(), httpSpan.getParentSpanContext().getSpanId());
        assertEquals(parentContext.getTraceId(), httpSpan.getSpanContext().getTraceId());
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

        assertInstrumentationScope(span.getInstrumentationScopeInfo());
    }

    private String traceparent(SpanContext context) {
        return String.format("00-%s-%s-%s", context.getTraceId(), context.getSpanId(), context.getTraceFlags());
    }

    private HistogramData assertDurationMetric(HttpMethod method, String host, int port, int statusCode,
        String errorType, SpanContext spanContext) {
        AttributesBuilder attributesBuilder = Attributes.builder()
            .put(AttributeKey.stringKey("server.address"), host)
            .put(AttributeKey.longKey("server.port"), (long) port)
            .put(AttributeKey.stringKey("http.request.method"), method.toString());
        if (statusCode > 0) {
            attributesBuilder.put(AttributeKey.longKey("http.response.status_code"), (long) statusCode);
        }

        if (errorType != null) {
            attributesBuilder.put(AttributeKey.stringKey("error.type"), errorType);
        }

        Collection<MetricData> metrics = meterReader.collectAllMetrics();
        assertThat(metrics).satisfiesExactly(metric -> OpenTelemetryAssertions.assertThat(metric)
            .hasName("http.client.request.duration")
            .hasDescription("Duration of HTTP client requests")
            .hasUnit("s")
            .hasHistogramSatisfying(h -> h.isCumulative().hasPointsSatisfying(point -> {
                point.hasAttributes(attributesBuilder.build())
                    .hasCount(1)
                    .hasBucketBoundaries(0.005d, 0.01d, 0.025d, 0.05d, 0.075d, 0.1d, 0.25d, 0.5d, 0.75d, 1d, 2.5d, 5d,
                        7.5d, 10d);

                if (spanContext.isSampled()) {
                    point.hasExemplarsSatisfying(
                        exemplar -> exemplar.hasTraceId(spanContext.getTraceId()).hasSpanId(spanContext.getSpanId()));
                }
            })));

        assertThat(metrics).satisfiesExactly(m -> assertInstrumentationScope(m.getInstrumentationScopeInfo()));

        return (HistogramData) metrics.iterator().next().getData();
    }

    private void assertInstrumentationScope(InstrumentationScopeInfo scope) {
        assertThat(scope).satisfies(info -> {
            assertThat(info.getName()).isEqualTo("core");
            assertThat(info.getVersion()).isNotNull();
            assertThat(info.getSchemaUrl()).isEqualTo("https://opentelemetry.io/schemas/1.29.0");
        });
    }

    private void assertDuration(long approxDurationNs, HistogramPointData data) {
        assertThat((long) (data.getSum() * 1_000_000_000L)).isStrictlyBetween(0L, approxDurationNs + 1);
    }
}
