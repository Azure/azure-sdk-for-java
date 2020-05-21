// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.NGramTokenizer;
import com.azure.search.documents.models.TokenCharacterKind;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.NGramTokenizer} and
 * {@link NGramTokenizer}.
 */
public final class NGramTokenizerConverter {
    private static final ClientLogger LOGGER = new ClientLogger(NGramTokenizerConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.NGramTokenizer} to {@link NGramTokenizer}.
     */
    public static NGramTokenizer map(com.azure.search.documents.implementation.models.NGramTokenizer obj) {
        if (obj == null) {
            return null;
        }
        NGramTokenizer nGramTokenizer = new NGramTokenizer();

        String _name = obj.getName();
        nGramTokenizer.setName(_name);

        Integer _maxGram = obj.getMaxGram();
        nGramTokenizer.setMaxGram(_maxGram);

        if (obj.getTokenChars() != null) {
            List<TokenCharacterKind> _tokenChars =
                obj.getTokenChars().stream().map(TokenCharacterKindConverter::map).collect(Collectors.toList());
            nGramTokenizer.setTokenChars(_tokenChars);
        }

        Integer _minGram = obj.getMinGram();
        nGramTokenizer.setMinGram(_minGram);
        return nGramTokenizer;
    }

    /**
     * Maps from {@link NGramTokenizer} to {@link com.azure.search.documents.implementation.models.NGramTokenizer}.
     */
    public static com.azure.search.documents.implementation.models.NGramTokenizer map(NGramTokenizer obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.NGramTokenizer nGramTokenizer =
            new com.azure.search.documents.implementation.models.NGramTokenizer();

        String _name = obj.getName();
        nGramTokenizer.setName(_name);

        Integer _maxGram = obj.getMaxGram();
        nGramTokenizer.setMaxGram(_maxGram);

        if (obj.getTokenChars() != null) {
            List<com.azure.search.documents.implementation.models.TokenCharacterKind> _tokenChars =
                obj.getTokenChars().stream().map(TokenCharacterKindConverter::map).collect(Collectors.toList());
            nGramTokenizer.setTokenChars(_tokenChars);
        }

        Integer _minGram = obj.getMinGram();
        nGramTokenizer.setMinGram(_minGram);
        return nGramTokenizer;
    }
}
