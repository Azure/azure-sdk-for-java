// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.models;

/**
 * Optional parameters for listing all Virtual Machine Images supported by the Azure Batch service.
 */
public class ListSupportedBatchImagesOptions extends BatchBaseOptions {
    private String filter;

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
     * @return The {@link ListSupportedBatchImagesOptions} object itself, allowing for method chaining.
     */
    public ListSupportedBatchImagesOptions setFilter(String filter) {
        this.filter = filter;
        return this;
    }

}
