// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.communication.callautomation.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callautomation.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private final List<CommunicationIdentifier> participants;

    @JsonCreator
    private ParticipantsUpdatedEvent(@JsonProperty("participants") List<Map<String, Object>> participants) {
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

    /**
     * Get the participants property: List of current participants in the call.
     *
     * @return the participants value.
     */
    public List<CommunicationIdentifier> getParticipants() {
        return this.participants;
    }
}
