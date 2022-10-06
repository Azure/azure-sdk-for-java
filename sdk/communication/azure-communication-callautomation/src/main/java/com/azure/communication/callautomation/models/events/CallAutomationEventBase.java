// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.fasterxml.jackson.annotation.JsonProperty;

/** The base event interface. */
public abstract class CallAutomationEventBase {
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

    /*
     * The eventSource property.
     */
    @JsonProperty(value = "eventSource")
    private final String eventSource;

    /*
     * Used to determine the version of the event.
     */
    @JsonProperty(value = "version")
    private final String version;

    /*
     * The public event namespace used as the "type" property in the
     * CloudEvent.
     */
    @JsonProperty(value = "publicEventType")
    private final String publicEventType;

    /*
     * Operation context
     */
    @JsonProperty(value = "operationContext")
    private final String operationContext;

    /*
     * The resultInformation property.
     */
    @JsonProperty(value = "resultInformation")
    private final ResultInformation resultInformation;

    CallAutomationEventBase() {
        this.serverCallId = null;
        this.callConnectionId = null;
        this.correlationId = null;
        this.eventSource = null;
        this.version = null;
        this.publicEventType = null;
        this.operationContext = null;
        this.resultInformation = null;
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

    /**
     * Get the eventSource property: The eventSource property.
     *
     * @return the eventSource value.
     */
    public String getEventSource() {
        return this.eventSource;
    }

    /**
     * Get the version property: Used to determine the version of the event.
     *
     * @return the version value.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Get the publicEventType property: The public event namespace used as the "type" property in the CloudEvent.
     *
     * @return the publicEventType value.
     */
    public String getPublicEventType() {
        return this.publicEventType;
    }

    /**
     * Get the operationContext property: Operation context.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return this.operationContext;
    }

    /**
     * Get the resultInformation property: The resultInformation property.
     *
     * @return the resultInformation value.
     */
    public ResultInformation getResultInformation() {
        return this.resultInformation;
    }
}
