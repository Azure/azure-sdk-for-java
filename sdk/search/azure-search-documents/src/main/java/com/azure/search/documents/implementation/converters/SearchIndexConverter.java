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
    private static final ClientLogger LOGGER = new ClientLogger(SearchIndexConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.SearchIndex} to {@link SearchIndex}.
     */
    public static SearchIndex map(com.azure.search.documents.implementation.models.SearchIndex obj) {
        if (obj == null) {
            return null;
        }
        SearchIndex searchIndex = new SearchIndex();

        if (obj.getTokenizers() != null) {
            List<LexicalTokenizer> _tokenizers =
                obj.getTokenizers().stream().map(LexicalTokenizerConverter::map).collect(Collectors.toList());
            searchIndex.setTokenizers(_tokenizers);
        }

        if (obj.getSuggesters() != null) {
            List<Suggester> _suggesters =
                obj.getSuggesters().stream().map(SuggesterConverter::map).collect(Collectors.toList());
            searchIndex.setSuggesters(_suggesters);
        }

        if (obj.getCharFilters() != null) {
            List<CharFilter> _charFilters =
                obj.getCharFilters().stream().map(CharFilterConverter::map).collect(Collectors.toList());
            searchIndex.setCharFilters(_charFilters);
        }

        if (obj.getTokenFilters() != null) {
            List<TokenFilter> _tokenFilters =
                obj.getTokenFilters().stream().map(TokenFilterConverter::map).collect(Collectors.toList());
            searchIndex.setTokenFilters(_tokenFilters);
        }

        if (obj.getEncryptionKey() != null) {
            SearchResourceEncryptionKey _encryptionKey =
                SearchResourceEncryptionKeyConverter.map(obj.getEncryptionKey());
            searchIndex.setEncryptionKey(_encryptionKey);
        }

        String _defaultScoringProfile = obj.getDefaultScoringProfile();
        searchIndex.setDefaultScoringProfile(_defaultScoringProfile);

        if (obj.getAnalyzers() != null) {
            List<LexicalAnalyzer> _analyzers =
                obj.getAnalyzers().stream().map(LexicalAnalyzerConverter::map).collect(Collectors.toList());
            searchIndex.setAnalyzers(_analyzers);
        }

        if (obj.getSimilarity() != null) {
            Similarity _similarity = SimilarityConverter.map(obj.getSimilarity());
            searchIndex.setSimilarity(_similarity);
        }

        String _name = obj.getName();
        searchIndex.setName(_name);

        if (obj.getCorsOptions() != null) {
            CorsOptions _corsOptions = CorsOptionsConverter.map(obj.getCorsOptions());
            searchIndex.setCorsOptions(_corsOptions);
        }

        String _eTag = obj.getETag();
        searchIndex.setETag(_eTag);

        if (obj.getScoringProfiles() != null) {
            List<ScoringProfile> _scoringProfiles =
                obj.getScoringProfiles().stream().map(ScoringProfileConverter::map).collect(Collectors.toList());
            searchIndex.setScoringProfiles(_scoringProfiles);
        }

        if (obj.getFields() != null) {
            List<SearchField> _fields =
                obj.getFields().stream().map(SearchFieldConverter::map).collect(Collectors.toList());
            searchIndex.setFields(_fields);
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
            List<com.azure.search.documents.implementation.models.LexicalTokenizer> _tokenizers =
                obj.getTokenizers().stream().map(LexicalTokenizerConverter::map).collect(Collectors.toList());
            searchIndex.setTokenizers(_tokenizers);
        }

        if (obj.getSuggesters() != null) {
            List<com.azure.search.documents.implementation.models.Suggester> _suggesters =
                obj.getSuggesters().stream().map(SuggesterConverter::map).collect(Collectors.toList());
            searchIndex.setSuggesters(_suggesters);
        }

        if (obj.getCharFilters() != null) {
            List<com.azure.search.documents.implementation.models.CharFilter> _charFilters =
                obj.getCharFilters().stream().map(CharFilterConverter::map).collect(Collectors.toList());
            searchIndex.setCharFilters(_charFilters);
        }

        if (obj.getTokenFilters() != null) {
            List<com.azure.search.documents.implementation.models.TokenFilter> _tokenFilters =
                obj.getTokenFilters().stream().map(TokenFilterConverter::map).collect(Collectors.toList());
            searchIndex.setTokenFilters(_tokenFilters);
        }

        if (obj.getEncryptionKey() != null) {
            com.azure.search.documents.implementation.models.SearchResourceEncryptionKey _encryptionKey =
                SearchResourceEncryptionKeyConverter.map(obj.getEncryptionKey());
            searchIndex.setEncryptionKey(_encryptionKey);
        }

        String _defaultScoringProfile = obj.getDefaultScoringProfile();
        searchIndex.setDefaultScoringProfile(_defaultScoringProfile);

        if (obj.getAnalyzers() != null) {
            List<com.azure.search.documents.implementation.models.LexicalAnalyzer> _analyzers =
                obj.getAnalyzers().stream().map(LexicalAnalyzerConverter::map).collect(Collectors.toList());
            searchIndex.setAnalyzers(_analyzers);
        }

        if (obj.getSimilarity() != null) {
            com.azure.search.documents.implementation.models.Similarity _similarity =
                SimilarityConverter.map(obj.getSimilarity());
            searchIndex.setSimilarity(_similarity);
        }

        String _name = obj.getName();
        searchIndex.setName(_name);

        if (obj.getCorsOptions() != null) {
            com.azure.search.documents.implementation.models.CorsOptions _corsOptions =
                CorsOptionsConverter.map(obj.getCorsOptions());
            searchIndex.setCorsOptions(_corsOptions);
        }

        String _eTag = obj.getETag();
        searchIndex.setETag(_eTag);

        if (obj.getScoringProfiles() != null) {
            List<com.azure.search.documents.implementation.models.ScoringProfile> _scoringProfiles =
                obj.getScoringProfiles().stream().map(ScoringProfileConverter::map).collect(Collectors.toList());
            searchIndex.setScoringProfiles(_scoringProfiles);
        }

        if (obj.getFields() != null) {
            List<com.azure.search.documents.implementation.models.SearchField> _fields =
                obj.getFields().stream().map(SearchFieldConverter::map).collect(Collectors.toList());
            searchIndex.setFields(_fields);
        }
        return searchIndex;
    }
}
