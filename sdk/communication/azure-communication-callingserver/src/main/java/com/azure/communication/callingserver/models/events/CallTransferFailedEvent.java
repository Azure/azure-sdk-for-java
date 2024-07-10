// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The CallTransferFailedEvent model. */
@Immutable
public final class CallTransferFailedEvent extends CallAutomationEventBase {
    /*
     * Operation context
     */
    private final String operationContext;

    /*
     * The resultInfo property.
     */
    private final ResultInfo resultInfo;

    private CallTransferFailedEvent(String operationContext, ResultInfo resultInfo) {
        this.operationContext = operationContext;
        this.resultInfo = resultInfo;
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
        jsonWriter.writeStringField("callConnectionId", super.getCallConnectionId());
        jsonWriter.writeStringField("serverCallId", super.getServerCallId());
        jsonWriter.writeStringField("correlationId", super.getCorrelationId());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of CallTransferFailedEvent from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of CallTransferFailedEvent if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the CallTransferFailedEvent.
     */
    public static CallTransferFailedEvent fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String operationContext = null;
            ResultInfo resultInfo = null;
            String callConnectionId = null;
            String serverCallId = null;
            String correlationId = null;
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("operationContext".equals(fieldName)) {
                    operationContext = reader.getString();
                } else if ("resultInfo".equals(fieldName)) {
                    resultInfo = ResultInfo.fromJson(reader);
                } else if ("callConnectionId".equals(fieldName)) {
                    callConnectionId = reader.getString();
                } else if ("serverCallId".equals(fieldName)) {
                    serverCallId = reader.getString();
                } else if ("correlationId".equals(fieldName)) {
                    correlationId = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            final CallTransferFailedEvent event = new CallTransferFailedEvent(operationContext, resultInfo);
            event.setCorrelationId(correlationId)
                .setServerCallId(serverCallId)
                .setCallConnectionId(callConnectionId);
            return event;
        });
    }
}
