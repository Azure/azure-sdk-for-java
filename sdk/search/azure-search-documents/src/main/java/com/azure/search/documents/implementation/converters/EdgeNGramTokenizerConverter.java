// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.EdgeNGramTokenizer;
import com.azure.search.documents.indexes.models.TokenCharacterKind;

import java.util.List;
import java.util.stream.Collectors;

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
        EdgeNGramTokenizer edgeNGramTokenizer = new EdgeNGramTokenizer();

        String name = obj.getName();
        edgeNGramTokenizer.setName(name);

        Integer maxGram = obj.getMaxGram();
        edgeNGramTokenizer.setMaxGram(maxGram);

        if (obj.getTokenChars() != null) {
            List<TokenCharacterKind> tokenChars =
                obj.getTokenChars().stream().map(TokenCharacterKindConverter::map).collect(Collectors.toList());
            edgeNGramTokenizer.setTokenChars(tokenChars);
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
            new com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenizer();

        String name = obj.getName();
        edgeNGramTokenizer.setName(name);

        Integer maxGram = obj.getMaxGram();
        edgeNGramTokenizer.setMaxGram(maxGram);

        if (obj.getTokenChars() != null) {
            List<com.azure.search.documents.indexes.implementation.models.TokenCharacterKind> tokenChars =
                obj.getTokenChars().stream().map(TokenCharacterKindConverter::map).collect(Collectors.toList());
            edgeNGramTokenizer.setTokenChars(tokenChars);
        }

        Integer minGram = obj.getMinGram();
        edgeNGramTokenizer.setMinGram(minGram);
        return edgeNGramTokenizer;
    }

    private EdgeNGramTokenizerConverter() {
    }
}
