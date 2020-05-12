// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;

/**
 * Represents an excluded path of the IndexingPolicy in the Azure Cosmos DB database service.
 */
public final class ExcludedPath extends JsonSerializableWrapper{

    /**
     * Constructor.
     */
    public ExcludedPath() {
        this.jsonSerializable = new JsonSerializable();
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the excluded path.
     */
    ExcludedPath(String jsonString) {
        this.jsonSerializable = new JsonSerializable(jsonString);
    }

    /**
     * Gets path.
     *
     * @return the path.
     */
    public String getPath() {
        return this.jsonSerializable.getString(Constants.Properties.PATH);
    }

    /**
     * Sets path.
     *
     * @param path the path.
     * @return the Exculded path.
     */
    public ExcludedPath setPath(String path) {
        this.jsonSerializable.set(Constants.Properties.PATH, path);
        return this;
    }
}
