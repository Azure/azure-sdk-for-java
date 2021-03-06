/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.storagecache.v2020_10_01;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Details about operation related to metrics.
 */
public class MetricSpecification {
    /**
     * The name of the metric.
     */
    @JsonProperty(value = "name")
    private String name;

    /**
     * Localized display name of the metric.
     */
    @JsonProperty(value = "displayName")
    private String displayName;

    /**
     * The description of the metric.
     */
    @JsonProperty(value = "displayDescription")
    private String displayDescription;

    /**
     * The unit that the metric is measured in.
     */
    @JsonProperty(value = "unit")
    private String unit;

    /**
     * The type of metric aggregation.
     */
    @JsonProperty(value = "aggregationType")
    private String aggregationType;

    /**
     * Support metric aggregation type.
     */
    @JsonProperty(value = "supportedAggregationTypes")
    private List<MetricAggregationType> supportedAggregationTypes;

    /**
     * Type of metrics.
     */
    @JsonProperty(value = "metricClass")
    private String metricClass;

    /**
     * Dimensions of the metric.
     */
    @JsonProperty(value = "dimensions")
    private List<MetricDimension> dimensions;

    /**
     * Get the name of the metric.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name of the metric.
     *
     * @param name the name value to set
     * @return the MetricSpecification object itself.
     */
    public MetricSpecification withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get localized display name of the metric.
     *
     * @return the displayName value
     */
    public String displayName() {
        return this.displayName;
    }

    /**
     * Set localized display name of the metric.
     *
     * @param displayName the displayName value to set
     * @return the MetricSpecification object itself.
     */
    public MetricSpecification withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Get the description of the metric.
     *
     * @return the displayDescription value
     */
    public String displayDescription() {
        return this.displayDescription;
    }

    /**
     * Set the description of the metric.
     *
     * @param displayDescription the displayDescription value to set
     * @return the MetricSpecification object itself.
     */
    public MetricSpecification withDisplayDescription(String displayDescription) {
        this.displayDescription = displayDescription;
        return this;
    }

    /**
     * Get the unit that the metric is measured in.
     *
     * @return the unit value
     */
    public String unit() {
        return this.unit;
    }

    /**
     * Set the unit that the metric is measured in.
     *
     * @param unit the unit value to set
     * @return the MetricSpecification object itself.
     */
    public MetricSpecification withUnit(String unit) {
        this.unit = unit;
        return this;
    }

    /**
     * Get the type of metric aggregation.
     *
     * @return the aggregationType value
     */
    public String aggregationType() {
        return this.aggregationType;
    }

    /**
     * Set the type of metric aggregation.
     *
     * @param aggregationType the aggregationType value to set
     * @return the MetricSpecification object itself.
     */
    public MetricSpecification withAggregationType(String aggregationType) {
        this.aggregationType = aggregationType;
        return this;
    }

    /**
     * Get support metric aggregation type.
     *
     * @return the supportedAggregationTypes value
     */
    public List<MetricAggregationType> supportedAggregationTypes() {
        return this.supportedAggregationTypes;
    }

    /**
     * Set support metric aggregation type.
     *
     * @param supportedAggregationTypes the supportedAggregationTypes value to set
     * @return the MetricSpecification object itself.
     */
    public MetricSpecification withSupportedAggregationTypes(List<MetricAggregationType> supportedAggregationTypes) {
        this.supportedAggregationTypes = supportedAggregationTypes;
        return this;
    }

    /**
     * Get type of metrics.
     *
     * @return the metricClass value
     */
    public String metricClass() {
        return this.metricClass;
    }

    /**
     * Set type of metrics.
     *
     * @param metricClass the metricClass value to set
     * @return the MetricSpecification object itself.
     */
    public MetricSpecification withMetricClass(String metricClass) {
        this.metricClass = metricClass;
        return this;
    }

    /**
     * Get dimensions of the metric.
     *
     * @return the dimensions value
     */
    public List<MetricDimension> dimensions() {
        return this.dimensions;
    }

    /**
     * Set dimensions of the metric.
     *
     * @param dimensions the dimensions value to set
     * @return the MetricSpecification object itself.
     */
    public MetricSpecification withDimensions(List<MetricDimension> dimensions) {
        this.dimensions = dimensions;
        return this;
    }

}
