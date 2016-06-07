/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Class repesenting metrics availability and retention.
 */
public class MetricAvailabilily {
    /**
     * Time grain.
     */
    private String timeGrain;

    /**
     * Retention period for the current
     * {Microsoft.Web.Hosting.Administration.MetricAvailabilily.TimeGrain}.
     */
    private String retention;

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
     * @return the MetricAvailabilily object itself.
     */
    public MetricAvailabilily withTimeGrain(String timeGrain) {
        this.timeGrain = timeGrain;
        return this;
    }

    /**
     * Get the retention value.
     *
     * @return the retention value
     */
    public String retention() {
        return this.retention;
    }

    /**
     * Set the retention value.
     *
     * @param retention the retention value to set
     * @return the MetricAvailabilily object itself.
     */
    public MetricAvailabilily withRetention(String retention) {
        this.retention = retention;
        return this;
    }

}
