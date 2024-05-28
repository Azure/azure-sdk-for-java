// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.heartbeat;

import com.azure.core.test.annotation.LiveOnly;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MetricsData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class HeartbeatTests {
    // Dummy consumer that does nothing.
    private final Consumer<List<TelemetryItem>> telemetryItemsConsumer = ignored -> {};

    @Test
    @LiveOnly
    void heartBeatPayloadContainsDataByDefault() throws InterruptedException {
        // LiveOnly because it is intermittently failing in CI
        // given
        HeartbeatExporter provider = new HeartbeatExporter(60, (b, r) -> {
        }, telemetryItemsConsumer);

        // some of the initialization above happens in a separate thread
        Thread.sleep(5000);

        // then
        MetricsData data = (MetricsData) provider.gatherData().getData().getBaseData();
        assertThat(data).isNotNull();
        System.out.println(data.getProperties());
        assertThat(data.getProperties()).isNotEmpty();
    }

    @Test
    void heartBeatPayloadContainsSpecificProperties() {
        // given
        HeartbeatExporter provider = new HeartbeatExporter(60, (b, r) -> {
        }, telemetryItemsConsumer);

        // then
        assertThat(provider.addHeartBeatProperty("test", "testVal", true)).isTrue();

        MetricsData data = (MetricsData) provider.gatherData().getData().getBaseData();
        assertThat(data.getProperties()).containsEntry("test", "testVal");
    }

    @Test
    void heartbeatMetricIsNonZeroWhenFailureConditionPresent() {
        // given
        HeartbeatExporter provider = new HeartbeatExporter(60, (b, r) -> {
        }, telemetryItemsConsumer);

        // then
        assertThat(provider.addHeartBeatProperty("test", "testVal", false)).isTrue();

        MetricsData data = (MetricsData) provider.gatherData().getData().getBaseData();
        assertThat(data.getMetrics().get(0).getValue()).isEqualTo(1);
    }

    @Test
    void heartbeatMetricCountsForAllFailures() {
        // given
        HeartbeatExporter provider = new HeartbeatExporter(60, (b, r) -> {
        }, telemetryItemsConsumer);

        // then
        assertThat(provider.addHeartBeatProperty("test", "testVal", false)).isTrue();
        assertThat(provider.addHeartBeatProperty("test1", "testVal1", false)).isTrue();

        MetricsData data = (MetricsData) provider.gatherData().getData().getBaseData();
        assertThat(data.getMetrics().get(0).getValue()).isEqualTo(2);
    }

    @Test
    void sentHeartbeatContainsExpectedDefaultFields() {
        ConcurrentMap<String, String> props = new ConcurrentHashMap<>();

        HeartbeatExporter mockProvider = new HeartbeatExporter(60, (b, r) -> {
        }, telemetryItemsConsumer) {
            @Override
            public boolean addHeartBeatProperty(String propertyName, String propertyValue, boolean isHealthy) {
                props.put(propertyName, propertyValue);
                return true;
            }
        };

        DefaultHeartBeatPropertyProvider defaultProvider = new DefaultHeartBeatPropertyProvider();

        HeartbeatDefaultPayload.populateDefaultPayload(mockProvider).run();
        for (String fieldName : defaultProvider.defaultFields) {
            assertThat(props.containsKey(fieldName)).isTrue();
            assertThat(!props.get(fieldName).isEmpty()).isTrue();
        }
    }

    @Test
    void heartBeatProviderDoesNotAllowDuplicateProperties() {
        // given
        HeartbeatExporter provider = new HeartbeatExporter(60, (b, r) -> {
        }, telemetryItemsConsumer);

        // then
        provider.addHeartBeatProperty("test01", "test val", true);
        assertThat(provider.addHeartBeatProperty("test01", "test val 2", true)).isFalse();
    }

    @Test
    void cannotAddUnknownDefaultProperty() {
        DefaultHeartBeatPropertyProvider base = new DefaultHeartBeatPropertyProvider();
        String testKey = "testKey";

        base.defaultFields.add(testKey);

        HeartbeatExporter provider = new HeartbeatExporter(60, (b, r) -> {
        }, telemetryItemsConsumer);

        base.setDefaultPayload(provider).run();
        MetricsData data = (MetricsData) provider.gatherData().getData().getBaseData();
        assertThat(data.getProperties().containsKey("testKey")).isFalse();
    }
}
