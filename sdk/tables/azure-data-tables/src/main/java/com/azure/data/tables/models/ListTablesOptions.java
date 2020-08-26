// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.models;

import com.azure.core.annotation.Fluent;

/**
 * helps construct a query
 */
@Fluent
public final class ListTablesOptions {
    private Integer top;
    private String filter;

    /**
     * Get the top property: Maximum number of records to return.
     *
     * @return the top value.
     */
    public Integer getTop() {
        return this.top;
    }

    /**
     * Set the top property: Maximum number of records to return.
     *
     * @param top the top value to set.
     * @return the TableQueryOptions object itself.
     */
    public ListTablesOptions setTop(Integer top) {
        this.top = top;
        return this;
    }

    /**
     * Get the filter property: OData filter expression.
     *
     * @return the filter value.
     */
    public String getFilter() {
        return this.filter;
    }

    /**
     * Set the filter property: OData filter expression.
     *
     * @param filter the filter value to set.
     * @return the TableQueryOptions object itself.
     */
    public ListTablesOptions setFilter(String filter) {
        this.filter = filter;
        return this;
    }
}

