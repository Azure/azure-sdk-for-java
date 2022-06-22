// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.implementation.converters.SuggestResultHelper;
import com.azure.search.documents.implementation.util.Utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.azure.core.util.serializer.TypeReference.createInstance;

/**
 * A result containing a document found by a suggestion query, plus associated
 * metadata.
 */
@Fluent
public final class SuggestResult {
    private static final ClientLogger LOGGER = new ClientLogger(SuggestResult.class);
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
        SuggestResultHelper.setAccessor(SuggestResult::setAdditionalProperties);
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
        if (jsonSerializer == null) {
            try {
                return Utility.convertValue(additionalProperties, modelClass);
            } catch (IOException ex) {
                throw LOGGER.logExceptionAsError(new RuntimeException("Failed to deserialize suggestion result.", ex));
            }
        }
        ByteArrayOutputStream sourceStream = new ByteArrayOutputStream();
        jsonSerializer.serialize(sourceStream, additionalProperties);
        return jsonSerializer.deserialize(new ByteArrayInputStream(sourceStream.toByteArray()),
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
