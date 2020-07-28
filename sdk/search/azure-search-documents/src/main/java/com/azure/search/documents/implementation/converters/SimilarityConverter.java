// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.indexes.implementation.models.BM25Similarity;
import com.azure.search.documents.indexes.implementation.models.ClassicSimilarity;
import com.azure.search.documents.indexes.models.SimilarityAlgorithm;

import static com.azure.search.documents.implementation.util.Constants.ABSTRACT_EXTERNAL_ERROR_MSG;
import static com.azure.search.documents.implementation.util.Constants.ABSTRACT_INTERNAL_ERROR_MSG;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.Similarity} and {@link SimilarityAlgorithm}.
 */
public final class SimilarityConverter {
    private static final ClientLogger LOGGER = new ClientLogger(SimilarityConverter.class);
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.Similarity} to {@link SimilarityAlgorithm}.
     */
    public static SimilarityAlgorithm map(com.azure.search.documents.indexes.implementation.models.Similarity obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof ClassicSimilarity) {
            return ClassicSimilarityConverter.map((ClassicSimilarity) obj);
        }
        if (obj instanceof BM25Similarity) {
            return BM25SimilarityConverter.map((BM25Similarity) obj);
        }
        throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ABSTRACT_EXTERNAL_ERROR_MSG,
            obj.getClass().getSimpleName())));
    }

    /**
     * Maps from {@link SimilarityAlgorithm} to {@link com.azure.search.documents.indexes.implementation.models.Similarity}.
     */
    public static com.azure.search.documents.indexes.implementation.models.Similarity map(SimilarityAlgorithm obj) {
        if (obj instanceof com.azure.search.documents.indexes.models.ClassicSimilarityAlgorithm) {
            return ClassicSimilarityConverter.map((com.azure.search.documents.indexes.models.ClassicSimilarityAlgorithm) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.BM25SimilarityAlgorithm) {
            return BM25SimilarityConverter.map((com.azure.search.documents.indexes.models.BM25SimilarityAlgorithm) obj);
        }
        throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ABSTRACT_INTERNAL_ERROR_MSG,
            obj.getClass().getSimpleName())));
    }

    private SimilarityConverter() {
    }
}
