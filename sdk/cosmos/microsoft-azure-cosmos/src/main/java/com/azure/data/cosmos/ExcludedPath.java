// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Constants;

/**
 * Represents an excluded path of the IndexingPolicy in the Azure Cosmos DB database service.
 */
public class ExcludedPath extends JsonSerializable {

    /**
     * Constructor.
     */
    public ExcludedPath() {
        super();
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the excluded path.
     */
    ExcludedPath(String jsonString) {
        super(jsonString);
    }

    /**
     * Gets path.
     *
     * @return the path.
     */
    public String path() {
        return super.getString(Constants.Properties.PATH);
    }

    /**
     * Sets path.
     *
     * @param path the path.
     * @return the Exculded path.
     */
    public ExcludedPath path(String path) {
        super.set(Constants.Properties.PATH, path);
        return this;
    }
}
