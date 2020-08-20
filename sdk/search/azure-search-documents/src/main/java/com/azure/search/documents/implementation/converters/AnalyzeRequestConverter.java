// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.AnalyzeTextOptions;
import com.azure.search.documents.indexes.models.CharFilterName;
import com.azure.search.documents.indexes.models.LexicalTokenizerName;
import com.azure.search.documents.indexes.models.TokenFilterName;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.AnalyzeRequest} and
 * {@link AnalyzeTextOptions}.
 */
public final class AnalyzeRequestConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.AnalyzeRequest} to {@link AnalyzeTextOptions}.
     */
    public static AnalyzeTextOptions map(com.azure.search.documents.indexes.implementation.models.AnalyzeRequest obj) {
        if (obj == null) {
            return null;
        }
        AnalyzeTextOptions analyzeTextOptions;

        if (obj.getTokenizer() != null) {
            LexicalTokenizerName tokenizer = LexicalTokenizerNameConverter.map(obj.getTokenizer());
            analyzeTextOptions = new AnalyzeTextOptions(obj.getText(), tokenizer);
        } else {
            analyzeTextOptions = new AnalyzeTextOptions(obj.getText(), obj.getAnalyzer());
        }

        if (obj.getCharFilters() != null) {
            analyzeTextOptions.setCharFilters(obj.getCharFilters().toArray(new CharFilterName[]{}));
        }


        if (obj.getTokenFilters() != null) {
            analyzeTextOptions.setTokenFilters(obj.getTokenFilters().stream()
                .map(TokenFilterNameConverter::map)
                .toArray(TokenFilterName[]::new));
        }


        return analyzeTextOptions;
    }

    /**
     * Maps from {@link AnalyzeTextOptions} to {@link com.azure.search.documents.indexes.implementation.models.AnalyzeRequest}.
     */
    public static com.azure.search.documents.indexes.implementation.models.AnalyzeRequest map(AnalyzeTextOptions obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.AnalyzeRequest analyzeRequest =
            new com.azure.search.documents.indexes.implementation.models.AnalyzeRequest(obj.getText());

        if (obj.getCharFilters() != null) {
            analyzeRequest.setCharFilters(obj.getCharFilters());
        }

        if (obj.getAnalyzerName() != null) {
            analyzeRequest.setAnalyzer(obj.getAnalyzerName());
        }

        if (obj.getTokenFilters() != null) {
            List<com.azure.search.documents.indexes.implementation.models.TokenFilterName> tokenFilters =
                obj.getTokenFilters().stream().map(TokenFilterNameConverter::map).collect(Collectors.toList());
            analyzeRequest.setTokenFilters(tokenFilters);
        }

        if (obj.getTokenizerName() != null) {
            com.azure.search.documents.indexes.implementation.models.LexicalTokenizerName tokenizer =
                LexicalTokenizerNameConverter.map(obj.getTokenizerName());
            analyzeRequest.setTokenizer(tokenizer);
        }

        return analyzeRequest;
    }

    private AnalyzeRequestConverter() {
    }
}
