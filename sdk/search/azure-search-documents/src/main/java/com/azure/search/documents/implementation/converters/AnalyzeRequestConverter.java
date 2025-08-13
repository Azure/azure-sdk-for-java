// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.AnalyzeTextOptions;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.AnalyzeRequest} and
 * {@link AnalyzeTextOptions}.
 */
public final class AnalyzeRequestConverter {
    /**
     * Maps from {@link AnalyzeTextOptions} to {@link com.azure.search.documents.indexes.implementation.models.AnalyzeRequest}.
     */
    public static com.azure.search.documents.indexes.implementation.models.AnalyzeRequest map(AnalyzeTextOptions obj) {
        if (obj == null) {
            return null;
        }

        return new com.azure.search.documents.indexes.implementation.models.AnalyzeRequest(obj.getText())
            .setAnalyzer(obj.getAnalyzerName())
            .setTokenizer(obj.getTokenizerName())
            .setCharFilters(obj.getCharFilters())
            .setTokenFilters(obj.getTokenFilters())
            .setNormalizer(obj.getNormalizerName());
    }

    private AnalyzeRequestConverter() {
    }
}
