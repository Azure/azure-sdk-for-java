// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.MetricData;
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
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;
import java.util.Collection;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OperationInstrumentationTests {
    private static final LibraryInstrumentationOptions DEFAULT_LIB_OPTIONS
        = new LibraryInstrumentationOptions("test-lib").setLibraryVersion("1.0.0")
            .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0");
    private static final URI DEFAULT_ENDPOINT = URI.create("https://localhost");

    private InMemorySpanExporter exporter;
    private SdkTracerProvider tracerProvider;
    private SdkMeterProvider meterProvider;
    private InMemoryMetricReader meterReader;
    private TestClock testClock;

    private InstrumentationOptions otelOptions;

    @BeforeEach
    public void setUp() {
        exporter = InMemorySpanExporter.create();
        tracerProvider = SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(exporter)).build();
        testClock = TestClock.create();
        meterReader = InMemoryMetricReader.create();

        meterProvider = SdkMeterProvider.builder().setClock(testClock).registerMetricReader(meterReader).build();

        OpenTelemetry openTelemetry
            = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).setMeterProvider(meterProvider).build();
        otelOptions = new InstrumentationOptions().setTelemetryProvider(openTelemetry);
    }

    @AfterEach
    public void tearDown() {
        exporter.reset();
        tracerProvider.close();
    }

    @Test
    public void invalidArguments() {
        Instrumentation instrumentation = Instrumentation.create(otelOptions, DEFAULT_LIB_OPTIONS);
        assertThrows(NullPointerException.class, () -> instrumentation.createOperationInstrumentation(null));

        assertThrows(NullPointerException.class, () -> new InstrumentedOperationDetails(null, "call"));
        assertThrows(NullPointerException.class, () -> new InstrumentedOperationDetails("test", null));

        OperationInstrumentation instr
            = instrumentation.createOperationInstrumentation(new InstrumentedOperationDetails("test", "call"));
        assertThrows(NullPointerException.class, () -> instr.startScope(null));
        assertThrows(IllegalStateException.class, () -> instr.startScope(RequestOptions.none()));
    }

    @ParameterizedTest
    @MethodSource("spanKindSource")
    public void basicCall(SpanKind kind) {
        Instrumentation instrumentation = Instrumentation.create(otelOptions, DEFAULT_LIB_OPTIONS);
        OperationInstrumentation call = instrumentation.createOperationInstrumentation(
            new InstrumentedOperationDetails("test", "call").spanKind(kind).endpoint(DEFAULT_ENDPOINT));

        RequestOptions options = new RequestOptions();
        OperationInstrumentation.Scope scope = call.startScope(options);

        Span current = Span.current();
        assertTrue(current.getSpanContext().isValid());
        assertEquals(current.getSpanContext().getTraceId(), scope.getInstrumentationContext().getTraceId());
        assertEquals(current.getSpanContext().getSpanId(), scope.getInstrumentationContext().getSpanId());

        scope.close();
        assertFalse(Span.current().getSpanContext().isValid());
        assertEquals(1, exporter.getFinishedSpanItems().size());
        assertCallSpan(exporter.getFinishedSpanItems().get(0), "call", kind, "localhost", 443L, null);

        Collection<MetricData> metrics = meterReader.collectAllMetrics();
        assertDurationMetric(metrics, "test", "call", "localhost", 443L, null, current.getSpanContext());
    }

    @Test
    public void callWithError() {
        Instrumentation instrumentation = Instrumentation.create(otelOptions, DEFAULT_LIB_OPTIONS);
        OperationInstrumentation call = instrumentation.createOperationInstrumentation(
            new InstrumentedOperationDetails("test", "call").endpoint(DEFAULT_ENDPOINT));

        RequestOptions options = new RequestOptions();

        OperationInstrumentation.Scope scope = call.startScope(options);
        RuntimeException error = new RuntimeException("Test error");
        scope.setError(error);
        scope.close();

        assertEquals(1, exporter.getFinishedSpanItems().size());
        SpanData spanData = exporter.getFinishedSpanItems().get(0);
        assertCallSpan(spanData, "call", SpanKind.CLIENT, "localhost", 443L, error.getClass().getCanonicalName());
        Collection<MetricData> metrics = meterReader.collectAllMetrics();
        assertDurationMetric(metrics, "test", "call", "localhost", 443L, error.getClass().getCanonicalName(),
            spanData.getSpanContext());
    }

    @Test
    public void noEndpoint() {
        Instrumentation instrumentation = Instrumentation.create(otelOptions, DEFAULT_LIB_OPTIONS);
        OperationInstrumentation call
            = instrumentation.createOperationInstrumentation(new InstrumentedOperationDetails("test", "call"));

        RequestOptions options = new RequestOptions();

        call.startScope(options).close();

        assertEquals(1, exporter.getFinishedSpanItems().size());
        SpanData spanData = exporter.getFinishedSpanItems().get(0);
        assertCallSpan(spanData, "call", SpanKind.CLIENT, null, null, null);
        Collection<MetricData> metrics = meterReader.collectAllMetrics();
        assertDurationMetric(metrics, "test", "call", null, null, null, spanData.getSpanContext());
    }

    @ParameterizedTest
    @ValueSource(strings = { "http://example.com", "https://example.com:8080", "http://example.com:9090" })
    public void testEndpoints(String uri) {
        Instrumentation instrumentation = Instrumentation.create(otelOptions, DEFAULT_LIB_OPTIONS);
        URI endpoint = URI.create(uri);
        OperationInstrumentation call = instrumentation
            .createOperationInstrumentation(new InstrumentedOperationDetails("test", "Call").endpoint(endpoint));

        RequestOptions options = new RequestOptions();

        call.startScope(options).close();

        assertEquals(1, exporter.getFinishedSpanItems().size());
        SpanData spanData = exporter.getFinishedSpanItems().get(0);

        Long expectedPort = endpoint.getPort() == -1 ? 80 : (long) endpoint.getPort();
        assertCallSpan(spanData, "Call", SpanKind.CLIENT, endpoint.getHost(), expectedPort, null);
        Collection<MetricData> metrics = meterReader.collectAllMetrics();
        assertDurationMetric(metrics, "test", "Call", endpoint.getHost(), expectedPort, null,
            spanData.getSpanContext());
    }

    @Test
    public void tracingDisabled() {
        otelOptions.setTracingEnabled(false);
        Instrumentation instrumentation = Instrumentation.create(otelOptions, DEFAULT_LIB_OPTIONS);
        OperationInstrumentation call = instrumentation.createOperationInstrumentation(
            new InstrumentedOperationDetails("test.client.operation.duration", "call").endpoint(DEFAULT_ENDPOINT));

        RequestOptions options = new RequestOptions();

        assertTrue(call.shouldInstrument(options));
        OperationInstrumentation.Scope scope = call.startScope(options);
        assertFalse(Span.current().getSpanContext().isValid());
        scope.close();

        assertEquals(0, exporter.getFinishedSpanItems().size());
        Collection<MetricData> metrics = meterReader.collectAllMetrics();
        assertDurationMetric(metrics, "test.client.operation.duration", "call", "localhost", 443L, null,
            SpanContext.getInvalid());
    }

    @Test
    public void metricsDisabled() {
        otelOptions.setMetricsEnabled(false);
        Instrumentation instrumentation = Instrumentation.create(otelOptions, DEFAULT_LIB_OPTIONS);
        OperationInstrumentation call = instrumentation.createOperationInstrumentation(
            new InstrumentedOperationDetails("test.client.operation.duration", "call").endpoint(DEFAULT_ENDPOINT));

        RequestOptions options = new RequestOptions();
        assertTrue(call.shouldInstrument(options));
        OperationInstrumentation.Scope scope = call.startScope(options);
        scope.close();

        assertEquals(1, exporter.getFinishedSpanItems().size());
        assertCallSpan(exporter.getFinishedSpanItems().get(0), "call", SpanKind.CLIENT, "localhost", 443L, null);
        assertEquals(0, meterReader.collectAllMetrics().size());
    }

    @Test
    public void tracingAmdMetricsDisabled() {
        otelOptions.setTracingEnabled(false);
        otelOptions.setMetricsEnabled(false);
        Instrumentation instrumentation = Instrumentation.create(otelOptions, DEFAULT_LIB_OPTIONS);
        OperationInstrumentation call = instrumentation.createOperationInstrumentation(
            new InstrumentedOperationDetails("test.client.operation.duration", "call").endpoint(DEFAULT_ENDPOINT));

        RequestOptions options = new RequestOptions();
        assertFalse(call.shouldInstrument(options));
        OperationInstrumentation.Scope scope = call.startScope(options);
        scope.close();

        assertEquals(0, exporter.getFinishedSpanItems().size());
        assertEquals(0, meterReader.collectAllMetrics().size());
    }

    @Test
    public void testNestedOperations() {
        LibraryInstrumentationOptions libOptions1
            = new LibraryInstrumentationOptions("test-lib1").setLibraryVersion("1.0.0")
                .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0");
        LibraryInstrumentationOptions libOptions2
            = new LibraryInstrumentationOptions("test-lib2").setLibraryVersion("2.0.0")
                .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0");
        Instrumentation instrumentation1 = Instrumentation.create(otelOptions, libOptions1);
        Instrumentation instrumentation2 = Instrumentation.create(otelOptions, libOptions2);

        OperationInstrumentation call1 = instrumentation1.createOperationInstrumentation(
            new InstrumentedOperationDetails("test1", "call1").spanKind(SpanKind.CONSUMER));
        OperationInstrumentation call2
            = instrumentation2.createOperationInstrumentation(new InstrumentedOperationDetails("test2", "call2"));
        OperationInstrumentation call3
            = instrumentation2.createOperationInstrumentation(new InstrumentedOperationDetails("test3", "call3"));

        RequestOptions options = new RequestOptions();

        OperationInstrumentation.Scope scope1 = call1.startScope(options);
        assertSame(scope1.getInstrumentationContext(), options.getInstrumentationContext());
        OperationInstrumentation.Scope scope2 = call2.startScope(options);
        assertSame(scope2.getInstrumentationContext(), options.getInstrumentationContext());
        OperationInstrumentation.Scope scope3 = call3.startScope(options);
        assertFalse(scope3.getInstrumentationContext().isValid());
        assertSame(scope2.getInstrumentationContext(), options.getInstrumentationContext());

        scope3.close();
        scope2.close();
        scope1.close();

        assertEquals(2, exporter.getFinishedSpanItems().size());
        SpanData spanData2 = exporter.getFinishedSpanItems().get(0);
        SpanData spanData1 = exporter.getFinishedSpanItems().get(1);
        assertCallSpan(spanData2, "call2", SpanKind.CLIENT, null, null, null);
        assertCallSpan(spanData1, "call1", SpanKind.CONSUMER, null, null, null);
        assertEquals(spanData1.getSpanContext().getTraceId(), spanData2.getSpanContext().getTraceId());
        assertEquals(spanData1.getSpanContext().getSpanId(), spanData2.getParentSpanContext().getSpanId());

        Collection<MetricData> metrics = meterReader.collectAllMetrics();
        assertEquals(2, metrics.size());
        assertDurationMetric(metrics, "test2", "call2", null, null, null, spanData2.getSpanContext());
        assertDurationMetric(metrics, "test1", "call1", null, null, null, spanData1.getSpanContext());
    }

    @Test
    public void testSiblingOperations() {
        Instrumentation instrumentation = Instrumentation.create(otelOptions, DEFAULT_LIB_OPTIONS);
        OperationInstrumentation call1 = instrumentation.createOperationInstrumentation(
            new InstrumentedOperationDetails("test1", "call1").spanKind(SpanKind.CONSUMER).endpoint(DEFAULT_ENDPOINT));
        OperationInstrumentation call2 = instrumentation.createOperationInstrumentation(
            new InstrumentedOperationDetails("test2", "call2").endpoint(DEFAULT_ENDPOINT));
        OperationInstrumentation call3 = instrumentation.createOperationInstrumentation(
            new InstrumentedOperationDetails("test3", "call3").endpoint(DEFAULT_ENDPOINT));

        RequestOptions options1 = new RequestOptions();

        OperationInstrumentation.Scope scope1 = call1.startScope(options1);
        InstrumentationContext parent = scope1.getInstrumentationContext();

        OperationInstrumentation.Scope scope2
            = call2.startScope(new RequestOptions().setInstrumentationContext(parent));
        scope2.close();

        OperationInstrumentation.Scope scope3
            = call3.startScope(new RequestOptions().setInstrumentationContext(parent));
        scope3.close();

        scope1.close();

        assertEquals(3, exporter.getFinishedSpanItems().size());
        SpanData spanData2 = exporter.getFinishedSpanItems().get(0);
        SpanData spanData3 = exporter.getFinishedSpanItems().get(1);
        SpanData spanData1 = exporter.getFinishedSpanItems().get(2);
        assertCallSpan(spanData2, "call2", SpanKind.CLIENT, "localhost", 443L, null);
        assertCallSpan(spanData3, "call3", SpanKind.CLIENT, "localhost", 443L, null);
        assertCallSpan(spanData1, "call1", SpanKind.CONSUMER, "localhost", 443L, null);
        assertEquals(parent.getTraceId(), spanData2.getSpanContext().getTraceId());
        assertEquals(parent.getSpanId(), spanData2.getParentSpanContext().getSpanId());
        assertEquals(parent.getTraceId(), spanData3.getSpanContext().getTraceId());

        Collection<MetricData> metrics = meterReader.collectAllMetrics();
        assertEquals(3, metrics.size());
        assertDurationMetric(metrics, "test2", "call2", "localhost", 443L, null, spanData2.getSpanContext());
        assertDurationMetric(metrics, "test3", "call3", "localhost", 443L, null, spanData3.getSpanContext());
        assertDurationMetric(metrics, "test1", "call1", "localhost", 443L, null, spanData1.getSpanContext());
    }

    private void assertCallSpan(SpanData spanData, String name, SpanKind kind, String host, Long port,
        String errorType) {
        assertEquals(name, spanData.getName());
        assertEquals(kind.name(), spanData.getKind().name());
        assertEquals(host, spanData.getAttributes().get(AttributeKey.stringKey("server.address")));
        assertEquals(name, spanData.getAttributes().get(AttributeKey.stringKey("operation.name")));
        if (port != null) {
            assertEquals(port, spanData.getAttributes().get(AttributeKey.longKey("server.port")));
        } else {
            assertNull(spanData.getAttributes().get(AttributeKey.longKey("server.port")));
        }

        if (errorType != null) {
            assertEquals(errorType, spanData.getAttributes().get(AttributeKey.stringKey("error.type")));
        } else {
            assertNull(spanData.getAttributes().get(AttributeKey.stringKey("error.type")));
        }
    }

    private void assertDurationMetric(Collection<MetricData> metrics, String metricName, String operationName,
        String host, Long port, String errorType, SpanContext spanContext) {
        AttributesBuilder attributesBuilder
            = Attributes.builder().put(AttributeKey.stringKey("operation.name"), operationName);

        if (host != null) {
            attributesBuilder.put(AttributeKey.stringKey("server.address"), host);
        }

        if (port != null) {
            attributesBuilder.put(AttributeKey.longKey("server.port"), port);
        }

        if (errorType != null) {
            attributesBuilder.put(AttributeKey.stringKey("error.type"), errorType);
        }

        assertThat(metrics).anySatisfy(metric -> OpenTelemetryAssertions.assertThat(metric)
            .hasName(metricName)
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

        assertThat(metrics).anySatisfy(m -> assertInstrumentationScope(m.getInstrumentationScopeInfo()));
    }

    private void assertInstrumentationScope(InstrumentationScopeInfo scope) {
        assertThat(scope).satisfies(info -> {
            assertThat(info.getName()).startsWith("test-lib");
            assertThat(info.getVersion()).isNotNull();
            assertThat(info.getSchemaUrl()).isEqualTo("https://opentelemetry.io/schemas/1.29.0");
        });
    }

    public static Stream<SpanKind> spanKindSource() {
        return Stream.of(SpanKind.CLIENT, SpanKind.SERVER, SpanKind.PRODUCER, SpanKind.CONSUMER, SpanKind.INTERNAL);
    }
}
