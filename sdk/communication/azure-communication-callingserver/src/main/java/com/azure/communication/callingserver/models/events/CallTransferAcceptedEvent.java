// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The CallTransferAcceptedEvent model. */
@Immutable
public final class CallTransferAcceptedEvent implements CallingServerBaseEvent {
    /*
     * Operation context
     */
    @JsonProperty(value = "operationContext")
    private final String operationContext;

    /*
     * The resultInfo property.
     */
    @JsonProperty(value = "resultInfo")
    private final ResultInfo resultInfo;

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

    private CallTransferAcceptedEvent() {
        this.serverCallId = null;
        this.callConnectionId = null;
        this.correlationId = null;
        this.type = null;
        this.resultInfo = null;
        this.operationContext = null;
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
     * Get the resultInfo property: The resultInfo property.
     *
     * @return the resultInfo value.
     */
    public ResultInfo getResultInfo() {
        return this.resultInfo;
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
