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
 * The ResponsesComputerCallItemSafetyCheck model.
 */
@Immutable
public final class ResponsesComputerCallItemSafetyCheck
    implements JsonSerializable<ResponsesComputerCallItemSafetyCheck> {

    /*
     * The id property.
     */
    @Generated
    private final String id;

    /*
     * The code property.
     */
    @Generated
    private final String code;

    /*
     * The message property.
     */
    @Generated
    private final String message;

    /**
     * Creates an instance of ResponsesComputerCallItemSafetyCheck class.
     *
     * @param id the id value to set.
     * @param code the code value to set.
     * @param message the message value to set.
     */
    @Generated
    public ResponsesComputerCallItemSafetyCheck(String id, String code, String message) {
        this.id = id;
        this.code = code;
        this.message = message;
    }

    /**
     * Get the id property: The id property.
     *
     * @return the id value.
     */
    @Generated
    public String getId() {
        return this.id;
    }

    /**
     * Get the code property: The code property.
     *
     * @return the code value.
     */
    @Generated
    public String getCode() {
        return this.code;
    }

    /**
     * Get the message property: The message property.
     *
     * @return the message value.
     */
    @Generated
    public String getMessage() {
        return this.message;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("id", this.id);
        jsonWriter.writeStringField("code", this.code);
        jsonWriter.writeStringField("message", this.message);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ResponsesComputerCallItemSafetyCheck from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesComputerCallItemSafetyCheck if the JsonReader was pointing to an instance of it,
     * or null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ResponsesComputerCallItemSafetyCheck.
     */
    @Generated
    public static ResponsesComputerCallItemSafetyCheck fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String id = null;
            String code = null;
            String message = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("id".equals(fieldName)) {
                    id = reader.getString();
                } else if ("code".equals(fieldName)) {
                    code = reader.getString();
                } else if ("message".equals(fieldName)) {
                    message = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return new ResponsesComputerCallItemSafetyCheck(id, code, message);
        });
    }
}
