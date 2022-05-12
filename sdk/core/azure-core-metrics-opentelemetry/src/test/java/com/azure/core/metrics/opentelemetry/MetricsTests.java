// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.Context;
import com.azure.core.util.MetricsOptions;
import com.azure.core.util.metrics.AzureLongCounter;
import com.azure.core.util.metrics.AzureLongHistogram;
import com.azure.core.util.metrics.AzureMeter;
import com.azure.core.util.metrics.AzureMeterProvider;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.MeterProvider;
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
import java.util.Map;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MetricsTests {
    private static final long SECOND_NANOS = 1_000_000_000;
    private static final Resource RESOURCE =
        Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
    private static final Map<String, Object> METRIC_ATTRIBUTES = Collections.singletonMap("key", "value");
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
        AzureMeter meter = AzureMeterProvider.getDefaultProvider().createMeter("az.sdk-name", null, new MetricsOptions().setProvider(sdkMeterProvider));
        assertTrue(meter.isEnabled());
        AzureLongHistogram longHistogram = meter.createLongHistogram("az.sdk.test-histogram", "important metric", null, null);

        longHistogram.record(1, Context.NONE);
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
        MetricsOptions options = new MetricsOptions().setProvider(sdkMeterProvider).enable(false);
        AzureMeter meter = AzureMeterProvider.getDefaultProvider().createMeter("az.sdk-name", null, options);
        AzureLongHistogram longHistogram = meter
            .createLongHistogram("az.sdk.test-histogram", "important metric", null, null);

        longHistogram.record(1, Context.NONE);
        testClock.advance(Duration.ofNanos(SECOND_NANOS));
        assertTrue(sdkMeterReader.collectAllMetrics().isEmpty());
        assertFalse(meter.isEnabled());
    }

    @Test
    public void noopOTelMeterProvider() {
        MetricsOptions options = new MetricsOptions().setProvider(MeterProvider.noop());
        AzureMeter meter = AzureMeterProvider.getDefaultProvider().createMeter("az.sdk-name", null, options);
        AzureLongHistogram longHistogram = AzureMeterProvider.getDefaultProvider()
            .createMeter("az.sdk-name", null, new MetricsOptions().setProvider(MeterProvider.noop()))
            .createLongHistogram("az.sdk.test-histogram", "important metric", null, null);

        longHistogram.record(1, Context.NONE);
        testClock.advance(Duration.ofNanos(SECOND_NANOS));
        assertTrue(sdkMeterReader.collectAllMetrics().isEmpty());
        assertFalse(meter.isEnabled());
    }

    @Test
    public void histogramWithAttributes() {
        AzureLongHistogram longHistogram = AzureMeterProvider.getDefaultProvider()
            .createMeter("az.sdk-name", "1.0.0-beta.1", new MetricsOptions().setProvider(sdkMeterProvider))
            .createLongHistogram("az.sdk.test-histogram", "important metric", "ms", METRIC_ATTRIBUTES);

        longHistogram.record(42, Context.NONE);
        longHistogram.record(420, Context.NONE);
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
    public void basicCounter() {
        AzureLongCounter longCounter = AzureMeterProvider.getDefaultProvider()
            .createMeter("az.sdk-name", "1.0.0-beta.1", new MetricsOptions().setProvider(sdkMeterProvider))
            .createLongCounter("az.sdk.test-counter", "important metric", "bytes", METRIC_ATTRIBUTES);

        longCounter.add(42, Context.NONE);
        longCounter.add(420, Context.NONE);
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
    public void attributeTypes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("string", "foo");
        attributes.put("boolean", true);
        attributes.put("double", 0.42d);
        attributes.put("long", 42L);
        attributes.put("string-array", new String[] {"foo", "bar"});
        attributes.put("long-array", new long[] {1L, 2L});
        attributes.put("double-array", new double[] {0.1d, 0.2d});
        attributes.put("boolean-array", new boolean[] {false, true});
        attributes.put("unsupported", RESOURCE);
        attributes.put("null", null);

        Attributes expected = Attributes.builder()
            .put(AttributeKey.stringKey("string"), "foo")
            .put(AttributeKey.booleanKey("boolean"), true)
            .put(AttributeKey.doubleKey("double"), 0.42d)
            .put(AttributeKey.longKey("long"), 42L)
            .put(AttributeKey.stringArrayKey("string-array"), "foo", "bar")
            .put(AttributeKey.longArrayKey("long-array"), 1L, 2L)
            .put(AttributeKey.doubleArrayKey("double-array"), 0.1d, 0.2d)
            .put(AttributeKey.booleanArrayKey("boolean-array"), false, true)
            .build();

        AzureLongCounter longCounter = AzureMeterProvider.getDefaultProvider()
            .createMeter("az.sdk-name", "1.0.0-beta.1", new MetricsOptions().setProvider(sdkMeterProvider))
            .createLongCounter("az.sdk.test-counter", "important metric", "bytes", attributes);

        longCounter.add(42, Context.NONE);
        longCounter.add(420, Context.NONE);
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
    public void multipleMeters() {
        AzureMeter meter = AzureMeterProvider.getDefaultProvider()
            .createMeter("az.sdk-name", "1.0.0-beta.1", new MetricsOptions().setProvider(sdkMeterProvider));

        AzureLongHistogram longHistogram1 = meter.createLongHistogram("az.sdk.test-histogram", "important metric", "ms", METRIC_ATTRIBUTES);
        AzureLongHistogram longHistogram2 = meter.createLongHistogram("az.sdk.test-histogram", "important metric", "ms", METRIC_ATTRIBUTES);

        longHistogram1.record(42, Context.NONE);
        longHistogram2.record(420, Context.NONE);
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
        assertThrows(NullPointerException.class, () -> AzureMeterProvider.getDefaultProvider().createMeter(null, null, null));
    }

    @Test
    public void noopMeterCreateInstrumentInvalidArgumentsThrow() {
        AzureMeter meter = AzureMeterProvider.getDefaultProvider().createMeter("foo", null, null);
        assertThrows(NullPointerException.class, () -> meter.createLongHistogram(null, "description", null, null));
        assertThrows(NullPointerException.class, () -> meter.createLongHistogram("name", null, null, null));
        assertThrows(NullPointerException.class, () -> meter.createLongCounter(null, "description", null, null));
        assertThrows(NullPointerException.class, () -> meter.createLongCounter("name", null, null, null));
    }

    @Test
    public void noGlobalOTelNullOptions() {
        AzureMeter meter = AzureMeterProvider.getDefaultProvider().createMeter("az.sdk-name", null, null);
        AzureLongHistogram longHistogram = meter.createLongHistogram("az.sdk.test-histogram", "important metric", null, null);

        longHistogram.record(1, Context.NONE);
        testClock.advance(Duration.ofNanos(SECOND_NANOS));
        assertTrue(sdkMeterReader.collectAllMetrics().isEmpty());
        assertFalse(meter.isEnabled());
    }
}
