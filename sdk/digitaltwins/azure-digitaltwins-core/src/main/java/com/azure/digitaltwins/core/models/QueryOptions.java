// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.models;

import com.azure.core.annotation.Fluent;

/** Parameter group. */
@Fluent
public final class QueryOptions {
    /*
     * The maximum number of items to retrieve per request. The server may choose to return less than the requested
     * number.
     */
    private Integer maxItemsPerPage;

    /**
     * Creates a new instance of {@link QueryOptions}.
     */
    public QueryOptions() {
    }

    /**
     * Get the maxItemsPerPage property: The maximum number of items to retrieve per request. The server may choose to
     * return less than the requested number.
     *
     * @return the maxItemsPerPage value.
     */
    public Integer getMaxItemsPerPage() {
        return this.maxItemsPerPage;
    }

    /**
     * Set the maxItemsPerPage property: The maximum number of items to retrieve per request. The server may choose to
     * return less than the requested number.
     *
     * @param maxItemsPerPage the maxItemsPerPage value to set.
     * @return the QueryOptions object itself.
     */
    public QueryOptions setMaxItemsPerPage(Integer maxItemsPerPage) {
        this.maxItemsPerPage = maxItemsPerPage;
        return this;
    }
}
