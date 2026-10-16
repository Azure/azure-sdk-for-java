// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.knowledgebases.models;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.Immutable;

/**
 * Represents the {@code error} knowledge base retrieval stream event.
 * Instances are created by the SDK, not by applications.
 * Access the typed payload with {@link #getValue()}.
 *
 * @see KnowledgeBaseRetrievalStreamEvent
 */
@Immutable
public final class KnowledgeBaseErrorStreamEvent extends KnowledgeBaseRetrievalStreamEvent {

    @Generated
    private final KnowledgeBaseStreamErrorEvent value;

    /**
     * Creates an event wrapper.
     *
     * @param value The event payload.
     */
    @Generated
    KnowledgeBaseErrorStreamEvent(KnowledgeBaseStreamErrorEvent value) {
        super("error");
        this.value = value;
    }

    /**
     * Gets the event payload.
     *
     * @return The event payload.
     */
    @Generated
    public KnowledgeBaseStreamErrorEvent getValue() {
        return value;
    }

    @Generated
    @Override
    public boolean isTerminal() {
        return true;
    }
}
