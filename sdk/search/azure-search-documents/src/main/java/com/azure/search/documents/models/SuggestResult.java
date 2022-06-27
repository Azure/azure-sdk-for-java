// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.implementation.converters.SuggestResultHelper;

import static com.azure.core.util.serializer.TypeReference.createInstance;

/**
 * A result containing a document found by a suggestion query, plus associated
 * metadata.
 */
@Fluent
public final class SuggestResult {
    /*
     * Unmatched properties from the message are deserialized this collection
     */
    private SearchDocument additionalProperties;

    /*
     * The text of the suggestion result.
     */
    private final String text;

    private JsonSerializer jsonSerializer;

    static {
        SuggestResultHelper.setAccessor(new SuggestResultHelper.SuggestResultAccessor() {
            @Override
            public void setAdditionalProperties(SuggestResult suggestResult, SearchDocument additionalProperties) {
                suggestResult.setAdditionalProperties(additionalProperties);
            }

            @Override
            public void setJsonSerializer(SuggestResult suggestResult, JsonSerializer jsonSerializer) {
                suggestResult.jsonSerializer = jsonSerializer;
            }
        });
    }

    /**
     * Constructor of {@link SuggestResult}.
     *
     * @param text The text of the suggestion result.
     */
    public SuggestResult(String text) {
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
        return jsonSerializer.deserializeFromBytes(jsonSerializer.serializeToBytes(additionalProperties),
            createInstance(modelClass));
    }

    /**
     * Get the text property: The text of the suggestion result.
     *
     * @return the text value.
     */
    public String getText() {
        return this.text;
    }

    /**
     * The private setter to set the select property
     * via {@link SuggestResultHelper.SuggestResultAccessor}.
     *
     * @param additionalProperties The unmatched properties from the message.
     */
    private void setAdditionalProperties(SearchDocument additionalProperties) {
        this.additionalProperties = additionalProperties;
    }
}
