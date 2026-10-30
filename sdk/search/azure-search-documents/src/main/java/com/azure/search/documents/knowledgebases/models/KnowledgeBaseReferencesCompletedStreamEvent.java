// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.knowledgebases.models;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;

/**
 * Represents the {@code references.completed} knowledge base retrieval stream event.
 */
@Immutable
public final class KnowledgeBaseReferencesCompletedStreamEvent extends KnowledgeBaseRetrievalStreamEvent
    implements JsonSerializable<KnowledgeBaseReferencesCompletedStreamEvent> {

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

    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeArray(value, (writer, item) -> item.toJson(writer));
    }

    /**
     * Reads an event wrapper from JSON.
     *
     * @param jsonReader The reader to read from.
     * @return The parsed event wrapper.
     * @throws IOException If the event payload cannot be read.
     */
    @Generated
    public static KnowledgeBaseReferencesCompletedStreamEvent fromJson(JsonReader jsonReader) throws IOException {
        return new KnowledgeBaseReferencesCompletedStreamEvent(
            jsonReader.readArray(reader -> KnowledgeBaseReference.fromJson(reader)));
    }
}
