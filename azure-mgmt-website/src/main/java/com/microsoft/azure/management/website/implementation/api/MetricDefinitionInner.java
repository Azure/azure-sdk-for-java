/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * Class repesenting metadata for the metrics.
 */
@JsonFlatten
public class MetricDefinitionInner extends Resource {
    /**
     * Name of the metric.
     */
    @JsonProperty(value = "properties.name")
    private String metricDefinitionName;

    /**
     * Unit of the metric.
     */
    @JsonProperty(value = "properties.unit")
    private String unit;

    /**
     * Primary aggregation type.
     */
    @JsonProperty(value = "properties.primaryAggregationType")
    private String primaryAggregationType;

    /**
     * List of time grains supported for the metric together with retention
     * period.
     */
    @JsonProperty(value = "properties.metricAvailabilities")
    private List<MetricAvailabilily> metricAvailabilities;

    /**
     * Friendly name shown in the UI.
     */
    @JsonProperty(value = "properties.displayName")
    private String displayName;

    /**
     * Get the metricDefinitionName value.
     *
     * @return the metricDefinitionName value
     */
    public String metricDefinitionName() {
        return this.metricDefinitionName;
    }

    /**
     * Set the metricDefinitionName value.
     *
     * @param metricDefinitionName the metricDefinitionName value to set
     * @return the MetricDefinitionInner object itself.
     */
    public MetricDefinitionInner withMetricDefinitionName(String metricDefinitionName) {
        this.metricDefinitionName = metricDefinitionName;
        return this;
    }

    /**
     * Get the unit value.
     *
     * @return the unit value
     */
    public String unit() {
        return this.unit;
    }

    /**
     * Set the unit value.
     *
     * @param unit the unit value to set
     * @return the MetricDefinitionInner object itself.
     */
    public MetricDefinitionInner withUnit(String unit) {
        this.unit = unit;
        return this;
    }

    /**
     * Get the primaryAggregationType value.
     *
     * @return the primaryAggregationType value
     */
    public String primaryAggregationType() {
        return this.primaryAggregationType;
    }

    /**
     * Set the primaryAggregationType value.
     *
     * @param primaryAggregationType the primaryAggregationType value to set
     * @return the MetricDefinitionInner object itself.
     */
    public MetricDefinitionInner withPrimaryAggregationType(String primaryAggregationType) {
        this.primaryAggregationType = primaryAggregationType;
        return this;
    }

    /**
     * Get the metricAvailabilities value.
     *
     * @return the metricAvailabilities value
     */
    public List<MetricAvailabilily> metricAvailabilities() {
        return this.metricAvailabilities;
    }

    /**
     * Set the metricAvailabilities value.
     *
     * @param metricAvailabilities the metricAvailabilities value to set
     * @return the MetricDefinitionInner object itself.
     */
    public MetricDefinitionInner withMetricAvailabilities(List<MetricAvailabilily> metricAvailabilities) {
        this.metricAvailabilities = metricAvailabilities;
        return this;
    }

    /**
     * Get the displayName value.
     *
     * @return the displayName value
     */
    public String displayName() {
        return this.displayName;
    }

    /**
     * Set the displayName value.
     *
     * @param displayName the displayName value to set
     * @return the MetricDefinitionInner object itself.
     */
    public MetricDefinitionInner withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

}
