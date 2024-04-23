// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Embedding settings within {@link CosmosVectorEmbeddingPolicy}
 */
public final class CosmosVectorEmbedding {
    @JsonProperty(Constants.Properties.PATH)
    private String path;
    @JsonProperty(Constants.Properties.VECTOR_DATA_TYPE)
    private String cosmosVectorDataType;
    @JsonProperty(Constants.Properties.VECTOR_DIMENSIONS)
    private Long dimensions;
    @JsonProperty(Constants.Properties.DISTANCE_FUNCTION)
    private String cosmosVectorDistanceFunction;
    private JsonSerializable jsonSerializable;

    /**
     * Constructor
     */
    public CosmosVectorEmbedding() {
        this.jsonSerializable = new JsonSerializable();
    }

    /**
     * Gets the path for the cosmosVectorEmbedding.
     *
     * @return path
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the path for the cosmosVectorEmbedding.
     *
     * @param path the path for the cosmosVectorEmbedding
     * @return CosmosVectorEmbedding
     */
    public CosmosVectorEmbedding setPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * Gets the data type for the cosmosVectorEmbedding.
     *
     * @return cosmosVectorDataType
     */
    public CosmosVectorDataType getVectorDataType() {
        return CosmosVectorDataType.valueOf(cosmosVectorDataType);
    }

    /**
     * Sets the data type for the cosmosVectorEmbedding.
     *
     * @param cosmosVectorDataType the data type for the cosmosVectorEmbedding
     * @return CosmosVectorEmbedding
     */
    public CosmosVectorEmbedding setVectorDataType(CosmosVectorDataType cosmosVectorDataType) {
        this.cosmosVectorDataType = cosmosVectorDataType.toString();
        return this;
    }

    /**
     * Gets the dimensions for the cosmosVectorEmbedding.
     *
     * @return dimensions
     */
    public Long getDimensions() {
        return dimensions;
    }

    /**
     * Sets the dimensions for the cosmosVectorEmbedding.
     *
     * @param dimensions the dimensions for the cosmosVectorEmbedding
     * @return CosmosVectorEmbedding
     */
    public CosmosVectorEmbedding setDimensions(Long dimensions) {
        this.dimensions = dimensions;
        return this;
    }

    /**
     * Gets the distanceFunction for the cosmosVectorEmbedding.
     *
     * @return cosmosVectorDistanceFunction
     */
    public CosmosVectorDistanceFunction getVectorDistanceFunction() {
        return CosmosVectorDistanceFunction.valueOf(cosmosVectorDistanceFunction);
    }

    /**
     * Sets the distanceFunction for the cosmosVectorEmbedding.
     *
     * @param cosmosVectorDistanceFunction the distanceFunction for the cosmosVectorEmbedding
     * @return CosmosVectorEmbedding
     */
    public CosmosVectorEmbedding setVectorDistanceFunction(CosmosVectorDistanceFunction cosmosVectorDistanceFunction) {
        this.cosmosVectorDistanceFunction = cosmosVectorDistanceFunction.toString();
        return this;
    }
}
