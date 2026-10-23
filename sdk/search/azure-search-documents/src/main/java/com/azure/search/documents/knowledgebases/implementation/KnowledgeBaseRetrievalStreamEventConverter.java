// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.knowledgebases.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.ReadValueCallback;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseActivityCompletedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseActivityStartedEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseActivityStartedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseAnswerCompletedEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseAnswerCompletedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseErrorStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseReferencesCompletedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseResponseCompletedEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseResponseCompletedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalStartedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalStartedEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseStreamErrorEvent;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;

/**
 * Converts knowledge base retrieval stream event payloads to typed event models.
 */
public final class KnowledgeBaseRetrievalStreamEventConverter {
    private static final ClientLogger LOGGER = new ClientLogger(KnowledgeBaseRetrievalStreamEventConverter.class);
    private static volatile ConstructorAccessor constructorAccessor;

    private KnowledgeBaseRetrievalStreamEventConverter() {
    }

    /**
     * Provides access to the SDK-only event constructors.
     */
    public interface ConstructorAccessor {
        /**
         * Creates a name-only event.
         *
         * @param eventName The event name.
         * @return The event.
         */
        KnowledgeBaseRetrievalStreamEvent createEvent(String eventName);

        /**
         * Creates a retrieval-started event.
         *
         * @param value The decoded payload.
         * @return The event.
         */
        KnowledgeBaseRetrievalStartedStreamEvent createRetrievalStarted(KnowledgeBaseRetrievalStartedEvent value);

        /**
         * Creates an activity-started event.
         *
         * @param value The decoded payload.
         * @return The event.
         */
        KnowledgeBaseActivityStartedStreamEvent createActivityStarted(KnowledgeBaseActivityStartedEvent value);

        /**
         * Creates an activity-completed event.
         *
         * @param value The decoded payload.
         * @return The event.
         */
        KnowledgeBaseActivityCompletedStreamEvent createActivityCompleted(KnowledgeBaseActivityRecord value);

        /**
         * Creates an answer-completed event.
         *
         * @param value The decoded payload.
         * @return The event.
         */
        KnowledgeBaseAnswerCompletedStreamEvent createAnswerCompleted(KnowledgeBaseAnswerCompletedEvent value);

        /**
         * Creates a references-completed event.
         *
         * @param value The decoded payload.
         * @return The event.
         */
        KnowledgeBaseReferencesCompletedStreamEvent createReferencesCompleted(List<KnowledgeBaseReference> value);

        /**
         * Creates an error event.
         *
         * @param value The decoded payload.
         * @return The event.
         */
        KnowledgeBaseErrorStreamEvent createError(KnowledgeBaseStreamErrorEvent value);

        /**
         * Creates a response-completed event.
         *
         * @param value The decoded payload.
         * @return The event.
         */
        KnowledgeBaseResponseCompletedStreamEvent createResponseCompleted(KnowledgeBaseResponseCompletedEvent value);
    }

    /**
     * Registers the event constructors from the models package.
     *
     * @param accessor The constructor accessor.
     */
    public static void setConstructorAccessor(ConstructorAccessor accessor) {
        constructorAccessor = Objects.requireNonNull(accessor, "'accessor' cannot be null.");
    }

    private static ConstructorAccessor getConstructorAccessor() {
        ConstructorAccessor accessor = constructorAccessor;
        if (accessor == null) {
            try {
                Class.forName(KnowledgeBaseRetrievalStreamEvent.class.getName(), true,
                    KnowledgeBaseRetrievalStreamEvent.class.getClassLoader());
            } catch (ClassNotFoundException | LinkageError exception) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "Failed to initialize knowledge base retrieval stream event constructors.", exception));
            }
            accessor = constructorAccessor;
            if (accessor == null) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "Knowledge base retrieval stream event constructors were not registered."));
            }
        }
        return accessor;
    }

    /**
     * Converts a stream event payload.
     *
     * @param eventName The stream event name.
     * @param data The stream event data.
     * @return The typed stream event.
     */
    public static KnowledgeBaseRetrievalStreamEvent convert(String eventName, String data) {
        ConstructorAccessor accessor = getConstructorAccessor();
        switch (eventName) {
            case "retrieval.started":
                return accessor
                    .createRetrievalStarted(read(eventName, data, KnowledgeBaseRetrievalStartedEvent::fromJson));

            case "activity.started":
                return accessor
                    .createActivityStarted(read(eventName, data, KnowledgeBaseActivityStartedEvent::fromJson));

            case "activity.completed":
                return accessor.createActivityCompleted(read(eventName, data, KnowledgeBaseActivityRecord::fromJson));

            case "answer.completed":
                return accessor
                    .createAnswerCompleted(read(eventName, data, KnowledgeBaseAnswerCompletedEvent::fromJson));

            case "references.completed":
                return accessor.createReferencesCompleted(
                    read(eventName, data, reader -> reader.readArray(KnowledgeBaseReference::fromJson)));

            case "error":
                return accessor.createError(read(eventName, data, KnowledgeBaseStreamErrorEvent::fromJson));

            case "response.completed":
                return accessor
                    .createResponseCompleted(read(eventName, data, KnowledgeBaseResponseCompletedEvent::fromJson));

            default:
                return accessor.createEvent(eventName);
        }
    }

    private static <T> T read(String eventName, String data, ReadValueCallback<JsonReader, T> eventReader) {
        try (JsonReader reader = JsonProviders.createReader(data)) {
            return eventReader.read(reader);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to decode knowledge base retrieval stream event: " + eventName,
                exception);
        }
    }

}
