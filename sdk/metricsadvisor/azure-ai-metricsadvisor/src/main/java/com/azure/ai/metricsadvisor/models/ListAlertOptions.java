// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.core.annotation.Fluent;

/**
 * Describes the additional parameters for the API to list the alerts triggered.
 */
@Fluent
public final class ListAlertOptions {
    private Integer maxPageSize;
    private Integer skip;
    private AlertQueryTimeMode alertQueryTimeMode;

    /**
     * Set the query time mode.
     *
     * @param alertQueryTimeMode the alert Query Time Mode value to set.
     * @return the ListAlertOptions object itself.
     */
    public ListAlertOptions setAlertQueryTimeMode(AlertQueryTimeMode alertQueryTimeMode) {
        this.alertQueryTimeMode = alertQueryTimeMode;
        return this;
    }

    /**
     * Gets the time mode.
     *
     * @return The time mode.
     */
    public AlertQueryTimeMode getTimeMode() {
        return this.alertQueryTimeMode;
    }

    /**
     * Gets limit indicating the number of items that will be included in a service returned page.
     *
     * @return The max page size value.
     */
    public Integer getMaxPageSize() {
        return this.maxPageSize;
    }

    /**
     * Gets the number of items in the queried collection that are to be skipped and not included
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
     * @param maxPageSize The max page size value.
     * @return ListAlertOptions itself.
     */
    public ListAlertOptions setMaxPageSize(Integer maxPageSize) {
        this.maxPageSize = maxPageSize;
        return this;
    }

    /**
     * Sets the number of items in the queried collection that are to be skipped and not included
     * in the returned result.
     *
     * @param skip The skip value.
     * @return ListAlertOptions itself.
     */
    public ListAlertOptions setSkip(Integer skip) {
        this.skip = skip;
        return this;
    }
}
