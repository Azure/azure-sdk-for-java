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
 * The ResponsesComputerCallDragAction model.
 */
@Immutable
public final class ResponsesComputerCallDragAction extends ResponsesComputerCallItemAction {

    /*
     * The type property.
     */
    @Generated
    private ResponsesComputerCallActionType type = ResponsesComputerCallActionType.DRAG;

    /*
     * The path property.
     */
    @Generated
    private final List<ResponsesComputerCallDragActionPath> path;

    /**
     * Creates an instance of ResponsesComputerCallDragAction class.
     *
     * @param path the path value to set.
     */
    @Generated
    public ResponsesComputerCallDragAction(List<ResponsesComputerCallDragActionPath> path) {
        this.path = path;
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
     * Get the path property: The path property.
     *
     * @return the path value.
     */
    @Generated
    public List<ResponsesComputerCallDragActionPath> getPath() {
        return this.path;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeArrayField("path", this.path, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeStringField("type", this.type == null ? null : this.type.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ResponsesComputerCallDragAction from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesComputerCallDragAction if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ResponsesComputerCallDragAction.
     */
    @Generated
    public static ResponsesComputerCallDragAction fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            List<ResponsesComputerCallDragActionPath> path = null;
            ResponsesComputerCallActionType type = ResponsesComputerCallActionType.DRAG;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("path".equals(fieldName)) {
                    path = reader.readArray(reader1 -> ResponsesComputerCallDragActionPath.fromJson(reader1));
                } else if ("type".equals(fieldName)) {
                    type = ResponsesComputerCallActionType.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            ResponsesComputerCallDragAction deserializedResponsesComputerCallDragAction
                = new ResponsesComputerCallDragAction(path);
            deserializedResponsesComputerCallDragAction.type = type;
            return deserializedResponsesComputerCallDragAction;
        });
    }
}
