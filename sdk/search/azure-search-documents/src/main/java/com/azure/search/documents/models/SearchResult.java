// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.implementation.converters.SearchResultHelper;
import com.azure.search.documents.implementation.util.Utility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.azure.core.util.serializer.TypeReference.createInstance;

/**
 * Contains a document found by a search query, plus associated metadata.
 */
@Fluent
public final class SearchResult {
    private final ClientLogger logger = new ClientLogger(SearchResult.class);

    /*
     * The relevance score of the document compared to other documents returned
     * by the query.
     */
    @JsonProperty(value = "@search.score", required = true, access = JsonProperty.Access.WRITE_ONLY)
    private double score;

    /*
     * The relevance score computed by the semantic ranker for the top search
     * results. Search results are sorted by the RerankerScore first and then
     * by the Score. RerankerScore is only returned for queries of type
     * 'semantic'.
     */
    @JsonProperty(value = "@search.rerankerScore", access = JsonProperty.Access.WRITE_ONLY)
    private Double rerankerScore;

    /*
     * Text fragments from the document that indicate the matching search
     * terms, organized by each applicable field; null if hit highlighting was
     * not enabled for the query.
     */
    @JsonProperty(value = "@search.highlights", access = JsonProperty.Access.WRITE_ONLY)
    private Map<String, List<String>> highlights;

    /*
     * Captions are the most representative passages from the document
     * relatively to the search query. They are often used as document summary.
     * Captions are only returned for queries of type 'semantic'.
     */
    @JsonProperty(value = "@search.captions", access = JsonProperty.Access.WRITE_ONLY)
    private List<CaptionResult> captions;

    /*
     * Contains a document found by a search query, plus associated metadata.
     */
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @JsonIgnore
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
            public void setRerankerScore(SearchResult searchResult, Double rerankerScore) {
                searchResult.setRerankerScore(rerankerScore);
            }

            @Override
            public void setCaptions(SearchResult searchResult, List<CaptionResult> captions) {
                searchResult.setCaptions(captions);
            }
        });
    }

    /**
     * Constructor of {@link SearchResult}.
     *
     * @param score The relevance score of the document compared to other documents returned by the query.
     */
    @JsonCreator
    public SearchResult(
        @JsonProperty(value = "@search.score", required = true, access = JsonProperty.Access.WRITE_ONLY)
            double score) {
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
     * Get the rerankerScore property: The relevance score computed by the semantic ranker for the top search results.
     * Search results are sorted by the RerankerScore first and then by the Score. RerankerScore is only returned for
     * queries of type 'semantic'.
     *
     * @return the rerankerScore value.
     */
    public Double getRerankerScore() {
        return this.rerankerScore;
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
     * Get the captions property: Captions are the most representative passages from the document relatively to the
     * search query. They are often used as document summary. Captions are only returned for queries of type
     * 'semantic'.
     *
     * @return the captions value.
     */
    public List<CaptionResult> getCaptions() {
        return this.captions;
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
        if (jsonSerializer == null) {
            try {
                return Utility.convertValue(additionalProperties, modelClass);
            } catch (IOException ex) {
                throw logger.logExceptionAsError(new RuntimeException("Failed to deserialize search result.", ex));
            }
        }

        byte[] rawJsonDocument = jsonSerializer.serializeToBytes(additionalProperties);
        return jsonSerializer.deserializeFromBytes(rawJsonDocument, createInstance(modelClass));
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
     * The private setter to set the rerankerScore property via {@code SearchResultHelper.setRerankerScore}.
     *
     * @param rerankerScore The reranker score.
     */
    private void setRerankerScore(Double rerankerScore) {
        this.rerankerScore = rerankerScore;
    }

    /**
     * The private setter to set the captions property via {@code SearchResultHelper.setCaptions}.
     *
     * @param captions The captions.
     */
    private void setCaptions(List<CaptionResult> captions) {
        this.captions = captions;
    }
}
