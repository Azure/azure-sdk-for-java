/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * RequestsBasedTrigger.
 */
public class RequestsBasedTrigger {
    /**
     * Count.
     */
    private Integer count;

    /**
     * TimeInterval.
     */
    private String timeInterval;

    /**
     * Get the count value.
     *
     * @return the count value
     */
    public Integer count() {
        return this.count;
    }

    /**
     * Set the count value.
     *
     * @param count the count value to set
     * @return the RequestsBasedTrigger object itself.
     */
    public RequestsBasedTrigger withCount(Integer count) {
        this.count = count;
        return this;
    }

    /**
     * Get the timeInterval value.
     *
     * @return the timeInterval value
     */
    public String timeInterval() {
        return this.timeInterval;
    }

    /**
     * Set the timeInterval value.
     *
     * @param timeInterval the timeInterval value to set
     * @return the RequestsBasedTrigger object itself.
     */
    public RequestsBasedTrigger withTimeInterval(String timeInterval) {
        this.timeInterval = timeInterval;
        return this;
    }

}
