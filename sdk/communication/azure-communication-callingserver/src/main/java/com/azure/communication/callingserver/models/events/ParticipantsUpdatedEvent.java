// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.communication.callingserver.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callingserver.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** The ParticipantsUpdatedEvent model. */
@Immutable
public final class ParticipantsUpdatedEvent implements CallingServerBaseEvent {
    /*
     * List of current participants in the call.
     */
    @JsonProperty(value = "participants")
    private final List<CommunicationIdentifier> participants;

    /*
     * The type property.
     */
    @JsonProperty(value = "type")
    private final AcsEventType type;

    /*
     * Call connection ID.
     */
    @JsonProperty(value = "callConnectionId")
    private final String callConnectionId;

    /*
     * Server call ID.
     */
    @JsonProperty(value = "serverCallId")
    private final String serverCallId;

    /*
     * Correlation ID for event to call correlation. Also called ChainId for
     * skype chain ID.
     */
    @JsonProperty(value = "correlationId")
    private final String correlationId;

    @JsonCreator
    private ParticipantsUpdatedEvent(@JsonProperty("participants") List<Map<String, Object>> participants) {
        this.serverCallId = null;
        this.callConnectionId = null;
        this.correlationId = null;
        this.type = null;

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

    /**
     * Get the type property: The type property.
     *
     * @return the type value.
     */
    public AcsEventType getType() {
        return this.type;
    }

    /**
     * Get the callConnectionId property: Call connection ID.
     *
     * @return the callConnectionId value.
     */
    public String getCallConnectionId() {
        return this.callConnectionId;
    }

    /**
     * Get the serverCallId property: Server call ID.
     *
     * @return the serverCallId value.
     */
    public String getServerCallId() {
        return this.serverCallId;
    }

    /**
     * Get the correlationId property: Correlation ID for event to call correlation. Also called ChainId for skype chain
     * ID.
     *
     * @return the correlationId value.
     */
    public String getCorrelationId() {
        return this.correlationId;
    }
}
