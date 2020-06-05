// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.CharFilterName;
import com.azure.search.documents.indexes.models.CustomAnalyzer;
import com.azure.search.documents.indexes.models.LexicalTokenizerName;
import com.azure.search.documents.indexes.models.TokenFilterName;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.CustomAnalyzer} and
 * {@link CustomAnalyzer}.
 */
public final class CustomAnalyzerConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.CustomAnalyzer} to {@link CustomAnalyzer}.
     */
    public static CustomAnalyzer map(com.azure.search.documents.indexes.implementation.models.CustomAnalyzer obj) {
        if (obj == null) {
            return null;
        }
        CustomAnalyzer customAnalyzer = new CustomAnalyzer();

        String name = obj.getName();
        customAnalyzer.setName(name);

        if (obj.getCharFilters() != null) {
            List<CharFilterName> charFilters =
                obj.getCharFilters().stream().map(CharFilterNameConverter::map).collect(Collectors.toList());
            customAnalyzer.setCharFilters(charFilters);
        }

        if (obj.getTokenFilters() != null) {
            List<TokenFilterName> tokenFilters =
                obj.getTokenFilters().stream().map(TokenFilterNameConverter::map).collect(Collectors.toList());
            customAnalyzer.setTokenFilters(tokenFilters);
        }

        if (obj.getTokenizer() != null) {
            LexicalTokenizerName tokenizer = LexicalTokenizerNameConverter.map(obj.getTokenizer());
            customAnalyzer.setTokenizer(tokenizer);
        }
        return customAnalyzer;
    }

    /**
     * Maps from {@link CustomAnalyzer} to
     * {@link com.azure.search.documents.indexes.implementation.models.CustomAnalyzer}.
     */
    public static com.azure.search.documents.indexes.implementation.models.CustomAnalyzer map(CustomAnalyzer obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.CustomAnalyzer customAnalyzer =
            new com.azure.search.documents.indexes.implementation.models.CustomAnalyzer();

        String name = obj.getName();
        customAnalyzer.setName(name);

        if (obj.getCharFilters() != null) {
            List<com.azure.search.documents.indexes.implementation.models.CharFilterName> charFilters =
                obj.getCharFilters().stream().map(CharFilterNameConverter::map).collect(Collectors.toList());
            customAnalyzer.setCharFilters(charFilters);
        }

        if (obj.getTokenFilters() != null) {
            List<com.azure.search.documents.indexes.implementation.models.TokenFilterName> tokenFilters =
                obj.getTokenFilters().stream().map(TokenFilterNameConverter::map).collect(Collectors.toList());
            customAnalyzer.setTokenFilters(tokenFilters);
        }

        if (obj.getTokenizer() != null) {
            com.azure.search.documents.indexes.implementation.models.LexicalTokenizerName tokenizer =
                LexicalTokenizerNameConverter.map(obj.getTokenizer());
            customAnalyzer.setTokenizer(tokenizer);
        }
        return customAnalyzer;
    }

    private CustomAnalyzerConverter() {
    }
}
