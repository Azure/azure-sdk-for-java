// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.Context;
import com.azure.core.util.metrics.LongHistogram;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.metrics.MeterProvider;
import io.opentelemetry.api.GlobalOpenTelemetry;
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
import org.junit.jupiter.api.parallel.Isolated;

import java.time.Duration;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Isolated
public class GlobalOpenTelemetryMetricsTests {
    private static final long SECOND_NANOS = 1_000_000_000;
    private static final Resource RESOURCE =
        Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));

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
    public void basicHistogramGlobalOTel() {
        OpenTelemetry otel = OpenTelemetrySdk.builder().setMeterProvider(sdkMeterProvider).buildAndRegisterGlobal();

        Meter meter = MeterProvider.getDefaultProvider().createMeter("az.sdk-name", null, null);
        LongHistogram longHistogram = meter.createLongHistogram("az.sdk.test-histogram", "important metric", null);
        assertTrue(longHistogram.isEnabled());

        longHistogram.record(1, null, Context.NONE);
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
}
