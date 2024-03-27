// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Vector Indexes spec for Azure CosmosDB service.
 */
public final class VectorIndexSpec {

    private final JsonSerializable jsonSerializable;
    private VectorIndexType vectorIndexType;

    /**
     * Constructor
     */
    public VectorIndexSpec() {
        this.jsonSerializable = new JsonSerializable();
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the included path.
     */
    public VectorIndexSpec(String jsonString) { this.jsonSerializable = new JsonSerializable(jsonString); }

    /**
     * Constructor.
     *
     * @param objectNode the object node that represents the included path.
     */
    public VectorIndexSpec(ObjectNode objectNode) { this.jsonSerializable = new JsonSerializable(objectNode); }

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
    public VectorIndexSpec setPath(String path) {
        this.jsonSerializable.set(Constants.Properties.PATH, path);
        return this;
    }

    /**
     * Gets the vector index type for the vector index
     *
     * @return the vector index type
     */
    public VectorIndexType getVectorIndexType() {
        if (this.vectorIndexType == null) {
            this.vectorIndexType = VectorIndexType.valueOf(this.jsonSerializable.getString(Constants.Properties.VECTOR_INDEX_TYPE));

            if (this.vectorIndexType == null) {
                throw new IllegalArgumentException("INVALID vectorIndexType of " + this.jsonSerializable.getString(Constants.Properties.VECTOR_INDEX_TYPE));
            }
        }
        return this.vectorIndexType;
    }

    /**
     * Sets the vector index type for the vector index
     *
     * @param vectorIndexType the vector index type
     * @return the VectorIndexSpec
     */
    public VectorIndexSpec setVectorIndexType(VectorIndexType vectorIndexType) {
        this.vectorIndexType = vectorIndexType;
        this.jsonSerializable.set(Constants.Properties.VECTOR_INDEX_TYPE, vectorIndexType.toString());
        return this;
    }

    void populatePropertyBag() {
        this.jsonSerializable.populatePropertyBag();
    }

    JsonSerializable getJsonSerializable() {
        return this.jsonSerializable;
    }
}
