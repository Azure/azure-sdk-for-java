// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.communication.callautomation.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callautomation.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The MoveParticipantFailed model. */
@Immutable
public final class MoveParticipantFailed extends CallAutomationEventBase {
    /*
     * Participant move failed
     */
    private CommunicationIdentifier participant;

    /*
     * The CallConnectionId for the call you want to move the participant from
     */
    private String fromCall;

    private MoveParticipantFailed() {

    }

    /**
     * Get the participant property: Participant move failed.
     *
     * @return the participant value.
     */
    public CommunicationIdentifier getParticipant() {
        return this.participant;
    }

    /**
     * Get the fromCall property: The CallConnectionId for the call you want to move the participant from.
     *
     * @return the fromCall value.
     */
    public String getFromCall() {
        return this.fromCall;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        final CommunicationIdentifierModel inner = CommunicationIdentifierConverter.convert(participant);
        jsonWriter.writeJsonField("participant", inner);
        jsonWriter.writeStringField("fromCall", fromCall);
        super.writeFields(jsonWriter);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of MoveParticipantFailed from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of MoveParticipantFailed if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the MoveParticipantFailed.
     */
    public static MoveParticipantFailed fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final MoveParticipantFailed event = new MoveParticipantFailed();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("participant".equals(fieldName)) {
                    final CommunicationIdentifierModel inner = CommunicationIdentifierModel.fromJson(reader);
                    event.participant = CommunicationIdentifierConverter.convert(inner);
                } else if ("fromCall".equals(fieldName)) {
                    event.fromCall = reader.getString();
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
