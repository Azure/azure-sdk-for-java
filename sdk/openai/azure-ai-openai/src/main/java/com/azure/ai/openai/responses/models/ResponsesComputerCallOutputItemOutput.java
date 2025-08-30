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
 * The ResponsesComputerCallOutputItemOutput model.
 */
@Immutable
public class ResponsesComputerCallOutputItemOutput implements JsonSerializable<ResponsesComputerCallOutputItemOutput> {

    /*
     * The type property.
     */
    @Generated
    private ResponsesComputerCallOutputItemOutputType type;

    /**
     * Creates an instance of ResponsesComputerCallOutputItemOutput class.
     */
    @Generated
    public ResponsesComputerCallOutputItemOutput() {
    }

    /**
     * Get the type property: The type property.
     *
     * @return the type value.
     */
    @Generated
    public ResponsesComputerCallOutputItemOutputType getType() {
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
     * Reads an instance of ResponsesComputerCallOutputItemOutput from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesComputerCallOutputItemOutput if the JsonReader was pointing to an instance of it,
     * or null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ResponsesComputerCallOutputItemOutput.
     */
    @Generated
    public static ResponsesComputerCallOutputItemOutput fromJson(JsonReader jsonReader) throws IOException {
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
                if ("computer_screenshot".equals(discriminatorValue)) {
                    return ResponsesComputerCallOutputItemScreenshot.fromJson(readerToUse.reset());
                } else {
                    return fromJsonKnownDiscriminator(readerToUse.reset());
                }
            }
        });
    }

    @Generated
    static ResponsesComputerCallOutputItemOutput fromJsonKnownDiscriminator(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ResponsesComputerCallOutputItemOutput deserializedResponsesComputerCallOutputItemOutput
                = new ResponsesComputerCallOutputItemOutput();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("type".equals(fieldName)) {
                    deserializedResponsesComputerCallOutputItemOutput.type
                        = ResponsesComputerCallOutputItemOutputType.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedResponsesComputerCallOutputItemOutput;
        });
    }
}
