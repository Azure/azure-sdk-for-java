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

/** The ParticipantsUpdatedEvent model. */
@Immutable
public final class ParticipantsUpdatedEvent extends CallAutomationEventBase {
    /*
     * List of current participants in the call.
     */
    private List<CommunicationIdentifier> participants;

    private ParticipantsUpdatedEvent() {
    }

    /**
     * Get the participants property: List of current participants in the call.
     *
     * @return the participants value.
     */
    public List<CommunicationIdentifier> getParticipants() {
        return this.participants;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return toJsonShared(jsonWriter.writeStartObject()).writeEndObject();
    }

    /**
     * Reads an instance of {@link ParticipantsUpdatedEvent} from the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} to read from.
     * @return An instance of {@link ParticipantsUpdatedEvent}, or null if the {@link JsonReader} was pointing to
     * {@link JsonToken#NULL}.
     * @throws IOException If an error occurs while reading the {@link JsonReader}.
     */
    public static ParticipantsUpdatedEvent fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ParticipantsUpdatedEvent event = new ParticipantsUpdatedEvent();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if (fromJsonShared(event, fieldName, reader)) {
                    continue;
                }

                if ("participants".equals(fieldName)) {
                    event.participants = reader.readArray(
                        r -> CommunicationIdentifierConverter.convert(CommunicationIdentifierModel.fromJson(r)));
                } else {
                    reader.skipChildren();
                }
            }

            return event;
        });
    }
}
