// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.Context;
import com.azure.core.util.MetricsOptions;
import com.azure.core.util.TelemetryAttributes;
import com.azure.core.util.metrics.LongCounter;
import com.azure.core.util.metrics.LongHistogram;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.metrics.MeterProvider;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.time.TestClock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MetricsTests {
    private static final long SECOND_NANOS = 1_000_000_000;
    private static final Resource RESOURCE =
        Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
    private static final TelemetryAttributes METRIC_ATTRIBUTES = new OpenTelemetryAttributes(Collections.singletonMap("key", "value"));
    private static final Attributes EXPECTED_ATTRIBUTES = Attributes.builder().put("key", "value").build();

    private InMemoryMetricReader sdkMeterReader;
    private TestClock testClock;
    private SdkMeterProvider sdkMeterProvider;

    @BeforeEach
    public void beforeEach() {
        GlobalOpenTelemetry.resetForTest();
        testClock = TestClock.create();
        sdkMeterReader = InMemoryMetricReader.create();
        sdkMeterProvider = SdkMeterProvider.builder()
            .setClock(testClock)
            .setResource(RESOURCE)
            .registerMetricReader(sdkMeterReader)
            .build();
    }

    @AfterEach
    public void afterEach() {
        GlobalOpenTelemetry.resetForTest();
    }

    @Test
    public void basicHistogram() {
        Meter meter = MeterProvider.getDefaultProvider().createMeter("az.sdk-name", null, new OpenTelemetryMetricsOptions().setProvider(sdkMeterProvider));
        LongHistogram longHistogram = meter.createLongHistogram("az.sdk.test-histogram", "important metric", null);
        assertTrue(longHistogram.isEnabled());
        longHistogram.record(1, new OpenTelemetryAttributes(Collections.emptyMap()), Context.NONE);
        testClock.advance(Duration.ofNanos(SECOND_NANOS));
        assertThat(sdkMeterReader.collectAllMetrics())
            .satisfiesExactly(
                metric ->
                    assertThat(metric)
                        .hasResource(RESOURCE)
                        .hasInstrumentationScope(InstrumentationScopeInfo.create("az.sdk-name", null, null))
                        .hasName("az.sdk.test-histogram")
                        .hasDescription("important metric")
                        .hasHistogramSatisfying(
                            histogram ->
                                histogram
                                    .isCumulative()
                                    .hasPointsSatisfying(
                                        point ->
                                            point
                                                .hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                                                .hasEpochNanos(testClock.now())
                                                .hasAttributes(Attributes.empty())
                                                .hasCount(1)
                                                .hasSum(1)
                                                .hasBucketBoundaries(
                                                    5, 10, 25, 50, 75, 100, 250, 500, 750, 1_000, 2_500,
                                                    5_000, 7_500, 10_000)
                                                .hasBucketCounts(
                                                    1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0))));
    }

    @Test
    public void disabledMetric() {
        MetricsOptions options = new OpenTelemetryMetricsOptions().setProvider(sdkMeterProvider).setEnabled(false);
        Meter meter = MeterProvider.getDefaultProvider().createMeter("az.sdk-name", null, options);
        LongHistogram longHistogram = meter
            .createLongHistogram("az.sdk.test-histogram", "important metric", null);

        longHistogram.record(1, new OpenTelemetryAttributes(Collections.emptyMap()), Context.NONE);
        testClock.advance(Duration.ofNanos(SECOND_NANOS));
        assertTrue(sdkMeterReader.collectAllMetrics().isEmpty());
        assertFalse(longHistogram.isEnabled());
    }

    @Test
    public void noopOTelMeterProvider() {
        MetricsOptions options = new OpenTelemetryMetricsOptions().setProvider(io.opentelemetry.api.metrics.MeterProvider.noop());
        Meter meter = MeterProvider.getDefaultProvider().createMeter("az.sdk-name", null, options);
        LongHistogram longHistogram = meter
            .createLongHistogram("az.sdk.test-histogram", "important metric", null);

        longHistogram.record(1, new OpenTelemetryAttributes(Collections.emptyMap()), Context.NONE);
        testClock.advance(Duration.ofNanos(SECOND_NANOS));
        assertTrue(sdkMeterReader.collectAllMetrics().isEmpty());
        assertFalse(longHistogram.isEnabled());
    }

    @Test
    public void histogramWithAttributes() {
        LongHistogram longHistogram = MeterProvider.getDefaultProvider()
            .createMeter("az.sdk-name", "1.0.0-beta.1", new OpenTelemetryMetricsOptions().setProvider(sdkMeterProvider))
            .createLongHistogram("az.sdk.test-histogram", "important metric", "ms");

        longHistogram.record(42, METRIC_ATTRIBUTES, Context.NONE);
        longHistogram.record(420, METRIC_ATTRIBUTES, Context.NONE);
        testClock.advance(Duration.ofNanos(SECOND_NANOS));
        assertThat(sdkMeterReader.collectAllMetrics())
            .satisfiesExactly(
                metric ->
                    assertThat(metric)
                        .hasResource(RESOURCE)
                        .hasInstrumentationScope(InstrumentationScopeInfo.create("az.sdk-name", "1.0.0-beta.1", null))
                        .hasName("az.sdk.test-histogram")
                        .hasDescription("important metric")
                        .hasUnit("ms")
                        .hasHistogramSatisfying(
                            histogram ->
                                histogram
                                    .isCumulative()
                                    .hasPointsSatisfying(
                                        point ->
                                            point
                                                .hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                                                .hasEpochNanos(testClock.now())
                                                .hasAttributes(Attributes.of(AttributeKey.stringKey("key"), "value"))
                                                .hasCount(2)
                                                .hasSum(462)
                                                .hasBucketBoundaries(
                                                    5, 10, 25, 50, 75, 100, 250, 500, 750, 1_000, 2_500,
                                                    5_000, 7_500, 10_000)
                                                .hasBucketCounts(
                                                    0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0))));
    }

    @Test
    public void histogramWithDifferentAttributes() {
        LongHistogram longHistogram = MeterProvider.getDefaultProvider()
            .createMeter("az.sdk-name", "1.0.0-beta.1", new OpenTelemetryMetricsOptions().setProvider(sdkMeterProvider))
            .createLongHistogram("az.sdk.test-histogram", "important metric", "ms");

        TelemetryAttributes attributes1 = new OpenTelemetryAttributes(Collections.singletonMap("key1", "value1"));
        TelemetryAttributes attributes2 = new OpenTelemetryAttributes(Collections.singletonMap("key2", "value2"));
        longHistogram.record(42, attributes1, Context.NONE);
        longHistogram.record(1, attributes1, Context.NONE);
        longHistogram.record(420, attributes2, Context.NONE);
        testClock.advance(Duration.ofNanos(SECOND_NANOS));
        assertThat(sdkMeterReader.collectAllMetrics())
            .satisfiesExactly(
                metric ->
                    assertThat(metric)
                        .hasResource(RESOURCE)
                        .hasInstrumentationScope(InstrumentationScopeInfo.create("az.sdk-name", "1.0.0-beta.1", null))
                        .hasName("az.sdk.test-histogram")
                        .hasDescription("important metric")
                        .hasUnit("ms")
                        .hasHistogramSatisfying(
                            histogram ->
                                histogram
                                    .isCumulative()
                                    .hasPointsSatisfying(
                                        point ->
                                            point
                                                .hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                                                .hasEpochNanos(testClock.now())
                                                .hasAttributes(attributeEntry("key1", "value1"))
                                                .hasCount(2)
                                                .hasSum(43),
                                        point ->
                                            point
                                                .hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                                                .hasEpochNanos(testClock.now())
                                                .hasAttributes(attributeEntry("key2", "value2"))
                                                .hasCount(1)
                                                .hasSum(420))));

    }

    @Test
    public void basicCounter() {
        LongCounter longCounter = MeterProvider.getDefaultProvider()
            .createMeter("az.sdk-name", "1.0.0-beta.1", new OpenTelemetryMetricsOptions().setProvider(sdkMeterProvider))
            .createLongCounter("az.sdk.test-counter", "important metric", "bytes");

        longCounter.add(42, METRIC_ATTRIBUTES, Context.NONE);
        longCounter.add(420, METRIC_ATTRIBUTES, Context.NONE);
        testClock.advance(Duration.ofNanos(SECOND_NANOS));
        assertThat(sdkMeterReader.collectAllMetrics())
            .satisfiesExactly(
                metric ->
                    assertThat(metric)
                        .hasResource(RESOURCE)
                        .hasInstrumentationScope(InstrumentationScopeInfo.create("az.sdk-name", "1.0.0-beta.1", null))
                        .hasName("az.sdk.test-counter")
                        .hasDescription("important metric")
                        .hasUnit("bytes")
                        .hasLongSumSatisfying(
                            sum ->
                                sum
                                    .isCumulative()
                                    .hasPointsSatisfying(
                                        point ->
                                            point
                                                .hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                                                .hasEpochNanos(testClock.now())
                                                .hasAttributes(EXPECTED_ATTRIBUTES)
                                                .hasValue(462))));
    }

    @Test
    public void basicUpDownCounter() {
        LongCounter longCounter = MeterProvider.getDefaultProvider()
            .createMeter("az.sdk-name", "1.0.0-beta.1", new OpenTelemetryMetricsOptions().setProvider(sdkMeterProvider))
            .createLongUpDownCounter("az.sdk.test-counter", "important metric", "bytes");

        longCounter.add(1, METRIC_ATTRIBUTES, Context.NONE);
        longCounter.add(10, METRIC_ATTRIBUTES, Context.NONE);
        longCounter.add(-2, METRIC_ATTRIBUTES, Context.NONE);
        testClock.advance(Duration.ofNanos(SECOND_NANOS));
        assertThat(sdkMeterReader.collectAllMetrics())
            .satisfiesExactly(
                metric ->
                    assertThat(metric)
                        .hasResource(RESOURCE)
                        .hasInstrumentationScope(InstrumentationScopeInfo.create("az.sdk-name", "1.0.0-beta.1", null))
                        .hasName("az.sdk.test-counter")
                        .hasDescription("important metric")
                        .hasUnit("bytes")
                        .hasLongSumSatisfying(
                            sum ->
                                sum
                                    .isCumulative()
                                    .hasPointsSatisfying(
                                        point ->
                                            point
                                                .hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                                                .hasEpochNanos(testClock.now())
                                                .hasAttributes(EXPECTED_ATTRIBUTES)
                                                .hasValue(9))));
    }

    @Test
    public void attributeTypes() {
        TelemetryAttributes attributes = new OpenTelemetryAttributes(new HashMap<String, Object>() {{
                put("string", "string-value");
                put("long", 42L);
                put("boolean", true);
                put("double", 0.42d);
            }});

        Attributes expected = Attributes.builder()
            .put(AttributeKey.stringKey("string"), "string-value")
            .put(AttributeKey.booleanKey("boolean"), true)
            .put(AttributeKey.doubleKey("double"), 0.42d)
            .put(AttributeKey.longKey("long"), 42L)
            .build();

        LongCounter longCounter = MeterProvider.getDefaultProvider()
            .createMeter("az.sdk-name", "1.0.0-beta.1", new OpenTelemetryMetricsOptions().setProvider(sdkMeterProvider))
            .createLongCounter("az.sdk.test-counter", "important metric", "bytes");

        longCounter.add(42, attributes, Context.NONE);
        longCounter.add(420, attributes, Context.NONE);
        testClock.advance(Duration.ofNanos(SECOND_NANOS));
        assertThat(sdkMeterReader.collectAllMetrics())
            .satisfiesExactly(
                metric ->
                    assertThat(metric)
                        .hasResource(RESOURCE)
                        .hasInstrumentationScope(InstrumentationScopeInfo.create("az.sdk-name", "1.0.0-beta.1", null))
                        .hasName("az.sdk.test-counter")
                        .hasDescription("important metric")
                        .hasUnit("bytes")
                        .hasLongSumSatisfying(
                            sum ->
                                sum
                                    .isCumulative()
                                    .hasPointsSatisfying(
                                        point ->
                                            point
                                                .hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                                                .hasEpochNanos(testClock.now())
                                                .hasAttributes(expected)
                                                .hasValue(462))));
    }


    @Test
    public void multipleMetersSameName() {
        Meter meter = MeterProvider.getDefaultProvider()
            .createMeter("az.sdk-name", "1.0.0-beta.1", new OpenTelemetryMetricsOptions().setProvider(sdkMeterProvider));

        LongHistogram longHistogram1 = meter.createLongHistogram("az.sdk.test-histogram", "important metric", "ms");
        LongHistogram longHistogram2 = meter.createLongHistogram("az.sdk.test-histogram", "important metric", "ms");

        longHistogram1.record(42, METRIC_ATTRIBUTES, Context.NONE);
        longHistogram2.record(420, METRIC_ATTRIBUTES, Context.NONE);
        testClock.advance(Duration.ofNanos(SECOND_NANOS));
        assertThat(sdkMeterReader.collectAllMetrics())
            .satisfiesExactly(
                metric ->
                    assertThat(metric)
                        .hasResource(RESOURCE)
                        .hasInstrumentationScope(InstrumentationScopeInfo.create("az.sdk-name", "1.0.0-beta.1", null))
                        .hasName("az.sdk.test-histogram")
                        .hasDescription("important metric")
                        .hasUnit("ms")
                        .hasHistogramSatisfying(
                            histogram ->
                                histogram
                                    .isCumulative()
                                    .hasPointsSatisfying(
                                        point ->
                                            point
                                                .hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                                                .hasEpochNanos(testClock.now())
                                                .hasAttributes(EXPECTED_ATTRIBUTES)
                                                .hasCount(2)
                                                .hasSum(462)
                                                .hasBucketBoundaries(
                                                    5, 10, 25, 50, 75, 100, 250, 500, 750, 1_000, 2_500,
                                                    5_000, 7_500, 10_000)
                                                .hasBucketCounts(
                                                    0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0))));
    }


    @Test
    public void createMeterNullNameThrows() {
        assertThrows(NullPointerException.class, () -> MeterProvider.getDefaultProvider().createMeter(null, null, null));
    }

    @Test
    public void noopMeterCreateInstrumentInvalidArgumentsThrow() {
        Meter meter = MeterProvider.getDefaultProvider().createMeter("foo", null, null);
        assertThrows(NullPointerException.class, () -> meter.createLongHistogram(null, "description", null));
        assertThrows(NullPointerException.class, () -> meter.createLongHistogram("name", null, null));
        assertThrows(NullPointerException.class, () -> meter.createLongCounter(null, "description", null));
        assertThrows(NullPointerException.class, () -> meter.createLongCounter("name", null, null));
    }
}
