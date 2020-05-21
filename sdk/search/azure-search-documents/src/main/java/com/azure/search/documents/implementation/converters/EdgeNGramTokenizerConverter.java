// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.EdgeNGramTokenizer;
import com.azure.search.documents.models.TokenCharacterKind;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.EdgeNGramTokenizer} and
 * {@link EdgeNGramTokenizer}.
 */
public final class EdgeNGramTokenizerConverter {
    private static final ClientLogger LOGGER = new ClientLogger(EdgeNGramTokenizerConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.EdgeNGramTokenizer} to
     * {@link EdgeNGramTokenizer}.
     */
    public static EdgeNGramTokenizer map(com.azure.search.documents.implementation.models.EdgeNGramTokenizer obj) {
        if (obj == null) {
            return null;
        }
        EdgeNGramTokenizer edgeNGramTokenizer = new EdgeNGramTokenizer();

        String _name = obj.getName();
        edgeNGramTokenizer.setName(_name);

        Integer _maxGram = obj.getMaxGram();
        edgeNGramTokenizer.setMaxGram(_maxGram);

        if (obj.getTokenChars() != null) {
            List<TokenCharacterKind> _tokenChars =
                obj.getTokenChars().stream().map(TokenCharacterKindConverter::map).collect(Collectors.toList());
            edgeNGramTokenizer.setTokenChars(_tokenChars);
        }

        Integer _minGram = obj.getMinGram();
        edgeNGramTokenizer.setMinGram(_minGram);
        return edgeNGramTokenizer;
    }

    /**
     * Maps from {@link EdgeNGramTokenizer} to
     * {@link com.azure.search.documents.implementation.models.EdgeNGramTokenizer}.
     */
    public static com.azure.search.documents.implementation.models.EdgeNGramTokenizer map(EdgeNGramTokenizer obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.EdgeNGramTokenizer edgeNGramTokenizer =
            new com.azure.search.documents.implementation.models.EdgeNGramTokenizer();

        String _name = obj.getName();
        edgeNGramTokenizer.setName(_name);

        Integer _maxGram = obj.getMaxGram();
        edgeNGramTokenizer.setMaxGram(_maxGram);

        if (obj.getTokenChars() != null) {
            List<com.azure.search.documents.implementation.models.TokenCharacterKind> _tokenChars =
                obj.getTokenChars().stream().map(TokenCharacterKindConverter::map).collect(Collectors.toList());
            edgeNGramTokenizer.setTokenChars(_tokenChars);
        }

        Integer _minGram = obj.getMinGram();
        edgeNGramTokenizer.setMinGram(_minGram);
        return edgeNGramTokenizer;
    }
}
