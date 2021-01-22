// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

/**
 * Describes the additional parameters for the API to list the alerts triggered.
 */
public final class ListAlertOptions {
    private Integer top;
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
     * Gets limit indicating the number of items to be included in a service returned page.
     *
     * @return The top value.
     */
    public Integer getTop() {
        return this.top;
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
     * @param top The skip value.
     * @return ListAlertOptions itself.
     */
    public ListAlertOptions setTop(int top) {
        this.top = top;
        return this;
    }

    /**
     * Sets the number of items in the queried collection that are to be skipped and not included
     * in the returned result.
     *
     * @param skip The skip value.
     * @return ListAlertOptions itself.
     */
    public ListAlertOptions setSkip(int skip) {
        this.skip = skip;
        return this;
    }
}
