// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.models;

import java.util.List;

/**
 * Optional parameters for getting information about a specified Compute Node.
 */
public class GetBatchNodeOptions extends BatchBaseOptions {
    private List<String> select;

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
     * @return The {@link GetBatchNodeOptions} object itself, allowing for method chaining.
     */
    public GetBatchNodeOptions setSelect(List<String> select) {
        this.select = select;
        return this;
    }

}
