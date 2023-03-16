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
        NGramTokenizer nGramTokenizer = new NGramTokenizer(obj.getName());

        Integer maxGram = obj.getMaxGram();
        nGramTokenizer.setMaxGram(maxGram);

        if (obj.getTokenChars() != null) {
            nGramTokenizer.setTokenChars(obj.getTokenChars());
        }

        Integer minGram = obj.getMinGram();
        nGramTokenizer.setMinGram(minGram);
        return nGramTokenizer;
    }

    /**
     * Maps from {@link NGramTokenizer} to {@link com.azure.search.documents.indexes.implementation.models.NGramTokenizer}.
     */
    public static com.azure.search.documents.indexes.implementation.models.NGramTokenizer map(NGramTokenizer obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.NGramTokenizer nGramTokenizer =
            new com.azure.search.documents.indexes.implementation.models.NGramTokenizer(obj.getName());

        Integer maxGram = obj.getMaxGram();
        nGramTokenizer.setMaxGram(maxGram);

        if (obj.getTokenChars() != null) {
            nGramTokenizer.setTokenChars(obj.getTokenChars());
        }

        Integer minGram = obj.getMinGram();
        nGramTokenizer.setMinGram(minGram);

        return nGramTokenizer;
    }

    private NGramTokenizerConverter() {
    }
}
