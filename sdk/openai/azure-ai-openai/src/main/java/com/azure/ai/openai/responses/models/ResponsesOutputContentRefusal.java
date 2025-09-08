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
 * The ResponsesOutputContentRefusal model.
 */
@Immutable
public final class ResponsesOutputContentRefusal extends ResponsesContent {

    /*
     * The type property.
     */
    @Generated
    private ResponsesContentType type = ResponsesContentType.REFUSAL;

    /*
     * The refusal property.
     */
    @Generated
    private final String refusal;

    /**
     * Creates an instance of ResponsesOutputContentRefusal class.
     *
     * @param refusal the refusal value to set.
     */
    @Generated
    public ResponsesOutputContentRefusal(String refusal) {
        this.refusal = refusal;
    }

    /**
     * Get the type property: The type property.
     *
     * @return the type value.
     */
    @Generated
    @Override
    public ResponsesContentType getType() {
        return this.type;
    }

    /**
     * Get the refusal property: The refusal property.
     *
     * @return the refusal value.
     */
    @Generated
    public String getRefusal() {
        return this.refusal;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("refusal", this.refusal);
        jsonWriter.writeStringField("type", this.type == null ? null : this.type.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ResponsesOutputContentRefusal from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesOutputContentRefusal if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ResponsesOutputContentRefusal.
     */
    @Generated
    public static ResponsesOutputContentRefusal fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String refusal = null;
            ResponsesContentType type = ResponsesContentType.REFUSAL;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("refusal".equals(fieldName)) {
                    refusal = reader.getString();
                } else if ("type".equals(fieldName)) {
                    type = ResponsesContentType.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            ResponsesOutputContentRefusal deserializedResponsesOutputContentRefusal
                = new ResponsesOutputContentRefusal(refusal);
            deserializedResponsesOutputContentRefusal.type = type;
            return deserializedResponsesOutputContentRefusal;
        });
    }
}
