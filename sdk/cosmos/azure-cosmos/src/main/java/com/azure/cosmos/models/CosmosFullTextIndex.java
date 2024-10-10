// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Objects;

/**
 * Represents cosmos full text index of the IndexingPolicy in the Azure Cosmos DB database service.
 */
public class CosmosFullTextIndex {
    private JsonSerializable jsonSerializable;

    /**
     * Constructor
     * @param path the cosmos full text path.
     */
    public CosmosFullTextIndex(String path) {
        this.jsonSerializable = new JsonSerializable();
        this.setPath(path);
    }

    /**
     * Constructor
     * @param objectNode the object node that represents the full text path.
     */
    CosmosFullTextIndex(ObjectNode objectNode) { this.jsonSerializable = new JsonSerializable(objectNode); }

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
            path,
            CosmosItemSerializer.DEFAULT_SERIALIZER);
        return this;
    }

    void populatePropertyBag() { this.jsonSerializable.populatePropertyBag(); }

    JsonSerializable getJsonSerializable() { return this.jsonSerializable; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CosmosFullTextIndex that = (CosmosFullTextIndex) o;
        return Objects.equals(jsonSerializable, that.jsonSerializable);
    }

    @Override
    public int hashCode() { return Objects.hash(jsonSerializable); }


}
