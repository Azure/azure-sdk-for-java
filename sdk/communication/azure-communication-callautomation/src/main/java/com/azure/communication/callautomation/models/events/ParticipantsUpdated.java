// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.communication.callautomation.implementation.converters.CallParticipantConverter;
import com.azure.communication.callautomation.implementation.models.CallParticipantInternal;
import com.azure.communication.callautomation.models.CallParticipant;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** The ParticipantsUpdated model. */
@Immutable
public final class ParticipantsUpdated extends CallAutomationEventBase {
    /*
     * List of current participants in the call.
     */
    @JsonIgnore
    private List<CallParticipant> participants;

    @JsonProperty(value = "sequenceNumber")
    private int sequenceNumber;

    @JsonCreator
    private ParticipantsUpdated(@JsonProperty("participants") List<Map<String, Object>> participants) {
        this.sequenceNumber = 0;
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        this.participants = participants
            .stream()
            .map(item -> mapper.convertValue(item, CallParticipantInternal.class))
            .collect(Collectors.toList())
            .stream()
            .map(CallParticipantConverter::convert)
            .collect(Collectors.toList());
    }

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

    static ParticipantsUpdated fromJsonImpl(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final ParticipantsUpdated event = new ParticipantsUpdated();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("sequenceNumber".equals(fieldName)) {
                    event.sequenceNumber = reader.getInt();
                } else if ("participants".equals(fieldName)) {
                    event.participants = reader.readArray(r -> {
                        // TODO (anu): final CallParticipantInternal inner = CallParticipantInternal.fromJson(reader);
                        final CallParticipantInternal inner = null;
                        return inner;
                    }).stream().map(CallParticipantConverter::convert).collect(Collectors.toList());
                } else {
                    reader.skipChildren();
                }
            }
            return event;
        });
    }
}
