// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.knowledgebases.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Represents the {@code answer.completed} knowledge base retrieval stream event.
 */
public final class KnowledgeBaseAnswerCompletedStreamEvent extends KnowledgeBaseRetrievalStreamEvent
    implements JsonSerializable<KnowledgeBaseAnswerCompletedStreamEvent> {
    private final KnowledgeBaseAnswerCompletedEvent value;

    /**
     * Creates an event wrapper.
     *
     * @param value The event payload.
     */
    public KnowledgeBaseAnswerCompletedStreamEvent(KnowledgeBaseAnswerCompletedEvent value) {
        super("answer.completed");
        this.value = value;
    }

    /**
     * Gets the event payload.
     *
     * @return The event payload.
     */
    public KnowledgeBaseAnswerCompletedEvent getValue() {
        return value;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return value.toJson(jsonWriter);
    }

    /**
     * Reads an event wrapper from JSON.
     *
     * @param jsonReader The reader to read from.
     * @return The parsed event wrapper.
     * @throws IOException If the event payload cannot be read.
     */
    public static KnowledgeBaseAnswerCompletedStreamEvent fromJson(JsonReader jsonReader) throws IOException {
        return new KnowledgeBaseAnswerCompletedStreamEvent(KnowledgeBaseAnswerCompletedEvent.fromJson(jsonReader));
    }
}
