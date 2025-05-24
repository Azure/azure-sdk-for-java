// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;

/**
 * Represents cosmos full text index of the IndexingPolicy in the Azure Cosmos DB database service.
 */
public final class CosmosFullTextIndex {
    private final JsonSerializable jsonSerializable;

    /**
     * Constructor
     */
    public CosmosFullTextIndex() {
        this.jsonSerializable = new JsonSerializable();
    }

    /**
     * Gets path.
     * @return the path.
     */
    public String getPath() { return this.jsonSerializable.getString(Constants.Properties.PATH); }

    /**
     * Sets the path.
     * @param path the path.
     * @return the CosmosFullTextIndex.
     */
    public CosmosFullTextIndex setPath(String path) {
        this.jsonSerializable.set(
            Constants.Properties.PATH,
            path
        );
        return this;
    }

    void populatePropertyBag() { this.jsonSerializable.populatePropertyBag(); }

    JsonSerializable getJsonSerializable() { return this.jsonSerializable; }
}
