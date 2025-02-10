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

/** The AddParticipantSucceeded model. */
@Immutable
public final class AddParticipantSucceeded extends CallAutomationEventBase {
    /*
     * Participant added
     */
    private CommunicationIdentifier participant;

    private AddParticipantSucceeded() {

    }

    /**
     * Get the participant property: Participant added.
     *
     * @return the participant value.
     */
    public CommunicationIdentifier getParticipant() {
        return this.participant;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        final CommunicationIdentifierModel inner = CommunicationIdentifierConverter.convert(participant);
        jsonWriter.writeJsonField("participant", inner);
        super.writeFields(jsonWriter);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of AddParticipantSucceeded from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of AddParticipantSucceeded if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the AddParticipantSucceeded.
     */
    public static AddParticipantSucceeded fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final AddParticipantSucceeded event = new AddParticipantSucceeded();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("participant".equals(fieldName)) {
                    final CommunicationIdentifierModel inner = CommunicationIdentifierModel.fromJson(reader);
                    event.participant = CommunicationIdentifierConverter.convert(inner);
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
