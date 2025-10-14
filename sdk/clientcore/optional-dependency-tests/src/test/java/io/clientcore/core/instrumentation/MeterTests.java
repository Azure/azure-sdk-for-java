// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.instrumentation.metrics.DoubleHistogram;
import io.clientcore.core.instrumentation.metrics.LongCounter;
import io.clientcore.core.instrumentation.metrics.Meter;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.clientcore.core.instrumentation.tracing.Tracer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.time.TestClock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

public class MeterTests {
    private static final SdkInstrumentationOptions DEFAULT_SDK_OPTIONS = new SdkInstrumentationOptions("test-library");
    private static final Resource RESOURCE
        = Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
    private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE
        = InstrumentationScopeInfo.builder(DEFAULT_SDK_OPTIONS.getSdkName())
            .setVersion(DEFAULT_SDK_OPTIONS.getSdkVersion())
            .setSchemaUrl(DEFAULT_SDK_OPTIONS.getSchemaUrl())
            .build();
    private static final long SECOND_NANOS = 1_000_000_000;

    private SdkMeterProvider meterProvider;
    private Meter meter;
    private InMemoryMetricReader sdkMeterReader;
    private TestClock testClock;
    private Instrumentation instrumentation;
    private InstrumentationAttributes emptyAttributes;

    @BeforeEach
    public void setUp() {
        testClock = TestClock.create();
        sdkMeterReader = InMemoryMetricReader.create();

        meterProvider = SdkMeterProvider.builder()
            .setResource(RESOURCE)
            .setClock(testClock)
            .registerMetricReader(sdkMeterReader)
            .build();

        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder().setMeterProvider(meterProvider).build();
        InstrumentationOptions otelOptions = new InstrumentationOptions().setTelemetryProvider(openTelemetry);
        instrumentation = Instrumentation.create(otelOptions, DEFAULT_SDK_OPTIONS);
        emptyAttributes = instrumentation.createAttributes(null);
        meter = instrumentation.getMeter();
    }

    @AfterEach
    public void tearDown() {
        meterProvider.close();
    }

    @Test
    public void basicHistogram() {
        assertTrue(meter.isEnabled());
        DoubleHistogram histogram = meter.createDoubleHistogram("core.test-histogram", "important metric", "s", null);
        assertTrue(histogram.isEnabled());
        histogram.record(1, emptyAttributes, null);
        testClock.advance(Duration.ofNanos(SECOND_NANOS));
        assertThat(sdkMeterReader.collectAllMetrics()).satisfiesExactly(metric -> assertThat(metric)
            .hasResource(RESOURCE)
            .hasInstrumentationScope(INSTRUMENTATION_SCOPE)
            .hasName("core.test-histogram")
            .hasDescription("important metric")
            .hasHistogramSatisfying(h -> h.isCumulative()
                .hasPointsSatisfying(point -> point.hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                    .hasEpochNanos(testClock.now())
                    .hasAttributes(Attributes.empty())
                    .hasCount(1)
                    .hasSum(1)
                    .hasBucketBoundaries(0, 5, 10, 25, 50, 75, 100, 250, 500, 750, 1_000, 2_500, 5_000, 7_500, 10_000)
                    .hasBucketCounts(0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0))));
    }

    @Test
    public void histogramWithAttributesAndBoundaries() {
        Map<String, Object> attributesMap = new HashMap<>();
        attributesMap.put("key1", "value");
        attributesMap.put("key2", 42);
        InstrumentationAttributes attributes = instrumentation.createAttributes(attributesMap);

        Attributes otelAttributes = Attributes.builder().put("key1", "value").put("key2", 42).build();

        DoubleHistogram histogram = meter.createDoubleHistogram("core.test-histogram", "important metric", "unit",
            Arrays.asList(0d, 1d, 2d, 42d));
        histogram.record(1, attributes, null);
        histogram.record(10, attributes, null);
        testClock.advance(Duration.ofNanos(SECOND_NANOS));
        assertThat(sdkMeterReader.collectAllMetrics())
            .satisfiesExactly(metric -> assertThat(metric).hasResource(RESOURCE)
                .hasInstrumentationScope(INSTRUMENTATION_SCOPE)
                .hasName("core.test-histogram")
                .hasDescription("important metric")
                .hasUnit("unit")
                .hasHistogramSatisfying(h -> h.isCumulative()
                    .hasPointsSatisfying(point -> point.hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                        .hasEpochNanos(testClock.now())
                        .hasAttributes(otelAttributes)
                        .hasCount(2)
                        .hasSum(11)
                        .hasBucketBoundaries(0, 1, 2, 42)
                        .hasBucketCounts(0, 1, 0, 1, 0))));
    }

    @Test
    public void histogramWithContext() {
        DoubleHistogram histogram
            = meter.createDoubleHistogram("core.test-histogram", "important metric", "unit", null);
        InstrumentationAttributes attributes
            = instrumentation.createAttributes(Collections.singletonMap("key1", "value"));
        Attributes otelAttributes = Attributes.builder().put("key1", "value").build();

        Tracer tracer = instrumentation.getTracer();
        Span span = tracer.spanBuilder("test-span", SpanKind.CLIENT, null).startSpan();
        InstrumentationContext context = Instrumentation.createInstrumentationContext(span);

        histogram.record(42, attributes, context);
        testClock.advance(Duration.ofNanos(SECOND_NANOS));
        assertThat(sdkMeterReader.collectAllMetrics()).satisfiesExactly(metric -> assertThat(metric)
            .hasResource(RESOURCE)
            .hasInstrumentationScope(INSTRUMENTATION_SCOPE)
            .hasName("core.test-histogram")
            .hasDescription("important metric")
            .hasUnit("unit")
            .hasHistogramSatisfying(h -> h.isCumulative()
                .hasPointsSatisfying(point -> point.hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                    .hasEpochNanos(testClock.now())
                    .hasAttributes(otelAttributes)
                    .hasCount(1)
                    .hasSum(42)
                    .hasBucketBoundaries(0, 5, 10, 25, 50, 75, 100, 250, 500, 750, 1_000, 2_500, 5_000, 7_500, 10_000)
                    .hasBucketCounts(0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
                    .hasExemplarsSatisfying(exemplar -> exemplar.hasValue(42)
                        .hasTraceId(span.getInstrumentationContext().getTraceId())
                        .hasSpanId(span.getInstrumentationContext().getSpanId())))));
    }

    @Test
    public void basicCounter() {
        LongCounter longCounter = meter.createLongCounter("core.test-counter", "important metric", "1");
        assertTrue(longCounter.isEnabled());
        longCounter.add(1, emptyAttributes, null);
        testClock.advance(Duration.ofNanos(SECOND_NANOS));
        assertThat(sdkMeterReader.collectAllMetrics())
            .satisfiesExactly(metric -> assertThat(metric).hasResource(RESOURCE)
                .hasInstrumentationScope(INSTRUMENTATION_SCOPE)
                .hasName("core.test-counter")
                .hasDescription("important metric")
                .hasLongSumSatisfying(s -> s.isCumulative()
                    .hasPointsSatisfying(point -> point.hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                        .hasEpochNanos(testClock.now())
                        .hasAttributes(Attributes.empty())
                        .hasValue(1))));
    }

    @Test
    public void counterWithAttributes() {
        Map<String, Object> attributesMap = new HashMap<>();
        attributesMap.put("key1", "value");
        attributesMap.put("key2", 42);
        InstrumentationAttributes attributes = instrumentation.createAttributes(attributesMap);

        Attributes otelAttributes = Attributes.builder().put("key1", "value").put("key2", 42).build();

        LongCounter longCounter = meter.createLongCounter("core.test-counter", "important metric", "unit");
        assertTrue(longCounter.isEnabled());
        longCounter.add(42, attributes, null);
        longCounter.add(1, attributes, null);
        testClock.advance(Duration.ofNanos(SECOND_NANOS));
        assertThat(sdkMeterReader.collectAllMetrics())
            .satisfiesExactly(metric -> assertThat(metric).hasResource(RESOURCE)
                .hasInstrumentationScope(INSTRUMENTATION_SCOPE)
                .hasName("core.test-counter")
                .hasDescription("important metric")
                .hasUnit("unit")
                .hasLongSumSatisfying(s -> s.isCumulative()
                    .hasPointsSatisfying(point -> point.hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                        .hasEpochNanos(testClock.now())
                        .hasAttributes(otelAttributes)
                        .hasValue(43))));
    }

    @Test
    public void basicUpDownCounter() {
        LongCounter longCounter = meter.createLongUpDownCounter("core.test-updowncounter", "important metric", "1");
        assertTrue(longCounter.isEnabled());
        longCounter.add(1, emptyAttributes, null);
        longCounter.add(1, emptyAttributes, null);
        longCounter.add(-1, emptyAttributes, null);
        testClock.advance(Duration.ofNanos(SECOND_NANOS));
        assertThat(sdkMeterReader.collectAllMetrics())
            .satisfiesExactly(metric -> assertThat(metric).hasResource(RESOURCE)
                .hasInstrumentationScope(INSTRUMENTATION_SCOPE)
                .hasName("core.test-updowncounter")
                .hasDescription("important metric")
                .hasLongSumSatisfying(s -> s.isCumulative()
                    .hasPointsSatisfying(point -> point.hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                        .hasEpochNanos(testClock.now())
                        .hasAttributes(Attributes.empty())
                        .hasValue(1))));
    }

    @Test
    public void upDownCounterWithAttributes() {
        Map<String, Object> attributesMap = new HashMap<>();
        attributesMap.put("key1", "value");
        attributesMap.put("key2", 42);
        InstrumentationAttributes attributes = instrumentation.createAttributes(attributesMap);

        Attributes otelAttributes = Attributes.builder().put("key1", "value").put("key2", 42).build();

        LongCounter longCounter = meter.createLongUpDownCounter("core.test-updowncounter", "important metric", "unit");
        assertTrue(longCounter.isEnabled());
        longCounter.add(42, attributes, null);
        longCounter.add(-1, attributes, null);
        longCounter.add(-42, attributes, null);
        testClock.advance(Duration.ofNanos(SECOND_NANOS));
        assertThat(sdkMeterReader.collectAllMetrics())
            .satisfiesExactly(metric -> assertThat(metric).hasResource(RESOURCE)
                .hasInstrumentationScope(INSTRUMENTATION_SCOPE)
                .hasName("core.test-updowncounter")
                .hasDescription("important metric")
                .hasUnit("unit")
                .hasLongSumSatisfying(s -> s.isCumulative()
                    .hasPointsSatisfying(point -> point.hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                        .hasEpochNanos(testClock.now())
                        .hasAttributes(otelAttributes)
                        .hasValue(-1))));
    }
}
