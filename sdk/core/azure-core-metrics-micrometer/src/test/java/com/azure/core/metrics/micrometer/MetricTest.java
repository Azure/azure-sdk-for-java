package com.azure.core.metrics.micrometer;
/*
import com.azure.core.util.Context;
import com.azure.core.util.MetricsOptions;
import com.azure.core.util.metrics.LongHistogram;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.metrics.ClientMeterProvider;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.time.TestClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

public class MetricTest {
    private static final long SECOND_NANOS = 1_000_000_000;
    private final TestClock testClock = TestClock.create();
    private static final Resource RESOURCE =
        Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));

    private final InMemoryMetricReader sdkMeterReader = InMemoryMetricReader.create();

    @Test
    public void basicHistogram() {
        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
            .setClock(testClock)
            .setResource(RESOURCE)
            .registerMetricReader(sdkMeterReader)
            .build();

        Map<String, Object> commonAttributes = Collections.singletonMap("key", "value");
        Map<String, Object> extraAttributes = Collections.singletonMap("extraKey", "extraValue");

        LongHistogram longHistogram = MeterProvider
            .createMeter("az.sdk-name", "1.0.0-beta.1", new MetricsOptions().setImplementationConfiguration(sdkMeterProvider))
            .getLongHistogram("az.sdk.test-histogram", "important metric", "ms", commonAttributes);

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
    public void multipleMeters() {
        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
            .setClock(testClock)
            .setResource(RESOURCE)
            .registerMetricReader(sdkMeterReader)
            .build();

        Map<String, Object> commonAttributes = Collections.singletonMap("key", "value");
        Meter meter = MeterProvider
            .createMeter("az.sdk-name", "1.0.0-beta.1", new MetricsOptions().setImplementationConfiguration(sdkMeterProvider));

        LongHistogram longHistogram1 = meter.getLongHistogram("az.sdk.test-histogram", "important metric", "ms", commonAttributes);
        LongHistogram longHistogram2 = meter.getLongHistogram("az.sdk.test-histogram", "important metric", "ms", commonAttributes);

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
                                                .hasAttributes(Attributes.of(AttributeKey.stringKey("key"), "value"))
                                                .hasCount(2)
                                                .hasSum(462)
                                                .hasBucketBoundaries(
                                                    5, 10, 25, 50, 75, 100, 250, 500, 750, 1_000, 2_500,
                                                    5_000, 7_500, 10_000)
                                                .hasBucketCounts(
                                                    0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0))));
    }
}
*/
