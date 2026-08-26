// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.knowledgebases.implementation;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseActivityCompletedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseActivityStartedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseAnswerCompletedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseErrorStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseReferencesCompletedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseResponseCompletedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalStartedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalStreamEvent;
import com.azure.search.documents.knowledgebases.models.UnknownKnowledgeBaseRetrievalStreamEvent;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Converts knowledge base retrieval stream event payloads to typed event models.
 */
public final class KnowledgeBaseRetrievalStreamEventConverter {
    private KnowledgeBaseRetrievalStreamEventConverter() {
    }

    /**
     * Converts a stream event payload.
     *
     * @param eventName The stream event name.
     * @param data The stream event data.
     * @return The typed stream event.
     */
    public static KnowledgeBaseRetrievalStreamEvent convert(String eventName, String data) {
        switch (eventName) {
            case "retrieval.started":
                return read(eventName, data, KnowledgeBaseRetrievalStartedStreamEvent::fromJson);

            case "activity.started":
                return read(eventName, data, KnowledgeBaseActivityStartedStreamEvent::fromJson);

            case "activity.completed":
                return read(eventName, data, KnowledgeBaseActivityCompletedStreamEvent::fromJson);

            case "answer.completed":
                return read(eventName, data, KnowledgeBaseAnswerCompletedStreamEvent::fromJson);

            case "references.completed":
                return read(eventName, data, KnowledgeBaseReferencesCompletedStreamEvent::fromJson);

            case "error":
                return read(eventName, data, KnowledgeBaseErrorStreamEvent::fromJson);

            case "response.completed":
                return read(eventName, data, KnowledgeBaseResponseCompletedStreamEvent::fromJson);

            default:
                return new UnknownKnowledgeBaseRetrievalStreamEvent(eventName, data);
        }
    }

    private static KnowledgeBaseRetrievalStreamEvent read(String eventName, String data, ReadValueCallback<KnowledgeBaseRetrievalStreamEvent, JsonReader> eventReader) {
        try (JsonReader reader = JsonProviders.createReader(data)) {
            return eventReader.read(reader);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to decode knowledge base retrieval stream event: " + eventName,
                exception);
        }
    }

    @FunctionalInterface
    private interface EventReader {
        KnowledgeBaseRetrievalStreamEvent read(JsonReader reader) throws IOException;
    }
}
