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

    public static OpenTelemetrySdk createOpenTelemetrySdk(HttpPipeline httpPipeline) {
        return createOpenTelemetrySdk(httpPipeline, Configuration.NONE);
    }

    public static OpenTelemetrySdk createOpenTelemetrySdk(
        HttpPipeline httpPipeline, Configuration configuration) {

        return new AzureMonitorExporterBuilder()
            .configuration(configuration)
            .connectionString(TRACE_CONNECTION_STRING)
            .httpPipeline(httpPipeline)
            .getOpenTelemetrySdkBuilder()
            .build()
            .getOpenTelemetrySdk();
    }

    private TestUtils() {
    }
}
