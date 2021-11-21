// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.monitor.query.implementation.metrics.models.MetricsHelper;

import java.util.List;

/**
 * Metric definition class specifies the metadata for a metric.
 */
public final class MetricDefinition {

    private Boolean isDimensionRequired;
    private String resourceId;
    private String namespace;
    private String name;
    private String description;
    private String category;
    private MetricClass metricClass;
    private MetricUnit unit;
    private AggregationType primaryAggregationType;
    private List<AggregationType> supportedAggregationTypes;
    private List<MetricAvailability> metricAvailabilities;
    private String id;
    private List<String> dimensions;

    static {
        MetricsHelper.setMetricDefinitionAccessor(MetricDefinition::setMetricDefinitionProperties);
    }

    private void setMetricDefinitionProperties(Boolean dimensionRequired,
                                               String resourceId,
                                               String namespace,
                                               String name,
                                               String displayDescription,
                                               String category,
                                               MetricClass metricClass,
                                               MetricUnit unit,
                                               AggregationType primaryAggregationType,
                                               List<AggregationType> supportedAggregationTypes,
                                               List<MetricAvailability> metricAvailabilities,
                                               String id,
                                               List<String> dimensions) {

        this.isDimensionRequired = dimensionRequired;
        this.resourceId = resourceId;
        this.namespace = namespace;
        this.name = name;
        this.description = displayDescription;
        this.category = category;
        this.metricClass = metricClass;
        this.unit = unit;
        this.primaryAggregationType = primaryAggregationType;
        this.supportedAggregationTypes = supportedAggregationTypes;
        this.metricAvailabilities = metricAvailabilities;
        this.id = id;
        this.dimensions = dimensions;
    }

    /**
     * Returns a flag to indicate whether the dimension is required.
     * @return Flag to indicate whether the dimension is required.
     */
    public Boolean isDimensionRequired() {
        return isDimensionRequired;
    }

    /**
     * Returns the resource identifier of the resource that emitted the metric.
     * @return the resource identifier of the resource that emitted the metric.
     */
    public String getResourceId() {
        return resourceId;
    }

    /**
     * Returns the namespace the metric belongs to.
     * @return the namespace the metric belongs to.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Returns the name of the metric,
     * @return the name of the metric,
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the detailed description of this metric.
     * @return detailed description of this metric.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the custom category name for this metric.
     * @return custom category name for this metric.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Returns the class of the metric.
     * @return the class of the metric.
     */
    public MetricClass getMetricClass() {
        return metricClass;
    }

    /**
     * Returns the unit of the metric.
     * @return the unit of the metric.
     */
    public MetricUnit getUnit() {
        return unit;
    }

    /**
     * Returns the primary aggregation type value defining how to use the values for display.
     * @return the primary aggregation type value defining how to use the values for display.
     */
    public AggregationType getPrimaryAggregationType() {
        return primaryAggregationType;
    }

    /**
     * Returns the collection of what aggregation types are supported.
     * @return the collection of what aggregation types are supported.
     */
    public List<AggregationType> getSupportedAggregationTypes() {
        return supportedAggregationTypes;
    }

    /**
     * Returns the collection of what aggregation intervals are available to be queried.
     * @return the collection of what aggregation intervals are available to be queried.
     */
    public List<MetricAvailability> getMetricAvailabilities() {
        return metricAvailabilities;
    }

    /**
     * The resource identifier of the metric definition.
     * @return the resource identifier of the metric definition.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the names of all the dimensions available for this metric.
     * @return the names of all the dimensions available for this metric.
     */
    public List<String> getDimensions() {
        return dimensions;
    }
}
