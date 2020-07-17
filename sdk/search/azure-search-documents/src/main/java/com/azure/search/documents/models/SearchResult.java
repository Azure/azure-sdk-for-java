// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.experimental.serializer.JsonSerializer;
import com.azure.core.experimental.serializer.JsonSerializerProviders;
import com.azure.search.documents.SearchDocument;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Contains a document found by a search query, plus associated metadata.
 */
@Fluent
public final class SearchResult {
    private static final JsonSerializer SERIALIZER = JsonSerializerProviders.createInstance();

    /*
     * Unmatched properties from the message are deserialized this collection
     */
    @JsonProperty(value = "")
    private SearchDocument additionalProperties;

    /*
     * The relevance score of the document compared to other documents returned
     * by the query.
     */
    @JsonProperty(value = "@search.score", required = true, access = JsonProperty.Access.WRITE_ONLY)
    private double score;

    /*
     * Text fragments from the document that indicate the matching search
     * terms, organized by each applicable field; null if hit highlighting was
     * not enabled for the query.
     */
    @JsonProperty(value = "@search.highlights", access = JsonProperty.Access.WRITE_ONLY)
    private Map<String, List<String>> highlights;

    /**
     * Constructor of {@link SearchResult}.
     *
     * @param score The relevance score of the document compared to other documents returned
     * by the query.
     */
    @JsonCreator
    public SearchResult(
        @JsonProperty(value = "@search.score", required = true, access = JsonProperty.Access.WRITE_ONLY)
            double score) {
        this.score = score;
    }
    /**
     * Get the additionalProperties property: Unmatched properties from the
     * message are deserialized this collection.
     *
     * @param modelClass The model class converts to.
     * @param <T> Convert document to the generic type.
     * @return the additionalProperties value.
     */
    public <T> T getDocument(Class<T> modelClass) {
        return SERIALIZER.convertValue(this.additionalProperties, modelClass).block();
    }

    /**
     * Get the score property: The relevance score of the document compared to
     * other documents returned by the query.
     *
     * @return the score value.
     */
    public double getScore() {
        return this.score;
    }

    /**
     * Get the highlights property: Text fragments from the document that
     * indicate the matching search terms, organized by each applicable field;
     * null if hit highlighting was not enabled for the query.
     *
     * @return the highlights value.
     */
    public Map<String, List<String>> getHighlights() {
        return this.highlights;
    }
}
