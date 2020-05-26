// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.CharFilter;
import com.azure.search.documents.models.CorsOptions;
import com.azure.search.documents.models.LexicalAnalyzer;
import com.azure.search.documents.models.LexicalTokenizer;
import com.azure.search.documents.models.ScoringProfile;
import com.azure.search.documents.models.SearchField;
import com.azure.search.documents.models.SearchIndex;
import com.azure.search.documents.models.SearchResourceEncryptionKey;
import com.azure.search.documents.models.Similarity;
import com.azure.search.documents.models.Suggester;
import com.azure.search.documents.models.TokenFilter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.SearchIndex} and {@link SearchIndex}.
 */
public final class SearchIndexConverter {
    /**
     * Maps from {@link com.azure.search.documents.implementation.models.SearchIndex} to {@link SearchIndex}.
     */
    public static SearchIndex map(com.azure.search.documents.implementation.models.SearchIndex obj) {
        if (obj == null) {
            return null;
        }
        SearchIndex searchIndex = new SearchIndex();

        if (obj.getTokenizers() != null) {
            List<LexicalTokenizer> tokenizers =
                obj.getTokenizers().stream().map(LexicalTokenizerConverter::map).collect(Collectors.toList());
            searchIndex.setTokenizers(tokenizers);
        }

        if (obj.getSuggesters() != null) {
            List<Suggester> suggesters =
                obj.getSuggesters().stream().map(SuggesterConverter::map).collect(Collectors.toList());
            searchIndex.setSuggesters(suggesters);
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
            Similarity similarity = SimilarityConverter.map(obj.getSimilarity());
            searchIndex.setSimilarity(similarity);
        }

        String name = obj.getName();
        searchIndex.setName(name);

        if (obj.getCorsOptions() != null) {
            CorsOptions corsOptions = CorsOptionsConverter.map(obj.getCorsOptions());
            searchIndex.setCorsOptions(corsOptions);
        }

        String eTag = obj.getETag();
        searchIndex.setETag(eTag);

        if (obj.getScoringProfiles() != null) {
            List<ScoringProfile> scoringProfiles =
                obj.getScoringProfiles().stream().map(ScoringProfileConverter::map).collect(Collectors.toList());
            searchIndex.setScoringProfiles(scoringProfiles);
        }

        if (obj.getFields() != null) {
            List<SearchField> fields =
                obj.getFields().stream().map(SearchFieldConverter::map).collect(Collectors.toList());
            searchIndex.setFields(fields);
        }
        return searchIndex;
    }

    /**
     * Maps from {@link SearchIndex} to {@link com.azure.search.documents.implementation.models.SearchIndex}.
     */
    public static com.azure.search.documents.implementation.models.SearchIndex map(SearchIndex obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.SearchIndex searchIndex =
            new com.azure.search.documents.implementation.models.SearchIndex();

        if (obj.getTokenizers() != null) {
            List<com.azure.search.documents.implementation.models.LexicalTokenizer> tokenizers =
                obj.getTokenizers().stream().map(LexicalTokenizerConverter::map).collect(Collectors.toList());
            searchIndex.setTokenizers(tokenizers);
        }

        if (obj.getSuggesters() != null) {
            List<com.azure.search.documents.implementation.models.Suggester> suggesters =
                obj.getSuggesters().stream().map(SuggesterConverter::map).collect(Collectors.toList());
            searchIndex.setSuggesters(suggesters);
        }

        if (obj.getCharFilters() != null) {
            List<com.azure.search.documents.implementation.models.CharFilter> charFilters =
                obj.getCharFilters().stream().map(CharFilterConverter::map).collect(Collectors.toList());
            searchIndex.setCharFilters(charFilters);
        }

        if (obj.getTokenFilters() != null) {
            List<com.azure.search.documents.implementation.models.TokenFilter> tokenFilters =
                obj.getTokenFilters().stream().map(TokenFilterConverter::map).collect(Collectors.toList());
            searchIndex.setTokenFilters(tokenFilters);
        }

        if (obj.getEncryptionKey() != null) {
            com.azure.search.documents.implementation.models.SearchResourceEncryptionKey encryptionKey =
                SearchResourceEncryptionKeyConverter.map(obj.getEncryptionKey());
            searchIndex.setEncryptionKey(encryptionKey);
        }

        String defaultScoringProfile = obj.getDefaultScoringProfile();
        searchIndex.setDefaultScoringProfile(defaultScoringProfile);

        if (obj.getAnalyzers() != null) {
            List<com.azure.search.documents.implementation.models.LexicalAnalyzer> analyzers =
                obj.getAnalyzers().stream().map(LexicalAnalyzerConverter::map).collect(Collectors.toList());
            searchIndex.setAnalyzers(analyzers);
        }

        if (obj.getSimilarity() != null) {
            com.azure.search.documents.implementation.models.Similarity similarity =
                SimilarityConverter.map(obj.getSimilarity());
            searchIndex.setSimilarity(similarity);
        }

        String name = obj.getName();
        searchIndex.setName(name);

        if (obj.getCorsOptions() != null) {
            com.azure.search.documents.implementation.models.CorsOptions corsOptions =
                CorsOptionsConverter.map(obj.getCorsOptions());
            searchIndex.setCorsOptions(corsOptions);
        }

        String eTag = obj.getETag();
        searchIndex.setETag(eTag);

        if (obj.getScoringProfiles() != null) {
            List<com.azure.search.documents.implementation.models.ScoringProfile> scoringProfiles =
                obj.getScoringProfiles().stream().map(ScoringProfileConverter::map).collect(Collectors.toList());
            searchIndex.setScoringProfiles(scoringProfiles);
        }

        if (obj.getFields() != null) {
            List<com.azure.search.documents.implementation.models.SearchField> fields =
                obj.getFields().stream().map(SearchFieldConverter::map).collect(Collectors.toList());
            searchIndex.setFields(fields);
        }
        return searchIndex;
    }

    private SearchIndexConverter() {
    }
}
