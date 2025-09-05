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
 * The ResponsesComputerCallScrollAction model.
 */
@Immutable
public final class ResponsesComputerCallScrollAction extends ResponsesComputerCallItemAction {

    /*
     * The type property.
     */
    @Generated
    private ResponsesComputerCallActionType type = ResponsesComputerCallActionType.SCROLL;

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

    /*
     * The scroll_x property.
     */
    @Generated
    private final int scrollX;

    /*
     * The scroll_y property.
     */
    @Generated
    private final int scrollY;

    /**
     * Creates an instance of ResponsesComputerCallScrollAction class.
     *
     * @param x the x value to set.
     * @param y the y value to set.
     * @param scrollX the scrollX value to set.
     * @param scrollY the scrollY value to set.
     */
    @Generated
    public ResponsesComputerCallScrollAction(int x, int y, int scrollX, int scrollY) {
        this.x = x;
        this.y = y;
        this.scrollX = scrollX;
        this.scrollY = scrollY;
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
     * Get the scrollX property: The scroll_x property.
     *
     * @return the scrollX value.
     */
    @Generated
    public int getScrollX() {
        return this.scrollX;
    }

    /**
     * Get the scrollY property: The scroll_y property.
     *
     * @return the scrollY value.
     */
    @Generated
    public int getScrollY() {
        return this.scrollY;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeIntField("x", this.x);
        jsonWriter.writeIntField("y", this.y);
        jsonWriter.writeIntField("scroll_x", this.scrollX);
        jsonWriter.writeIntField("scroll_y", this.scrollY);
        jsonWriter.writeStringField("type", this.type == null ? null : this.type.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ResponsesComputerCallScrollAction from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesComputerCallScrollAction if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ResponsesComputerCallScrollAction.
     */
    @Generated
    public static ResponsesComputerCallScrollAction fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            int x = 0;
            int y = 0;
            int scrollX = 0;
            int scrollY = 0;
            ResponsesComputerCallActionType type = ResponsesComputerCallActionType.SCROLL;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("x".equals(fieldName)) {
                    x = reader.getInt();
                } else if ("y".equals(fieldName)) {
                    y = reader.getInt();
                } else if ("scroll_x".equals(fieldName)) {
                    scrollX = reader.getInt();
                } else if ("scroll_y".equals(fieldName)) {
                    scrollY = reader.getInt();
                } else if ("type".equals(fieldName)) {
                    type = ResponsesComputerCallActionType.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            ResponsesComputerCallScrollAction deserializedResponsesComputerCallScrollAction
                = new ResponsesComputerCallScrollAction(x, y, scrollX, scrollY);
            deserializedResponsesComputerCallScrollAction.type = type;
            return deserializedResponsesComputerCallScrollAction;
        });
    }
}
