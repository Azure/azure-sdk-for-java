/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import org.joda.time.DateTime;
import java.util.List;

/**
 * Object representing a metric for any resource.
 */
public class ResourceMetric {
    /**
     * Name of metric.
     */
    private ResourceMetricName name;

    /**
     * Metric unit.
     */
    private String unit;

    /**
     * Metric granularity. E.g PT1H, PT5M, P1D.
     */
    private String timeGrain;

    /**
     * Metric start time.
     */
    private DateTime startTime;

    /**
     * Metric end time.
     */
    private DateTime endTime;

    /**
     * Metric resource Id.
     */
    private String resourceId;

    /**
     * Metric values.
     */
    private List<ResourceMetricValue> metricValues;

    /**
     * Properties.
     */
    private List<KeyValuePairStringString> properties;

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public ResourceMetricName name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the ResourceMetric object itself.
     */
    public ResourceMetric withName(ResourceMetricName name) {
        this.name = name;
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
     * @return the ResourceMetric object itself.
     */
    public ResourceMetric withUnit(String unit) {
        this.unit = unit;
        return this;
    }

    /**
     * Get the timeGrain value.
     *
     * @return the timeGrain value
     */
    public String timeGrain() {
        return this.timeGrain;
    }

    /**
     * Set the timeGrain value.
     *
     * @param timeGrain the timeGrain value to set
     * @return the ResourceMetric object itself.
     */
    public ResourceMetric withTimeGrain(String timeGrain) {
        this.timeGrain = timeGrain;
        return this;
    }

    /**
     * Get the startTime value.
     *
     * @return the startTime value
     */
    public DateTime startTime() {
        return this.startTime;
    }

    /**
     * Set the startTime value.
     *
     * @param startTime the startTime value to set
     * @return the ResourceMetric object itself.
     */
    public ResourceMetric withStartTime(DateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    /**
     * Get the endTime value.
     *
     * @return the endTime value
     */
    public DateTime endTime() {
        return this.endTime;
    }

    /**
     * Set the endTime value.
     *
     * @param endTime the endTime value to set
     * @return the ResourceMetric object itself.
     */
    public ResourceMetric withEndTime(DateTime endTime) {
        this.endTime = endTime;
        return this;
    }

    /**
     * Get the resourceId value.
     *
     * @return the resourceId value
     */
    public String resourceId() {
        return this.resourceId;
    }

    /**
     * Set the resourceId value.
     *
     * @param resourceId the resourceId value to set
     * @return the ResourceMetric object itself.
     */
    public ResourceMetric withResourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    /**
     * Get the metricValues value.
     *
     * @return the metricValues value
     */
    public List<ResourceMetricValue> metricValues() {
        return this.metricValues;
    }

    /**
     * Set the metricValues value.
     *
     * @param metricValues the metricValues value to set
     * @return the ResourceMetric object itself.
     */
    public ResourceMetric withMetricValues(List<ResourceMetricValue> metricValues) {
        this.metricValues = metricValues;
        return this;
    }

    /**
     * Get the properties value.
     *
     * @return the properties value
     */
    public List<KeyValuePairStringString> properties() {
        return this.properties;
    }

    /**
     * Set the properties value.
     *
     * @param properties the properties value to set
     * @return the ResourceMetric object itself.
     */
    public ResourceMetric withProperties(List<KeyValuePairStringString> properties) {
        this.properties = properties;
        return this;
    }

}
