// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.SimilarityAlgorithm;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.Similarity} and {@link SimilarityAlgorithm}.
 */
public final class SimilarityConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.Similarity} to {@link SimilarityAlgorithm}.
     */
    public static SimilarityAlgorithm map(com.azure.search.documents.indexes.implementation.models.Similarity obj) {
        if (obj == null) {
            return null;
        }
        SimilarityAlgorithm similarityAlgorithm = new SimilarityAlgorithm();
        return similarityAlgorithm;
    }

    /**
     * Maps from {@link SimilarityAlgorithm} to {@link com.azure.search.documents.indexes.implementation.models.Similarity}.
     */
    public static com.azure.search.documents.indexes.implementation.models.Similarity map(SimilarityAlgorithm obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.Similarity similarity =
            new com.azure.search.documents.indexes.implementation.models.Similarity();
        return similarity;
    }

    private SimilarityConverter() {
    }
}
