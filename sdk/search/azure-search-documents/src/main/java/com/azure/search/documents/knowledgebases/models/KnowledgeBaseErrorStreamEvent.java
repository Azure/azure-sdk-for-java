// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.knowledgebases.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Represents the {@code error} knowledge base retrieval stream event.
 */
public final class KnowledgeBaseErrorStreamEvent extends KnowledgeBaseRetrievalStreamEvent
    implements JsonSerializable<KnowledgeBaseErrorStreamEvent> {
    private final KnowledgeBaseStreamErrorEvent value;

    /**
     * Creates an event wrapper.
     *
     * @param value The event payload.
     */
    public KnowledgeBaseErrorStreamEvent(KnowledgeBaseStreamErrorEvent value) {
        super("error");
        this.value = value;
    }

    /**
     * Gets the event payload.
     *
     * @return The event payload.
     */
    public KnowledgeBaseStreamErrorEvent getValue() {
        return value;
    }

    @Override
    public boolean isTerminal() {
        return true;
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
    public static KnowledgeBaseErrorStreamEvent fromJson(JsonReader jsonReader) throws IOException {
        return new KnowledgeBaseErrorStreamEvent(KnowledgeBaseStreamErrorEvent.fromJson(jsonReader));
    }
}
