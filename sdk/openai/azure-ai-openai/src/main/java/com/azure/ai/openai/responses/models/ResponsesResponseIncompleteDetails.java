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
 * The ResponsesResponseIncompleteDetails1 model.
 */
@Immutable
public final class ResponsesResponseIncompleteDetails implements JsonSerializable<ResponsesResponseIncompleteDetails> {

    /*
     * The reason property.
     */
    @Generated
    private ResponsesResponseIncompleteDetailsReason reason;

    /**
     * Creates an instance of ResponsesResponseIncompleteDetails1 class.
     */
    @Generated
    private ResponsesResponseIncompleteDetails() {
    }

    /**
     * Get the reason property: The reason property.
     *
     * @return the reason value.
     */
    @Generated
    public ResponsesResponseIncompleteDetailsReason getReason() {
        return this.reason;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("reason", this.reason == null ? null : this.reason.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ResponsesResponseIncompleteDetails1 from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesResponseIncompleteDetails1 if the JsonReader was pointing to an instance of it,
     * or null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ResponsesResponseIncompleteDetails1.
     */
    @Generated
    public static ResponsesResponseIncompleteDetails fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ResponsesResponseIncompleteDetails deserializedResponsesResponseIncompleteDetails
                = new ResponsesResponseIncompleteDetails();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("reason".equals(fieldName)) {
                    deserializedResponsesResponseIncompleteDetails.reason
                        = ResponsesResponseIncompleteDetailsReason.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedResponsesResponseIncompleteDetails;
        });
    }
}
