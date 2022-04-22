// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.LexicalTokenizer;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.TokenFilter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.SearchIndex} and {@link
 * SearchIndex}.
 */
public final class SearchIndexConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.SearchIndex} to {@link SearchIndex}.
     */
    public static SearchIndex map(com.azure.search.documents.indexes.implementation.models.SearchIndex obj) {
        if (obj == null) {
            return null;
        }

        SearchIndex searchIndex = new SearchIndex(obj.getName(), obj.getFields());

        if (obj.getTokenizers() != null) {
            List<LexicalTokenizer> tokenizers =
                obj.getTokenizers().stream().map(LexicalTokenizerConverter::map).collect(Collectors.toList());
            searchIndex.setTokenizers(tokenizers);
        }

        searchIndex.setSuggesters(obj.getSuggesters());
        searchIndex.setCharFilters(obj.getCharFilters());

        if (obj.getTokenFilters() != null) {
            List<TokenFilter> tokenFilters =
                obj.getTokenFilters().stream().map(TokenFilterConverter::map).collect(Collectors.toList());
            searchIndex.setTokenFilters(tokenFilters);
        }

        searchIndex.setEncryptionKey(obj.getEncryptionKey());
        searchIndex.setDefaultScoringProfile(obj.getDefaultScoringProfile());
        searchIndex.setAnalyzers(obj.getAnalyzers());
        searchIndex.setSimilarity(obj.getSimilarity());
        searchIndex.setCorsOptions(obj.getCorsOptions());
        searchIndex.setETag(obj.getETag());
        searchIndex.setScoringProfiles(obj.getScoringProfiles());

        return searchIndex;
    }

    /**
     * Maps from {@link SearchIndex} to {@link com.azure.search.documents.indexes.implementation.models.SearchIndex}.
     */
    public static com.azure.search.documents.indexes.implementation.models.SearchIndex map(SearchIndex obj) {
        if (obj == null) {
            return null;
        }
        Objects.requireNonNull(obj.getName(), "The SearchIndex name cannot be null");
        com.azure.search.documents.indexes.implementation.models.SearchIndex searchIndex =
            new com.azure.search.documents.indexes.implementation.models.SearchIndex()
                .setName(obj.getName())
                .setFields(obj.getFields());

        if (obj.getTokenizers() != null) {
            List<com.azure.search.documents.indexes.implementation.models.LexicalTokenizer> tokenizers =
                obj.getTokenizers().stream().map(LexicalTokenizerConverter::map).collect(Collectors.toList());
            searchIndex.setTokenizers(tokenizers);
        }

        searchIndex.setSuggesters(obj.getSuggesters());
        searchIndex.setCharFilters(obj.getCharFilters());

        if (obj.getTokenFilters() != null) {
            List<com.azure.search.documents.indexes.implementation.models.TokenFilter> tokenFilters =
                obj.getTokenFilters().stream().map(TokenFilterConverter::map).collect(Collectors.toList());
            searchIndex.setTokenFilters(tokenFilters);
        }

        searchIndex.setEncryptionKey(obj.getEncryptionKey());
        searchIndex.setDefaultScoringProfile(obj.getDefaultScoringProfile());
        searchIndex.setAnalyzers(obj.getAnalyzers());
        searchIndex.setSimilarity(obj.getSimilarity());
        searchIndex.setCorsOptions(obj.getCorsOptions());
        searchIndex.setETag(obj.getETag());
        searchIndex.setScoringProfiles(obj.getScoringProfiles());

        return searchIndex;
    }

    private SearchIndexConverter() {
    }
}
