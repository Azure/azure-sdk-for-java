/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Value of resource metric.
 */
public class ResourceMetricValue {
    /**
     * Value timestamp.
     */
    private String timeStamp;

    /**
     * Value average.
     */
    private Double average;

    /**
     * Value minimum.
     */
    private Double minimum;

    /**
     * Value maximum.
     */
    private Double maximum;

    /**
     * Value total.
     */
    private Double total;

    /**
     * Value count.
     */
    private Double count;

    /**
     * Get the timeStamp value.
     *
     * @return the timeStamp value
     */
    public String timeStamp() {
        return this.timeStamp;
    }

    /**
     * Set the timeStamp value.
     *
     * @param timeStamp the timeStamp value to set
     * @return the ResourceMetricValue object itself.
     */
    public ResourceMetricValue withTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

    /**
     * Get the average value.
     *
     * @return the average value
     */
    public Double average() {
        return this.average;
    }

    /**
     * Set the average value.
     *
     * @param average the average value to set
     * @return the ResourceMetricValue object itself.
     */
    public ResourceMetricValue withAverage(Double average) {
        this.average = average;
        return this;
    }

    /**
     * Get the minimum value.
     *
     * @return the minimum value
     */
    public Double minimum() {
        return this.minimum;
    }

    /**
     * Set the minimum value.
     *
     * @param minimum the minimum value to set
     * @return the ResourceMetricValue object itself.
     */
    public ResourceMetricValue withMinimum(Double minimum) {
        this.minimum = minimum;
        return this;
    }

    /**
     * Get the maximum value.
     *
     * @return the maximum value
     */
    public Double maximum() {
        return this.maximum;
    }

    /**
     * Set the maximum value.
     *
     * @param maximum the maximum value to set
     * @return the ResourceMetricValue object itself.
     */
    public ResourceMetricValue withMaximum(Double maximum) {
        this.maximum = maximum;
        return this;
    }

    /**
     * Get the total value.
     *
     * @return the total value
     */
    public Double total() {
        return this.total;
    }

    /**
     * Set the total value.
     *
     * @param total the total value to set
     * @return the ResourceMetricValue object itself.
     */
    public ResourceMetricValue withTotal(Double total) {
        this.total = total;
        return this;
    }

    /**
     * Get the count value.
     *
     * @return the count value
     */
    public Double count() {
        return this.count;
    }

    /**
     * Set the count value.
     *
     * @param count the count value to set
     * @return the ResourceMetricValue object itself.
     */
    public ResourceMetricValue withCount(Double count) {
        this.count = count;
        return this;
    }

}
