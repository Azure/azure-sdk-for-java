// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.models;

/**
 * Optional parameters for listing the files in a Batch Task's directory on its Compute Node.
 */
public class ListBatchTaskFilesOptions extends BatchBaseOptions {
    private String filter;
    private Integer maxresults;
    private Boolean recursive;

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
    public ListBatchTaskFilesOptions setFilter(String filter) {
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
    public ListBatchTaskFilesOptions setMaxresults(Integer maxresults) {
        this.maxresults = maxresults;
        return this;
    }

    /**
     * Gets a value indicating whether to list children of the Task directory. This parameter can be used in combination with
     * the filter parameter to list specific type of files.
     *
     * @return A value indicating whether to list children of the Task directory.
     */
    public Boolean getRecursive() {
        return recursive;
    }

    /**
     * Sets a value indicating whether to list children of the Task directory. This parameter can be used in combination with
     * the filter parameter to list specific type of files.
     *
     * @param recursive A value indicating whether to list children of the Task directory.
     */
    public ListBatchTaskFilesOptions setRecursive(Boolean recursive) {
        this.recursive = recursive;
        return this;
    }

}
