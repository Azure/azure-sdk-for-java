// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.communication.callingserver.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callingserver.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/** The AddParticipantsFailedEvent model. */
@Immutable
public final class AddParticipantsFailedEvent extends CallAutomationEventBase {
    /*
     * Operation context
     */
    private final String operationContext;

    /*
     * The resultInfo property.
     */
    private final ResultInfo resultInfo;

    /*
     * Participants failed to be added
     */
    private final List<CommunicationIdentifier> participants;

    private AddParticipantsFailedEvent(String operationContext, ResultInfo resultInfo, List<CommunicationIdentifier> participants) {
        this.operationContext = operationContext;
        this.resultInfo = resultInfo;
        this.participants = participants;
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
     * Get the participants property: Participants failed to be added.
     *
     * @return the participants value.
     */
    public List<CommunicationIdentifier> getParticipants() {
        return this.participants;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("operationContext", operationContext);
        jsonWriter.writeJsonField("resultInfo", resultInfo);
        jsonWriter.writeStartArray("participants");
        for (CommunicationIdentifier participant : participants) {
            final CommunicationIdentifierModel inner = CommunicationIdentifierConverter.convert(participant);
            jsonWriter.writeJson(inner);
        }
        jsonWriter.writeEndArray();
        jsonWriter.writeStringField("callConnectionId", super.getCallConnectionId());
        jsonWriter.writeStringField("serverCallId", super.getServerCallId());
        jsonWriter.writeStringField("correlationId", super.getCorrelationId());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ParticipantsUpdated from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ParticipantsUpdated if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ParticipantsUpdated.
     */
    public static AddParticipantsFailedEvent fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String operationContext = null;
            ResultInfo resultInfo = null;
            List<CommunicationIdentifier> participants = null;
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
                } else if ("participants".equals(fieldName)) {
                    participants = reader.readArray(CommunicationIdentifierModel::fromJson)
                        .stream().map(CommunicationIdentifierConverter::convert).collect(Collectors.toList());
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
            final AddParticipantsFailedEvent event = new AddParticipantsFailedEvent(operationContext, resultInfo, participants);
            event.setCorrelationId(correlationId)
                .setServerCallId(serverCallId)
                .setCallConnectionId(callConnectionId);
            return event;
        });
    }
}
