// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.models;

/**
 * Optional parameters for listing all of the files in Task directories on a Batch Compute Node.
 */
public class ListBatchNodeFilesOptions extends BatchBaseOptions {
    private String filter;
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
     * @return The {@link ListBatchNodeFilesOptions} object itself, allowing for method chaining.
     */
    public ListBatchNodeFilesOptions setFilter(String filter) {
        this.filter = filter;
        return this;
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
     * @return The {@link ListBatchNodeFilesOptions} object itself, allowing for method chaining.
     */
    public ListBatchNodeFilesOptions setRecursive(Boolean recursive) {
        this.recursive = recursive;
        return this;
    }

}
