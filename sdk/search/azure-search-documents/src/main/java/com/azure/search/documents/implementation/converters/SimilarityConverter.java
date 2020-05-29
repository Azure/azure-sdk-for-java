// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.Similarity;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.Similarity} and {@link Similarity}.
 */
public final class SimilarityConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.Similarity} to {@link Similarity}.
     */
    public static Similarity map(com.azure.search.documents.indexes.implementation.models.Similarity obj) {
        if (obj == null) {
            return null;
        }
        Similarity similarity = new Similarity();
        return similarity;
    }

    /**
     * Maps from {@link Similarity} to {@link com.azure.search.documents.indexes.implementation.models.Similarity}.
     */
    public static com.azure.search.documents.indexes.implementation.models.Similarity map(Similarity obj) {
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
