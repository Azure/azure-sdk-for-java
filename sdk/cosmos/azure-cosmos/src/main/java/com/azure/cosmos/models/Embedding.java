// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Embedding settings within {@link VectorEmbeddingPolicy}
 */
public final class Embedding {
    @JsonProperty(Constants.Properties.PATH)
    private String path;
    @JsonProperty(Constants.Properties.VECTOR_DATA_TYPE)
    private String vectorDataType;
    @JsonProperty(Constants.Properties.VECTOR_DIMENSIONS)
    private Long dimensions;
    @JsonProperty(Constants.Properties.DISTANCE_FUNCTION)
    private String distanceFunction;

    /**
     * Gets the path for the embedding.
     *
     * @return path
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the path for the embedding.
     *
     * @param path the path for the embedding
     * @return Embedding
     */
    public Embedding setPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * Gets the data type for the embedding.
     *
     * @return vectorDataType
     */
    public String getVectorDataType() {
        return vectorDataType;
    }

    /**
     * Sets the data type for the embedding.
     *
     * @param vectorDataType the data type for the embedding
     * @return Embedding
     */
    public Embedding setVectorDataType(String vectorDataType) {
        this.vectorDataType = vectorDataType;
        return this;
    }

    /**
     * Gets the dimensions for the embedding.
     *
     * @return dimensions
     */
    public Long getDimensions() {
        return dimensions;
    }

    /**
     * Sets the dimensions for the embedding.
     *
     * @param dimensions the dimensions for the embedding
     * @return Embedding
     */
    public Embedding setDimensions(Long dimensions) {
        this.dimensions = dimensions;
        return this;
    }

    /**
     * Gets the distanceFunction for the embedding.
     *
     * @return distanceFunction
     */
    public String getDistanceFunction() {
        return distanceFunction;
    }

    /**
     * Sets the distanceFunction for the embedding.
     *
     * @param distanceFunction the distanceFunction for the embedding
     * @return Embedding
     */
    public Embedding setDistanceFunction(String distanceFunction) {
        this.distanceFunction = distanceFunction;
        return this;
    }
}
