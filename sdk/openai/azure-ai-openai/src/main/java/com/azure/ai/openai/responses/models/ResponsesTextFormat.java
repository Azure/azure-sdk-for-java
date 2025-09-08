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
 * The ResponsesTextFormat model.
 */
@Immutable
public class ResponsesTextFormat implements JsonSerializable<ResponsesTextFormat> {

    /*
     * The type property.
     */
    @Generated
    private ResponsesTextFormatType type;

    /**
     * Creates an instance of ResponsesTextFormat class.
     */
    @Generated
    public ResponsesTextFormat() {
    }

    /**
     * Get the type property: The type property.
     *
     * @return the type value.
     */
    @Generated
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
     * Reads an instance of ResponsesTextFormat from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesTextFormat if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the ResponsesTextFormat.
     */
    @Generated
    public static ResponsesTextFormat fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String discriminatorValue = null;
            try (JsonReader readerToUse = reader.bufferObject()) {
                // Prepare for reading
                readerToUse.nextToken();
                while (readerToUse.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = readerToUse.getFieldName();
                    readerToUse.nextToken();
                    if ("type".equals(fieldName)) {
                        discriminatorValue = readerToUse.getString();
                        break;
                    } else {
                        readerToUse.skipChildren();
                    }
                }
                // Use the discriminator value to determine which subtype should be deserialized.
                if ("text".equals(discriminatorValue)) {
                    return ResponsesTextFormatText.fromJson(readerToUse.reset());
                } else if ("json_object".equals(discriminatorValue)) {
                    return ResponsesTextFormatJsonObject.fromJson(readerToUse.reset());
                } else if ("json_schema".equals(discriminatorValue)) {
                    return ResponsesTextFormatJsonSchema.fromJson(readerToUse.reset());
                } else {
                    return fromJsonKnownDiscriminator(readerToUse.reset());
                }
            }
        });
    }

    @Generated
    static ResponsesTextFormat fromJsonKnownDiscriminator(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ResponsesTextFormat deserializedResponsesTextFormat = new ResponsesTextFormat();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("type".equals(fieldName)) {
                    deserializedResponsesTextFormat.type = ResponsesTextFormatType.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedResponsesTextFormat;
        });
    }
}
