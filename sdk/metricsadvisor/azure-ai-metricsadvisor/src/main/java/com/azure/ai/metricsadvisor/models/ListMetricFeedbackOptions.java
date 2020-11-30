// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.core.annotation.Fluent;

/**
 * Additional properties for filtering results on the lisMetricFeedbacks operation.
 */
@Fluent
public final class ListMetricFeedbackOptions {
    private ListMetricFeedbackFilter listDataFeedFilter;
    private Integer top;
    private Integer skip;

    /**
     * Get the additional metric feedback filter options that can be passed for filtering the result of the
     * metric feedbacks returned by the service
     *
     * @return The metric feedback filter options used for filtering the result of the metric feedbacks
     * returned by the service.
     */
    public ListMetricFeedbackFilter getFilter() {
        return listDataFeedFilter;
    }

    /**
     * Set the additional metric feedback filter options that can be passed for filtering the result of
     * lisMetricFeedbacks operation.
     *
     * @param listDataFeedFilter the additional metric feedback filter options that can be passed for
     * filtering the metric feedbacks returned by the service.
     *
     * @return the updated {@code ListMetricFeedbackOptions} value.
     */
    public ListMetricFeedbackOptions setFilter(final ListMetricFeedbackFilter listDataFeedFilter) {
        this.listDataFeedFilter = listDataFeedFilter;
        return this;
    }

    /**
     * Gets limit indicating the number of items that will be included in a service returned page.
     *
     * @return The top value.
     */
    public Integer getTop() {
        return this.top;
    }

    /**
     * Gets the number of items in the queried collection that will be skipped and not included
     * in the returned result.
     *
     * @return The skip value.
     */
    public Integer getSkip() {
        return this.skip;
    }

    /**
     * Sets limit indicating the number of items to be included in a service returned page.
     *
     * @param top The top value.
     *
     * @return The ListDataFeedOptions object itself.
     */
    public ListMetricFeedbackOptions setTop(int top) {
        this.top = top;
        return this;
    }

    /**
     * Sets the number of items in the queried collection that are to be skipped and not included
     * in the returned result.
     *
     * @param skip The skip value.
     * @return ListMetricFeedbackOptions itself.
     */
    public ListMetricFeedbackOptions setSkip(int skip) {
        this.skip = skip;
        return this;
    }
}
