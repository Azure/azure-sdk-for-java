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

/** The AddParticipantsSucceededEvent model. */
@Immutable
public final class AddParticipantsSucceededEvent extends CallAutomationEventBase {
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

    /*
     * Participants added
     */
    @JsonIgnore
    private List<CommunicationIdentifier> participants;

    @JsonCreator
    private AddParticipantsSucceededEvent(@JsonProperty("participants") List<Map<String, Object>> participants) {
        this.operationContext = null;
        this.resultInfo = null;

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

    private AddParticipantsSucceededEvent() {

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
     * Get the participants property: Participants added.
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
            // TODO (anu): Fix this
            // jsonWriter.writeJson(inner);
        }
        jsonWriter.writeEndArray();
        super.writeFields(jsonWriter);
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
    public static AddParticipantsSucceededEvent fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final AddParticipantsSucceededEvent event = new AddParticipantsSucceededEvent();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("operationContext".equals(fieldName)) {
                    event.operationContext = reader.getString();
                } else if ("resultInfo".equals(fieldName)) {
                    event.resultInfo = ResultInfo.fromJson(reader);
                } else if ("participants".equals(fieldName)) {
                    event.participants = null;
                    // TODO (anu): Fix this
                    // event.participants = reader.readArray(CommunicationIdentifierModel::fromJson)
                    //    .stream().map(CommunicationIdentifierConverter::convert).collect(Collectors.toList());
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
