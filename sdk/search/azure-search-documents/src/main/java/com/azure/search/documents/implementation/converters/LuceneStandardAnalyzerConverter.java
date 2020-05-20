// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.LuceneStandardAnalyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.LuceneStandardAnalyzer} and
 * {@link LuceneStandardAnalyzer}.
 */
public final class LuceneStandardAnalyzerConverter {
    private static final ClientLogger LOGGER = new ClientLogger(LuceneStandardAnalyzerConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.LuceneStandardAnalyzer} to
     * {@link LuceneStandardAnalyzer}.
     */
    public static LuceneStandardAnalyzer map(com.azure.search.documents.implementation.models.LuceneStandardAnalyzer obj) {
        if (obj == null) {
            return null;
        }
        LuceneStandardAnalyzer luceneStandardAnalyzer = new LuceneStandardAnalyzer();

        String _name = obj.getName();
        luceneStandardAnalyzer.setName(_name);

        Integer _maxTokenLength = obj.getMaxTokenLength();
        luceneStandardAnalyzer.setMaxTokenLength(_maxTokenLength);

        if (obj.getStopwords() != null) {
            List<String> _stopwords = new ArrayList<>(obj.getStopwords());
            luceneStandardAnalyzer.setStopwords(_stopwords);
        }
        return luceneStandardAnalyzer;
    }

    /**
     * Maps from {@link LuceneStandardAnalyzer} to
     * {@link com.azure.search.documents.implementation.models.LuceneStandardAnalyzer}.
     */
    public static com.azure.search.documents.implementation.models.LuceneStandardAnalyzer map(LuceneStandardAnalyzer obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.LuceneStandardAnalyzer luceneStandardAnalyzer =
            new com.azure.search.documents.implementation.models.LuceneStandardAnalyzer();

        String _name = obj.getName();
        luceneStandardAnalyzer.setName(_name);

        Integer _maxTokenLength = obj.getMaxTokenLength();
        luceneStandardAnalyzer.setMaxTokenLength(_maxTokenLength);

        if (obj.getStopwords() != null) {
            List<String> _stopwords = new ArrayList<>(obj.getStopwords());
            luceneStandardAnalyzer.setStopwords(_stopwords);
        }
        return luceneStandardAnalyzer;
    }
}
