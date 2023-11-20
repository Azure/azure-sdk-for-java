// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.models;

/**
 * Optional parameters for listing all applications available in a Batch Account.
 */
public class ListBatchApplicationsOptions extends BatchBaseOptions {
    private Integer maxresults;

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
     * @return The {@link ListBatchApplicationsOptions} object itself, allowing for method chaining.
     */
    public ListBatchApplicationsOptions setMaxresults(Integer maxresults) {
        this.maxresults = maxresults;
        return this;
    }

}
