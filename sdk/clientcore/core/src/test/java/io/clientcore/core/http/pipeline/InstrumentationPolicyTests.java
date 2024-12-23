package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.MockHttpResponse;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.observability.ObservabilityOptions;
import io.clientcore.core.observability.tracing.Tracer;
import io.clientcore.core.util.Context;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.SocketException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InstrumentationPolicyTests {

    private InMemorySpanExporter exporter;
    private SdkTracerProvider tracerProvider;
    private OpenTelemetry openTelemetry;
    private ObservabilityOptions<OpenTelemetry> otelOptions;

    @BeforeEach
    public void setUp(TestInfo testInfo) {
        exporter = InMemorySpanExporter.create();
        tracerProvider = SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(exporter)).build();

        openTelemetry = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();
        otelOptions = new ObservabilityOptions<OpenTelemetry>().setProvider(openTelemetry);
    }

    @AfterEach
    public void tearDown() {
        exporter.reset();
        tracerProvider.close();
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 201, 206, 302})
    public void simpleRequestIsRecorded(int statusCode) throws IOException {
        AtomicReference<Span> current = new AtomicReference<>();

        HttpPipeline pipeline  = new HttpPipelineBuilder().policies(new InstrumentationPolicy(otelOptions))
            .httpClient(request -> {
                assertStartAttributes((ReadableSpan) Span.current(), request.getHttpMethod(), request.getUri());
                current.set(Span.current());
                return new MockHttpResponse(request, statusCode);
            }).build();

        URI url = URI.create("https://localhost/");
        pipeline.send(new HttpRequest(HttpMethod.GET, url)).close();
        assertNotNull(exporter.getFinishedSpanItems());
        assertEquals(1, exporter.getFinishedSpanItems().size());

        assertNotNull(current.get());
        SpanData exportedSpan = exporter.getFinishedSpanItems().get(0);
        assertEquals(exportedSpan.getSpanId(), current.get().getSpanContext().getSpanId());
        assertEquals(exportedSpan.getTraceId(), current.get().getSpanContext().getTraceId());
        assertHttpSpan(exportedSpan, HttpMethod.GET, url, statusCode);

        assertNull(exportedSpan.getAttributes().get(AttributeKey.stringKey("error.type")));
        assertEquals(StatusCode.UNSET, exportedSpan.getStatus().getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 404, 500, 503})
    public void errorResponseIsRecorded(int statusCode) throws IOException {
        HttpPipeline pipeline  = new HttpPipelineBuilder().policies(new InstrumentationPolicy(otelOptions))
            .httpClient(request -> new MockHttpResponse(request, statusCode)).build();

        URI url = URI.create("https://localhost:8080/path/to/resource?query=param");
        pipeline.send(new HttpRequest(HttpMethod.GET, url)).close();
        assertNotNull(exporter.getFinishedSpanItems());
        assertEquals(1, exporter.getFinishedSpanItems().size());

        SpanData exportedSpan = exporter.getFinishedSpanItems().get(0);
        assertHttpSpan(exportedSpan, HttpMethod.GET, url, statusCode);
        assertEquals(String.valueOf(statusCode), exportedSpan.getAttributes().get(AttributeKey.stringKey("error.type")));
        assertEquals(StatusCode.ERROR, exportedSpan.getStatus().getStatusCode());
        assertEquals("", exportedSpan.getStatus().getDescription());
    }

    @Test
    public void unsampledSpan() throws IOException {
        SdkTracerProvider sampleNone = SdkTracerProvider.builder()
            .setSampler(Sampler.alwaysOff())
            .addSpanProcessor(SimpleSpanProcessor.create(exporter)).build();
        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder().setTracerProvider(sampleNone).build();
        ObservabilityOptions<OpenTelemetry> otelOptions = new ObservabilityOptions<OpenTelemetry>().setProvider(openTelemetry);

        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new InstrumentationPolicy(otelOptions))
            .httpClient(request -> {
                    assertTrue(Span.current().getSpanContext().isValid());
                    return new MockHttpResponse(request, 200);
                })
            .build();

        // just a sanity check - should not throw
        pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/")).close();
        assertNotNull(exporter.getFinishedSpanItems());
        assertEquals(0, exporter.getFinishedSpanItems().size());
    }

    @Test
    public void exceptionIsRecorded() {
        SocketException exception = new SocketException("test exception");
        HttpPipeline pipeline  = new HttpPipelineBuilder().policies(new InstrumentationPolicy(otelOptions))
            .httpClient(request -> {
                throw exception;
            }).build();

        URI url = URI.create("http://localhost/");
        assertThrows(UncheckedIOException.class, () -> pipeline.send(new HttpRequest(HttpMethod.GET, url)).close());
        assertNotNull(exporter.getFinishedSpanItems());
        assertEquals(1, exporter.getFinishedSpanItems().size());

        SpanData exportedSpan = exporter.getFinishedSpanItems().get(0);
        assertHttpSpan(exportedSpan, HttpMethod.GET, url, null);
        assertEquals(exception.getClass().getCanonicalName(), exportedSpan.getAttributes().get(AttributeKey.stringKey("error.type")));
        assertEquals(StatusCode.ERROR, exportedSpan.getStatus().getStatusCode());
        assertEquals(exception.getMessage(), exportedSpan.getStatus().getDescription());
    }

    @Test
    public void tracingIsDisabledOnInstance() throws IOException {
        ObservabilityOptions<OpenTelemetry> options = new ObservabilityOptions<OpenTelemetry>().setTracingEnabled(false).setProvider(openTelemetry);
        HttpPipeline pipeline  = new HttpPipelineBuilder().policies(new InstrumentationPolicy(options))
            .httpClient(request -> {
                assertFalse(Span.current().getSpanContext().isValid());
                assertFalse(Span.current().isRecording());
                return new MockHttpResponse(request, 200);
            }).build();

        URI url = URI.create("http://localhost/");
        pipeline.send(new HttpRequest(HttpMethod.GET, url)).close();
        assertNotNull(exporter.getFinishedSpanItems());
        assertEquals(0, exporter.getFinishedSpanItems().size());
    }

    @Test
    public void tracingIsDisabledOnRequest() throws IOException {
        ObservabilityOptions<OpenTelemetry> options = new ObservabilityOptions<OpenTelemetry>().setProvider(openTelemetry);
        HttpPipeline pipeline  = new HttpPipelineBuilder().policies(new InstrumentationPolicy(options))
            .httpClient(request -> {
                assertFalse(Span.current().getSpanContext().isValid());
                assertFalse(Span.current().isRecording());
                return new MockHttpResponse(request, 200);
            }).build();

        URI url = URI.create("http://localhost/");

        RequestOptions requestOptions = new RequestOptions()
            .setContext(Context.of(Tracer.DISABLE_TRACING_KEY, true));

        pipeline.send(new HttpRequest(HttpMethod.GET, url).setRequestOptions(requestOptions)).close();
        assertNotNull(exporter.getFinishedSpanItems());
        assertEquals(0, exporter.getFinishedSpanItems().size());
    }

    private void assertStartAttributes(ReadableSpan span, HttpMethod method, URI url) {
        assertEquals(url.toString(), span.getAttributes().get(AttributeKey.stringKey("url.full")));
        assertEquals(url.getHost(), span.getAttributes().get(AttributeKey.stringKey("server.address")));
        assertEquals(url.getPort() == -1 ? 443L : url.getPort(), span.getAttributes().get(AttributeKey.longKey("server.port")));
        assertEquals(method.toString(), span.getAttributes().get(AttributeKey.stringKey("http.request.method")));
    }

    private void assertHttpSpan(SpanData span, HttpMethod method, URI url, Integer statusCode) {
        assertEquals(method.toString(), span.getName());
        assertEquals(url.toString(), span.getAttributes().get(AttributeKey.stringKey("url.full")));
        assertEquals(url.getHost(), span.getAttributes().get(AttributeKey.stringKey("server.address")));
        assertEquals(url.getPort() == -1 ? 443L : url.getPort(), span.getAttributes().get(AttributeKey.longKey("server.port")));
        assertEquals(method.toString(), span.getAttributes().get(AttributeKey.stringKey("http.request.method")));
        if (statusCode != null) {
            assertEquals(statusCode.longValue(), span.getAttributes().get(AttributeKey.longKey("http.response.status_code")));
        } else {
            assertNull(span.getAttributes().get(AttributeKey.longKey("http.response.status_code")));
        }

        assertEquals("clientcore", span.getInstrumentationScopeInfo().getName());
        assertEquals("1.0.0", span.getInstrumentationScopeInfo().getVersion());
        assertEquals("https://opentelemetry.io/schemas/1.29.0", span.getInstrumentationScopeInfo().getSchemaUrl());
    }
}
