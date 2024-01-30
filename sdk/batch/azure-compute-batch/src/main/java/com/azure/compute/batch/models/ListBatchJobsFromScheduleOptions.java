// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.models;

import java.util.List;

/**
 * Optional parameters for listing all Jobs that have been created under a Batch Job Schedule.
 */
public class ListBatchJobsFromScheduleOptions extends BatchBaseOptions {
    private List<String> expand;
    private String filter;
    private List<String> select;

    /**
     * Gets the OData $expand clause.
     *
     * <p>The $expand clause specifies related entities or complex properties to include in the response.
     *
     * @return The OData $expand clause.
     */
    public List<String> getExpand() {
        return expand;
    }

    /**
     * Sets the OData $expand clause.
     *
     * <p>The $expand clause specifies related entities or complex properties to include in the response.
     *
     * @param expand The OData $expand clause.
     * @return The {@link ListBatchJobsFromScheduleOptions} object itself, allowing for method chaining.
     */
    public ListBatchJobsFromScheduleOptions setExpand(List<String> expand) {
        this.expand = expand;
        return this;
    }

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
     * @return The {@link ListBatchJobsFromScheduleOptions} object itself, allowing for method chaining.
     */
    public ListBatchJobsFromScheduleOptions setFilter(String filter) {
        this.filter = filter;
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
     * @return The {@link ListBatchJobsFromScheduleOptions} object itself, allowing for method chaining.
     */
    public ListBatchJobsFromScheduleOptions setSelect(List<String> select) {
        this.select = select;
        return this;
    }

}
