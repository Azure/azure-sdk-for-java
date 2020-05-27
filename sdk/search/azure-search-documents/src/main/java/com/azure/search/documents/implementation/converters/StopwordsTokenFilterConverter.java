// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.StopwordsList;
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
        StopwordsTokenFilter stopwordsTokenFilter = new StopwordsTokenFilter();

        String name = obj.getName();
        stopwordsTokenFilter.setName(name);

        Boolean removeTrailingStopWords = obj.isRemoveTrailingStopWords();
        stopwordsTokenFilter.setRemoveTrailingStopWords(removeTrailingStopWords);

        Boolean ignoreCase = obj.isIgnoreCase();
        stopwordsTokenFilter.setIgnoreCase(ignoreCase);

        if (obj.getStopwords() != null) {
            List<String> stopwords = new ArrayList<>(obj.getStopwords());
            stopwordsTokenFilter.setStopwords(stopwords);
        }

        if (obj.getStopwordsList() != null) {
            StopwordsList stopwordsList = StopwordsListConverter.map(obj.getStopwordsList());
            stopwordsTokenFilter.setStopwordsList(stopwordsList);
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
            new com.azure.search.documents.indexes.implementation.models.StopwordsTokenFilter();

        String name = obj.getName();
        stopwordsTokenFilter.setName(name);

        Boolean removeTrailingStopWords = obj.isRemoveTrailingStopWords();
        stopwordsTokenFilter.setRemoveTrailingStopWords(removeTrailingStopWords);

        Boolean ignoreCase = obj.isIgnoreCase();
        stopwordsTokenFilter.setIgnoreCase(ignoreCase);

        if (obj.getStopwords() != null) {
            List<String> stopwords = new ArrayList<>(obj.getStopwords());
            stopwordsTokenFilter.setStopwords(stopwords);
        }

        if (obj.getStopwordsList() != null) {
            com.azure.search.documents.indexes.implementation.models.StopwordsList stopwordsList =
                StopwordsListConverter.map(obj.getStopwordsList());
            stopwordsTokenFilter.setStopwordsList(stopwordsList);
        }
        return stopwordsTokenFilter;
    }

    private StopwordsTokenFilterConverter() {
    }
}
