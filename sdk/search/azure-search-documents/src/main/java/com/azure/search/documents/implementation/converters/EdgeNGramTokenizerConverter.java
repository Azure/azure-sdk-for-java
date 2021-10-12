// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.EdgeNGramTokenizer;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenizer} and
 * {@link EdgeNGramTokenizer}.
 */
public final class EdgeNGramTokenizerConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenizer} to
     * {@link EdgeNGramTokenizer}.
     */
    public static EdgeNGramTokenizer map(com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenizer obj) {
        if (obj == null) {
            return null;
        }
        EdgeNGramTokenizer edgeNGramTokenizer = new EdgeNGramTokenizer(obj.getName());

        Integer maxGram = obj.getMaxGram();
        edgeNGramTokenizer.setMaxGram(maxGram);

        if (obj.getTokenChars() != null) {
            edgeNGramTokenizer.setTokenChars(obj.getTokenChars());
        }

        Integer minGram = obj.getMinGram();
        edgeNGramTokenizer.setMinGram(minGram);
        return edgeNGramTokenizer;
    }

    /**
     * Maps from {@link EdgeNGramTokenizer} to
     * {@link com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenizer}.
     */
    public static com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenizer map(EdgeNGramTokenizer obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenizer edgeNGramTokenizer =
            new com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenizer(obj.getName());

        Integer maxGram = obj.getMaxGram();
        edgeNGramTokenizer.setMaxGram(maxGram);

        if (obj.getTokenChars() != null) {
            edgeNGramTokenizer.setTokenChars(obj.getTokenChars());
        }

        Integer minGram = obj.getMinGram();
        edgeNGramTokenizer.setMinGram(minGram);

        return edgeNGramTokenizer;
    }

    private EdgeNGramTokenizerConverter() {
    }
}
