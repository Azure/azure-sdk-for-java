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
 * A tool call to a computer use tool. See the
 * [computer use guide](/docs/guides/tools-computer-use) for more information.
 */
@Immutable
public final class ResponsesComputerCallItem extends ResponsesItem {

    /*
     * The type property.
     */
    @Generated
    private ResponsesItemType type = ResponsesItemType.COMPUTER_CALL;

    /*
     * An identifier used when responding to the tool call with output.
     */
    @Generated
    private final String callId;

    /*
     * The action property.
     */
    @Generated
    private final ResponsesComputerCallItemAction action;

    /*
     * The pending safety checks for the computer call.
     */
    @Generated
    private final List<ResponsesComputerCallItemSafetyCheck> pendingSafetyChecks;

    /*
     * The status of the item. One of `in_progress`, `completed`, or `incomplete`. Populated when items are returned via
     * API.
     */
    @Generated
    private ResponsesComputerCallItemStatus status;

    /**
     * Creates an instance of ResponsesComputerCallItem class.
     *
     * @param callId the callId value to set.
     * @param action the action value to set.
     * @param pendingSafetyChecks the pendingSafetyChecks value to set.
     */
    @Generated
    public ResponsesComputerCallItem(String callId, ResponsesComputerCallItemAction action,
        List<ResponsesComputerCallItemSafetyCheck> pendingSafetyChecks) {
        this.callId = callId;
        this.action = action;
        this.pendingSafetyChecks = pendingSafetyChecks;
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
     * Get the callId property: An identifier used when responding to the tool call with output.
     *
     * @return the callId value.
     */
    @Generated
    public String getCallId() {
        return this.callId;
    }

    /**
     * Get the action property: The action property.
     *
     * @return the action value.
     */
    @Generated
    public ResponsesComputerCallItemAction getAction() {
        return this.action;
    }

    /**
     * Get the pendingSafetyChecks property: The pending safety checks for the computer call.
     *
     * @return the pendingSafetyChecks value.
     */
    @Generated
    public List<ResponsesComputerCallItemSafetyCheck> getPendingSafetyChecks() {
        return this.pendingSafetyChecks;
    }

    /**
     * Get the status property: The status of the item. One of `in_progress`, `completed`, or `incomplete`. Populated
     * when items are returned via API.
     *
     * @return the status value.
     */
    @Generated
    public ResponsesComputerCallItemStatus getStatus() {
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
        jsonWriter.writeJsonField("action", this.action);
        jsonWriter.writeArrayField("pending_safety_checks", this.pendingSafetyChecks,
            (writer, element) -> writer.writeJson(element));
        jsonWriter.writeStringField("type", this.type == null ? null : this.type.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ResponsesComputerCallItem from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesComputerCallItem if the JsonReader was pointing to an instance of it, or null if
     * it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ResponsesComputerCallItem.
     */
    @Generated
    public static ResponsesComputerCallItem fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String id = null;
            String callId = null;
            ResponsesComputerCallItemAction action = null;
            List<ResponsesComputerCallItemSafetyCheck> pendingSafetyChecks = null;
            ResponsesComputerCallItemStatus status = null;
            ResponsesItemType type = ResponsesItemType.COMPUTER_CALL;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("id".equals(fieldName)) {
                    id = reader.getString();
                } else if ("call_id".equals(fieldName)) {
                    callId = reader.getString();
                } else if ("action".equals(fieldName)) {
                    action = ResponsesComputerCallItemAction.fromJson(reader);
                } else if ("pending_safety_checks".equals(fieldName)) {
                    pendingSafetyChecks
                        = reader.readArray(reader1 -> ResponsesComputerCallItemSafetyCheck.fromJson(reader1));
                } else if ("status".equals(fieldName)) {
                    status = ResponsesComputerCallItemStatus.fromString(reader.getString());
                } else if ("type".equals(fieldName)) {
                    type = ResponsesItemType.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            ResponsesComputerCallItem deserializedResponsesComputerCallItem
                = new ResponsesComputerCallItem(callId, action, pendingSafetyChecks);
            deserializedResponsesComputerCallItem.setId(id);
            deserializedResponsesComputerCallItem.status = status;
            deserializedResponsesComputerCallItem.type = type;
            return deserializedResponsesComputerCallItem;
        });
    }
}
