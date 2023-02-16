// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import com.azure.core.http.HttpPipeline;
import com.azure.core.util.Configuration;
import com.azure.monitor.opentelemetry.exporter.AzureMonitorExporterBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MetricDataPoint;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MetricsData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MonitorBase;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.OpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
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

    private static final String TRACE_CONNECTION_STRING =
        "InstrumentationKey=00000000-0000-0000-0000-000000000000;"
            + "IngestionEndpoint=https://test.in.applicationinsights.azure.com/;"
            + "LiveEndpoint=https://test.livediagnostics.monitor.azure.com/";

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

    public static Tracer configureAzureMonitorTraceExporter(HttpPipeline httpPipeline) {
        return createOpenTelemetrySdk(httpPipeline).getTracer("Sample");
    }

    public static OpenTelemetry createOpenTelemetrySdk(HttpPipeline httpPipeline) {
        return createOpenTelemetrySdk(httpPipeline, Configuration.NONE);
    }

    public static OpenTelemetry createOpenTelemetrySdk(
        HttpPipeline httpPipeline, Configuration configuration) {
        return createOpenTelemetrySdkDeprecated(httpPipeline, configuration);
    }

    // remove this after Log API is public and can be retrieved from the OpenTelemetry object
    public static OpenTelemetrySdk createOpenTelemetrySdkDeprecated(
        HttpPipeline httpPipeline, Configuration configuration) {

        OpenTelemetrySdkBuilder builder = OpenTelemetrySdk.builder();

        SpanExporter spanExporter =
            new AzureMonitorExporterBuilder()
                .configuration(configuration)
                .connectionString(TRACE_CONNECTION_STRING)
                .httpPipeline(httpPipeline)
                .buildTraceExporter();

        SdkTracerProvider tracerProvider =
            SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
                .build();

        builder.setTracerProvider(tracerProvider);

        MetricExporter metricExporter =
            new AzureMonitorExporterBuilder()
                .configuration(configuration)
                .connectionString(TRACE_CONNECTION_STRING)
                .httpPipeline(httpPipeline)
                .buildMetricExporter();

        PeriodicMetricReader metricReader =
            PeriodicMetricReader.builder(metricExporter).setInterval(Duration.ofMillis(10)).build();
        SdkMeterProvider meterProvider =
            SdkMeterProvider.builder().registerMetricReader(metricReader).build();

        builder.setMeterProvider(meterProvider);

        LogRecordExporter logRecordExporter =
            new AzureMonitorExporterBuilder()
                .configuration(configuration)
                .connectionString(TRACE_CONNECTION_STRING)
                .httpPipeline(httpPipeline)
                .buildLogRecordExporter();

        SdkLoggerProvider loggerProvider =
            SdkLoggerProvider.builder()
                .addLogRecordProcessor(SimpleLogRecordProcessor.create(logRecordExporter))
                .build();

        builder.setLoggerProvider(loggerProvider);

        return builder.build();
    }

    private TestUtils() {
    }
}
