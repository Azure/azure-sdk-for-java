// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Embedding settings within {@link CosmosVectorEmbeddingPolicy}
 */
public final class CosmosVectorEmbedding {
    @JsonProperty(Constants.Properties.PATH)
    private String path;
    @JsonProperty(Constants.Properties.VECTOR_DATA_TYPE)
    private String dataType;
    @JsonProperty(Constants.Properties.VECTOR_DIMENSIONS)
    private Long dimensions;
    @JsonProperty(Constants.Properties.DISTANCE_FUNCTION)
    private String distanceFunction;
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
        if (StringUtils.isEmpty(path)) {
            throw new NullPointerException("embedding path is either null or empty");
        }

        if (path.charAt(0) != '/' || path.lastIndexOf('/') != 0) {
            throw new IllegalArgumentException("");
        }

        this.path = path;
        return this;
    }

    /**
     * Gets the data type for the cosmosVectorEmbedding.
     *
     * @return dataType
     */
    public CosmosVectorDataType getDataType() {
        return CosmosVectorDataType.fromString(dataType);
    }

    /**
     * Sets the data type for the cosmosVectorEmbedding.
     *
     * @param dataType the data type for the cosmosVectorEmbedding
     * @return CosmosVectorEmbedding
     */
    public CosmosVectorEmbedding setDataType(CosmosVectorDataType dataType) {
        checkNotNull(dataType, "cosmosVectorDataType cannot be null");
        this.dataType = dataType.toString();
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
        checkNotNull(dimensions, "dimensions cannot be null");
        if (dimensions < 1) {
            throw new IllegalArgumentException("Dimensions for the embedding has to be a long value greater than 0 " +
                "for the vector embedding policy");
        }

        this.dimensions = dimensions;
        return this;
    }

    /**
     * Gets the distanceFunction for the cosmosVectorEmbedding.
     *
     * @return distanceFunction
     */
    public CosmosVectorDistanceFunction getDistanceFunction() {
        return CosmosVectorDistanceFunction.fromString(distanceFunction);
    }

    /**
     * Sets the distanceFunction for the cosmosVectorEmbedding.
     *
     * @param distanceFunction the distanceFunction for the cosmosVectorEmbedding
     * @return CosmosVectorEmbedding
     */
    public CosmosVectorEmbedding setDistanceFunction(CosmosVectorDistanceFunction distanceFunction) {
        checkNotNull(distanceFunction, "cosmosVectorDistanceFunction cannot be null");
        this.distanceFunction = distanceFunction.toString();
        return this;
    }
}
