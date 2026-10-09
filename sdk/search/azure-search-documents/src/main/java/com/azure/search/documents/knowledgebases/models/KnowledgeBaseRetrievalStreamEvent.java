// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.knowledgebases.models;

import com.azure.core.annotation.Generated;

/**
 * Abstract base for polymorphic events emitted by a streaming knowledge base retrieval.
 * Known events expose typed payloads through their subtype's {@code getValue()} method.
 * Use {@link #getEventName()} and {@link #getRawValue()} for unrecognized events.
 * Stream events are envelopes, not JSON payload models; serialize the typed payload instead.
 *
 * @see KnowledgeBaseRetrievalStartedStreamEvent
 * @see KnowledgeBaseActivityStartedStreamEvent
 * @see KnowledgeBaseActivityCompletedStreamEvent
 * @see KnowledgeBaseAnswerCompletedStreamEvent
 * @see KnowledgeBaseReferencesCompletedStreamEvent
 * @see KnowledgeBaseErrorStreamEvent
 * @see KnowledgeBaseResponseCompletedStreamEvent
 */
public abstract class KnowledgeBaseRetrievalStreamEvent {

    @Generated
    private final String eventName;

    /**
     * Creates a stream event.
     *
     * @param eventName The server-sent event name.
     */
    @Generated
    protected KnowledgeBaseRetrievalStreamEvent(String eventName) {
        this(eventName, null);
    }

    /**
     * Gets the server-sent event name.
     *
     * @return The event name.
     */
    @Generated
    public final String getEventName() {
        return eventName;
    }

    /**
     * Gets whether this event terminates the retrieval stream.
     *
     * @return {@code true} if this is a terminal event; otherwise {@code false}.
     */
    @Generated
    public boolean isTerminal() {
        return false;
    }

    @Generated
    private final String rawValue;

    /**
     * Creates a stream event with its original decoded SSE data.
     *
     * @param eventName The server-sent event name.
     * @param rawValue The original decoded SSE data, or null if none was supplied.
     */
    @Generated
    protected KnowledgeBaseRetrievalStreamEvent(String eventName, String rawValue) {
        this.eventName = eventName;
        this.rawValue = rawValue;
    }

    /**
     * Gets the original decoded SSE data, with multiline data joined by newlines.
     * This is not the complete wire frame. Supplied data, including an empty string, is preserved.
     * Known subtypes with no supplied data lazily serialize their current typed payload as JSON.
     * That generated JSON need not match an original wire representation.
     *
     * @return The supplied data, generated payload JSON, or null if neither data nor payload is present.
     * @throws java.io.UncheckedIOException If payload serialization fails.
     */
    @Generated
    public String getRawValue() {
        return rawValue;
    }
}
