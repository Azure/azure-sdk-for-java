// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.knowledgebases.models;

/**
 * Base type for events emitted by a streaming knowledge base retrieval.
 */
public abstract class KnowledgeBaseRetrievalStreamEvent {
    private final String eventName;

    /**
     * Creates a stream event.
     *
     * @param eventName The server-sent event name.
     */
    protected KnowledgeBaseRetrievalStreamEvent(String eventName) {
        this.eventName = eventName;
    }

    /**
     * Gets the server-sent event name.
     *
     * @return The event name.
     */
    public final String getEventName() {
        return eventName;
    }

    /**
     * Gets whether this event terminates the retrieval stream.
     *
     * @return {@code true} if this is a terminal event; otherwise {@code false}.
     */
    public boolean isTerminal() {
        return false;
    }
}
