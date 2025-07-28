// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.metrics.implementation;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.azure.core.http.HttpPipeline;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.monitor.query.implementation.metrics.models.MetricsHelper;
import com.azure.monitor.query.implementation.metrics.models.MetricsHelper.MetricsQueryResultResourceIdAccessor;
import com.azure.monitor.query.metrics.MetricsQueryServiceVersion;
import com.azure.monitor.query.metrics.implementation.models.Metric;
import com.azure.monitor.query.metrics.implementation.models.MetricResultsResponseValuesItem;
import com.azure.monitor.query.metrics.implementation.models.MetricUnit;
import com.azure.monitor.query.metrics.models.MetricResult;
import com.azure.monitor.query.metrics.models.MetricsQueryResult;
import com.azure.monitor.query.metrics.models.MetricsQueryTimeInterval;
import com.azure.monitor.query.models.MetricResult;
import com.azure.monitor.query.models.MetricsQueryResult;
import com.azure.monitor.query.models.QueryTimeInterval;

public final class MonitorQueryMetricsUtils {

    private static MetricsQueryResultResourceIdAccessor metricsQueryResultResourceIdAccessor;

    /**
     * Private constructor to prevent instantiation.
     */
    private MonitorQueryMetricsUtils() {
    }

    /**
     * Accessor interface
     */
    public interface MetricsQueryResultResourceIdAccessor {
        void setMetricsQueryResultResourceIdProperty(MetricsQueryResult metricsQueryResult, String resourceId);
    }

    public static void setMetricsQueryResultAccessor(final MetricsQueryResultResourceIdAccessor accessor) {
        MonitorQueryMetricsUtils.metricsQueryResultResourceIdAccessor = accessor;
    }

    public static void setMetricsQueryResultResourceIdProperty(MetricsQueryResult metricsQueryResult,
        String resourceId) {
        metricsQueryResultResourceIdAccessor.setMetricsQueryResultResourceIdProperty(metricsQueryResult, resourceId);
    }

    public static MonitorQueryMetricsClientImpl getMetricsClientImpl(HttpPipeline httpPipeline, SerializerAdapter serializerAdapter, String endpoint,
        MetricsQueryServiceVersion serviceVersion) {
        return new MonitorQueryMetricsClientImpl(httpPipeline, serializerAdapter, endpoint, serviceVersion);
    }

    public static String getSubscriptionFromResourceId(String s) {
        int i = s.indexOf("subscriptions/") + 14;
        String subscriptionId = s.substring(i, s.indexOf("/", i));
        return subscriptionId;
    }

    public static MetricsQueryResult mapToMetricsQueryResult(MetricResultsResponseValuesItem item) {
        List<MetricResult> metrics
            = item.getValue().stream().map(metric -> mapToMetrics(metric)).collect(Collectors.toList());

        MetricsQueryResult metricsQueryResult
            = new MetricsQueryResult(null, MetricsQueryTimeInterval.parse(item.getStarttime() + "/" + item.getEndtime()),
                Duration.parse(item.getInterval()), item.getNamespace(), item.getResourceregion(), metrics);
        setMetricsQueryResultResourceIdProperty(metricsQueryResult, item.getResourceid());
        return metricsQueryResult;
    }

    public static MetricResult mapToMetrics(Metric metric) {
        List<com.azure.monitor.query.metrics.models.TimeSeriesElement> timeSeries
            = metric.getTimeseries().stream().map(ts -> mapToTimeSeries(ts)).collect(Collectors.toList());
        MetricResult metricResult = new MetricResult(metric.getId(), metric.getType(),
            MetricUnit.fromString(metric.getUnit().toString()), metric.getName().getValue(), timeSeries,
            metric.getDisplayDescription(), new ResponseError(metric.getErrorCode(), metric.getErrorMessage()));
        return metricResult;
    }

    public static List<com.azure.monitor.query.models.TimeSeriesElement>
        mapTimeSeries(List<com.azure.monitor.query.implementation.metrics.models.TimeSeriesElement> timeseries) {
        return timeseries.stream()
            .map(timeSeriesElement -> new com.azure.monitor.query.models.TimeSeriesElement(
                mapMetricsData(timeSeriesElement.getData()), mapMetricsMetadata(timeSeriesElement.getMetadatavalues())))
            .collect(Collectors.toList());
    }
}