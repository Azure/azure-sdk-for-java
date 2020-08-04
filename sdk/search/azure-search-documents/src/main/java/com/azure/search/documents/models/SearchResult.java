// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.search.documents.SearchDocument;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.azure.core.util.serializer.TypeReference.createInstance;
import static com.azure.search.documents.implementation.util.Utility.initializeSerializerAdapter;

/**
 * Contains a document found by a search query, plus associated metadata.
 */
@Fluent
public final class SearchResult {
    private final ClientLogger logger = new ClientLogger(SearchResult.class);

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

    @JsonIgnore
    private JsonSerializer jsonSerializer;

    private static final JacksonAdapter searchJacksonAdapter = (JacksonAdapter) initializeSerializerAdapter();

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
     * @throws RuntimeException if there is IO error occurs.
     */
    public <T> T getDocument(Class<T> modelClass) {
        if (jsonSerializer == null) {
            try {
                String serializedJson = searchJacksonAdapter.serialize(additionalProperties, SerializerEncoding.JSON);
                return searchJacksonAdapter.deserialize(serializedJson, modelClass, SerializerEncoding.JSON);
            } catch (IOException ex) {
                throw logger.logExceptionAsError(new RuntimeException("Something wrong with the serialization."));
            }
        }
        ByteArrayOutputStream sourceStream = new ByteArrayOutputStream();
        jsonSerializer.serialize(sourceStream, additionalProperties);
        return jsonSerializer.deserialize(new ByteArrayInputStream(sourceStream.toByteArray()),
            createInstance(modelClass));
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
