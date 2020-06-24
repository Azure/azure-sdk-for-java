// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;
import com.azure.core.annotation.Fluent;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.implementation.SerializationUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A result containing a document found by a suggestion query, plus associated
 * metadata.
 */
@Fluent
public final class SuggestResult {
    private static final ObjectMapper MAPPER;
    static {
        MAPPER = new JacksonAdapter().serializer();
        SerializationUtil.configureMapper(MAPPER);
        MAPPER.setSerializationInclusion(JsonInclude.Include.ALWAYS);
    }

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
     * Get the additionalProperties property: Unmatched properties from the
     * message are deserialized this collection.
     *
     * @param modelClass The model class converts to.
     * @param <T> Convert document to the generic type.
     * @return the additionalProperties value.
     */
    public <T> T getDocument(Class<T> modelClass) {
        return MAPPER.convertValue(this.additionalProperties, modelClass);
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
