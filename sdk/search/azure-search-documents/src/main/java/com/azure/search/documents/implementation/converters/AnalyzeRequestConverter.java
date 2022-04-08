// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.AnalyzeTextOptions;
import com.azure.search.documents.indexes.models.CharFilterName;
import com.azure.search.documents.indexes.models.TokenFilterName;

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
            analyzeTextOptions = new AnalyzeTextOptions(obj.getText(), obj.getTokenizer());
        } else {
            analyzeTextOptions = new AnalyzeTextOptions(obj.getText(), obj.getAnalyzer());
        }

        if (obj.getCharFilters() != null) {
            analyzeTextOptions.setCharFilters(obj.getCharFilters().toArray(new CharFilterName[]{}));
        }


        if (obj.getTokenFilters() != null) {
            analyzeTextOptions.setTokenFilters(obj.getTokenFilters().toArray(new TokenFilterName[0]));
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
            analyzeRequest.setTokenFilters(obj.getTokenFilters());
        }

        if (obj.getTokenizerName() != null) {
            analyzeRequest.setTokenizer(obj.getTokenizerName());
        }

        return analyzeRequest;
    }

    private AnalyzeRequestConverter() {
    }
}
