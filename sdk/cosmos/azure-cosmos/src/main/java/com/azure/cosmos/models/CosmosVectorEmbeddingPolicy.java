// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

/**
 * Vector Embedding Policy
 */
public final class CosmosVectorEmbeddingPolicy {

    private JsonSerializable jsonSerializable;
    /**
     * Paths for embeddings along with path-specific settings for the item.
     */
    @JsonProperty(Constants.Properties.VECTOR_EMBEDDINGS)
    private List<CosmosVectorEmbedding> cosmosVectorEmbeddings;

    /**
     * Constructor
     *
     * @param cosmosVectorEmbeddings list of path for embeddings along with path-specific settings for the item.
     */
    public CosmosVectorEmbeddingPolicy(List<CosmosVectorEmbedding> cosmosVectorEmbeddings) {
        validateEmbeddings(cosmosVectorEmbeddings);
        this.jsonSerializable = new JsonSerializable();
        this.setCosmosVectorEmbeddings(cosmosVectorEmbeddings);
    }

    private static void validateEmbeddings(List<CosmosVectorEmbedding> cosmosVectorEmbeddings) {
        cosmosVectorEmbeddings.forEach(embedding -> {
            if (embedding == null) {
                throw new IllegalArgumentException("Embedding cannot be null.");
            }
            validateEmbeddingPath(embedding.getPath());
            validateEmbeddingDimensions(embedding.getDimensions());
            validateEmbeddingVectorDataType(embedding.getVectorDataType());
            validateEmbeddingDistanceFunction(embedding.getVectorDistanceFunction());
        });
    }

    private static void validateEmbeddingPath(String path) {
        if (StringUtils.isEmpty(path)) {
            throw new NullPointerException("embedding path is empty");
        }

        if (path.charAt(0) != '/' || path.lastIndexOf('/') != 0) {
            throw new IllegalArgumentException("");
        }
    }

    private static void validateEmbeddingDimensions(Long dimensions) {
        if (dimensions == null) {
            throw new NullPointerException("Dimensions for the embedding cannot be null " +
                "for the vector embedding policy");
        }
        if (dimensions < 1) {
            throw new IllegalArgumentException("Dimensions for the embedding has to be a long value greater than 1 " +
                "for the vector embedding policy");
        }
    }

    private static void validateEmbeddingVectorDataType(CosmosVectorDataType value) {
        if (Arrays.stream(CosmosVectorDataType.values()).noneMatch(vectorDataType ->
            vectorDataType.equals(value))) {
            throw new IllegalArgumentException("Invalid vector data type for the vector embedding policy.");
        }
    }

    private static void validateEmbeddingDistanceFunction(CosmosVectorDistanceFunction value) {
        if (Arrays.stream(CosmosVectorDistanceFunction.values()).noneMatch(distanceFunction ->
            distanceFunction.equals(value))) {
            throw new IllegalArgumentException("Invalid distance function for the vector embedding policy.");
        }
    }

    /**
     * Gets the paths for embeddings along with path-specific settings for the item.
     *
     * @return the paths for embeddings along with path-specific settings for the item.
     */
    public List<CosmosVectorEmbedding> getVectorEmbeddings() {
        return this.cosmosVectorEmbeddings;
    }

    /**
     * Sets the paths for embeddings along with path-specific settings for the item.
     *
     * @param cosmosVectorEmbeddings paths for embeddings along with path-specific settings for the item.
     */
    public void setCosmosVectorEmbeddings(List<CosmosVectorEmbedding> cosmosVectorEmbeddings) {
        this.cosmosVectorEmbeddings = cosmosVectorEmbeddings;
    }

}
