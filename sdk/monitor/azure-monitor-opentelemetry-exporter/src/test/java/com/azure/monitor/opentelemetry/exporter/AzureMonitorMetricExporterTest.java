// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.monitor.opentelemetry.exporter.implementation.MetricDataMapper;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.MetricTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MetricDataPoint;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MetricsData;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricExporter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.opentelemetry.sdk.metrics.data.MetricDataType.DOUBLE_GAUGE;
import static io.opentelemetry.sdk.metrics.data.MetricDataType.DOUBLE_SUM;
import static io.opentelemetry.sdk.metrics.data.MetricDataType.HISTOGRAM;
import static io.opentelemetry.sdk.metrics.data.MetricDataType.LONG_GAUGE;
import static io.opentelemetry.sdk.metrics.data.MetricDataType.LONG_SUM;
import static org.assertj.core.api.Assertions.assertThat;

public class AzureMonitorMetricExporterTest {

    private Meter meter;
    private InMemoryMetricExporter inMemoryMetricExporter;

    @BeforeEach
    public void setup() {
        inMemoryMetricExporter = InMemoryMetricExporter.create();
        PeriodicMetricReader metricReader =
            PeriodicMetricReader.builder(inMemoryMetricExporter)
                .setInterval(Duration.ofMillis(100))
                .build();
        SdkMeterProvider meterProvider =
            SdkMeterProvider.builder().registerMetricReader(metricReader).build();

        OpenTelemetry openTelemetry =
            OpenTelemetrySdk.builder().setMeterProvider(meterProvider).build();

        meter =
            openTelemetry
                .meterBuilder("AzureMonitorMetricExporterTest")
                .setInstrumentationVersion("1.0.0")
                .build();
    }

    @AfterEach
    public void cleanup() {
        inMemoryMetricExporter.reset();
    }

    @Test
    public void testDoubleCounter() throws InterruptedException {
        DoubleCounter counter = meter.counterBuilder("testDoubleCounter").ofDoubles().build();
        counter.add(3.1415);

        List<MetricData> metricDatas = getFinishedMetricItems(1);

        MetricData metricData = metricDatas.get(0);
        for (PointData pointData : metricData.getData().getPoints()) {
            MetricTelemetryBuilder builder = MetricTelemetryBuilder.create();
            MetricDataMapper.updateMetricPointBuilder(
                builder, metricDatas.get(0), pointData, true, false);
            MetricsData metricsData = (MetricsData) builder.build().getData().getBaseData();
            assertThat(metricsData.getMetrics().size()).isEqualTo(1);
            assertThat(metricsData.getMetrics().get(0).getValue()).isEqualTo(3.1415);
        }

        assertThat(metricData.getType()).isEqualTo(DOUBLE_SUM);
        assertThat(metricData.getName()).isEqualTo("testDoubleCounter");
    }

    @Test
    public void testDoubleGauge() throws InterruptedException {
        meter
            .gaugeBuilder("testDoubleGauge")
            .setDescription("the current temperature")
            .setUnit("C")
            .buildWithCallback(
                m -> {
                    m.record(20.0, Attributes.of(AttributeKey.stringKey("thing"), "engine"));
                });

        List<MetricData> metricDataList = getFinishedMetricItems(1);

        MetricData metricData = metricDataList.get(0);
        for (PointData pointData : metricData.getData().getPoints()) {
            MetricTelemetryBuilder builder = MetricTelemetryBuilder.create();
            MetricDataMapper.updateMetricPointBuilder(builder, metricData, pointData, true, false);
            MetricsData metricsData = (MetricsData) builder.build().getData().getBaseData();
            assertThat(metricsData.getMetrics().size()).isEqualTo(1);
            assertThat(metricsData.getMetrics().get(0).getValue()).isEqualTo(20.0);
            assertThat(metricsData.getProperties().size()).isEqualTo(1);
            assertThat(metricsData.getProperties()).containsEntry("thing", "engine");
        }

        assertThat(metricData.getType()).isEqualTo(DOUBLE_GAUGE);
        assertThat(metricData.getName()).isEqualTo("testDoubleGauge");
    }

    @Test
    public void testLongCounter() throws InterruptedException {
        LongCounter counter = meter.counterBuilder("testLongCounter").build();
        counter.add(
            1,
            Attributes.of(
                AttributeKey.stringKey("name"), "apple", AttributeKey.stringKey("color"), "red"));
        counter.add(
            2,
            Attributes.of(
                AttributeKey.stringKey("name"), "lemon", AttributeKey.stringKey("color"), "yellow"));
        counter.add(
            1,
            Attributes.of(
                AttributeKey.stringKey("name"), "lemon", AttributeKey.stringKey("color"), "yellow"));
        counter.add(
            2,
            Attributes.of(
                AttributeKey.stringKey("name"), "apple", AttributeKey.stringKey("color"), "green"));
        counter.add(
            5,
            Attributes.of(
                AttributeKey.stringKey("name"), "apple", AttributeKey.stringKey("color"), "red"));
        counter.add(
            4,
            Attributes.of(
                AttributeKey.stringKey("name"), "lemon", AttributeKey.stringKey("color"), "yellow"));

        List<MetricData> metricDataList = getFinishedMetricItems(1);

        MetricData metricData = metricDataList.get(0);
        @SuppressWarnings("unchecked")
        Collection<LongPointData> points = (Collection<LongPointData>) metricData.getData().getPoints();
        assertThat(points.size()).isEqualTo(3);

        points =
            points.stream()
                .sorted(Comparator.comparing(LongPointData::getValue))
                .collect(Collectors.toList());

        Iterator<LongPointData> iterator = points.iterator();
        LongPointData longPointData1 = iterator.next();
        assertThat(longPointData1.getValue()).isEqualTo(2L);
        assertThat(longPointData1.getAttributes().get(AttributeKey.stringKey("name")))
            .isEqualTo("apple");
        assertThat(longPointData1.getAttributes().get(AttributeKey.stringKey("color")))
            .isEqualTo("green");

        LongPointData longPointData2 = iterator.next();
        assertThat(longPointData2.getValue()).isEqualTo(6L);
        assertThat(longPointData2.getAttributes().get(AttributeKey.stringKey("name")))
            .isEqualTo("apple");
        assertThat(longPointData2.getAttributes().get(AttributeKey.stringKey("color")))
            .isEqualTo("red");

        LongPointData longPointData3 = iterator.next();
        assertThat(longPointData3.getValue()).isEqualTo(7L);
        assertThat(longPointData3.getAttributes().get(AttributeKey.stringKey("name")))
            .isEqualTo("lemon");
        assertThat(longPointData3.getAttributes().get(AttributeKey.stringKey("color")))
            .isEqualTo("yellow");

        MetricTelemetryBuilder builder = MetricTelemetryBuilder.create();
        MetricDataMapper.updateMetricPointBuilder(builder, metricData, longPointData1, true, false);
        MetricsData metricsData = (MetricsData) builder.build().getData().getBaseData();
        assertThat(metricsData.getMetrics().size()).isEqualTo(1);
        MetricDataPoint metricDataPoint = metricsData.getMetrics().get(0);
        assertThat(metricDataPoint.getValue()).isEqualTo(2);

        Map<String, String> properties = metricsData.getProperties();
        assertThat(properties.size()).isEqualTo(2);
        assertThat(properties).containsEntry("name", "apple");
        assertThat(properties).containsEntry("color", "green");

        builder = MetricTelemetryBuilder.create();
        MetricDataMapper.updateMetricPointBuilder(builder, metricData, longPointData2, true, false);
        metricsData = (MetricsData) builder.build().getData().getBaseData();
        assertThat(metricsData.getMetrics().size()).isEqualTo(1);
        metricDataPoint = metricsData.getMetrics().get(0);
        assertThat(metricDataPoint.getValue()).isEqualTo(6);

        properties = metricsData.getProperties();
        assertThat(properties.size()).isEqualTo(2);
        assertThat(properties).containsEntry("name", "apple");
        assertThat(properties).containsEntry("color", "red");

        builder = MetricTelemetryBuilder.create();
        MetricDataMapper.updateMetricPointBuilder(builder, metricData, longPointData3, true, false);
        metricsData = (MetricsData) builder.build().getData().getBaseData();
        assertThat(metricsData.getMetrics().size()).isEqualTo(1);
        metricDataPoint = metricsData.getMetrics().get(0);
        assertThat(metricDataPoint.getValue()).isEqualTo(7);

        properties = metricsData.getProperties();
        assertThat(properties.size()).isEqualTo(2);
        assertThat(properties).containsEntry("name", "lemon");
        assertThat(properties).containsEntry("color", "yellow");

        assertThat(metricData.getType()).isEqualTo(LONG_SUM);
        assertThat(metricData.getName()).isEqualTo("testLongCounter");
    }

    @Test
    public void testLongGauge() throws InterruptedException {
        meter
            .gaugeBuilder("testLongGauge")
            .ofLongs()
            .setDescription("the current temperature")
            .setUnit("C")
            .buildWithCallback(
                m -> {
                    m.record(20, Attributes.of(AttributeKey.stringKey("thing"), "engine"));
                });

        List<MetricData> metricDataList = getFinishedMetricItems(1);

        MetricData metricData = metricDataList.get(0);
        for (PointData pointData : metricData.getData().getPoints()) {
            MetricTelemetryBuilder builder = MetricTelemetryBuilder.create();
            MetricDataMapper.updateMetricPointBuilder(builder, metricData, pointData, true, false);
            MetricsData metricsData = (MetricsData) builder.build().getData().getBaseData();
            assertThat(metricsData.getMetrics().size()).isEqualTo(1);
            assertThat(metricsData.getMetrics().get(0).getValue()).isEqualTo(20);
            assertThat(metricsData.getProperties().size()).isEqualTo(1);
            assertThat(metricsData.getProperties()).containsEntry("thing", "engine");
        }

        assertThat(metricData.getType()).isEqualTo(LONG_GAUGE);
        assertThat(metricData.getName()).isEqualTo("testLongGauge");
    }

    @Test
    public void testDoubleHistogram() throws InterruptedException {
        DoubleHistogram doubleHistogram =
            meter
                .histogramBuilder("testDoubleHistogram")
                .setDescription("http.client.duration")
                .setUnit("ms")
                .build();

        doubleHistogram.record(25.45);

        List<MetricData> metricDataList = getFinishedMetricItems(1);

        MetricData metricData = metricDataList.get(0);
        assertThat(metricData.getData().getPoints().size()).isEqualTo(1);
        PointData pointData = metricData.getData().getPoints().iterator().next();
        MetricTelemetryBuilder builder = MetricTelemetryBuilder.create();
        MetricDataMapper.updateMetricPointBuilder(builder, metricData, pointData, true, false);
        MetricsData metricsData = (MetricsData) builder.build().getData().getBaseData();
        assertThat(metricsData.getMetrics().size()).isEqualTo(1);
        assertThat(metricsData.getMetrics().get(0).getCount()).isEqualTo(1);
        assertThat(metricsData.getMetrics().get(0).getValue()).isEqualTo(25.45);
        assertThat(metricsData.getProperties()).isNull();
        assertThat(metricsData.getMetrics().get(0).getMax()).isEqualTo(25.45);
        assertThat(metricsData.getMetrics().get(0).getMin()).isEqualTo(25.45);

        assertThat(metricData.getType()).isEqualTo(HISTOGRAM);
        assertThat(metricData.getName()).isEqualTo("testDoubleHistogram");
    }

    private List<MetricData> getFinishedMetricItems(int expected) throws InterruptedException {
        long startMillis = System.currentTimeMillis();
        while (System.currentTimeMillis() - startMillis < 5000) {
            List<MetricData> finishedMetricItems = inMemoryMetricExporter.getFinishedMetricItems();
            if (finishedMetricItems.size() >= expected) {
                return finishedMetricItems;
            }
            Thread.sleep(10);
        }
        List<MetricData> finishedMetricItems = inMemoryMetricExporter.getFinishedMetricItems();
        assertThat(finishedMetricItems).hasSizeGreaterThanOrEqualTo(expected);
        return finishedMetricItems;
    }
}
