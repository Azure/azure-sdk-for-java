// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;

/**
 * The ResponsesReasoningItem model.
 */
@Immutable
public final class ResponsesReasoningItem extends ResponsesItem {

    /*
     * The type property.
     */
    @Generated
    private ResponsesItemType type = ResponsesItemType.REASONING;

    /*
     * The status property.
     */
    @Generated
    private ResponsesReasoningItemStatus status;

    /*
     * The summary property.
     */
    @Generated
    private final List<ResponsesReasoningItemSummaryElement> summary;

    /**
     * Creates an instance of ResponsesReasoningItem class.
     *
     * @param summary the summary value to set.
     */
    @Generated
    public ResponsesReasoningItem(List<ResponsesReasoningItemSummaryElement> summary) {
        this.summary = summary;
    }

    /**
     * Get the type property: The type property.
     *
     * @return the type value.
     */
    @Generated
    @Override
    public ResponsesItemType getType() {
        return this.type;
    }

    /**
     * Get the status property: The status property.
     *
     * @return the status value.
     */
    @Generated
    public ResponsesReasoningItemStatus getStatus() {
        return this.status;
    }

    /**
     * Get the summary property: The summary property.
     *
     * @return the summary value.
     */
    @Generated
    public List<ResponsesReasoningItemSummaryElement> getSummary() {
        return this.summary;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeArrayField("summary", this.summary, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeStringField("type", this.type == null ? null : this.type.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ResponsesReasoningItem from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesReasoningItem if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ResponsesReasoningItem.
     */
    @Generated
    public static ResponsesReasoningItem fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String id = null;
            List<ResponsesReasoningItemSummaryElement> summary = null;
            ResponsesItemType type = ResponsesItemType.REASONING;
            ResponsesReasoningItemStatus status = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("id".equals(fieldName)) {
                    id = reader.getString();
                } else if ("summary".equals(fieldName)) {
                    summary = reader.readArray(reader1 -> ResponsesReasoningItemSummaryElement.fromJson(reader1));
                } else if ("type".equals(fieldName)) {
                    type = ResponsesItemType.fromString(reader.getString());
                } else if ("status".equals(fieldName)) {
                    status = ResponsesReasoningItemStatus.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            ResponsesReasoningItem deserializedResponsesReasoningItem = new ResponsesReasoningItem(summary);
            deserializedResponsesReasoningItem.setId(id);
            deserializedResponsesReasoningItem.type = type;
            deserializedResponsesReasoningItem.status = status;
            return deserializedResponsesReasoningItem;
        });
    }
}
