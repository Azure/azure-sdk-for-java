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
     * @return The {@link ListBatchJobPreparationAndReleaseTaskStatusOptions} object itself, allowing for method chaining.
     */
    public ListBatchJobPreparationAndReleaseTaskStatusOptions setFilter(String filter) {
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
     * @return The {@link ListBatchJobPreparationAndReleaseTaskStatusOptions} object itself, allowing for method chaining.
     */
    public ListBatchJobPreparationAndReleaseTaskStatusOptions setSelect(List<String> select) {
        this.select = select;
        return this;
    }

}
