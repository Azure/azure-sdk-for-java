// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.AnalyzeRequest;
import com.azure.search.documents.models.CharFilterName;
import com.azure.search.documents.models.LexicalAnalyzerName;
import com.azure.search.documents.models.LexicalTokenizerName;
import com.azure.search.documents.models.TokenFilterName;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.AnalyzeRequest} and
 * {@link AnalyzeRequest}.
 */
public final class AnalyzeRequestConverter {
    private static final ClientLogger LOGGER = new ClientLogger(AnalyzeRequestConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.AnalyzeRequest} to {@link AnalyzeRequest}.
     */
    public static AnalyzeRequest map(com.azure.search.documents.implementation.models.AnalyzeRequest obj) {
        if (obj == null) {
            return null;
        }
        AnalyzeRequest analyzeRequest = new AnalyzeRequest();

        if (obj.getCharFilters() != null) {
            List<CharFilterName> _charFilters =
                obj.getCharFilters().stream().map(CharFilterNameConverter::map).collect(Collectors.toList());
            analyzeRequest.setCharFilters(_charFilters);
        }

        if (obj.getAnalyzer() != null) {
            LexicalAnalyzerName _analyzer = LexicalAnalyzerNameConverter.map(obj.getAnalyzer());
            analyzeRequest.setAnalyzer(_analyzer);
        }

        if (obj.getTokenFilters() != null) {
            List<TokenFilterName> _tokenFilters =
                obj.getTokenFilters().stream().map(TokenFilterNameConverter::map).collect(Collectors.toList());
            analyzeRequest.setTokenFilters(_tokenFilters);
        }

        String _text = obj.getText();
        analyzeRequest.setText(_text);

        if (obj.getTokenizer() != null) {
            LexicalTokenizerName _tokenizer = LexicalTokenizerNameConverter.map(obj.getTokenizer());
            analyzeRequest.setTokenizer(_tokenizer);
        }
        return analyzeRequest;
    }

    /**
     * Maps from {@link AnalyzeRequest} to {@link com.azure.search.documents.implementation.models.AnalyzeRequest}.
     */
    public static com.azure.search.documents.implementation.models.AnalyzeRequest map(AnalyzeRequest obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.AnalyzeRequest analyzeRequest =
            new com.azure.search.documents.implementation.models.AnalyzeRequest();

        if (obj.getCharFilters() != null) {
            List<com.azure.search.documents.implementation.models.CharFilterName> _charFilters =
                obj.getCharFilters().stream().map(CharFilterNameConverter::map).collect(Collectors.toList());
            analyzeRequest.setCharFilters(_charFilters);
        }

        if (obj.getAnalyzer() != null) {
            com.azure.search.documents.implementation.models.LexicalAnalyzerName _analyzer =
                LexicalAnalyzerNameConverter.map(obj.getAnalyzer());
            analyzeRequest.setAnalyzer(_analyzer);
        }

        if (obj.getTokenFilters() != null) {
            List<com.azure.search.documents.implementation.models.TokenFilterName> _tokenFilters =
                obj.getTokenFilters().stream().map(TokenFilterNameConverter::map).collect(Collectors.toList());
            analyzeRequest.setTokenFilters(_tokenFilters);
        }

        String _text = obj.getText();
        analyzeRequest.setText(_text);

        if (obj.getTokenizer() != null) {
            com.azure.search.documents.implementation.models.LexicalTokenizerName _tokenizer =
                LexicalTokenizerNameConverter.map(obj.getTokenizer());
            analyzeRequest.setTokenizer(_tokenizer);
        }
        return analyzeRequest;
    }
}
