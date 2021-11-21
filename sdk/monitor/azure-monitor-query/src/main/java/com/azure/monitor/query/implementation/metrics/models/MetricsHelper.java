// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.implementation.metrics.models;

import com.azure.monitor.query.LogsQueryAsyncClient;
import com.azure.monitor.query.models.AggregationType;
import com.azure.monitor.query.models.LogsBatchQuery;
import com.azure.monitor.query.models.MetricAvailability;
import com.azure.monitor.query.models.MetricClass;
import com.azure.monitor.query.models.MetricDefinition;
import com.azure.monitor.query.models.MetricNamespace;
import com.azure.monitor.query.models.MetricUnit;
import com.azure.monitor.query.models.NamespaceClassification;

import java.time.Duration;
import java.util.List;

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

    private MetricsHelper() {
        // private ctor
    }

}
