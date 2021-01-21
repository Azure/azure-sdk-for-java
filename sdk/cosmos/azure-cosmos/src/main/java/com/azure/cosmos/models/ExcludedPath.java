// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Objects;

/**
 * Represents an excluded path of the IndexingPolicy in the Azure Cosmos DB database service.
 */
public final class ExcludedPath {
    private JsonSerializable jsonSerializable;

    /**
     * Constructor.
     *
     * @param path the excluded path.
     */
    public ExcludedPath(String path) {
        this.jsonSerializable = new JsonSerializable();
        this.setPath(path);
    }

    /**
     * Constructor.
     *
     * @param objectNode the object node that represents the excluded path.
     */
    ExcludedPath(ObjectNode objectNode) {
        this.jsonSerializable = new JsonSerializable(objectNode);
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
     * @return the excluded path.
     */
    public ExcludedPath setPath(String path) {
        this.jsonSerializable.set(Constants.Properties.PATH, path);
        return this;
    }

    void populatePropertyBag() {
        this.jsonSerializable.populatePropertyBag();
    }

    JsonSerializable getJsonSerializable() { return this.jsonSerializable; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ExcludedPath that = (ExcludedPath) o;
        return Objects.equals(jsonSerializable, that.jsonSerializable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jsonSerializable);
    }
}
