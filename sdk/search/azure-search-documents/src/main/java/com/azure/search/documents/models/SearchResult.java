// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.implementation.converters.SearchResultHelper;

import java.util.List;
import java.util.Map;

import static com.azure.core.util.serializer.TypeReference.createInstance;

/**
 * Contains a document found by a search query, plus associated metadata.
 */
@Fluent
public final class SearchResult {

    /*
     * The relevance score of the document compared to other documents returned
     * by the query.
     */
    private final double score;

    /*
     * The semantic search results based on the search request.
     */
    private SemanticSearchResult semanticSearch;

    /*
     * Text fragments from the document that indicate the matching search
     * terms, organized by each applicable field; null if hit highlighting was
     * not enabled for the query.
     */
    private Map<String, List<String>> highlights;

    /*
     * Contains a document found by a search query, plus associated metadata.
     */
    private Map<String, Object> additionalProperties;

    /*
     * The json serializer.
     */
    private JsonSerializer jsonSerializer;

    static {
        SearchResultHelper.setAccessor(new SearchResultHelper.SearchResultAccessor() {
            @Override
            public void setAdditionalProperties(SearchResult searchResult, SearchDocument additionalProperties) {
                searchResult.setAdditionalProperties(additionalProperties);
            }

            @Override
            public void setHighlights(SearchResult searchResult, Map<String, List<String>> highlights) {
                searchResult.setHighlights(highlights);
            }

            @Override
            public void setJsonSerializer(SearchResult searchResult, JsonSerializer jsonSerializer) {
                searchResult.setJsonSerializer(jsonSerializer);
            }

            @Override
            public void setSemanticSearchResults(SearchResult searchResult, Double rerankerScore,
                List<QueryCaptionResult> captions) {
                searchResult.setSemanticSearchResult(rerankerScore, captions);
            }
        });
    }

    /**
     * Constructor of {@link SearchResult}.
     *
     * @param score The relevance score of the document compared to other documents returned by the query.
     */
    public SearchResult(double score) {
        this.score = score;
    }

    /**
     * Get the score property: The relevance score of the document compared to other documents returned by the query.
     *
     * @return the score value.
     */
    public double getScore() {
        return this.score;
    }

    /**
     * Get the highlights property: Text fragments from the document that indicate the matching search terms, organized
     * by each applicable field; null if hit highlighting was not enabled for the query.
     *
     * @return the highlights value.
     */
    public Map<String, List<String>> getHighlights() {
        return this.highlights;
    }

    /**
     * Get the semanticSearchResult property: The semantic search results based on the search request.
     * <p>
     * If semantic search wasn't requested this will return a {@link SemanticSearchResult} with no values.
     *
     * @return the semanticSearchResult value.
     */
    public SemanticSearchResult getSemanticSearch() {
        return this.semanticSearch;
    }

    /**
     * Get the additionalProperties property: Unmatched properties from the message are deserialized this collection.
     *
     * @param modelClass The model class converts to.
     * @param <T> Convert document to the generic type.
     * @return the additionalProperties value.
     * @throws RuntimeException if there is IO error occurs.
     */
    public <T> T getDocument(Class<T> modelClass) {
        return jsonSerializer.deserializeFromBytes(jsonSerializer.serializeToBytes(additionalProperties),
            createInstance(modelClass));
    }

    /**
     * The private setter to set the additionalProperties property via {@code SearchResultHelper.SearchResultAccessor}.
     *
     * @param additionalProperties The Unmatched properties from the message are deserialized this collection.
     */
    private void setAdditionalProperties(SearchDocument additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    /**
     * The private setter to set the highlights property via {@code SearchResultHelper.SearchResultAccessor}.
     *
     * @param highlights The Text fragments from the document that indicate the matching search terms.
     */
    private void setHighlights(Map<String, List<String>> highlights) {
        this.highlights = highlights;
    }

    /**
     * The private setter to set the jsonSerializer property via {@code SearchResultHelper.SearchResultAccessor}.
     *
     * @param jsonSerializer The json serializer.
     */
    private void setJsonSerializer(JsonSerializer jsonSerializer) {
        this.jsonSerializer = jsonSerializer;
    }

    /**
     * The private setter to set the semanticSearchResult property via
     * {@code SearchResultHelper.setSemanticSearchResult}.
     *
     * @param rerankerScore The reranker score.
     * @param captions The captions.
     */
    private void setSemanticSearchResult(Double rerankerScore, List<QueryCaptionResult> captions) {
        this.semanticSearch = new SemanticSearchResult(rerankerScore, captions);
    }
}
