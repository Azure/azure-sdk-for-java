// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.statsbeat;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.MetricDataPoint;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.MetricsData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.pipeline.TelemetryItemExporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class NonessentialStatsbeatTest {

    private NonessentialStatsbeat nonessentialStatsbeat;

    @BeforeEach
    public void init() {
        nonessentialStatsbeat = new NonessentialStatsbeat();
    }

    @Test
    public void testIncrementReadFailureCount() {
        assertThat(nonessentialStatsbeat.getReadFailureCount()).isEqualTo(0);
        for (int i = 0; i < 100; i++) {
            nonessentialStatsbeat.incrementReadFailureCount();
        }
        assertThat(nonessentialStatsbeat.getReadFailureCount()).isEqualTo(100);
    }

    @Test
    public void testIncrementWriteFailureCount() {
        assertThat(nonessentialStatsbeat.getWriteFailureCount()).isEqualTo(0);
        for (int i = 0; i < 100; i++) {
            nonessentialStatsbeat.incrementWriteFailureCount();
        }
        assertThat(nonessentialStatsbeat.getWriteFailureCount()).isEqualTo(100);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSendReadFailureCount() {
        TelemetryItemExporter mockExporter = mock(TelemetryItemExporter.class);

        for (int i = 0; i < 5; i++) {
            nonessentialStatsbeat.incrementReadFailureCount();
        }

        nonessentialStatsbeat.send(mockExporter);

        // Capture all sent telemetry items
        ArgumentCaptor<List<TelemetryItem>> telemetryItemCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockExporter, atLeastOnce()).send(telemetryItemCaptor.capture());

        // Find and verify the Read_Failure_Count metric
        MetricDataPoint readFailureMetric = findMetricByName(telemetryItemCaptor.getAllValues(), "Read_Failure_Count");
        assertThat(readFailureMetric).isNotNull();
        assertThat(readFailureMetric.getValue()).isEqualTo(5.0);

        // Counter should be reset after send
        assertThat(nonessentialStatsbeat.getReadFailureCount()).isEqualTo(0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSendWriteFailureCount() {
        TelemetryItemExporter mockExporter = mock(TelemetryItemExporter.class);

        for (int i = 0; i < 3; i++) {
            nonessentialStatsbeat.incrementWriteFailureCount();
        }

        nonessentialStatsbeat.send(mockExporter);

        // Capture all sent telemetry items
        ArgumentCaptor<List<TelemetryItem>> telemetryItemCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockExporter, atLeastOnce()).send(telemetryItemCaptor.capture());

        // Find and verify the Write_Failure_Count metric
        MetricDataPoint writeFailureMetric
            = findMetricByName(telemetryItemCaptor.getAllValues(), "Write_Failure_Count");
        assertThat(writeFailureMetric).isNotNull();
        assertThat(writeFailureMetric.getValue()).isEqualTo(3.0);

        // Counter should be reset after send
        assertThat(nonessentialStatsbeat.getWriteFailureCount()).isEqualTo(0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSendReadAndWriteFailure() {
        TelemetryItemExporter mockExporter = mock(TelemetryItemExporter.class);

        for (int i = 0; i < 2; i++) {
            nonessentialStatsbeat.incrementReadFailureCount();
        }
        for (int i = 0; i < 5; i++) {
            nonessentialStatsbeat.incrementWriteFailureCount();
        }

        nonessentialStatsbeat.send(mockExporter);

        // Capture all sent telemetry items
        ArgumentCaptor<List<TelemetryItem>> telemetryItemCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockExporter, atLeastOnce()).send(telemetryItemCaptor.capture());

        List<List<TelemetryItem>> allCapturedValues = telemetryItemCaptor.getAllValues();

        // Find and verify Read_Failure_Count metric
        MetricDataPoint readFailureMetric = findMetricByName(allCapturedValues, "Read_Failure_Count");
        assertThat(readFailureMetric).isNotNull();
        assertThat(readFailureMetric.getValue()).isEqualTo(2.0);

        // Find and verify Write_Failure_Count metric
        MetricDataPoint writeFailureMetric = findMetricByName(allCapturedValues, "Write_Failure_Count");
        assertThat(writeFailureMetric).isNotNull();
        assertThat(writeFailureMetric.getValue()).isEqualTo(5.0);

        // Both counters should be reset after send
        assertThat(nonessentialStatsbeat.getReadFailureCount()).isEqualTo(0);
        assertThat(nonessentialStatsbeat.getWriteFailureCount()).isEqualTo(0);
    }

    private MetricDataPoint findMetricByName(List<List<TelemetryItem>> allCapturedValues, String metricName) {
        return allCapturedValues.stream()
            .flatMap(List::stream)
            .filter(item -> item.getData().getBaseData() instanceof MetricsData)
            .map(item -> (MetricsData) item.getData().getBaseData())
            .flatMap(metricsData -> metricsData.getMetrics().stream())
            .filter(metric -> metricName.equals(metric.getName()))
            .findFirst()
            .orElse(null);
    }
}
