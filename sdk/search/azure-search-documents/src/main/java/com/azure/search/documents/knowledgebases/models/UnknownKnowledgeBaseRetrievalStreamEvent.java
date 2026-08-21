// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.knowledgebases.models;

/**
 * Represents a knowledge base retrieval stream event that is not recognized by this SDK version.
 */
public final class UnknownKnowledgeBaseRetrievalStreamEvent extends KnowledgeBaseRetrievalStreamEvent {
    private final String data;

    /**
     * Creates an unknown stream event.
     *
     * @param eventName The server-sent event name.
     * @param data The raw server-sent event data.
     */
    public UnknownKnowledgeBaseRetrievalStreamEvent(String eventName, String data) {
        super(eventName);
        this.data = data;
    }

    /**
     * Gets the raw server-sent event data.
     *
     * @return The raw event data.
     */
    public String getData() {
        return data;
    }
}
