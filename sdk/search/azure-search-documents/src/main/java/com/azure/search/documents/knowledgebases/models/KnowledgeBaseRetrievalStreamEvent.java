// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.knowledgebases.models;

import com.azure.core.annotation.Generated;
import com.azure.search.documents.knowledgebases.implementation.KnowledgeBaseRetrievalStreamEventConverter;
import java.util.List;

/**
 * Base for polymorphic events emitted by a streaming knowledge base retrieval.
 * Known events expose typed payloads through their subtype's {@code getValue()} method.
 * Unrecognized events expose only their names through
 * {@link #getEventName()}; their payloads are not available and they are not terminal.
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
    KnowledgeBaseRetrievalStreamEvent(String eventName) {
        this.eventName = eventName;
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

    static {
        KnowledgeBaseRetrievalStreamEventConverter
            .setConstructorAccessor(new KnowledgeBaseRetrievalStreamEventConverter.ConstructorAccessor() {

                @Override
                public KnowledgeBaseRetrievalStreamEvent createEvent(String eventName) {
                    return new KnowledgeBaseRetrievalStreamEvent(eventName) {
                    };
                }

                @Override
                public KnowledgeBaseRetrievalStartedStreamEvent
                    createRetrievalStarted(KnowledgeBaseRetrievalStartedEvent value) {
                    return new KnowledgeBaseRetrievalStartedStreamEvent(value);
                }

                @Override
                public KnowledgeBaseActivityStartedStreamEvent
                    createActivityStarted(KnowledgeBaseActivityStartedEvent value) {
                    return new KnowledgeBaseActivityStartedStreamEvent(value);
                }

                @Override
                public KnowledgeBaseActivityCompletedStreamEvent
                    createActivityCompleted(KnowledgeBaseActivityRecord value) {
                    return new KnowledgeBaseActivityCompletedStreamEvent(value);
                }

                @Override
                public KnowledgeBaseAnswerCompletedStreamEvent
                    createAnswerCompleted(KnowledgeBaseAnswerCompletedEvent value) {
                    return new KnowledgeBaseAnswerCompletedStreamEvent(value);
                }

                @Override
                public KnowledgeBaseReferencesCompletedStreamEvent
                    createReferencesCompleted(List<KnowledgeBaseReference> value) {
                    return new KnowledgeBaseReferencesCompletedStreamEvent(value);
                }

                @Override
                public KnowledgeBaseErrorStreamEvent createError(KnowledgeBaseStreamErrorEvent value) {
                    return new KnowledgeBaseErrorStreamEvent(value);
                }

                @Override
                public KnowledgeBaseResponseCompletedStreamEvent
                    createResponseCompleted(KnowledgeBaseResponseCompletedEvent value) {
                    return new KnowledgeBaseResponseCompletedStreamEvent(value);
                }
            });
    }
}
