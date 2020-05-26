// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.models.BM25Similarity;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.BM25Similarity} and
 * {@link BM25Similarity}.
 */
public final class BM25SimilarityConverter {
    /**
     * Maps from {@link com.azure.search.documents.implementation.models.BM25Similarity} to {@link BM25Similarity}.
     */
    public static BM25Similarity map(com.azure.search.documents.implementation.models.BM25Similarity obj) {
        if (obj == null) {
            return null;
        }
        BM25Similarity bM25Similarity = new BM25Similarity();

        Double b = obj.getB();
        bM25Similarity.setB(b);

        Double k1 = obj.getK1();
        bM25Similarity.setK1(k1);
        return bM25Similarity;
    }

    /**
     * Maps from {@link BM25Similarity} to {@link com.azure.search.documents.implementation.models.BM25Similarity}.
     */
    public static com.azure.search.documents.implementation.models.BM25Similarity map(BM25Similarity obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.BM25Similarity bM25Similarity =
            new com.azure.search.documents.implementation.models.BM25Similarity();

        Double b = obj.getB();
        bM25Similarity.setB(b);

        Double k1 = obj.getK1();
        bM25Similarity.setK1(k1);
        return bM25Similarity;
    }

    private BM25SimilarityConverter() {
    }
}
