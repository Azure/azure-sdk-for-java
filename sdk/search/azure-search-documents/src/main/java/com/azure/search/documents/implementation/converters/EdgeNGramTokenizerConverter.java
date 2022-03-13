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

        return new EdgeNGramTokenizer(obj.getName())
            .setMaxGram(obj.getMaxGram())
            .setMinGram(obj.getMinGram())
            .setTokenChars(obj.getTokenChars());
    }

    /**
     * Maps from {@link EdgeNGramTokenizer} to
     * {@link com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenizer}.
     */
    public static com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenizer map(EdgeNGramTokenizer obj) {
        if (obj == null) {
            return null;
        }

        return new com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenizer(obj.getName())
            .setMaxGram(obj.getMaxGram())
            .setMinGram(obj.getMinGram())
            .setTokenChars(obj.getTokenChars());
    }

    private EdgeNGramTokenizerConverter() {
    }
}
