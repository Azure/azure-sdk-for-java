// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * The ResponsesWebSearchCallItem model.
 */
@Immutable
public final class ResponsesWebSearchCallItem extends ResponsesItem {

    /*
     * The type property.
     */
    @Generated
    private ResponsesItemType type = ResponsesItemType.WEB_SEARCH_CALL;

    /*
     * The status property.
     */
    @Generated
    private ResponsesWebSearchCallItemStatus status;

    /**
     * Creates an instance of ResponsesWebSearchCallItem class.
     */
    @Generated
    public ResponsesWebSearchCallItem() {
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
    public ResponsesWebSearchCallItemStatus getStatus() {
        return this.status;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("type", this.type == null ? null : this.type.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ResponsesWebSearchCallItem from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesWebSearchCallItem if the JsonReader was pointing to an instance of it, or null if
     * it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ResponsesWebSearchCallItem.
     */
    @Generated
    public static ResponsesWebSearchCallItem fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ResponsesWebSearchCallItem deserializedResponsesWebSearchCallItem = new ResponsesWebSearchCallItem();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("id".equals(fieldName)) {
                    deserializedResponsesWebSearchCallItem.setId(reader.getString());
                } else if ("status".equals(fieldName)) {
                    deserializedResponsesWebSearchCallItem.status
                        = ResponsesWebSearchCallItemStatus.fromString(reader.getString());
                } else if ("type".equals(fieldName)) {
                    deserializedResponsesWebSearchCallItem.type = ResponsesItemType.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedResponsesWebSearchCallItem;
        });
    }
}
