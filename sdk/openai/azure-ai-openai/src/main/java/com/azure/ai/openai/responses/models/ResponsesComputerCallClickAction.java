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
 * The ResponsesComputerCallClickAction model.
 */
@Immutable
public final class ResponsesComputerCallClickAction extends ResponsesComputerCallItemAction {

    /*
     * The type property.
     */
    @Generated
    private ResponsesComputerCallActionType type = ResponsesComputerCallActionType.CLICK;

    /*
     * The button property.
     */
    @Generated
    private final ResponsesComputerCallClickButtonType button;

    /*
     * The x property.
     */
    @Generated
    private final int x;

    /*
     * The y property.
     */
    @Generated
    private final int y;

    /**
     * Creates an instance of ResponsesComputerCallClickAction class.
     *
     * @param button the button value to set.
     * @param x the x value to set.
     * @param y the y value to set.
     */
    @Generated
    public ResponsesComputerCallClickAction(ResponsesComputerCallClickButtonType button, int x, int y) {
        this.button = button;
        this.x = x;
        this.y = y;
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
     * Get the button property: The button property.
     *
     * @return the button value.
     */
    @Generated
    public ResponsesComputerCallClickButtonType getButton() {
        return this.button;
    }

    /**
     * Get the x property: The x property.
     *
     * @return the x value.
     */
    @Generated
    public int getX() {
        return this.x;
    }

    /**
     * Get the y property: The y property.
     *
     * @return the y value.
     */
    @Generated
    public int getY() {
        return this.y;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("button", this.button == null ? null : this.button.toString());
        jsonWriter.writeIntField("x", this.x);
        jsonWriter.writeIntField("y", this.y);
        jsonWriter.writeStringField("type", this.type == null ? null : this.type.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ResponsesComputerCallClickAction from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesComputerCallClickAction if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ResponsesComputerCallClickAction.
     */
    @Generated
    public static ResponsesComputerCallClickAction fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ResponsesComputerCallClickButtonType button = null;
            int x = 0;
            int y = 0;
            ResponsesComputerCallActionType type = ResponsesComputerCallActionType.CLICK;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("button".equals(fieldName)) {
                    button = ResponsesComputerCallClickButtonType.fromString(reader.getString());
                } else if ("x".equals(fieldName)) {
                    x = reader.getInt();
                } else if ("y".equals(fieldName)) {
                    y = reader.getInt();
                } else if ("type".equals(fieldName)) {
                    type = ResponsesComputerCallActionType.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            ResponsesComputerCallClickAction deserializedResponsesComputerCallClickAction
                = new ResponsesComputerCallClickAction(button, x, y);
            deserializedResponsesComputerCallClickAction.type = type;
            return deserializedResponsesComputerCallClickAction;
        });
    }
}
