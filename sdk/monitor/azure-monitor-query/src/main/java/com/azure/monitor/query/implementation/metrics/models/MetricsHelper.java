// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.implementation.metrics.models;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.models.ResponseError;
import com.azure.core.util.CoreUtils;
import com.azure.monitor.query.LogsQueryAsyncClient;
import com.azure.monitor.query.implementation.metricsdefinitions.models.LocalizableString;
import com.azure.monitor.query.models.AggregationType;
import com.azure.monitor.query.models.LogsBatchQuery;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper to access package-private method of {@link LogsBatchQuery} from {@link LogsQueryAsyncClient}.
 */
public final class MetricsHelper {
    private static MetricDefinitionAccessor metricDefinitionAccessor;
    private static MetricAvailabilityAccessor metricAvailabilityAccessor;
    private static MetricNamespaceAccessor metricNamespaceAccessor;

    /**
     * Accessor interface
     */
    public interface MetricDefinitionAccessor {
        void setMetricDefinitionProperties(MetricDefinition metricDefinition, Boolean dimensionRequired,
                                           String resourceId, String namespace, String name,
                                           String displayDescription, String category,
                                           MetricClass metricClass,
                                           MetricUnit unit, AggregationType primaryAggregationType,
                                           List<AggregationType> supportedAggregationTypes,
                                           List<MetricAvailability> metricAvailabilities, String id,
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
        void setMetricNamespaceProperties(MetricNamespace metricNamespace, NamespaceClassification classification, String id, String name,
                                          String fullyQualifiedName, String type);
    }

    /**
     * Sets the accessor instance.
     * @param metricDefinitionAccessor the accessor instance
     */
    public static void setMetricDefinitionAccessor(final MetricDefinitionAccessor metricDefinitionAccessor) {
        MetricsHelper.metricDefinitionAccessor = metricDefinitionAccessor;
    }

    public static void setMetricAvailabilityAccessor(final MetricAvailabilityAccessor metricAvailabilityAccessor) {
        MetricsHelper.metricAvailabilityAccessor = metricAvailabilityAccessor;
    }

    public static void setMetricNamespaceAccessor(MetricNamespaceAccessor metricNamespaceAccessor) {
        MetricsHelper.metricNamespaceAccessor = metricNamespaceAccessor;
    }

    public static void setMetricDefinitionProperties(MetricDefinition metricDefinition, Boolean dimensionRequired,
                                                     String resourceId, String namespace, String name,
                                                     String displayDescription, String category,
                                                     MetricClass metricClass,
                                                     MetricUnit unit, AggregationType primaryAggregationType,
                                                     List<AggregationType> supportedAggregationTypes,
                                                     List<MetricAvailability> metricAvailabilities, String id,
                                                     List<String> dimensions) {
        metricDefinitionAccessor.setMetricDefinitionProperties(metricDefinition, dimensionRequired, resourceId, namespace,
                name, displayDescription, category, metricClass, unit, primaryAggregationType,
                supportedAggregationTypes, metricAvailabilities, id, dimensions);
    }

    public static void setMetricAvailabilityProperties(MetricAvailability metricAvailability, Duration retention,
                                                       Duration granularity) {
        metricAvailabilityAccessor.setMetricAvailabilityProperties(metricAvailability, retention, granularity);

    }

    public static void setMetricNamespaceProperties(MetricNamespace metricNamespace, NamespaceClassification classification, String id, String name,
                                                    String fullyQualifiedName, String type) {
        metricNamespaceAccessor.setMetricNamespaceProperties(metricNamespace, classification, id, name,
                fullyQualifiedName, type);

    }

    public static Response<MetricsQueryResult> convertToMetricsQueryResult(Response<MetricsResponse> response) {
        MetricsResponse metricsResponse = response.getValue();
        MetricsQueryResult metricsQueryResult = new MetricsQueryResult(
            metricsResponse.getCost(),
            metricsResponse.getTimespan() == null ? null : QueryTimeInterval.parse(metricsResponse.getTimespan()),
            metricsResponse.getInterval(),
            metricsResponse.getNamespace(), metricsResponse.getResourceregion(), mapMetrics(metricsResponse.getValue()));

        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), metricsQueryResult);
    }

    public static List<MetricResult> mapMetrics(List<Metric> value) {
        return value.stream()
            .map(metric -> {
                MetricUnit metricUnit = metric.getUnit() == null ? null : MetricUnit.fromString(metric.getUnit().toString());
                return new MetricResult(metric.getId(), metric.getType(),
                    metricUnit,
                    metric.getName().getValue(),
                    mapTimeSeries(metric.getTimeseries()), metric.getDisplayDescription(),
                    new ResponseError(metric.getErrorCode(), metric.getErrorMessage()));
            })
            .collect(Collectors.toList());
    }

    public static List<com.azure.monitor.query.models.TimeSeriesElement> mapTimeSeries(List<com.azure.monitor.query.implementation.metrics.models.TimeSeriesElement> timeseries) {
        return timeseries.stream()
            .map(timeSeriesElement -> new com.azure.monitor.query.models.TimeSeriesElement(mapMetricsData(timeSeriesElement.getData()),
                mapMetricsMetadata(timeSeriesElement.getMetadatavalues())))
            .collect(Collectors.toList());
    }

    public static Map<String, String> mapMetricsMetadata(List<MetadataValue> metadataValues) {
        if (metadataValues == null) {
            return null;
        }
        return metadataValues.stream()
            .collect(Collectors.toMap(value -> value.getName().getValue(), MetadataValue::getValue));
    }

    public static List<com.azure.monitor.query.models.MetricValue> mapMetricsData(List<com.azure.monitor.query.implementation.metrics.models.MetricValue> data) {
        return data.stream()
            .map(metricValue -> new com.azure.monitor.query.models.MetricValue(metricValue.getTimeStamp(),
                metricValue.getAverage(), metricValue.getMinimum(), metricValue.getMaximum(), metricValue.getTotal(),
                metricValue.getCount()))
            .collect(Collectors.toList());
    }

    public static MetricNamespace mapMetricNamespace(com.azure.monitor.query.implementation.metricsnamespaces.models.MetricNamespace namespaceImpl) {
        MetricNamespace metricNamespace = new MetricNamespace();
        NamespaceClassification classification = namespaceImpl.getClassification() == null
            ? null
            : NamespaceClassification.fromString(namespaceImpl.getClassification().toString());
        MetricsHelper.setMetricNamespaceProperties(metricNamespace,
            classification, namespaceImpl.getId(), namespaceImpl.getName(),
            namespaceImpl.getProperties() == null ? null : namespaceImpl.getProperties().getMetricNamespaceName(),
            namespaceImpl.getType());

        return metricNamespace;
    }

    public static MetricDefinition mapToMetricDefinition(com.azure.monitor.query.implementation.metricsdefinitions.models.MetricDefinition definition) {
        MetricDefinition metricDefinition = new MetricDefinition();
        List<String> dimensions = null;
        if (!CoreUtils.isNullOrEmpty(definition.getDimensions())) {
            dimensions = definition.getDimensions().stream().map(LocalizableString::getValue)
                .collect(Collectors.toList());
        }
        MetricClass metricClass = definition.getMetricClass() == null ? null : MetricClass.fromString(definition.getMetricClass().toString());
        MetricUnit metricUnit = definition.getUnit() == null ? null : MetricUnit.fromString(definition.getUnit().toString());
        AggregationType primaryAggregationType = definition.getPrimaryAggregationType() == null
            ? null
            : AggregationType.fromString(definition.getPrimaryAggregationType().toString());
        List<AggregationType> supportedAggregationTypes = null;
        if (CoreUtils.isNullOrEmpty(definition.getSupportedAggregationTypes())) {
            supportedAggregationTypes = definition.getSupportedAggregationTypes()
                .stream()
                .map(aggregationType -> AggregationType.fromString(aggregationType.toString()))
                .collect(Collectors.toList());
        }
        MetricsHelper.setMetricDefinitionProperties(metricDefinition,
            definition.isDimensionRequired(), definition.getResourceId(), definition.getNamespace(),
            definition.getName().getValue(), definition.getDisplayDescription(), definition.getCategory(),
            metricClass, metricUnit, primaryAggregationType, supportedAggregationTypes,
            mapMetricAvailabilities(definition.getMetricAvailabilities()), definition.getId(),
            dimensions);
        return metricDefinition;
    }

    private static List<MetricAvailability> mapMetricAvailabilities(List<com.azure.monitor.query.implementation.metricsdefinitions.models.MetricAvailability> metricAvailabilities) {
        return metricAvailabilities.stream()
            .map(availabilityImpl -> {
                MetricAvailability metricAvailability = new MetricAvailability();
                MetricsHelper.setMetricAvailabilityProperties(metricAvailability, availabilityImpl.getRetention(),
                    availabilityImpl.getTimeGrain());
                return metricAvailability;
            }).collect(Collectors.toList());
    }

    private MetricsHelper() {
        // private ctor
    }

}
