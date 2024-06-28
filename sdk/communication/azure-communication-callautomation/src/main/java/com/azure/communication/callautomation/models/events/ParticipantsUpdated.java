// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.communication.callautomation.implementation.converters.CallParticipantConverter;
import com.azure.communication.callautomation.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callautomation.implementation.models.CallParticipantInternal;
import com.azure.communication.callautomation.models.CallParticipant;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/** The ParticipantsUpdated model. */
@Immutable
public final class ParticipantsUpdated extends CallAutomationEventBase {
    /*
     * List of current participants in the call.
     */
    private List<CallParticipant> participants;

    private int sequenceNumber;

    private ParticipantsUpdated() {

    }

    /**
     * Get the participants property: List of current participants in the call.
     *
     * @return the participants value.
     */
    public List<CallParticipant> getParticipants() {
        return this.participants;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeNumberField("sequenceNumber", sequenceNumber);
        jsonWriter.writeStartArray("participants");
        for (CallParticipant participant : participants) {
            final CallParticipantInternal inner = new CallParticipantInternal();
            inner.setIdentifier(CommunicationIdentifierConverter.convert(participant.getIdentifier()));
            inner.setIsMuted(participant.isMuted());
            inner.setIsOnHold(participant.isOnHold());
            jsonWriter.writeJson(inner);
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
    public static ParticipantsUpdated fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final ParticipantsUpdated event = new ParticipantsUpdated();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("sequenceNumber".equals(fieldName)) {
                    event.sequenceNumber = reader.getInt();
                } else if ("participants".equals(fieldName)) {
                    event.participants = reader.readArray(CallParticipantInternal::fromJson)
                        .stream().map(CallParticipantConverter::convert).collect(Collectors.toList());
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
