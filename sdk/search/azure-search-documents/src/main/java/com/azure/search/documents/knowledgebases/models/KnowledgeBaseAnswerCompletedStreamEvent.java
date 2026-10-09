// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.knowledgebases.models;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Represents the {@code answer.completed} knowledge base retrieval stream event.
 * Access the typed payload with {@link #getValue()} or the decoded SSE data with {@link #getRawValue()}.
 *
 * @see KnowledgeBaseRetrievalStreamEvent
 */
@Immutable
public final class KnowledgeBaseAnswerCompletedStreamEvent extends KnowledgeBaseRetrievalStreamEvent {

    @Generated
    private final KnowledgeBaseAnswerCompletedEvent value;

    /**
     * Creates an event wrapper.
     *
     * @param value The event payload.
     */
    @Generated
    public KnowledgeBaseAnswerCompletedStreamEvent(KnowledgeBaseAnswerCompletedEvent value) {
        this(value, null);
    }

    /**
     * Gets the event payload.
     *
     * @return The event payload.
     */
    @Generated
    public KnowledgeBaseAnswerCompletedEvent getValue() {
        return value;
    }

    /**
     * Creates an event wrapper with its original decoded SSE data.
     *
     * @param value The event payload.
     * @param rawValue The original decoded SSE data, or null to serialize the payload lazily.
     */
    @Generated
    public KnowledgeBaseAnswerCompletedStreamEvent(KnowledgeBaseAnswerCompletedEvent value, String rawValue) {
        super("answer.completed", rawValue);
        this.value = value;
    }

    /**
     * Gets the original decoded SSE data, or lazily generated JSON for the current payload.
     * Supplied data, including an empty string, always takes precedence over the typed payload.
     * Generated JSON is not cached and need not match an original wire representation.
     *
     * @return The supplied data, generated payload JSON, or null if neither data nor payload is present.
     * @throws UncheckedIOException If payload serialization fails.
     */
    @Generated
    @Override
    public String getRawValue() {
        String rawValue = super.getRawValue();
        if (rawValue != null || value == null) {
            return rawValue;
        }
        try {
            return value.toJsonString();
        } catch (IOException exception) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(
                "Failed to serialize knowledge base retrieval stream event: answer.completed", exception));
        }
    }

    @Generated
    private static final ClientLogger LOGGER = new ClientLogger(KnowledgeBaseAnswerCompletedStreamEvent.class);
}
