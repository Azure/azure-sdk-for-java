// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.communication.callautomation.implementation.converters.CallParticipantConverter;
import com.azure.communication.callautomation.implementation.models.CallParticipantInternal;
import com.azure.communication.callautomation.models.CallParticipant;
import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private final List<CallParticipant> participants;

    @JsonProperty(value = "sequenceNumber")
    private final int sequenceNumber;

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

    /**
     * Get the participants property: List of current participants in the call.
     *
     * @return the participants value.
     */
    public List<CallParticipant> getParticipants() {
        return this.participants;
    }
}
