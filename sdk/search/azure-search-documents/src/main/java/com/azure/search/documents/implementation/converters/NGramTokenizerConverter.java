// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.NGramTokenizer;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.NGramTokenizer} and
 * {@link NGramTokenizer}.
 */
public final class NGramTokenizerConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.NGramTokenizer} to {@link NGramTokenizer}.
     */
    public static NGramTokenizer map(com.azure.search.documents.indexes.implementation.models.NGramTokenizer obj) {
        if (obj == null) {
            return null;
        }

        return new NGramTokenizer(obj.getName())
            .setMaxGram(obj.getMaxGram())
            .setMinGram(obj.getMinGram())
            .setTokenChars(obj.getTokenChars());
    }

    /**
     * Maps from {@link NGramTokenizer} to {@link com.azure.search.documents.indexes.implementation.models.NGramTokenizer}.
     */
    public static com.azure.search.documents.indexes.implementation.models.NGramTokenizer map(NGramTokenizer obj) {
        if (obj == null) {
            return null;
        }

        return new com.azure.search.documents.indexes.implementation.models.NGramTokenizer(obj.getName())
            .setMaxGram(obj.getMaxGram())
            .setMinGram(obj.getMinGram())
            .setTokenChars(obj.getTokenChars());
    }

    private NGramTokenizerConverter() {
    }
}
