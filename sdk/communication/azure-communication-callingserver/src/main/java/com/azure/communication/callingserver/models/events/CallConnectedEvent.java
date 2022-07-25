// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The CallConnectedEvent model. */
@Immutable
public final class CallConnectedEvent implements CallingServerBaseEvent {
    /*
     * The event types.
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

    private CallConnectedEvent() {
        this.serverCallId = null;
        this.callConnectionId = null;
        this.correlationId = null;
        this.type = null;
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
