// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.CallConnection;
import com.azure.communication.callautomation.CallConnectionAsync;
import com.azure.communication.callautomation.CallAutomationEventProcessor;

/**
 * The abstract class used as parent of [action]CallResult
 */
public abstract class CallResult {
    /*
     * The callConnectionProperties
     */
    private final CallConnectionProperties callConnectionProperties;

    /*
     * The callConnection
     */
    private final CallConnection callConnection;

    /*
     * The callConnectionAsync
     */
    private final CallConnectionAsync callConnectionAsync;

    /**
     * The event processor that handles events.
     */
    protected CallAutomationEventProcessor eventProcessor;
    /**
     * The call's connection id.
     */
    protected String callConnectionId;
    /**
     * Operation context from the api request.
     */
    protected String operationContextFromRequest;

    CallResult(CallConnectionProperties callConnectionProperties, CallConnection callConnection,
        CallConnectionAsync callConnectionAsync) {
        this.callConnectionProperties = callConnectionProperties;
        this.callConnection = callConnection;
        this.callConnectionAsync = callConnectionAsync;
    }

    /**
     * Sets the event processor
     *
     * @param eventProcessor the event processor
     * @param callConnectionId the call connection id
     * @param operationContext the operation context
     */
    public void setEventProcessor(CallAutomationEventProcessor eventProcessor, String callConnectionId,
        String operationContext) {
        this.eventProcessor = eventProcessor;
        this.callConnectionId = callConnectionId;
        this.operationContextFromRequest = operationContext;
    }

    /**
     * Get the callConnectionProperties.
     *
     * @return the callConnectionProperties.
     */
    public CallConnectionProperties getCallConnectionProperties() {
        return callConnectionProperties;
    }

    /**
     * Get the callConnection.
     *
     * @return the callConnection.
     */
    public CallConnection getCallConnection() {
        return callConnection;
    }

    /**
     * Get the callConnectionAsync.
     *
     * @return the callConnectionAsync.
     */
    public CallConnectionAsync getCallConnectionAsync() {
        return callConnectionAsync;
    }
}
