// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.models;

/**
 * Optional parameters for getting the number of Compute Nodes in each state, grouped by Batch Pool.
 * Note that the numbers returned may not always be up to date.
 * If you need exact node counts, use a list query.
 */
public class ListBatchPoolNodeCountsOptions extends BatchBaseOptions {
    private String filter;
    private Integer maxresults;

    /**
     * Gets the OData $filter clause used for filtering results.
     *
     * @return The OData $filter clause.
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Sets the OData $filter clause used for filtering results.
     *
     * @param filter The OData $filter clause.
     * @return The {@link ListBatchPoolNodeCountsOptions} object itself, allowing for method chaining.
     */
    public ListBatchPoolNodeCountsOptions setFilter(String filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Gets the maximum number of items to return in the response. A maximum of 1000 applications can be returned.
     *
     * @return The maximum number of items to return in the response.
     */
    public Integer getMaxresults() {
        return maxresults;
    }

    /**
     * Sets the maximum number of items to return in the response. A maximum of 1000 applications can be returned.
     *
     * @param maxresults The maximum number of items to return in the response.
     * @return The {@link ListBatchPoolNodeCountsOptions} object itself, allowing for method chaining.
     */
    public ListBatchPoolNodeCountsOptions setMaxresults(Integer maxresults) {
        this.maxresults = maxresults;
        return this;
    }
}
