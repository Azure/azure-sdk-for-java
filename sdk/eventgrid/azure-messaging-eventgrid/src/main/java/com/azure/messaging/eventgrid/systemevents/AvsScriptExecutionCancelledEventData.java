// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.
package com.azure.messaging.eventgrid.systemevents;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Generated;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;

/**
 * Schema of the Data property of an EventGridEvent for a Microsoft.AVS.ScriptExecutionCancelled event.
 * 
 * @deprecated This class is deprecated and may be removed in future releases. System events are now available in the
 * azure-messaging-eventgrid-systemevents package.
 */
@Fluent
@Deprecated
public final class AvsScriptExecutionCancelledEventData extends AvsScriptExecutionEventData {

    /**
     * Creates an instance of AvsScriptExecutionCancelledEventData class.
     */
    @Generated
    public AvsScriptExecutionCancelledEventData() {
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public AvsScriptExecutionCancelledEventData setOperationId(String operationId) {
        super.setOperationId(operationId);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public AvsScriptExecutionCancelledEventData setCmdletId(String cmdletId) {
        super.setCmdletId(cmdletId);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public AvsScriptExecutionCancelledEventData setOutput(List<String> output) {
        super.setOutput(output);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("operationId", getOperationId());
        jsonWriter.writeStringField("cmdletId", getCmdletId());
        jsonWriter.writeArrayField("output", getOutput(), (writer, element) -> writer.writeString(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of AvsScriptExecutionCancelledEventData from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of AvsScriptExecutionCancelledEventData if the JsonReader was pointing to an instance of it,
     * or null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the AvsScriptExecutionCancelledEventData.
     */
    @Generated
    public static AvsScriptExecutionCancelledEventData fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            AvsScriptExecutionCancelledEventData deserializedAvsScriptExecutionCancelledEventData
                = new AvsScriptExecutionCancelledEventData();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("operationId".equals(fieldName)) {
                    deserializedAvsScriptExecutionCancelledEventData.setOperationId(reader.getString());
                } else if ("cmdletId".equals(fieldName)) {
                    deserializedAvsScriptExecutionCancelledEventData.setCmdletId(reader.getString());
                } else if ("output".equals(fieldName)) {
                    List<String> output = reader.readArray(reader1 -> reader1.getString());
                    deserializedAvsScriptExecutionCancelledEventData.setOutput(output);
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedAvsScriptExecutionCancelledEventData;
        });
    }
}
