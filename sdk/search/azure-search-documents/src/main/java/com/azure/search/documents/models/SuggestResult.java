// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.experimental.serializer.JsonSerializer;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.serializer.SearchSerializerProviders;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * A result containing a document found by a suggestion query, plus associated
 * metadata.
 */
@Fluent
public final class SuggestResult {
    private static final JsonSerializer SERIALIZER = SearchSerializerProviders.createInstance();

    /*
     * Unmatched properties from the message are deserialized this collection
     */
    @JsonProperty(value = "")
    private SearchDocument additionalProperties;

    /*
     * The text of the suggestion result.
     */
    @JsonProperty(value = "@search.text", required = true, access = JsonProperty.Access.WRITE_ONLY)
    private String text;

    /**
     * Constructor of {@link SuggestResult}.
     *
     * @param text The text of the suggestion result.
     */
    @JsonCreator
    public SuggestResult(
        @JsonProperty(value = "@search.text", required = true, access = JsonProperty.Access.WRITE_ONLY)
            String text) {
        this.text = text;
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
        return SERIALIZER.serialize(new ByteArrayOutputStream(), additionalProperties).flatMap(
            sourceStream -> SERIALIZER.deserialize(new ByteArrayInputStream(sourceStream.toByteArray()), modelClass))
            .block();
    }

    /**
     * Get the text property: The text of the suggestion result.
     *
     * @return the text value.
     */
    public String getText() {
        return this.text;
    }
}
