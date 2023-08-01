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

public final class MetricTelemetryBuilder extends AbstractTelemetryBuilder {

    private final MetricsData data;

    public static MetricTelemetryBuilder create() {
        return new MetricTelemetryBuilder(new MetricsData());
    }

    public static MetricTelemetryBuilder create(String name, double value) {
        MetricTelemetryBuilder telemetryBuilder = new MetricTelemetryBuilder(new MetricsData());

        MetricPointBuilder point = new MetricPointBuilder();

        point.setName(name);
        point.setValue(value);
        telemetryBuilder.setMetricPoint(point);

        telemetryBuilder.setTime(FormattedTime.offSetDateTimeFromNow());

        return telemetryBuilder;
    }

    private MetricTelemetryBuilder(MetricsData data) {
        super(data, "Metric", "MetricData");
        this.data = data;
    }

    public void setMetricPoint(MetricPointBuilder point) {
        List<MetricDataPoint> metrics = data.getMetrics();
        if (metrics == null) {
            metrics = new ArrayList<>();
            data.setMetrics(metrics);
        }
        if (metrics.isEmpty()) {
            metrics.add(point.build());
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
