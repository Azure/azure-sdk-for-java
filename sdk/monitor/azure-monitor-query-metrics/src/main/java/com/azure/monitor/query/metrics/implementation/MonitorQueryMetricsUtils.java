// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.metrics.implementation;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.models.ResponseError;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.monitor.query.metrics.MetricsServiceVersion;
import com.azure.monitor.query.metrics.implementation.models.MetadataValue;
import com.azure.monitor.query.metrics.implementation.models.Metric;
import com.azure.monitor.query.metrics.implementation.models.MetricResultsResponseValuesItem;
import com.azure.monitor.query.metrics.models.MetricResult;
import com.azure.monitor.query.metrics.models.MetricUnit;
import com.azure.monitor.query.metrics.models.MetricsQueryResourcesOptions;
import com.azure.monitor.query.metrics.models.MetricsQueryResult;
import com.azure.monitor.query.metrics.models.MetricsQueryTimeInterval;

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

    public static MonitorQueryMetricsClientImpl getMetricsClientImpl(HttpPipeline httpPipeline,
        SerializerAdapter serializerAdapter, String endpoint, MetricsServiceVersion serviceVersion) {
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

        MetricsQueryResult metricsQueryResult = new MetricsQueryResult(null,
            MetricsQueryTimeInterval.parse(item.getStarttime() + "/" + item.getEndtime()),
            Duration.parse(item.getInterval()), item.getNamespace(), item.getResourceregion(), metrics);
        setMetricsQueryResultResourceIdProperty(metricsQueryResult, item.getResourceid());
        return metricsQueryResult;
    }

    public static MetricResult mapToMetrics(Metric metric) {
        List<com.azure.monitor.query.metrics.models.TimeSeriesElement> timeSeries
            = mapTimeSeries(metric.getTimeSeries());
        MetricResult metricResult = new MetricResult(metric.getId(), metric.getType(),
            MetricUnit.fromString(metric.getUnit().toString()), metric.getName().getValue(), timeSeries,
            metric.getDisplayDescription(), new ResponseError(metric.getErrorCode(), metric.getErrorMessage()));
        return metricResult;
    }

    public static List<com.azure.monitor.query.metrics.models.TimeSeriesElement>
        mapTimeSeries(List<com.azure.monitor.query.metrics.implementation.models.TimeSeriesElement> timeseries) {
        return timeseries.stream()
            .map(timeSeriesElement -> new com.azure.monitor.query.metrics.models.TimeSeriesElement(
                timeSeriesElement.getData(), mapMetricsMetadata(timeSeriesElement.getMetadatavalues())))
            .collect(Collectors.toList());
    }

    public static Map<String, String> mapMetricsMetadata(List<MetadataValue> metadataValues) {
        if (metadataValues == null) {
            return null;
        }
        return metadataValues.stream()
            .collect(Collectors.toMap(value -> value.getName().getValue(), MetadataValue::getValue));
    }

    public static RequestOptions mapToRequestOptions(MetricsQueryResourcesOptions options) {
        RequestOptions requestOptions = new RequestOptions();
        if (options != null) {
            if (options.getFilter() != null) {
                requestOptions.addQueryParam("filter", options.getFilter(), false);
            }

            if (options.getGranularity() != null) {
                requestOptions.addQueryParam("interval", options.getGranularity().toString(), false);
            }

            if (options.getAggregations() != null && !options.getAggregations().isEmpty()) {
                String aggregations = options.getAggregations()
                    .stream()
                    .map(aggregationType -> aggregationType.toString())
                    .collect(Collectors.joining(","));
                requestOptions.addQueryParam("aggregation", aggregations, false);
            }

            if (options.getTop() != null) {
                requestOptions.addQueryParam("top", options.getTop().toString(), false);
            }

            if (options.getOrderBy() != null) {
                requestOptions.addQueryParam("orderby", options.getOrderBy(), false);
            }

            if (options.getRollupBy() != null) {
                requestOptions.addQueryParam("rollupBy", options.getRollupBy(), false);
            }

            if (options.getTimeInterval() != null) {
                if (options.getTimeInterval().getStartTime() != null) {
                    requestOptions.addQueryParam("starttime", options.getTimeInterval().getStartTime().toString(),
                        false);
                }
                if (options.getTimeInterval().getEndTime() != null) {
                    requestOptions.addQueryParam("endtime", options.getTimeInterval().getEndTime().toString(), false);
                }
            }
        }
        return requestOptions;
    }
}
