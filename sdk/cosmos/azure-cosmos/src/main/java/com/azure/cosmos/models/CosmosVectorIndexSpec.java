// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Vector Indexes spec for Azure CosmosDB service.
 */
public final class CosmosVectorIndexSpec {

    private final JsonSerializable jsonSerializable;
    private String type;

    /**
     * Constructor
     */
    public CosmosVectorIndexSpec() {
        this.jsonSerializable = new JsonSerializable();
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
     * @return the SpatialSpec.
     */
    public CosmosVectorIndexSpec setPath(String path) {
        this.jsonSerializable.set(Constants.Properties.PATH, path, CosmosItemSerializer.DEFAULT_SERIALIZER);
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
        checkNotNull(type, "cosmosVectorIndexType cannot be null");
        this.type = type;
        this.jsonSerializable.set(Constants.Properties.VECTOR_INDEX_TYPE, this.type, CosmosItemSerializer.DEFAULT_SERIALIZER);
        return this;
    }

    void populatePropertyBag() {
        this.jsonSerializable.populatePropertyBag();
    }

    JsonSerializable getJsonSerializable() {
        return this.jsonSerializable;
    }
}
