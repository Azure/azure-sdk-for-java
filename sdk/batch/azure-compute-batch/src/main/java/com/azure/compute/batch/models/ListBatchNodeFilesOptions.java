// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.models;

/**
 * Optional parameters for listing all of the files in Task directories on a Batch Compute Node.
 */
public class ListBatchNodeFilesOptions extends BatchBaseOptions {
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
    public void setFilter(String filter) {
        this.filter = filter;
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
    public void setMaxresults(Integer maxresults) {
        this.maxresults = maxresults;
    }

    /**
     * Gets a value indicating whether to list children of a directory.
     *
     * @return A value indicating whether to list children of a directory.
     */
    public Boolean getRecursive() {
        return recursive;
    }

    /**
     * Sets a value indicating whether to list children of a directory.
     *
     * @param recursive A value indicating whether to list children of a directory.
     */
    public void setRecursive(Boolean recursive) {
        this.recursive = recursive;
    }

}
