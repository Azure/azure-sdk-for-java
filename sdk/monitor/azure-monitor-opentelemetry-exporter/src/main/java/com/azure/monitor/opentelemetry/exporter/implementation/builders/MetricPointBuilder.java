// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.builders;

import com.azure.monitor.opentelemetry.exporter.implementation.models.MetricDataPoint;

import static com.azure.monitor.opentelemetry.exporter.implementation.builders.TelemetryTruncation.truncateTelemetry;

public final class MetricPointBuilder {

    private static final int MAX_METRIC_NAME_SPACE_LENGTH = 256;
    private static final int MAX_NAME_LENGTH = 1024;

    private final MetricDataPoint data = new MetricDataPoint();

    public void setNamespace(String namespace) {
        data.setNamespace(truncateTelemetry(namespace, MAX_METRIC_NAME_SPACE_LENGTH, "MetricPoint.namespace"));
    }

    public void setName(String name) {
        data.setName(truncateTelemetry(name, MAX_NAME_LENGTH, "MetricPoint.name"));
    }

    public void setValue(double value) {
        data.setValue(value);
    }

    public void setCount(Integer count) {
        data.setCount(count);
    }

    public void setMin(Double min) {
        data.setMin(min);
    }

    public void setMax(Double max) {
        data.setMax(max);
    }

    public void setStdDev(Double stdDev) {
        data.setStdDev(stdDev);
    }

    MetricDataPoint build() {
        return data;
    }
}
