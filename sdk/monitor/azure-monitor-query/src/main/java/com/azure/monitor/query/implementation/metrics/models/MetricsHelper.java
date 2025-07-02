// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.implementation.metrics.models;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.models.ResponseError;
import com.azure.core.util.CoreUtils;
import com.azure.monitor.query.implementation.metricsbatch.models.MetricResultsResponseValuesItem;
import com.azure.monitor.query.implementation.metricsdefinitions.models.LocalizableString;
import com.azure.monitor.query.models.AggregationType;
import com.azure.monitor.query.models.MetricAvailability;
import com.azure.monitor.query.models.MetricClass;
import com.azure.monitor.query.models.MetricDefinition;
import com.azure.monitor.query.models.MetricNamespace;
import com.azure.monitor.query.models.MetricResult;
import com.azure.monitor.query.models.MetricUnit;
import com.azure.monitor.query.models.MetricsQueryResult;
import com.azure.monitor.query.models.NamespaceClassification;
import com.azure.monitor.query.models.QueryTimeInterval;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper for metrics-related utilities and conversions.
 */
public final class MetricsHelper {
    private static MetricDefinitionAccessor metricDefinitionAccessor;
    private static MetricAvailabilityAccessor metricAvailabilityAccessor;
    private static MetricNamespaceAccessor metricNamespaceAccessor;
    private static MetricsQueryResultResourceIdAccessor metricsQueryResultResourceIdAccessor;

    /**
     * Accessor interface
     */
    public interface MetricDefinitionAccessor {
        void setMetricDefinitionProperties(MetricDefinition metricDefinition, Boolean dimensionRequired,
            String resourceId, String namespace, String name, String displayDescription, String category,
            MetricClass metricClass, MetricUnit unit, AggregationType primaryAggregationType,
            List<AggregationType> supportedAggregationTypes, List<MetricAvailability> metricAvailabilities, String id,
            List<String> dimensions);
    }

    /**
     * Accessor interface
     */
    public interface MetricAvailabilityAccessor {
        void setMetricAvailabilityProperties(MetricAvailability metricAvailability, Duration retention,
            Duration granularity);
    }

    /**
     * Accessor interface
     */
    public interface MetricNamespaceAccessor {
        void setMetricNamespaceProperties(MetricNamespace metricNamespace, NamespaceClassification classification,
            String id, String name, String fullyQualifiedName, String type);
    }

    /**
     * Accessor interface
     */
    public interface MetricsQueryResultResourceIdAccessor {
        void setMetricsQueryResultResourceIdProperty(MetricsQueryResult metricsQueryResult, String resourceId);
    }

    /**
     * Sets the accessor instance.
     * @param metricDefinitionAccessor the accessor instance
     */
    public static void setMetricDefinitionAccessor(final MetricDefinitionAccessor metricDefinitionAccessor) {
        MetricsHelper.metricDefinitionAccessor = metricDefinitionAccessor;
    }

    /**
     * Sets the accessor instance.
     * @param metricAvailabilityAccessor the accessor instance
     */
    public static void setMetricAvailabilityAccessor(final MetricAvailabilityAccessor metricAvailabilityAccessor) {
        MetricsHelper.metricAvailabilityAccessor = metricAvailabilityAccessor;
    }

    /**
     * Sets the accessor instance.
     * @param metricNamespaceAccessor the accessor instance
     */
    public static void setMetricNamespaceAccessor(MetricNamespaceAccessor metricNamespaceAccessor) {
        MetricsHelper.metricNamespaceAccessor = metricNamespaceAccessor;
    }

    /**
     * Sets the accessor instance.
     * @param accessor the accessor instance
     */
    public static void setMetricsQueryResultAccessor(final MetricsQueryResultResourceIdAccessor accessor) {
        MetricsHelper.metricsQueryResultResourceIdAccessor = accessor;
    }

    /**
     * Sets the resource ID property on a MetricsQueryResult.
     * @param metricsQueryResult the metrics query result to update
     * @param resourceId the resource ID to set
     */
    public static void setMetricsQueryResultResourceIdProperty(MetricsQueryResult metricsQueryResult,
        String resourceId) {
        metricsQueryResultResourceIdAccessor.setMetricsQueryResultResourceIdProperty(metricsQueryResult, resourceId);
    }

    /**
     * Sets properties on a MetricDefinition.
     * @param metricDefinition the metric definition to update
     * @param dimensionRequired whether dimensions are required
     * @param resourceId the resource ID
     * @param namespace the namespace
     * @param name the name
     * @param displayDescription the display description
     * @param category the category
     * @param metricClass the metric class
     * @param unit the unit
     * @param primaryAggregationType the primary aggregation type
     * @param supportedAggregationTypes the supported aggregation types
     * @param metricAvailabilities the metric availabilities
     * @param id the ID
     * @param dimensions the dimensions
     */
    public static void setMetricDefinitionProperties(MetricDefinition metricDefinition, Boolean dimensionRequired,
        String resourceId, String namespace, String name, String displayDescription, String category,
        MetricClass metricClass, MetricUnit unit, AggregationType primaryAggregationType,
        List<AggregationType> supportedAggregationTypes, List<MetricAvailability> metricAvailabilities, String id,
        List<String> dimensions) {
        metricDefinitionAccessor.setMetricDefinitionProperties(metricDefinition, dimensionRequired, resourceId,
            namespace, name, displayDescription, category, metricClass, unit, primaryAggregationType,
            supportedAggregationTypes, metricAvailabilities, id, dimensions);
    }

    /**
     * Sets properties on a MetricAvailability.
     * @param metricAvailability the metric availability to update
     * @param retention the retention period
     * @param granularity the granularity
     */
    public static void setMetricAvailabilityProperties(MetricAvailability metricAvailability, Duration retention,
        Duration granularity) {
        metricAvailabilityAccessor.setMetricAvailabilityProperties(metricAvailability, retention, granularity);
    }

    /**
     * Sets properties on a MetricNamespace.
     * @param metricNamespace the metric namespace to update
     * @param classification the namespace classification
     * @param id the ID
     * @param name the name
     * @param fullyQualifiedName the fully qualified name
     * @param type the type
     */
    public static void setMetricNamespaceProperties(MetricNamespace metricNamespace,
        NamespaceClassification classification, String id, String name, String fullyQualifiedName, String type) {
        metricNamespaceAccessor.setMetricNamespaceProperties(metricNamespace, classification, id, name,
            fullyQualifiedName, type);
    }

    public static Response<MetricsQueryResult> convertToMetricsQueryResult(Response<MetricsResponse> response) {
        MetricsResponse metricsResponse = response.getValue();
        MetricsQueryResult metricsQueryResult = new MetricsQueryResult(metricsResponse.getCost(),
            metricsResponse.getTimespan() == null ? null : QueryTimeInterval.parse(metricsResponse.getTimespan()),
            metricsResponse.getInterval(), metricsResponse.getNamespace(), metricsResponse.getResourceregion(),
            mapMetrics(metricsResponse.getValue()));

        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            metricsQueryResult);
    }

    public static List<MetricResult> mapMetrics(List<Metric> value) {
        return value.stream().map(metric -> {
            MetricUnit metricUnit
                = metric.getUnit() == null ? null : MetricUnit.fromString(metric.getUnit().toString());
            return new MetricResult(metric.getId(), metric.getType(), metricUnit, metric.getName().getValue(),
                mapTimeSeries(metric.getTimeseries()), metric.getDisplayDescription(),
                new ResponseError(metric.getErrorCode(), metric.getErrorMessage()));
        }).collect(Collectors.toList());
    }

    public static List<com.azure.monitor.query.models.TimeSeriesElement>
        mapTimeSeries(List<com.azure.monitor.query.implementation.metrics.models.TimeSeriesElement> timeseries) {
        return timeseries.stream()
            .map(timeSeriesElement -> new com.azure.monitor.query.models.TimeSeriesElement(
                mapMetricsData(timeSeriesElement.getData()), mapMetricsMetadata(timeSeriesElement.getMetadatavalues())))
            .collect(Collectors.toList());
    }

    public static Map<String, String> mapMetricsMetadata(List<MetadataValue> metadataValues) {
        if (metadataValues == null) {
            return null;
        }
        return metadataValues.stream()
            .collect(Collectors.toMap(value -> value.getName().getValue(), MetadataValue::getValue));
    }

    public static List<com.azure.monitor.query.models.MetricValue>
        mapMetricsData(List<com.azure.monitor.query.implementation.metrics.models.MetricValue> data) {
        return data.stream()
            .map(metricValue -> new com.azure.monitor.query.models.MetricValue(metricValue.getTimeStamp(),
                metricValue.getAverage(), metricValue.getMinimum(), metricValue.getMaximum(), metricValue.getTotal(),
                metricValue.getCount()))
            .collect(Collectors.toList());
    }

    public static MetricNamespace mapMetricNamespace(
        com.azure.monitor.query.implementation.metricsnamespaces.models.MetricNamespace namespaceImpl) {
        MetricNamespace metricNamespace = new MetricNamespace();
        NamespaceClassification classification = namespaceImpl.getClassification() == null
            ? null
            : NamespaceClassification.fromString(namespaceImpl.getClassification().toString());
        MetricsHelper.setMetricNamespaceProperties(metricNamespace, classification, namespaceImpl.getId(),
            namespaceImpl.getName(),
            namespaceImpl.getProperties() == null ? null : namespaceImpl.getProperties().getMetricNamespaceName(),
            namespaceImpl.getType());

        return metricNamespace;
    }

    public static MetricDefinition mapToMetricDefinition(
        com.azure.monitor.query.implementation.metricsdefinitions.models.MetricDefinition definition) {
        MetricDefinition metricDefinition = new MetricDefinition();
        List<String> dimensions = null;
        if (!CoreUtils.isNullOrEmpty(definition.getDimensions())) {
            dimensions
                = definition.getDimensions().stream().map(LocalizableString::getValue).collect(Collectors.toList());
        }
        MetricClass metricClass = definition.getMetricClass() == null
            ? null
            : MetricClass.fromString(definition.getMetricClass().toString());
        MetricUnit metricUnit
            = definition.getUnit() == null ? null : MetricUnit.fromString(definition.getUnit().toString());
        AggregationType primaryAggregationType = definition.getPrimaryAggregationType() == null
            ? null
            : AggregationType.fromString(definition.getPrimaryAggregationType().toString());
        List<AggregationType> supportedAggregationTypes = null;
        if (!CoreUtils.isNullOrEmpty(definition.getSupportedAggregationTypes())) {
            supportedAggregationTypes = definition.getSupportedAggregationTypes()
                .stream()
                .map(aggregationType -> AggregationType.fromString(aggregationType.toString()))
                .collect(Collectors.toList());
        }
        MetricsHelper.setMetricDefinitionProperties(metricDefinition, definition.isDimensionRequired(),
            definition.getResourceId(), definition.getNamespace(), definition.getName().getValue(),
            definition.getDisplayDescription(), definition.getCategory(), metricClass, metricUnit,
            primaryAggregationType, supportedAggregationTypes,
            mapMetricAvailabilities(definition.getMetricAvailabilities()), definition.getId(), dimensions);
        return metricDefinition;
    }

    private static List<MetricAvailability> mapMetricAvailabilities(
        List<com.azure.monitor.query.implementation.metricsdefinitions.models.MetricAvailability> metricAvailabilities) {
        return metricAvailabilities.stream().map(availabilityImpl -> {
            MetricAvailability metricAvailability = new MetricAvailability();
            MetricsHelper.setMetricAvailabilityProperties(metricAvailability, availabilityImpl.getRetention(),
                availabilityImpl.getTimeGrain());
            return metricAvailability;
        }).collect(Collectors.toList());
    }

    private MetricsHelper() {
        // private ctor
    }

    public static MetricsQueryResult mapToMetricsQueryResult(MetricResultsResponseValuesItem item) {
        List<MetricResult> metrics
            = item.getValue().stream().map(metric -> mapToMetrics(metric)).collect(Collectors.toList());

        MetricsQueryResult metricsQueryResult
            = new MetricsQueryResult(null, QueryTimeInterval.parse(item.getStarttime() + "/" + item.getEndtime()),
                Duration.parse(item.getInterval()), item.getNamespace(), item.getResourceregion(), metrics);
        setMetricsQueryResultResourceIdProperty(metricsQueryResult, item.getResourceid());
        return metricsQueryResult;
    }

    public static MetricResult mapToMetrics(com.azure.monitor.query.implementation.metricsbatch.models.Metric metric) {
        List<com.azure.monitor.query.models.TimeSeriesElement> timeSeries
            = metric.getTimeseries().stream().map(ts -> mapToTimeSeries(ts)).collect(Collectors.toList());
        MetricResult metricResult = new MetricResult(metric.getId(), metric.getType(),
            MetricUnit.fromString(metric.getUnit().toString()), metric.getName().getValue(), timeSeries,
            metric.getDisplayDescription(), new ResponseError(metric.getErrorCode(), metric.getErrorMessage()));
        return metricResult;
    }

    public static com.azure.monitor.query.models.TimeSeriesElement
        mapToTimeSeries(com.azure.monitor.query.implementation.metricsbatch.models.TimeSeriesElement ts) {
        List<com.azure.monitor.query.models.MetricValue> values
            = ts.getData().stream().map(mv -> mapToMetricValue(mv)).collect(Collectors.toList());
        Map<String, String> metadata = ts.getMetadatavalues()
            .stream()
            .collect(Collectors.toMap(md -> md.getName().getValue(), md -> md.getValue()));
        com.azure.monitor.query.models.TimeSeriesElement timeSeriesElement
            = new com.azure.monitor.query.models.TimeSeriesElement(values, metadata);
        return timeSeriesElement;
    }

    public static com.azure.monitor.query.models.MetricValue
        mapToMetricValue(com.azure.monitor.query.implementation.metricsbatch.models.MetricValue mv) {
        com.azure.monitor.query.models.MetricValue metricValue = new com.azure.monitor.query.models.MetricValue(
            mv.getTimeStamp(), mv.getAverage(), mv.getMinimum(), mv.getMaximum(), mv.getTotal(), mv.getCount());
        return metricValue;
    }

    public static String getSubscriptionFromResourceId(String s) {
        int i = s.indexOf("subscriptions/") + 14;
        String subscriptionId = s.substring(i, s.indexOf("/", i));
        return subscriptionId;
    }

    /**
     * Converts a {@link QueryTimeInterval} to ISO 8601 string format suitable for Azure Monitor Metrics API.
     * For duration-only intervals, this method converts them to absolute start/end times based on current time.
     *
     * @param timeInterval The time interval to convert.
     * @return ISO 8601 formatted string representation with absolute start/end times.
     */
    public static String toMetricsTimespan(QueryTimeInterval timeInterval) {
        if (timeInterval == null) {
            return null;
        }

        // If we have both start and end times, use them directly
        if (timeInterval.getStartTime() != null && timeInterval.getEndTime() != null) {
            return formatForMetrics(timeInterval.getStartTime()) + "/" + formatForMetrics(timeInterval.getEndTime());
        }

        // If we have start time and duration, calculate end time
        if (timeInterval.getStartTime() != null && timeInterval.getDuration() != null) {
            return formatForMetrics(timeInterval.getStartTime()) + "/"
                + formatForMetrics(timeInterval.getStartTime().plus(timeInterval.getDuration()));
        }

        // If we have duration and end time, calculate start time
        if (timeInterval.getDuration() != null && timeInterval.getEndTime() != null) {
            return formatForMetrics(timeInterval.getEndTime().minus(timeInterval.getDuration())) + "/"
                + formatForMetrics(timeInterval.getEndTime());
        }

        // If we only have duration, calculate absolute start and end times based on current time
        if (timeInterval.getDuration() != null) {
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            OffsetDateTime startTime = now.minus(timeInterval.getDuration());
            return formatForMetrics(startTime) + "/" + formatForMetrics(now);
        }

        return null;
    }

    /**
     * Formats an OffsetDateTime for Azure Monitor Metrics API.
     * Uses ISO format but omits seconds when they are zero.
     *
     * @param dateTime The datetime to format.
     * @return Formatted string.
     */
    private static String formatForMetrics(OffsetDateTime dateTime) {
        if (dateTime.getSecond() == 0 && dateTime.getNano() == 0) {
            // Format without seconds when they are zero: 2025-01-01T00:00Z
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmXXX"));
        } else {
            // Format with full precision when seconds are non-zero
            return dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
    }

}
