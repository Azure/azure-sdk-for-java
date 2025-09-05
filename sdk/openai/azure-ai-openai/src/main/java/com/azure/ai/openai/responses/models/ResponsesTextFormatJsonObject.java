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
 * The ResponsesTextFormatJsonObject model.
 */
@Immutable
public final class ResponsesTextFormatJsonObject extends ResponsesTextFormat {

    /*
     * The type property.
     */
    @Generated
    private ResponsesTextFormatType type = ResponsesTextFormatType.JSON_OBJECT;

    /**
     * Creates an instance of ResponsesTextFormatJsonObject class.
     */
    @Generated
    public ResponsesTextFormatJsonObject() {
    }

    /**
     * Get the type property: The type property.
     *
     * @return the type value.
     */
    @Generated
    @Override
    public ResponsesTextFormatType getType() {
        return this.type;
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
     * Reads an instance of ResponsesTextFormatJsonObject from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesTextFormatJsonObject if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ResponsesTextFormatJsonObject.
     */
    @Generated
    public static ResponsesTextFormatJsonObject fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ResponsesTextFormatJsonObject deserializedResponsesTextFormatJsonObject
                = new ResponsesTextFormatJsonObject();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("type".equals(fieldName)) {
                    deserializedResponsesTextFormatJsonObject.type
                        = ResponsesTextFormatType.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedResponsesTextFormatJsonObject;
        });
    }
}
