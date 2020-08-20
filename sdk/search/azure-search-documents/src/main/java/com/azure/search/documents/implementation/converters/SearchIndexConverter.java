// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.CharFilter;
import com.azure.search.documents.indexes.models.LexicalAnalyzer;
import com.azure.search.documents.indexes.models.LexicalTokenizer;
import com.azure.search.documents.indexes.models.ScoringProfile;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchResourceEncryptionKey;
import com.azure.search.documents.indexes.models.SearchSuggester;
import com.azure.search.documents.indexes.models.SimilarityAlgorithm;
import com.azure.search.documents.indexes.models.TokenFilter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.SearchIndex} and {@link SearchIndex}.
 */
public final class SearchIndexConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.SearchIndex} to {@link SearchIndex}.
     */
    public static SearchIndex map(com.azure.search.documents.indexes.implementation.models.SearchIndex obj) {
        if (obj == null) {
            return null;
        }

        List<SearchField> fields = obj.getFields() == null ? null
            : obj.getFields().stream().map(SearchFieldConverter::map).collect(Collectors.toList());
        SearchIndex searchIndex = new SearchIndex(obj.getName(), fields);

        if (obj.getTokenizers() != null) {
            List<LexicalTokenizer> tokenizers =
                obj.getTokenizers().stream().map(LexicalTokenizerConverter::map).collect(Collectors.toList());
            searchIndex.setTokenizers(tokenizers);
        }

        if (obj.getSuggesters() != null) {
            List<SearchSuggester> searchSuggesters =
                obj.getSuggesters().stream().map(SuggesterConverter::map).collect(Collectors.toList());
            searchIndex.setSuggesters(searchSuggesters);
        }

        if (obj.getCharFilters() != null) {
            List<CharFilter> charFilters =
                obj.getCharFilters().stream().map(CharFilterConverter::map).collect(Collectors.toList());
            searchIndex.setCharFilters(charFilters);
        }

        if (obj.getTokenFilters() != null) {
            List<TokenFilter> tokenFilters =
                obj.getTokenFilters().stream().map(TokenFilterConverter::map).collect(Collectors.toList());
            searchIndex.setTokenFilters(tokenFilters);
        }

        if (obj.getEncryptionKey() != null) {
            SearchResourceEncryptionKey encryptionKey =
                SearchResourceEncryptionKeyConverter.map(obj.getEncryptionKey());
            searchIndex.setEncryptionKey(encryptionKey);
        }

        String defaultScoringProfile = obj.getDefaultScoringProfile();
        searchIndex.setDefaultScoringProfile(defaultScoringProfile);

        if (obj.getAnalyzers() != null) {
            List<LexicalAnalyzer> analyzers =
                obj.getAnalyzers().stream().map(LexicalAnalyzerConverter::map).collect(Collectors.toList());
            searchIndex.setAnalyzers(analyzers);
        }

        if (obj.getSimilarity() != null) {
            SimilarityAlgorithm similarityAlgorithm = SimilarityConverter.map(obj.getSimilarity());
            searchIndex.setSimilarity(similarityAlgorithm);
        }

        if (obj.getCorsOptions() != null) {
            searchIndex.setCorsOptions(obj.getCorsOptions());
        }

        String eTag = obj.getETag();
        searchIndex.setETag(eTag);

        if (obj.getScoringProfiles() != null) {
            List<ScoringProfile> scoringProfiles =
                obj.getScoringProfiles().stream().map(ScoringProfileConverter::map).collect(Collectors.toList());
            searchIndex.setScoringProfiles(scoringProfiles);
        }

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
        List<com.azure.search.documents.indexes.implementation.models.SearchField> fields = obj.getFields() == null ?
            null : obj.getFields().stream().map(SearchFieldConverter::map).collect(Collectors.toList());
        com.azure.search.documents.indexes.implementation.models.SearchIndex searchIndex =
            new com.azure.search.documents.indexes.implementation.models.SearchIndex()
                .setName(obj.getName())
                .setFields(fields);

        if (obj.getTokenizers() != null) {
            List<com.azure.search.documents.indexes.implementation.models.LexicalTokenizer> tokenizers =
                obj.getTokenizers().stream().map(LexicalTokenizerConverter::map).collect(Collectors.toList());
            searchIndex.setTokenizers(tokenizers);
        }

        if (obj.getSuggesters() != null) {
            List<com.azure.search.documents.indexes.implementation.models.Suggester> suggesters =
                obj.getSuggesters().stream().map(SuggesterConverter::map).collect(Collectors.toList());
            searchIndex.setSuggesters(suggesters);
        }

        if (obj.getCharFilters() != null) {
            List<com.azure.search.documents.indexes.implementation.models.CharFilter> charFilters =
                obj.getCharFilters().stream().map(CharFilterConverter::map).collect(Collectors.toList());
            searchIndex.setCharFilters(charFilters);
        }

        if (obj.getTokenFilters() != null) {
            List<com.azure.search.documents.indexes.implementation.models.TokenFilter> tokenFilters =
                obj.getTokenFilters().stream().map(TokenFilterConverter::map).collect(Collectors.toList());
            searchIndex.setTokenFilters(tokenFilters);
        }

        if (obj.getEncryptionKey() != null) {
            com.azure.search.documents.indexes.implementation.models.SearchResourceEncryptionKey encryptionKey =
                SearchResourceEncryptionKeyConverter.map(obj.getEncryptionKey());
            searchIndex.setEncryptionKey(encryptionKey);
        }

        String defaultScoringProfile = obj.getDefaultScoringProfile();
        searchIndex.setDefaultScoringProfile(defaultScoringProfile);

        if (obj.getAnalyzers() != null) {
            List<com.azure.search.documents.indexes.implementation.models.LexicalAnalyzer> analyzers =
                obj.getAnalyzers().stream().map(LexicalAnalyzerConverter::map).collect(Collectors.toList());
            searchIndex.setAnalyzers(analyzers);
        }

        if (obj.getSimilarity() != null) {
            com.azure.search.documents.indexes.implementation.models.Similarity similarity =
                SimilarityConverter.map(obj.getSimilarity());
            searchIndex.setSimilarity(similarity);
        }

        if (obj.getCorsOptions() != null) {
            searchIndex.setCorsOptions(obj.getCorsOptions());
        }

        String eTag = obj.getETag();
        searchIndex.setETag(eTag);

        if (obj.getScoringProfiles() != null) {
            List<com.azure.search.documents.indexes.implementation.models.ScoringProfile> scoringProfiles =
                obj.getScoringProfiles().stream().map(ScoringProfileConverter::map).collect(Collectors.toList());
            searchIndex.setScoringProfiles(scoringProfiles);
        }

        return searchIndex;
    }

    private SearchIndexConverter() {
    }
}
