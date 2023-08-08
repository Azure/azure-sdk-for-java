// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.builders;

import com.azure.monitor.opentelemetry.exporter.implementation.models.MetricDataPoint;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MetricsData;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.FormattedTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StatsbeatTelemetryBuilder extends AbstractTelemetryBuilder {

    private final MetricsData data;

    public static StatsbeatTelemetryBuilder create(String name, double value) {
        StatsbeatTelemetryBuilder telemetryBuilder = new StatsbeatTelemetryBuilder(new MetricsData());

        MetricDataPoint point = new MetricDataPoint();
        point.setName(name);
        point.setValue(value);
        telemetryBuilder.setMetricDataPoint(point);

        telemetryBuilder.setTime(FormattedTime.offSetDateTimeFromNow());

        return telemetryBuilder;
    }

    private StatsbeatTelemetryBuilder(MetricsData data) {
        // not using the default telemetry name for metrics (which is "Metric")
        super(data, "Statsbeat", "MetricData");
        this.data = data;
    }

    public void setMetricDataPoint(MetricDataPoint point) {
        List<MetricDataPoint> metrics = data.getMetrics();
        if (metrics == null) {
            metrics = new ArrayList<>();
            data.setMetrics(metrics);
        }
        if (metrics.isEmpty()) {
            metrics.add(point);
        }
    }

    @Override
    protected Map<String, String> getProperties() {
        Map<String, String> properties = data.getProperties();
        if (properties == null) {
            properties = new HashMap<>();
            data.setProperties(properties);
        }
        return properties;
    }
}
