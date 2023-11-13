// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.models;

/**
 * Optional parameters for listing all Virtual Machine Images supported by the Azure Batch service.
 */
public class ListSupportedBatchImagesOptions extends BatchBaseOptions {
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
     */
    public ListSupportedBatchImagesOptions setFilter(String filter) {
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
     */
    public ListSupportedBatchImagesOptions setMaxresults(Integer maxresults) {
        this.maxresults = maxresults;
        return this;

    }

}
