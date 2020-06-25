// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.ClassicSimilarityAlgorithm;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.ClassicSimilarity} and
 * {@link ClassicSimilarityAlgorithm}.
 */
public final class ClassicSimilarityConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.ClassicSimilarity} to
     * {@link ClassicSimilarityAlgorithm}.
     */
    public static ClassicSimilarityAlgorithm map(com.azure.search.documents.indexes.implementation.models.ClassicSimilarity obj) {
        if (obj == null) {
            return null;
        }
        ClassicSimilarityAlgorithm classicSimilarity = new ClassicSimilarityAlgorithm();
        return classicSimilarity;
    }

    /**
     * Maps from {@link ClassicSimilarityAlgorithm} to
     * {@link com.azure.search.documents.indexes.implementation.models.ClassicSimilarity}.
     */
    public static com.azure.search.documents.indexes.implementation.models.ClassicSimilarity map(ClassicSimilarityAlgorithm obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.ClassicSimilarity classicSimilarity =
            new com.azure.search.documents.indexes.implementation.models.ClassicSimilarity();
        return classicSimilarity;
    }

    private ClassicSimilarityConverter() {
    }
}
