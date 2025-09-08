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
 * The ResponsesFunctionCallOutput model.
 */
@Immutable
public final class ResponsesFunctionCallOutput extends ResponsesItem {

    /*
     * The type property.
     */
    @Generated
    private ResponsesItemType type = ResponsesItemType.FUNCTION_CALL_OUTPUT;

    /*
     * The status property.
     */
    @Generated
    private ResponsesFunctionCallOutputStatus status;

    /*
     * The call_id property.
     */
    @Generated
    private final String callId;

    /*
     * The output property.
     */
    @Generated
    private final String output;

    /**
     * Creates an instance of ResponsesFunctionCallOutput class.
     *
     * @param callId the callId value to set.
     * @param output the output value to set.
     */
    @Generated
    public ResponsesFunctionCallOutput(String callId, String output) {
        this.callId = callId;
        this.output = output;
    }

    /**
     * Get the type property: The type property.
     *
     * @return the type value.
     */
    @Generated
    @Override
    public ResponsesItemType getType() {
        return this.type;
    }

    /**
     * Get the status property: The status property.
     *
     * @return the status value.
     */
    @Generated
    public ResponsesFunctionCallOutputStatus getStatus() {
        return this.status;
    }

    /**
     * Get the callId property: The call_id property.
     *
     * @return the callId value.
     */
    @Generated
    public String getCallId() {
        return this.callId;
    }

    /**
     * Get the output property: The output property.
     *
     * @return the output value.
     */
    @Generated
    public String getOutput() {
        return this.output;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("call_id", this.callId);
        jsonWriter.writeStringField("output", this.output);
        jsonWriter.writeStringField("type", this.type == null ? null : this.type.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ResponsesFunctionCallOutput from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesFunctionCallOutput if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ResponsesFunctionCallOutput.
     */
    @Generated
    public static ResponsesFunctionCallOutput fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String id = null;
            String callId = null;
            String output = null;
            ResponsesItemType type = ResponsesItemType.FUNCTION_CALL_OUTPUT;
            ResponsesFunctionCallOutputStatus status = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("id".equals(fieldName)) {
                    id = reader.getString();
                } else if ("call_id".equals(fieldName)) {
                    callId = reader.getString();
                } else if ("output".equals(fieldName)) {
                    output = reader.getString();
                } else if ("type".equals(fieldName)) {
                    type = ResponsesItemType.fromString(reader.getString());
                } else if ("status".equals(fieldName)) {
                    status = ResponsesFunctionCallOutputStatus.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            ResponsesFunctionCallOutput deserializedResponsesFunctionCallOutput
                = new ResponsesFunctionCallOutput(callId, output);
            deserializedResponsesFunctionCallOutput.setId(id);
            deserializedResponsesFunctionCallOutput.type = type;
            deserializedResponsesFunctionCallOutput.status = status;
            return deserializedResponsesFunctionCallOutput;
        });
    }
}
