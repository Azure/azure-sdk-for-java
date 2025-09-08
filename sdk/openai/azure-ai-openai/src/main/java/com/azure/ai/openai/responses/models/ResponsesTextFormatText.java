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
 * The ResponsesTextFormatText model.
 */
@Immutable
public final class ResponsesTextFormatText extends ResponsesTextFormat {

    /*
     * The type property.
     */
    @Generated
    private ResponsesTextFormatType type = ResponsesTextFormatType.TEXT;

    /**
     * Creates an instance of ResponsesTextFormatText class.
     */
    @Generated
    public ResponsesTextFormatText() {
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
     * Reads an instance of ResponsesTextFormatText from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesTextFormatText if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ResponsesTextFormatText.
     */
    @Generated
    public static ResponsesTextFormatText fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ResponsesTextFormatText deserializedResponsesTextFormatText = new ResponsesTextFormatText();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("type".equals(fieldName)) {
                    deserializedResponsesTextFormatText.type = ResponsesTextFormatType.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedResponsesTextFormatText;
        });
    }
}
