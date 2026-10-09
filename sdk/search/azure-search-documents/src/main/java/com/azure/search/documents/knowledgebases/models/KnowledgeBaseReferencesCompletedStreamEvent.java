// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.knowledgebases.models;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.Immutable;
import java.util.List;

/**
 * Represents the {@code references.completed} knowledge base retrieval stream event.
 * Access the typed payload with {@link #getValue()}.
 *
 * @see KnowledgeBaseRetrievalStreamEvent
 */
@Immutable
public final class KnowledgeBaseReferencesCompletedStreamEvent extends KnowledgeBaseRetrievalStreamEvent {

    @Generated
    private final List<KnowledgeBaseReference> value;

    /**
     * Creates an event wrapper.
     *
     * @param value The event payload.
     */
    @Generated
    public KnowledgeBaseReferencesCompletedStreamEvent(List<KnowledgeBaseReference> value) {
        super("references.completed");
        this.value = value;
    }

    /**
     * Gets the event payload.
     *
     * @return The event payload.
     */
    @Generated
    public List<KnowledgeBaseReference> getValue() {
        return value;
    }
}
