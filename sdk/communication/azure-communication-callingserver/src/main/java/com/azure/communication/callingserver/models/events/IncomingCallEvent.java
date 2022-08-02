// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.communication.callingserver.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callingserver.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/** The AddParticipantsSucceededEvent model. */
@Immutable
public final class IncomingCallEvent implements CallingServerBaseEvent {
    /*
     * From property
     */
    @JsonIgnore
    private final CommunicationIdentifier from;

    /*
     * To property
     */
    @JsonIgnore
    private final CommunicationIdentifier to;

    /*
     * hasIncomingVideo
     */
    @JsonProperty(value = "hasIncomingVideo")
    private final boolean hasIncomingVideo;

    /*
     * The type property.
     */
    @JsonIgnore
    private final AcsEventType type;

    /*
     * callerDisplayName
     */
    @JsonProperty(value = "callerDisplayName")
    private final String callerDisplayName;

    /*
     * incomingCallContext
     */
    @JsonProperty(value = "incomingCallContext")
    private final String incomingCallContext;

    /*
     * Correlation ID for event to call correlation. Also called ChainId for
     * skype chain ID.
     */
    @JsonProperty(value = "correlationId")
    private final String correlationId;

    @JsonCreator
    private IncomingCallEvent(@JsonProperty("from") Map<String, Object> from, @JsonProperty("to") Map<String, Object> to) {
        this.hasIncomingVideo = false;
        this.callerDisplayName = null;
        this.incomingCallContext = null;
        this.correlationId = null;
        this.type = AcsEventType.INCOMING_CALL_EVENT;

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        this.from = CommunicationIdentifierConverter.convert(
            mapper.convertValue(from, CommunicationIdentifierModel.class)
        );
        this.to = CommunicationIdentifierConverter.convert(
            mapper.convertValue(to, CommunicationIdentifierModel.class)
        );
    }

    /**
     * Get the from property
     *
     * @return the from value.
     */
    public CommunicationIdentifier getFrom() {
        return this.from;
    }

    /**
     * Get the to property
     *
     * @return the to value.
     */
    public CommunicationIdentifier getTo() {
        return this.to;
    }

    /**
     * Get the hasIncomingVideo.
     *
     * @return the hasIncomingVideo value.
     */
    public boolean getHasIncomingVideo() {
        return this.hasIncomingVideo;
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
     * Get the callerDisplayName property.
     *
     * @return the callerDisplayName value.
     */
    public String getCallerDisplayName() {
        return this.callerDisplayName;
    }

    /**
     * Get the incomingCallContext property.
     *
     * @return the incomingCallContext value.
     */
    public String getIncomingCallContext() {
        return this.incomingCallContext;
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
