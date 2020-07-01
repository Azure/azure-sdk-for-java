// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

/**
 * helps construct a query
 */
public final class QueryOptions {
    private Integer top;
    private String select;
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
     * @return the QueryOptions object itself.
     */
    public QueryOptions setTop(Integer top) {
        this.top = top;
        return this;
    }

    /**
     * Get the select property: Select expression using OData notation. Limits the columns on each record to just those
     * requested, e.g. "$select=PolicyAssignmentId, ResourceId".
     *
     * @return the select value.
     */
    public String getSelect() {
        return this.select;
    }

    /**
     * Set the select property: Select expression using OData notation. Limits the columns on each record to just those
     * requested, e.g. "$select=PolicyAssignmentId, ResourceId".
     *
     * @param select the select value to set.
     * @return the QueryOptions object itself.
     */
    public QueryOptions setSelect(String select) {
        this.select = select;
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
     * @return the QueryOptions object itself.
     */
    public QueryOptions setFilter(String filter) {
        this.filter = filter;
        return this;
    }
}

