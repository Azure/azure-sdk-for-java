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
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** The ParticipantsUpdatedEvent model. */
@Immutable
public final class ParticipantsUpdatedEvent extends CallAutomationEventBase {
    /*
     * List of current participants in the call.
     */
    @JsonIgnore
    private List<CommunicationIdentifier> participants;

    @JsonCreator
    private ParticipantsUpdatedEvent(@JsonProperty("participants") List<Map<String, Object>> participants, Integer ignore) {

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        this.participants = participants
            .stream()
            .map(item -> mapper.convertValue(item, CommunicationIdentifierModel.class))
            .collect(Collectors.toList())
            .stream()
            .map(CommunicationIdentifierConverter::convert)
            .collect(Collectors.toList());
    }

    private ParticipantsUpdatedEvent(List<CommunicationIdentifier> participants) {
        this.participants = participants;
    }

    /**
     * Get the participants property: List of current participants in the call.
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
        jsonWriter.writeStartArray("participants");
        for (CommunicationIdentifier participant : participants) {
            final CommunicationIdentifierModel inner = CommunicationIdentifierConverter.convert(participant);
            // TODO (anu): Enable this after refreshing the protocol layer.
            // jsonWriter.writeJson(inner);
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
    public static ParticipantsUpdatedEvent fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            List<CommunicationIdentifier> participants = null;
            String callConnectionId = null;
            String serverCallId = null;
            String correlationId = null;
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("participants".equals(fieldName)) {
                    participants = null;
                    // TODO (anu): Enable this after refreshing the protocol layer.
                    // event.participants = reader.readArray(CommunicationIdentifierModel::fromJson)
                    //    .stream().map(CommunicationIdentifierConverter::convert).collect(Collectors.toList());
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
            final ParticipantsUpdatedEvent event = new ParticipantsUpdatedEvent(participants);
            event.setCorrelationId(correlationId)
                .setServerCallId(serverCallId)
                .setCallConnectionId(callConnectionId);
            return event;
        });
    }
}
