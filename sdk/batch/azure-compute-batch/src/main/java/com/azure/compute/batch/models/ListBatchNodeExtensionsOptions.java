// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.models;

import java.util.List;

/**
 * Optional parameters for listing the Compute Node Extensions in a Batch Pool.
 */
public class ListBatchNodeExtensionsOptions extends BatchBaseOptions {
    private Integer maxresults;
    private List<String> select;

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
     * @return The {@link ListBatchNodeExtensionsOptions} object itself, allowing for method chaining.
     */
    public ListBatchNodeExtensionsOptions setMaxresults(Integer maxresults) {
        this.maxresults = maxresults;
        return this;
    }

    /**
     * Gets the OData $select clause.
     *
     * The $select clause specifies which properties should be included in the response.
     *
     * @return The OData $select clause.
     */
    public List<String> getSelect() {
        return select;
    }

    /**
     * Sets the OData $select clause.
     *
     * The $select clause specifies which properties should be included in the response.
     *
     * @param select The OData $select clause.
     * @return The {@link ListBatchNodeExtensionsOptions} object itself, allowing for method chaining.
     */
    public ListBatchNodeExtensionsOptions setSelect(List<String> select) {
        this.select = select;
        return this;
    }

}
