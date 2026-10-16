// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.knowledgebases.models;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.Immutable;

/**
 * Represents the {@code retrieval.started} knowledge base retrieval stream event.
 * Instances are created by the SDK, not by applications.
 * Access the typed payload with {@link #getValue()}.
 *
 * @see KnowledgeBaseRetrievalStreamEvent
 */
@Immutable
public final class KnowledgeBaseRetrievalStartedStreamEvent extends KnowledgeBaseRetrievalStreamEvent {

    @Generated
    private final KnowledgeBaseRetrievalStartedEvent value;

    /**
     * Creates an event wrapper.
     *
     * @param value The event payload.
     */
    @Generated
    KnowledgeBaseRetrievalStartedStreamEvent(KnowledgeBaseRetrievalStartedEvent value) {
        super("retrieval.started");
        this.value = value;
    }

    /**
     * Gets the event payload.
     *
     * @return The event payload.
     */
    @Generated
    public KnowledgeBaseRetrievalStartedEvent getValue() {
        return value;
    }
}
