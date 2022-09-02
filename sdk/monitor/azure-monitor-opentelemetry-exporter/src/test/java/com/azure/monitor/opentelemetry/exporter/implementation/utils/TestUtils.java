// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.monitor.opentelemetry.exporter.AzureMonitorExporterBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MetricDataPoint;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MetricsData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MonitorBase;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TestUtils {

    public static TelemetryItem createMetricTelemetry(
        String name, int value, String connectionString) {
        TelemetryItem telemetry = new TelemetryItem();
        telemetry.setVersion(1);
        telemetry.setName("Metric");
        telemetry.setConnectionString(connectionString);
        Map<String, String> tags = new HashMap<>();
        tags.put("ai.internal.sdkVersion", "test_version");
        tags.put("ai.internal.nodeName", "test_role_name");
        tags.put("ai.cloud.roleInstance", "test_cloud_name");
        telemetry.setTags(tags);

        MetricsData data = new MetricsData();
        List<MetricDataPoint> dataPoints = new ArrayList<>();
        MetricDataPoint dataPoint = new MetricDataPoint();
        dataPoint.setName(name);
        dataPoint.setValue(value);
        dataPoint.setCount(1);
        dataPoints.add(dataPoint);

        Map<String, String> properties = new HashMap<>();
        properties.put("state", "blocked");

        data.setMetrics(dataPoints);
        data.setProperties(properties);

        MonitorBase monitorBase = new MonitorBase();
        monitorBase.setBaseType("MetricData");
        monitorBase.setBaseData(data);
        telemetry.setData(monitorBase);
        telemetry.setTime(FormattedTime.offSetDateTimeFromNow());

        return telemetry;
    }

    public static Tracer configureAzureMonitorTraceExporter(HttpPipelinePolicy validator) {
        SpanExporter exporter =
            new AzureMonitorExporterBuilder()
                .connectionString(System.getenv("APPLICATIONINSIGHTS_CONNECTION_STRING"))
                .addHttpPipelinePolicy(validator)
                .buildTraceExporter();

        SdkTracerProvider tracerProvider =
            SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(exporter)).build();

        OpenTelemetrySdk openTelemetrySdk =
            OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();
        return openTelemetrySdk.getTracer("Sample");
    }

    public static Meter configureAzureMonitorMetricExporter(HttpPipelinePolicy policy) {
        MetricExporter exporter =
            new AzureMonitorExporterBuilder()
                .connectionString(System.getenv("APPLICATIONINSIGHTS_CONNECTION_STRING"))
                .addHttpPipelinePolicy(policy)
                .buildMetricExporter();

        PeriodicMetricReader metricReader =
            PeriodicMetricReader.builder(exporter).setInterval(Duration.ofMillis(10)).build();
        SdkMeterProvider meterProvider =
            SdkMeterProvider.builder().registerMetricReader(metricReader).build();
        OpenTelemetry openTelemetry =
            OpenTelemetrySdk.builder().setMeterProvider(meterProvider).build();

        return openTelemetry.getMeter("Sample");
    }

    private TestUtils() {
    }
}
