// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.StopwordsTokenFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.StopwordsTokenFilter} and
 * {@link StopwordsTokenFilter}.
 */
public final class StopwordsTokenFilterConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.StopwordsTokenFilter} to
     * {@link StopwordsTokenFilter}.
     */
    public static StopwordsTokenFilter map(com.azure.search.documents.indexes.implementation.models.StopwordsTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        StopwordsTokenFilter stopwordsTokenFilter = new StopwordsTokenFilter(obj.getName());

        Boolean removeTrailingStopWords = obj.isRemoveTrailingStopWords();
        stopwordsTokenFilter.setTrailingStopWordsRemoved(removeTrailingStopWords);

        Boolean ignoreCase = obj.isIgnoreCase();
        stopwordsTokenFilter.setCaseIgnored(ignoreCase);

        if (obj.getStopwords() != null) {
            List<String> stopwords = new ArrayList<>(obj.getStopwords());
            stopwordsTokenFilter.setStopwords(stopwords);
        }

        if (obj.getStopwordsList() != null) {
            stopwordsTokenFilter.setStopwordsList(obj.getStopwordsList());
        }
        return stopwordsTokenFilter;
    }

    /**
     * Maps from {@link StopwordsTokenFilter} to
     * {@link com.azure.search.documents.indexes.implementation.models.StopwordsTokenFilter}.
     */
    public static com.azure.search.documents.indexes.implementation.models.StopwordsTokenFilter map(StopwordsTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.StopwordsTokenFilter stopwordsTokenFilter =
            new com.azure.search.documents.indexes.implementation.models.StopwordsTokenFilter(obj.getName());


        Boolean removeTrailingStopWords = obj.areTrailingStopWordsRemoved();
        stopwordsTokenFilter.setRemoveTrailingStopWords(removeTrailingStopWords);

        Boolean ignoreCase = obj.isCaseIgnored();
        stopwordsTokenFilter.setIgnoreCase(ignoreCase);

        if (obj.getStopwords() != null) {
            List<String> stopwords = new ArrayList<>(obj.getStopwords());
            stopwordsTokenFilter.setStopwords(stopwords);
        }

        if (obj.getStopwordsList() != null) {
            stopwordsTokenFilter.setStopwordsList(obj.getStopwordsList());
        }

        return stopwordsTokenFilter;
    }

    private StopwordsTokenFilterConverter() {
    }
}
