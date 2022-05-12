package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.Context;
import com.azure.core.util.MetricsOptions;
import com.azure.core.util.metrics.DoubleHistogram;
import com.azure.core.util.metrics.MeterProxy;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.time.TestClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class MetricTest {
    private static final long SECOND_NANOS = 1_000_000_000;
    private final TestClock testClock = TestClock.create();
    private final InMemoryMetricReader sdkMeterReader = InMemoryMetricReader.create();

    @Test
    public void testMe() {
        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
            .setClock(testClock)
            .registerMetricReader(sdkMeterReader)
            .build();
        DoubleHistogram histogram = MeterProxy.getDoubleHistogram("az.sdk.test-me", "important metric", "ms", new MetricsOptions<>(sdkMeterProvider));

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("key", "value");

        histogram.record(0.1, attributes, Context.NONE);
        testClock.advance(Duration.ofNanos(SECOND_NANOS));
        assertFalse(sdkMeterReader.collectAllMetrics().isEmpty());
    }
}
