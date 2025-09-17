// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.http.models.RequestContext;
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
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OperationInstrumentationTests {
    private static final String DEFAULT_ENDPOINT = "https://localhost";

    private InMemorySpanExporter exporter;
    private SdkTracerProvider tracerProvider;
    private SdkMeterProvider meterProvider;
    private InMemoryMetricReader meterReader;
    private TestClock testClock;

    private InstrumentationOptions otelOptions;
    private InstrumentationOptions otelOptionsWithExperimentalFeatures;
    private SdkInstrumentationOptions sdkInstrumentationOptions
        = new SdkInstrumentationOptions("test-lib").setSdkVersion("1.0.0")
            .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0");

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
        otelOptionsWithExperimentalFeatures
            = new InstrumentationOptions().setTelemetryProvider(openTelemetry).setExperimentalFeaturesEnabled(true);
    }

    @AfterEach
    public void tearDown() {
        exporter.reset();
        tracerProvider.close();
    }

    @Test
    public void invalidArguments() {
        Instrumentation instrumentation = Instrumentation.create(otelOptions, sdkInstrumentationOptions);
        assertThrows(NullPointerException.class,
            () -> instrumentation.instrumentWithResponse(null, RequestContext.none(), o -> "done"));
        assertThrows(NullPointerException.class, () -> instrumentation.instrument("call", RequestContext.none(), null));
    }

    @Test
    public void basicCall() {
        Instrumentation instrumentation = Instrumentation.create(otelOptionsWithExperimentalFeatures,
            sdkInstrumentationOptions.setEndpoint(DEFAULT_ENDPOINT));

        AtomicReference<Span> current = new AtomicReference<>();
        instrumentation.instrument("call", RequestContext.none(), context -> {
            current.set(Span.current());
            assertTrue(current.get().getSpanContext().isValid());
            assertEquals(current.get().getSpanContext().getTraceId(), context.getInstrumentationContext().getTraceId());
            assertEquals(current.get().getSpanContext().getSpanId(), context.getInstrumentationContext().getSpanId());
        });

        assertFalse(Span.current().getSpanContext().isValid());
        assertEquals(1, exporter.getFinishedSpanItems().size());
        assertCallSpan(exporter.getFinishedSpanItems().get(0), "call", SpanKind.INTERNAL, "localhost", 443L, null);

        Collection<MetricData> metrics = meterReader.collectAllMetrics();
        assertDurationMetric(metrics, "test.lib.client.operation.duration", "call", "localhost", 443L, null,
            current.get().getSpanContext());
    }

    @Test
    public void basicCallExperimentalFeaturesDisabled() {
        Instrumentation instrumentation
            = Instrumentation.create(otelOptions, sdkInstrumentationOptions.setEndpoint(DEFAULT_ENDPOINT));

        AtomicReference<Span> current = new AtomicReference<>();
        instrumentation.instrument("call", RequestContext.none(), context -> {
            current.set(Span.current());
            assertTrue(current.get().getSpanContext().isValid());
            assertEquals(current.get().getSpanContext().getTraceId(), context.getInstrumentationContext().getTraceId());
            assertEquals(current.get().getSpanContext().getSpanId(), context.getInstrumentationContext().getSpanId());
        });

        assertFalse(Span.current().getSpanContext().isValid());
        assertEquals(1, exporter.getFinishedSpanItems().size());
        assertCallSpan(exporter.getFinishedSpanItems().get(0), "call", SpanKind.INTERNAL, "localhost", 443L, null);

        assertEquals(0, meterReader.collectAllMetrics().size());
    }

    @Test
    public void callWithError() {
        Instrumentation instrumentation = Instrumentation.create(otelOptionsWithExperimentalFeatures,
            sdkInstrumentationOptions.setEndpoint(DEFAULT_ENDPOINT));
        RuntimeException error = new RuntimeException("Test error");
        assertThrows(RuntimeException.class, () -> instrumentation.instrument("call", RequestContext.none(), o -> {
            throw error;
        }));

        assertEquals(1, exporter.getFinishedSpanItems().size());
        SpanData spanData = exporter.getFinishedSpanItems().get(0);
        assertCallSpan(spanData, "call", SpanKind.INTERNAL, "localhost", 443L, error.getClass().getCanonicalName());
        Collection<MetricData> metrics = meterReader.collectAllMetrics();
        assertDurationMetric(metrics, "test.lib.client.operation.duration", "call", "localhost", 443L,
            error.getClass().getCanonicalName(), spanData.getSpanContext());
    }

    @Test
    public void noEndpoint() {
        Instrumentation instrumentation
            = Instrumentation.create(otelOptionsWithExperimentalFeatures, sdkInstrumentationOptions);
        instrumentation.instrument("call", RequestContext.none(), __ -> {
        });

        assertEquals(1, exporter.getFinishedSpanItems().size());
        SpanData spanData = exporter.getFinishedSpanItems().get(0);
        assertCallSpan(spanData, "call", SpanKind.INTERNAL, null, null, null);
        Collection<MetricData> metrics = meterReader.collectAllMetrics();
        assertDurationMetric(metrics, "test.lib.client.operation.duration", "call", null, null, null,
            spanData.getSpanContext());
    }

    @ParameterizedTest
    @ValueSource(strings = { "http://example.com", "https://example.com:8080", "http://example.com:9090" })
    public void testEndpoints(String endpoint) {
        Instrumentation instrumentation = Instrumentation.create(otelOptionsWithExperimentalFeatures,
            sdkInstrumentationOptions.setEndpoint(endpoint));
        instrumentation.instrument("Call", RequestContext.none(), __ -> {
        });

        assertEquals(1, exporter.getFinishedSpanItems().size());
        SpanData spanData = exporter.getFinishedSpanItems().get(0);

        URI uri = URI.create(endpoint);
        Long expectedPort = uri.getPort() == -1 ? 80 : (long) uri.getPort();
        assertCallSpan(spanData, "Call", SpanKind.INTERNAL, uri.getHost(), expectedPort, null);
        Collection<MetricData> metrics = meterReader.collectAllMetrics();
        assertDurationMetric(metrics, "test.lib.client.operation.duration", "Call", uri.getHost(), expectedPort, null,
            spanData.getSpanContext());
    }

    @Test
    public void tracingDisabled() {
        otelOptionsWithExperimentalFeatures.setTracingEnabled(false);
        Instrumentation instrumentation = Instrumentation.create(otelOptionsWithExperimentalFeatures,
            sdkInstrumentationOptions.setEndpoint(DEFAULT_ENDPOINT));
        instrumentation.instrument("call", RequestContext.none(), __ -> {
            assertFalse(Span.current().getSpanContext().isValid());
        });

        assertEquals(0, exporter.getFinishedSpanItems().size());
        Collection<MetricData> metrics = meterReader.collectAllMetrics();
        assertDurationMetric(metrics, "test.lib.client.operation.duration", "call", "localhost", 443L, null,
            SpanContext.getInvalid());
    }

    @Test
    public void metricsDisabled() {
        otelOptions.setMetricsEnabled(false);
        Instrumentation instrumentation
            = Instrumentation.create(otelOptions, sdkInstrumentationOptions.setEndpoint(DEFAULT_ENDPOINT));
        instrumentation.instrument("call", RequestContext.none(), __ -> {
        });

        assertEquals(1, exporter.getFinishedSpanItems().size());
        assertCallSpan(exporter.getFinishedSpanItems().get(0), "call", SpanKind.INTERNAL, "localhost", 443L, null);
        assertEquals(0, meterReader.collectAllMetrics().size());
    }

    @Test
    public void tracingAndMetricsDisabled() {
        otelOptions.setTracingEnabled(false);
        otelOptions.setMetricsEnabled(false);
        Instrumentation instrumentation
            = Instrumentation.create(otelOptions, sdkInstrumentationOptions.setEndpoint(DEFAULT_ENDPOINT));
        instrumentation.instrument("call", RequestContext.none(), __ -> {
        });
        assertEquals(0, exporter.getFinishedSpanItems().size());
        assertEquals(0, meterReader.collectAllMetrics().size());
    }

    @Test
    public void testNestedOperations() {
        SdkInstrumentationOptions sdkOptions1 = new SdkInstrumentationOptions("test-lib1").setSdkVersion("1.0.0")
            .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0");
        SdkInstrumentationOptions sdkOptions2 = new SdkInstrumentationOptions("test-lib2").setSdkVersion("2.0.0")
            .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0");
        Instrumentation instrumentation1 = Instrumentation.create(otelOptionsWithExperimentalFeatures, sdkOptions1);
        Instrumentation instrumentation2 = Instrumentation.create(otelOptionsWithExperimentalFeatures, sdkOptions2);

        io.clientcore.core.instrumentation.tracing.Span span = instrumentation1.getTracer()
            .spanBuilder("call1", SpanKind.CONSUMER, null)
            .setAttribute("operation.name", "call1")
            .startSpan();

        RequestContext context
            = RequestContext.builder().setInstrumentationContext(span.getInstrumentationContext()).build();
        instrumentation2.instrument("call2", context, o2 -> {
            assertTrue(o2.getInstrumentationContext().isValid());
            assertNotSame(o2.getInstrumentationContext(), context.getInstrumentationContext());
            instrumentation2.instrument("call3", o2, o3 -> {
                // this call is suppressed
                assertSame(o2.getInstrumentationContext(), o3.getInstrumentationContext());
                assertNotSame(o3.getInstrumentationContext(), context.getInstrumentationContext());
            });
        });
        span.end();

        assertEquals(2, exporter.getFinishedSpanItems().size());
        SpanData spanData2 = exporter.getFinishedSpanItems().get(0);
        SpanData spanData1 = exporter.getFinishedSpanItems().get(1);
        assertCallSpan(spanData2, "call2", SpanKind.INTERNAL, null, null, null);
        assertCallSpan(spanData1, "call1", SpanKind.CONSUMER, null, null, null);
        assertEquals(spanData1.getSpanContext().getTraceId(), spanData2.getSpanContext().getTraceId());
        assertEquals(spanData1.getSpanContext().getSpanId(), spanData2.getParentSpanContext().getSpanId());

        Collection<MetricData> metrics = meterReader.collectAllMetrics();
        assertEquals(1, metrics.size());
        assertDurationMetric(metrics, "test.lib2.client.operation.duration", "call2", null, null, null,
            spanData2.getSpanContext());
    }

    @Test
    public void testSiblingOperations() {
        Instrumentation instrumentation = Instrumentation.create(otelOptionsWithExperimentalFeatures,
            sdkInstrumentationOptions.setEndpoint("https://localhost"));

        AtomicReference<InstrumentationContext> parent = new AtomicReference<>();

        io.clientcore.core.instrumentation.tracing.Span span = instrumentation.getTracer()
            .spanBuilder("call1", SpanKind.CONSUMER, null)
            .setAttribute("operation.name", "call1")
            .startSpan();

        parent.set(span.getInstrumentationContext());

        RequestContext context
            = RequestContext.builder().setInstrumentationContext(span.getInstrumentationContext()).build();
        instrumentation.instrument("call2", context, o2 -> {
            assertTrue(o2.getInstrumentationContext().isValid());
            assertNotSame(o2.getInstrumentationContext(), context.getInstrumentationContext());
        });

        instrumentation.instrument("call3", context, o3 -> {
            assertTrue(o3.getInstrumentationContext().isValid());
            assertNotSame(o3.getInstrumentationContext(), context.getInstrumentationContext());
            assertNotSame(o3.getInstrumentationContext(), parent.get());
        });

        span.end();

        assertEquals(3, exporter.getFinishedSpanItems().size());
        SpanData spanData2 = exporter.getFinishedSpanItems().get(0);
        SpanData spanData3 = exporter.getFinishedSpanItems().get(1);
        SpanData spanData1 = exporter.getFinishedSpanItems().get(2);
        assertCallSpan(spanData2, "call2", SpanKind.INTERNAL, "localhost", 443L, null);
        assertCallSpan(spanData3, "call3", SpanKind.INTERNAL, "localhost", 443L, null);
        assertCallSpan(spanData1, "call1", SpanKind.CONSUMER, null, null, null);
        assertEquals(parent.get().getTraceId(), spanData2.getSpanContext().getTraceId());
        assertEquals(parent.get().getSpanId(), spanData2.getParentSpanContext().getSpanId());
        assertEquals(parent.get().getTraceId(), spanData3.getSpanContext().getTraceId());

        List<MetricData> metrics = meterReader.collectAllMetrics().stream().collect(Collectors.toList());
        assertEquals(1, metrics.size());
        assertEquals(2, metrics.get(0).getHistogramData().getPoints().size());
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
}
