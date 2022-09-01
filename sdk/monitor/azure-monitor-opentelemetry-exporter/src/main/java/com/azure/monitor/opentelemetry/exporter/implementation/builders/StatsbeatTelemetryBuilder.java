/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

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
