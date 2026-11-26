// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.clienttelemetry.TagName;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosMetricName;
import com.azure.cosmos.models.CosmosMetricTagName;
import com.azure.cosmos.models.CosmosMicrometerMetricsOptions;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

final class FailoverMetricsTestHelper implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(FailoverMetricsTestHelper.class);
    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();

    CosmosClientTelemetryConfig createTelemetryConfig() {
        return new CosmosClientTelemetryConfig().metricsOptions(new CosmosMicrometerMetricsOptions()
            .meterRegistry(this.registry)
            .setEnabled(true)
            .configureDefaultTagNames(CosmosMetricTagName.DEFAULT, CosmosMetricTagName.PARTITION_KEY_RANGE_ID,
                CosmosMetricTagName.SERVICE_ENDPOINT)
            .enableHistogramsByDefault(false)
            .configureDefaultPercentiles()
            .applyDiagnosticThresholdsForTransportLevelMeters(false));
    }

    Map<Meter.Id, Double> snapshotRequestCounts() {
        return this.registry.getMeters().stream()
            .filter(meter -> meter instanceof Counter)
            .collect(Collectors.toMap(Meter::getId, meter -> ((Counter) meter).count()));
    }

    void assertRequestCountIncreased(ConnectionMode connectionMode, Tags tags, Map<Meter.Id, Double> baseline) {
        String metricName = (connectionMode == ConnectionMode.DIRECT
            ? CosmosMetricName.REQUEST_SUMMARY_DIRECT_REQUESTS
            : CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS).toString();
        Collection<Counter> counters = this.registry.find(metricName).tags(tags).counters();
        assertThat(counters).as("Request counters for %s with %s", metricName, tags).isNotEmpty();
        double delta = counters.stream()
            .mapToDouble(counter -> counter.count() - baseline.getOrDefault(counter.getId(), 0d))
            .sum();
        assertThat(delta).as("Request counter delta for %s with %s", metricName, tags).isGreaterThan(0);
        LOGGER.info("Failover request metric {} with {} increased by {}", metricName, tags, delta);
    }

    static Tag regionTag(String regionName) {
        return Tag.of(TagName.RegionName.toString(), regionName.toLowerCase(Locale.ROOT));
    }

    @Override
    public void close() {
        this.registry.close();
    }
}
