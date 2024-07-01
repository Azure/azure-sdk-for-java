// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;

/** The CallTransferAcceptedEvent model. */
@Immutable
public final class CallTransferAcceptedEvent extends CallAutomationEventBase {
    /*
     * Operation context
     */
    @JsonProperty(value = "operationContext")
    private String operationContext;

    /*
     * The resultInfo property.
     */
    @JsonProperty(value = "resultInfo")
    private ResultInfo resultInfo;

    private CallTransferAcceptedEvent() {
        this.resultInfo = null;
        this.operationContext = null;
    }

    /**
     * Get the operationContext property: Operation context.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return this.operationContext;
    }

    /**
     * Get the resultInfo property: The resultInfo property.
     *
     * @return the resultInfo value.
     */
    public ResultInfo getResultInfo() {
        return this.resultInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("operationContext", operationContext);
        jsonWriter.writeJsonField("resultInfo", resultInfo);
        super.writeFields(jsonWriter);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of CallTransferAcceptedEvent from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of CallTransferAcceptedEvent if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the CallTransferAcceptedEvent.
     */
    public static CallTransferAcceptedEvent fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final CallTransferAcceptedEvent event = new CallTransferAcceptedEvent();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("resultInfo".equals(fieldName)) {
                    event.resultInfo = ResultInfo.fromJson(reader);
                } else {
                    if (!event.readField(fieldName, reader)) {
                        reader.skipChildren();
                    }
                }
            }
            return event;
        });
    }
}
