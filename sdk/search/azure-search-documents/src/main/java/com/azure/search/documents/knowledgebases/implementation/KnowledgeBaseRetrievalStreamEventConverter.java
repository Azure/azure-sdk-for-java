// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.knowledgebases.implementation;

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
                return new KnowledgeBaseRetrievalStartedStreamEvent(
                    read(eventName, data, KnowledgeBaseRetrievalStartedEvent::fromJson));

            case "activity.started":
                return new KnowledgeBaseActivityStartedStreamEvent(
                    read(eventName, data, KnowledgeBaseActivityStartedEvent::fromJson));

            case "activity.completed":
                return new KnowledgeBaseActivityCompletedStreamEvent(
                    read(eventName, data, KnowledgeBaseActivityRecord::fromJson));

            case "answer.completed":
                return new KnowledgeBaseAnswerCompletedStreamEvent(
                    read(eventName, data, KnowledgeBaseAnswerCompletedEvent::fromJson));

            case "references.completed":
                return new KnowledgeBaseReferencesCompletedStreamEvent(
                    read(eventName, data, reader -> reader.readArray(KnowledgeBaseReference::fromJson)));

            case "error":
                return new KnowledgeBaseErrorStreamEvent(
                    read(eventName, data, KnowledgeBaseStreamErrorEvent::fromJson));

            case "response.completed":
                return new KnowledgeBaseResponseCompletedStreamEvent(
                    read(eventName, data, KnowledgeBaseResponseCompletedEvent::fromJson));

            default:
                return new UnrecognizedStreamEvent(eventName);
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

    private static final class UnrecognizedStreamEvent extends KnowledgeBaseRetrievalStreamEvent {
        private UnrecognizedStreamEvent(String eventName) {
            super(eventName);
        }
    }
}
