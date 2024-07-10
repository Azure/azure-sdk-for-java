// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.json.JsonSerializable;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The base event interface. */
public abstract class CallAutomationEventBase implements JsonSerializable<CallAutomationEventBase> {
    /*
     * Call connection ID.
     */
    @JsonProperty(value = "callConnectionId")
    private String callConnectionId;

    /*
     * Server call ID.
     */
    @JsonProperty(value = "serverCallId")
    private String serverCallId;

    /*
     * Correlation ID for event to call correlation. Also called ChainId for
     * skype chain ID.
     */
    @JsonProperty(value = "correlationId")
    private String correlationId;

    CallAutomationEventBase() {
        this.serverCallId = null;
        this.callConnectionId = null;
        this.correlationId = null;
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

    CallAutomationEventBase setServerCallId(String serverCallId) {
        this.serverCallId = serverCallId;
        return this;
    }

    CallAutomationEventBase setCallConnectionId(String callConnectionId) {
        this.callConnectionId = callConnectionId;
        return this;
    }

    CallAutomationEventBase setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }
}
