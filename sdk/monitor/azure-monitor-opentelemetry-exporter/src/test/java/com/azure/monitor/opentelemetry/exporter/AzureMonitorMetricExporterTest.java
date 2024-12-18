// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.monitor.opentelemetry.exporter.implementation.MetricDataMapper;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.MetricTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MetricDataPoint;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MetricsData;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricExporter;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.azure.monitor.opentelemetry.exporter.implementation.SemanticAttributes.HTTP_RESPONSE_STATUS_CODE;
import static com.azure.monitor.opentelemetry.exporter.implementation.SemanticAttributes.SERVER_ADDRESS;
import static io.opentelemetry.sdk.metrics.data.MetricDataType.DOUBLE_GAUGE;
import static io.opentelemetry.sdk.metrics.data.MetricDataType.DOUBLE_SUM;
import static io.opentelemetry.sdk.metrics.data.MetricDataType.HISTOGRAM;
import static io.opentelemetry.sdk.metrics.data.MetricDataType.LONG_GAUGE;
import static io.opentelemetry.sdk.metrics.data.MetricDataType.LONG_SUM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class AzureMonitorMetricExporterTest {

    @Test
    public void testDoubleCounter() {
        InMemoryMetricExporter inMemoryMetricExporter = InMemoryMetricExporter.create();
        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
            .registerMetricReader(PeriodicMetricReader.builder(inMemoryMetricExporter).build())
            .build();
        Meter meter = meterProvider.get("AzureMonitorMetricExporterTest");

        DoubleCounter counter = meter.counterBuilder("testDoubleCounter").ofDoubles().build();
        counter.add(3.1415);

        meterProvider.forceFlush();

        List<MetricData> metricDataList = inMemoryMetricExporter.getFinishedMetricItems();
        assertThat(metricDataList).hasSize(1);

        MetricData metricData = metricDataList.get(0);
        for (PointData pointData : metricData.getData().getPoints()) {
            MetricTelemetryBuilder builder = MetricTelemetryBuilder.create();
            MetricDataMapper.updateMetricPointBuilder(builder, metricDataList.get(0), pointData, true, false);
            MetricsData metricsData = (MetricsData) builder.build().getData().getBaseData();
            assertThat(metricsData.getMetrics().size()).isEqualTo(1);
            assertThat(metricsData.getMetrics().get(0).getValue()).isEqualTo(3.1415);
        }

        assertThat(metricData.getType()).isEqualTo(DOUBLE_SUM);
        assertThat(metricData.getName()).isEqualTo("testDoubleCounter");
    }

    @Test
    public void testDoubleGauge() {
        InMemoryMetricExporter inMemoryMetricExporter = InMemoryMetricExporter.create();
        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
            .registerMetricReader(PeriodicMetricReader.builder(inMemoryMetricExporter).build())
            .build();
        Meter meter = meterProvider.get("AzureMonitorMetricExporterTest");

        meter.gaugeBuilder("testDoubleGauge")
            .setDescription("the current temperature")
            .setUnit("C")
            .buildWithCallback(m -> m.record(20.0, Attributes.of(AttributeKey.stringKey("thing"), "engine")));

        meterProvider.forceFlush();

        List<MetricData> metricDataList = inMemoryMetricExporter.getFinishedMetricItems();
        assertThat(metricDataList).hasSize(1);

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
    public void testAttributesOnCustomMetric() {
        InMemoryMetricExporter inMemoryMetricExporter = InMemoryMetricExporter.create();
        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
            .registerMetricReader(PeriodicMetricReader.builder(inMemoryMetricExporter).build())
            .build();
        Meter meter = meterProvider.get("AzureMonitorMetricExporterTest");

        DoubleCounter counter = meter.counterBuilder("testAttributes").ofDoubles().build();
        Attributes attributes
            = Attributes.builder().put(SERVER_ADDRESS, "example.io").put(AttributeKey.stringKey("foo"), "bar").build();

        counter.add(1, attributes);

        meterProvider.forceFlush();

        List<MetricData> metricDatas = inMemoryMetricExporter.getFinishedMetricItems();

        MetricData metric = metricDatas.get(0);
        PointData pointData = metric.getData().getPoints().stream().findFirst().get();
        MetricTelemetryBuilder builder = MetricTelemetryBuilder.create();
        MetricDataMapper.updateMetricPointBuilder(builder, metric, pointData, true, false);

        MetricsData metricsData = (MetricsData) builder.build().getData().getBaseData();
        assertThat(metricsData.getMetrics().size()).isEqualTo(1);
        Map<String, String> properties = metricsData.getProperties();

        assertThat(properties.size()).isEqualTo(2);
        assertThat(properties.get(SERVER_ADDRESS.getKey())).isEqualTo("example.io");
        assertThat(properties.get("foo")).isEqualTo("bar");
    }

    @Test
    public void testAttributesOnStandardMetric() {
        InMemoryMetricExporter inMemoryMetricExporter = InMemoryMetricExporter.create();
        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
            .registerMetricReader(PeriodicMetricReader.builder(inMemoryMetricExporter).build())
            .build();
        Meter meter = meterProvider.get("AzureMonitorMetricExporterTest");

        DoubleHistogram serverDuration = meter.histogramBuilder("http.server.duration").build();
        Attributes attributes = Attributes.builder()
            .put(HTTP_RESPONSE_STATUS_CODE, 200)
            .put(SERVER_ADDRESS, "example.io")
            .put(AttributeKey.stringKey("foo"), "baz")
            .build();
        serverDuration.record(0.1, attributes);

        meterProvider.forceFlush();

        List<MetricData> metricDatas = inMemoryMetricExporter.getFinishedMetricItems();

        MetricData metric = metricDatas.get(0);
        PointData pointData = metric.getData().getPoints().stream().findFirst().get();
        MetricTelemetryBuilder builder = MetricTelemetryBuilder.create();
        MetricDataMapper.updateMetricPointBuilder(builder, metric, pointData, true, true);

        MetricsData metricsData = (MetricsData) builder.build().getData().getBaseData();
        assertThat(metricsData.getMetrics().size()).isEqualTo(1);
        Map<String, String> properties = metricsData.getProperties();

        assertThat(properties.size()).isEqualTo(5);
        assertThat(properties.get("operation/synthetic")).isEqualTo("False");
        assertThat(properties.get("Request.Success")).isEqualTo("True");
        assertThat(properties.get("request/resultCode")).isEqualTo("200");
        assertThat(properties.get("_MS.IsAutocollected")).isEqualTo("True");
        assertThat(properties.get("_MS.MetricId")).isEqualTo("requests/duration");
    }

    @Test
    public void testLongCounter() {
        InMemoryMetricExporter inMemoryMetricExporter = InMemoryMetricExporter.create();
        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
            .registerMetricReader(PeriodicMetricReader.builder(inMemoryMetricExporter).build())
            .build();
        Meter meter = meterProvider.get("AzureMonitorMetricExporterTest");

        LongCounter counter = meter.counterBuilder("testLongCounter").build();
        counter.add(1, Attributes.of(AttributeKey.stringKey("name"), "apple", AttributeKey.stringKey("color"), "red"));
        counter.add(2,
            Attributes.of(AttributeKey.stringKey("name"), "lemon", AttributeKey.stringKey("color"), "yellow"));
        counter.add(1,
            Attributes.of(AttributeKey.stringKey("name"), "lemon", AttributeKey.stringKey("color"), "yellow"));
        counter.add(2,
            Attributes.of(AttributeKey.stringKey("name"), "apple", AttributeKey.stringKey("color"), "green"));
        counter.add(5, Attributes.of(AttributeKey.stringKey("name"), "apple", AttributeKey.stringKey("color"), "red"));
        counter.add(4,
            Attributes.of(AttributeKey.stringKey("name"), "lemon", AttributeKey.stringKey("color"), "yellow"));

        meterProvider.forceFlush();

        List<MetricData> metricDataList = inMemoryMetricExporter.getFinishedMetricItems();
        assertThat(metricDataList).hasSize(1);

        MetricData metricData = metricDataList.get(0);
        @SuppressWarnings("unchecked")
        Collection<LongPointData> points = (Collection<LongPointData>) metricData.getData().getPoints();
        assertThat(points.size()).isEqualTo(3);

        points = points.stream().sorted(Comparator.comparing(LongPointData::getValue)).collect(Collectors.toList());

        Iterator<LongPointData> iterator = points.iterator();
        LongPointData longPointData1 = iterator.next();
        assertThat(longPointData1.getValue()).isEqualTo(2L);
        assertThat(longPointData1.getAttributes().get(AttributeKey.stringKey("name"))).isEqualTo("apple");
        assertThat(longPointData1.getAttributes().get(AttributeKey.stringKey("color"))).isEqualTo("green");

        LongPointData longPointData2 = iterator.next();
        assertThat(longPointData2.getValue()).isEqualTo(6L);
        assertThat(longPointData2.getAttributes().get(AttributeKey.stringKey("name"))).isEqualTo("apple");
        assertThat(longPointData2.getAttributes().get(AttributeKey.stringKey("color"))).isEqualTo("red");

        LongPointData longPointData3 = iterator.next();
        assertThat(longPointData3.getValue()).isEqualTo(7L);
        assertThat(longPointData3.getAttributes().get(AttributeKey.stringKey("name"))).isEqualTo("lemon");
        assertThat(longPointData3.getAttributes().get(AttributeKey.stringKey("color"))).isEqualTo("yellow");

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
    public void testLongGauge() {
        InMemoryMetricExporter inMemoryMetricExporter = InMemoryMetricExporter.create();
        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
            .registerMetricReader(PeriodicMetricReader.builder(inMemoryMetricExporter).build())
            .build();
        Meter meter = meterProvider.get("AzureMonitorMetricExporterTest");

        meter.gaugeBuilder("testLongGauge")
            .ofLongs()
            .setDescription("the current temperature")
            .setUnit("C")
            .buildWithCallback(m -> {
                m.record(20, Attributes.of(AttributeKey.stringKey("thing"), "engine"));
            });

        meterProvider.forceFlush();

        List<MetricData> metricDataList = inMemoryMetricExporter.getFinishedMetricItems();
        assertThat(metricDataList).hasSize(1);

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
    public void testDoubleHistogram() {
        InMemoryMetricExporter inMemoryMetricExporter = InMemoryMetricExporter.create();
        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
            .registerMetricReader(PeriodicMetricReader.builder(inMemoryMetricExporter).build())
            .build();
        Meter meter = meterProvider.get("AzureMonitorMetricExporterTest");

        DoubleHistogram doubleHistogram = meter.histogramBuilder("testDoubleHistogram")
            .setDescription("http.client.duration")
            .setUnit("ms")
            .build();

        doubleHistogram.record(25.45);

        meterProvider.forceFlush();

        List<MetricData> metricDataList = inMemoryMetricExporter.getFinishedMetricItems();
        assertThat(metricDataList).hasSize(1);

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

    @Test
    public void testNoAttributeWithPrefixApplicationInsightsInternal() {
        InMemoryMetricExporter inMemoryMetricExporter = InMemoryMetricExporter.create();
        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
            .registerMetricReader(PeriodicMetricReader.builder(inMemoryMetricExporter).build())
            .build();
        Meter meter = meterProvider.get("AzureMonitorMetricExporterTest");

        // create a long counter with two attributes including one with "applicationinsights.internal." as prefix
        LongCounter longCounter
            = meter.counterBuilder("testLongCounter").setDescription("testLongCounter").setUnit("1").build();

        Attributes attributes = Attributes.of(AttributeKey.stringKey("applicationinsights.internal.test"), "test",
            AttributeKey.stringKey("foo"), "bar");
        longCounter.add(1, attributes);

        meterProvider.forceFlush();

        List<MetricData> metricDataList = inMemoryMetricExporter.getFinishedMetricItems();
        assertThat(metricDataList).hasSize(1);

        MetricData metricData = metricDataList.get(0);
        assertThat(metricData.getData().getPoints().size()).isEqualTo(1);
        PointData pointData = metricData.getData().getPoints().iterator().next();
        MetricTelemetryBuilder builder = MetricTelemetryBuilder.create();
        MetricDataMapper.updateMetricPointBuilder(builder, metricData, pointData, true, false);
        MetricsData metricsData = (MetricsData) builder.build().getData().getBaseData();
        assertThat(metricsData.getMetrics().size()).isEqualTo(1);
        assertThat(metricsData.getProperties()).isNotNull();
        assertThat(metricsData.getProperties().size()).isEqualTo(1);
        assertThat(metricsData.getProperties()).containsExactly(entry("foo", "bar"));
        assertThat(metricsData.getProperties().get("applicationinsights.internal.test")).isNull();

        assertThat(metricData.getType()).isEqualTo(LONG_SUM);
        assertThat(metricData.getName()).isEqualTo("testLongCounter");
    }
}
