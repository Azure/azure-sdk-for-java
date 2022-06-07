// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.AzureAttributeBuilder;
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
    private static final AzureAttributeBuilder METRIC_ATTRIBUTES = new OpenTelemetryAzureAttributeBuilder().add("key", "value");
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
        AzureLongHistogram longHistogram = meter.createLongHistogram("az.sdk.test-histogram", "important metric", null);

        longHistogram.record(1, new OpenTelemetryAzureAttributeBuilder(), Context.NONE);
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
            .createLongHistogram("az.sdk.test-histogram", "important metric", null);

        longHistogram.record(1, new OpenTelemetryAzureAttributeBuilder(), Context.NONE);
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
            .createLongHistogram("az.sdk.test-histogram", "important metric", null);

        longHistogram.record(1, new OpenTelemetryAzureAttributeBuilder(), Context.NONE);
        testClock.advance(Duration.ofNanos(SECOND_NANOS));
        assertTrue(sdkMeterReader.collectAllMetrics().isEmpty());
        assertFalse(meter.isEnabled());
    }

    @Test
    public void histogramWithAttributes() {
        AzureLongHistogram longHistogram = AzureMeterProvider.getDefaultProvider()
            .createMeter("az.sdk-name", "1.0.0-beta.1", new MetricsOptions().setProvider(sdkMeterProvider))
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
        AzureLongHistogram longHistogram = AzureMeterProvider.getDefaultProvider()
            .createMeter("az.sdk-name", "1.0.0-beta.1", new MetricsOptions().setProvider(sdkMeterProvider))
            .createLongHistogram("az.sdk.test-histogram", "important metric", "ms");

        AzureAttributeBuilder attributes1 = new OpenTelemetryAzureAttributeBuilder().add("key1", "value1");
        AzureAttributeBuilder attributes2 = new OpenTelemetryAzureAttributeBuilder().add("key2", "value2");
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
        AzureLongCounter longCounter = AzureMeterProvider.getDefaultProvider()
            .createMeter("az.sdk-name", "1.0.0-beta.1", new MetricsOptions().setProvider(sdkMeterProvider))
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
    public void attributeTypes() {
        AzureAttributeBuilder attributes = new OpenTelemetryAzureAttributeBuilder()
            .add("string", "foo")
            .add("boolean", true)
            .add("double", 0.42d)
            .add("long", 42L);

        Attributes expected = Attributes.builder()
            .put(AttributeKey.stringKey("string"), "foo")
            .put(AttributeKey.booleanKey("boolean"), true)
            .put(AttributeKey.doubleKey("double"), 0.42d)
            .put(AttributeKey.longKey("long"), 42L)
            .build();

        AzureLongCounter longCounter = AzureMeterProvider.getDefaultProvider()
            .createMeter("az.sdk-name", "1.0.0-beta.1", new MetricsOptions().setProvider(sdkMeterProvider))
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
        AzureMeter meter = AzureMeterProvider.getDefaultProvider()
            .createMeter("az.sdk-name", "1.0.0-beta.1", new MetricsOptions().setProvider(sdkMeterProvider));

        AzureLongHistogram longHistogram1 = meter.createLongHistogram("az.sdk.test-histogram", "important metric", "ms");
        AzureLongHistogram longHistogram2 = meter.createLongHistogram("az.sdk.test-histogram", "important metric", "ms");

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
        assertThrows(NullPointerException.class, () -> AzureMeterProvider.getDefaultProvider().createMeter(null, null, null));
    }

    @Test
    public void noopMeterCreateInstrumentInvalidArgumentsThrow() {
        AzureMeter meter = AzureMeterProvider.getDefaultProvider().createMeter("foo", null, null);
        assertThrows(NullPointerException.class, () -> meter.createLongHistogram(null, "description", null));
        assertThrows(NullPointerException.class, () -> meter.createLongHistogram("name", null, null));
        assertThrows(NullPointerException.class, () -> meter.createLongCounter(null, "description", null));
        assertThrows(NullPointerException.class, () -> meter.createLongCounter("name", null, null));
    }
}
