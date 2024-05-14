// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.models;

/**
 * Optional parameters for listing the files in a Batch Task's directory on its Compute Node.
 */
public class ListBatchTaskFilesOptions extends BatchBaseOptions {
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
     * @return The {@link ListBatchTaskFilesOptions} object itself, allowing for method chaining.
     */
    public ListBatchTaskFilesOptions setFilter(String filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Gets a value indicating whether to list children of the Task directory. This parameter can be used in combination with
     * the filter parameter to list specific type of files.
     *
     * @return A value indicating whether to list children of the Task directory.
     */
    public Boolean isRecursive() {
        return recursive;
    }

    /**
     * Sets a value indicating whether to list children of the Task directory. This parameter can be used in combination with
     * the filter parameter to list specific type of files.
     *
     * @param recursive A value indicating whether to list children of the Task directory.
     * @return The {@link ListBatchTaskFilesOptions} object itself, allowing for method chaining.
     */
    public ListBatchTaskFilesOptions setRecursive(Boolean recursive) {
        this.recursive = recursive;
        return this;
    }

}
