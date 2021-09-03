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

        if (obj.getAnalyzer() != null) {
            analyzeTextOptions = new AnalyzeTextOptions(obj.getText(), obj.getAnalyzer());
        } else if (obj.getNormalizer() != null) {
            analyzeTextOptions = new AnalyzeTextOptions(obj.getText(), obj.getNormalizer());
        } else {
            analyzeTextOptions = new AnalyzeTextOptions(obj.getText(), obj.getTokenizer());
        }

        if (obj.getCharFilters() != null) {
            analyzeTextOptions.setCharFilters(obj.getCharFilters().toArray(new CharFilterName[0]));
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

        return new com.azure.search.documents.indexes.implementation.models.AnalyzeRequest(obj.getText())
            .setAnalyzer(obj.getAnalyzerName())
            .setNormalizer(obj.getNormalizer())
            .setTokenizer(obj.getTokenizerName())
            .setCharFilters(obj.getCharFilters())
            .setTokenFilters(obj.getTokenFilters());
    }

    private AnalyzeRequestConverter() {
    }
}
