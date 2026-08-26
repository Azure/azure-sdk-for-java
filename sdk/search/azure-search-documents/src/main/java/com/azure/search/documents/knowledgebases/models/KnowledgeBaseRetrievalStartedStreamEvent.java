// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.knowledgebases.models;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Represents the {@code retrieval.started} knowledge base retrieval stream event.
 */
@Immutable
public final class KnowledgeBaseRetrievalStartedStreamEvent extends KnowledgeBaseRetrievalStreamEvent
    implements JsonSerializable<KnowledgeBaseRetrievalStartedStreamEvent> {

    @Generated
    private final KnowledgeBaseRetrievalStartedEvent value;

    /**
     * Creates an event wrapper.
     *
     * @param value The event payload.
     */
    @Generated
    public KnowledgeBaseRetrievalStartedStreamEvent(KnowledgeBaseRetrievalStartedEvent value) {
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

    @Generated
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
    @Generated
    public static KnowledgeBaseRetrievalStartedStreamEvent fromJson(JsonReader jsonReader) throws IOException {
        return new KnowledgeBaseRetrievalStartedStreamEvent(KnowledgeBaseRetrievalStartedEvent.fromJson(jsonReader));
    }
}
