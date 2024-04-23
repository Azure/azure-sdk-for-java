// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Vector Indexes spec for Azure CosmosDB service.
 */
public final class CosmosVectorIndexSpec {

    private final JsonSerializable jsonSerializable;
    private String type;

    /**
     * Constructor
     *
     * @param path the path.
     */
    public CosmosVectorIndexSpec(String path) {
        this.jsonSerializable = new JsonSerializable();
        this.setPath(path);
    }

    /**
     * Constructor.
     *
     * @param objectNode the object node that represents the included path.
     */
    private CosmosVectorIndexSpec(ObjectNode objectNode) { this.jsonSerializable = new JsonSerializable(objectNode); }

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
     * @return the SpatialSpec.
     */
    public CosmosVectorIndexSpec setPath(String path) {
        this.jsonSerializable.set(Constants.Properties.PATH, path);
        return this;
    }

    /**
     * Gets the vector index type for the vector index
     *
     * @return the vector index type
     */
    public String getType() {
        if (this.type == null) {
            this.type = this.jsonSerializable.getString(Constants.Properties.VECTOR_INDEX_TYPE);

            if (this.type == null) {
                throw new IllegalArgumentException("INVALID vectorIndexType of " + this.jsonSerializable.getString(Constants.Properties.VECTOR_INDEX_TYPE));
            }
        }
        return this.type;
    }

    /**
     * Sets the vector index type for the vector index
     *
     * @param type the vector index type
     * @return the VectorIndexSpec
     */
    public CosmosVectorIndexSpec setType(String type) {
        this.type = type;
        this.jsonSerializable.set(Constants.Properties.VECTOR_INDEX_TYPE, this.type);
        return this;
    }

    void populatePropertyBag() {
        this.jsonSerializable.populatePropertyBag();
    }

    JsonSerializable getJsonSerializable() {
        return this.jsonSerializable;
    }
}
