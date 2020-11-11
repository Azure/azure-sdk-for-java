// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.LuceneStandardAnalyzer;

import java.util.ArrayList;
import java.util.List;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.LuceneStandardAnalyzer} and
 * {@link LuceneStandardAnalyzer}.
 */
public final class LuceneStandardAnalyzerConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.LuceneStandardAnalyzer} to
     * {@link LuceneStandardAnalyzer}.
     */
    public static LuceneStandardAnalyzer map(com.azure.search.documents.indexes.implementation.models.LuceneStandardAnalyzer obj) {
        if (obj == null) {
            return null;
        }
        LuceneStandardAnalyzer luceneStandardAnalyzer = new LuceneStandardAnalyzer(obj.getName());

        Integer maxTokenLength = obj.getMaxTokenLength();
        luceneStandardAnalyzer.setMaxTokenLength(maxTokenLength);

        if (obj.getStopwords() != null) {
            luceneStandardAnalyzer.setStopwords(obj.getStopwords());
        }
        return luceneStandardAnalyzer;
    }

    /**
     * Maps from {@link LuceneStandardAnalyzer} to
     * {@link com.azure.search.documents.indexes.implementation.models.LuceneStandardAnalyzer}.
     */
    public static com.azure.search.documents.indexes.implementation.models.LuceneStandardAnalyzer map(LuceneStandardAnalyzer obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.LuceneStandardAnalyzer luceneStandardAnalyzer =
            new com.azure.search.documents.indexes.implementation.models.LuceneStandardAnalyzer(obj.getName());

        Integer maxTokenLength = obj.getMaxTokenLength();
        luceneStandardAnalyzer.setMaxTokenLength(maxTokenLength);

        if (obj.getStopwords() != null) {
            List<String> stopwords = new ArrayList<>(obj.getStopwords());
            luceneStandardAnalyzer.setStopwords(stopwords);
        }

        return luceneStandardAnalyzer;
    }

    private LuceneStandardAnalyzerConverter() {
    }
}
