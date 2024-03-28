// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Vector Embedding Policy
 */
public final class VectorEmbeddingPolicy {

    /**
     * Paths for embeddings along with path-specific settings for the item.
     */
    @JsonProperty(Constants.Properties.VECTOR_EMBEDDINGS)
    private List<Embedding> embeddings;

    /**
     * Constructor
     *
     * @param embeddings list of path for embeddings along with path-specific settings for the item.
     */
    public VectorEmbeddingPolicy(List<Embedding> embeddings) {
        validateEmbeddings(embeddings);
        this.embeddings = embeddings;
    }

    private static void validateEmbeddings(List<Embedding> embeddings) {
        embeddings.forEach(embedding -> {
            if (embedding == null) {
                throw new IllegalArgumentException("Embedding cannot be empty.");
            }
            validateEmbeddingPath(embedding.getPath());
            validateEmbeddingDimensions(embedding.getDimensions());
            validateEmbeddingVectorDataType(embedding.getVectorDataType());
            validateEmbeddingDistanceFunction(embedding.getDistanceFunction());
        });
    }

    private static void validateEmbeddingPath(String path) {
        if (StringUtils.isEmpty(path)) {
            throw new IllegalArgumentException("embedding path is empty");
        }

        if (path.charAt(0) != '/' || path.lastIndexOf('/') != 0) {
            throw new IllegalArgumentException("");
        }
    }

    private static void validateEmbeddingDimensions(Long dimensions) {
        if (dimensions < 1) {
            throw new IllegalArgumentException("dimensions for the embedding has to be a long value greater than 1");
        }
    }

    private static void validateEmbeddingVectorDataType(VectorDataType value) {
        Optional.ofNullable(value)
            .filter(vectorDataType -> !vectorDataType.isEmpty())
            .map(vectorDataType -> Arrays.stream(VectorDataType.values())
                .filter(dataType -> dataType.getValue().equals(vectorDataType.getValue()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("")))
            .orElseThrow(() -> new IllegalArgumentException(""));
    }

    private static void validateEmbeddingDistanceFunction(DistanceFunction value) {
        Optional.ofNullable(value)
            .filter(distanceFunction -> !distanceFunction.isEmpty())
            .map(distanceFunction -> Arrays.stream(DistanceFunction.values())
                .filter(distFunction -> distFunction.getValue().equals(distanceFunction.getValue()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("")))
            .orElseThrow(() -> new IllegalArgumentException(""));
    }

    /**
     * Gets the paths for embeddings along with path-specific settings for the item.
     *
     * @return the paths for embeddings along with path-specific settings for the item.
     */
    public List<Embedding> getEmbeddings() {
        return this.embeddings;
    }
}
