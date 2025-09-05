// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * The ResponsesComputerCallDragActionPath model.
 */
@Immutable
public final class ResponsesComputerCallDragActionPath
    implements JsonSerializable<ResponsesComputerCallDragActionPath> {

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
     * Creates an instance of ResponsesComputerCallDragActionPath class.
     *
     * @param x the x value to set.
     * @param y the y value to set.
     */
    @Generated
    public ResponsesComputerCallDragActionPath(int x, int y) {
        this.x = x;
        this.y = y;
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
        jsonWriter.writeIntField("x", this.x);
        jsonWriter.writeIntField("y", this.y);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ResponsesComputerCallDragActionPath from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesComputerCallDragActionPath if the JsonReader was pointing to an instance of it,
     * or null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ResponsesComputerCallDragActionPath.
     */
    @Generated
    public static ResponsesComputerCallDragActionPath fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            int x = 0;
            int y = 0;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("x".equals(fieldName)) {
                    x = reader.getInt();
                } else if ("y".equals(fieldName)) {
                    y = reader.getInt();
                } else {
                    reader.skipChildren();
                }
            }
            return new ResponsesComputerCallDragActionPath(x, y);
        });
    }
}
