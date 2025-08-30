// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;

/**
 * The ResponsesComputerCallOutputItem model.
 */
@Immutable
public final class ResponsesComputerCallOutputItem extends ResponsesItem {

    /*
     * The type property.
     */
    @Generated
    private ResponsesItemType type = ResponsesItemType.COMPUTER_CALL_OUTPUT;

    /*
     * The ID of the computer tool call that produced the output.
     */
    @Generated
    private final String callId;

    /*
     * The safety checks reported by the API that have been acknowledged by the
     * developer.
     */
    @Generated
    private final List<ResponsesComputerCallItemSafetyCheck> acknowledgedSafetyChecks;

    /*
     * The output property.
     */
    @Generated
    private final ResponsesComputerCallOutputItemOutput output;

    /*
     * The status of the message input. One of `in_progress`, `completed`, or `incomplete`. Populated when input items
     * are returned via API.
     */
    @Generated
    private ResponsesComputerCallOutputItemStatus status;

    /**
     * Creates an instance of ResponsesComputerCallOutputItem class.
     *
     * @param callId the callId value to set.
     * @param acknowledgedSafetyChecks the acknowledgedSafetyChecks value to set.
     * @param output the output value to set.
     */
    @Generated
    public ResponsesComputerCallOutputItem(String callId,
        List<ResponsesComputerCallItemSafetyCheck> acknowledgedSafetyChecks,
        ResponsesComputerCallOutputItemOutput output) {
        this.callId = callId;
        this.acknowledgedSafetyChecks = acknowledgedSafetyChecks;
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
     * Get the callId property: The ID of the computer tool call that produced the output.
     *
     * @return the callId value.
     */
    @Generated
    public String getCallId() {
        return this.callId;
    }

    /**
     * Get the acknowledgedSafetyChecks property: The safety checks reported by the API that have been acknowledged by
     * the
     * developer.
     *
     * @return the acknowledgedSafetyChecks value.
     */
    @Generated
    public List<ResponsesComputerCallItemSafetyCheck> getAcknowledgedSafetyChecks() {
        return this.acknowledgedSafetyChecks;
    }

    /**
     * Get the output property: The output property.
     *
     * @return the output value.
     */
    @Generated
    public ResponsesComputerCallOutputItemOutput getOutput() {
        return this.output;
    }

    /**
     * Get the status property: The status of the message input. One of `in_progress`, `completed`, or `incomplete`.
     * Populated when input items are returned via API.
     *
     * @return the status value.
     */
    @Generated
    public ResponsesComputerCallOutputItemStatus getStatus() {
        return this.status;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("call_id", this.callId);
        jsonWriter.writeArrayField("acknowledged_safety_checks", this.acknowledgedSafetyChecks,
            (writer, element) -> writer.writeJson(element));
        jsonWriter.writeJsonField("output", this.output);
        jsonWriter.writeStringField("type", this.type == null ? null : this.type.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ResponsesComputerCallOutputItem from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesComputerCallOutputItem if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ResponsesComputerCallOutputItem.
     */
    @Generated
    public static ResponsesComputerCallOutputItem fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String id = null;
            String callId = null;
            List<ResponsesComputerCallItemSafetyCheck> acknowledgedSafetyChecks = null;
            ResponsesComputerCallOutputItemOutput output = null;
            ResponsesItemType type = ResponsesItemType.COMPUTER_CALL_OUTPUT;
            ResponsesComputerCallOutputItemStatus status = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("id".equals(fieldName)) {
                    id = reader.getString();
                } else if ("call_id".equals(fieldName)) {
                    callId = reader.getString();
                } else if ("acknowledged_safety_checks".equals(fieldName)) {
                    acknowledgedSafetyChecks
                        = reader.readArray(reader1 -> ResponsesComputerCallItemSafetyCheck.fromJson(reader1));
                } else if ("output".equals(fieldName)) {
                    output = ResponsesComputerCallOutputItemOutput.fromJson(reader);
                } else if ("type".equals(fieldName)) {
                    type = ResponsesItemType.fromString(reader.getString());
                } else if ("status".equals(fieldName)) {
                    status = ResponsesComputerCallOutputItemStatus.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            ResponsesComputerCallOutputItem deserializedResponsesComputerCallOutputItem
                = new ResponsesComputerCallOutputItem(callId, acknowledgedSafetyChecks, output);
            deserializedResponsesComputerCallOutputItem.setId(id);
            deserializedResponsesComputerCallOutputItem.type = type;
            deserializedResponsesComputerCallOutputItem.status = status;
            return deserializedResponsesComputerCallOutputItem;
        });
    }
}
