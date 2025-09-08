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
 * The ResponsesWebSearchLocation model.
 */
@Immutable
public class ResponsesWebSearchLocation implements JsonSerializable<ResponsesWebSearchLocation> {

    /*
     * The type property.
     */
    @Generated
    private String type = "ResponsesWebSearchLocation";

    /**
     * Creates an instance of ResponsesWebSearchLocation class.
     */
    @Generated
    public ResponsesWebSearchLocation() {
    }

    /**
     * Get the type property: The type property.
     *
     * @return the type value.
     */
    @Generated
    public String getType() {
        return this.type;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("type", this.type);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ResponsesWebSearchLocation from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesWebSearchLocation if the JsonReader was pointing to an instance of it, or null if
     * it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ResponsesWebSearchLocation.
     */
    @Generated
    public static ResponsesWebSearchLocation fromJson(JsonReader jsonReader) throws IOException {
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
                if ("approximate".equals(discriminatorValue)) {
                    return ResponsesWebSearchApproximateLocation.fromJson(readerToUse.reset());
                } else {
                    return fromJsonKnownDiscriminator(readerToUse.reset());
                }
            }
        });
    }

    @Generated
    static ResponsesWebSearchLocation fromJsonKnownDiscriminator(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ResponsesWebSearchLocation deserializedResponsesWebSearchLocation = new ResponsesWebSearchLocation();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("type".equals(fieldName)) {
                    deserializedResponsesWebSearchLocation.type = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedResponsesWebSearchLocation;
        });
    }
}
