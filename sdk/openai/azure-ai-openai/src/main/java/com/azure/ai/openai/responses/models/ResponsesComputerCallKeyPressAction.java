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
 * The ResponsesComputerCallKeyPressAction model.
 */
@Immutable
public final class ResponsesComputerCallKeyPressAction extends ResponsesComputerCallItemAction {

    /*
     * The type property.
     */
    @Generated
    private ResponsesComputerCallActionType type = ResponsesComputerCallActionType.KEYPRESS;

    /*
     * The keys property.
     */
    @Generated
    private final List<String> keys;

    /**
     * Creates an instance of ResponsesComputerCallKeyPressAction class.
     *
     * @param keys the keys value to set.
     */
    @Generated
    public ResponsesComputerCallKeyPressAction(List<String> keys) {
        this.keys = keys;
    }

    /**
     * Get the type property: The type property.
     *
     * @return the type value.
     */
    @Generated
    @Override
    public ResponsesComputerCallActionType getType() {
        return this.type;
    }

    /**
     * Get the keys property: The keys property.
     *
     * @return the keys value.
     */
    @Generated
    public List<String> getKeys() {
        return this.keys;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeArrayField("keys", this.keys, (writer, element) -> writer.writeString(element));
        jsonWriter.writeStringField("type", this.type == null ? null : this.type.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ResponsesComputerCallKeyPressAction from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesComputerCallKeyPressAction if the JsonReader was pointing to an instance of it,
     * or null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ResponsesComputerCallKeyPressAction.
     */
    @Generated
    public static ResponsesComputerCallKeyPressAction fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            List<String> keys = null;
            ResponsesComputerCallActionType type = ResponsesComputerCallActionType.KEYPRESS;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("keys".equals(fieldName)) {
                    keys = reader.readArray(reader1 -> reader1.getString());
                } else if ("type".equals(fieldName)) {
                    type = ResponsesComputerCallActionType.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            ResponsesComputerCallKeyPressAction deserializedResponsesComputerCallKeyPressAction
                = new ResponsesComputerCallKeyPressAction(keys);
            deserializedResponsesComputerCallKeyPressAction.type = type;
            return deserializedResponsesComputerCallKeyPressAction;
        });
    }
}
