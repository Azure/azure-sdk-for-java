// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.models;

import java.util.List;

/**
 * Optional parameters for listing the execution status of the Job Preparation and Job Release Task
 * for the specified Batch Job across the Compute Nodes where the Job has run.
 */
public class ListBatchJobPreparationAndReleaseTaskStatusOptions extends BatchBaseOptions {
    private String filter;
    private Integer maxresults;
    private List<String> select;

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
    public ListBatchJobPreparationAndReleaseTaskStatusOptions setFilter(String filter) {
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
    public ListBatchJobPreparationAndReleaseTaskStatusOptions setMaxresults(Integer maxresults) {
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
     */
    public ListBatchJobPreparationAndReleaseTaskStatusOptions setSelect(List<String> select) {
        this.select = select;
        return this;
    }

}
