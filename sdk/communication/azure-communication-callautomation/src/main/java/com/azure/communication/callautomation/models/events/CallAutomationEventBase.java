// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.json.JsonReader;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;

/** The base event interface. */
public abstract class CallAutomationEventBase {
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
     * Correlation ID for event to call correlation.
     */
    @JsonProperty(value = "correlationId")
    private String correlationId;

    /*
     * Operation context
     */
    @JsonProperty(value = "operationContext")
    private String operationContext;

    CallAutomationEventBase() {
        this.serverCallId = null;
        this.callConnectionId = null;
        this.correlationId = null;
        this.operationContext = null;
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
     * Get the correlationId property: Correlation ID for event to call correlation.
     *
     * @return the correlationId value.
     */
    public String getCorrelationId() {
        return this.correlationId;
    }

    /**
     * Get the operationContext property: Operation context.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return this.operationContext;
    }

    boolean handleField(String fieldName, JsonReader reader) throws IOException {
        if ("callConnectionId".equals(fieldName)) {
            this.callConnectionId = reader.getString();
            return true;
        }
        if ("serverCallId".equals(fieldName)) {
            this.serverCallId = reader.getString();
            return true;
        }
        if ("correlationId".equals(fieldName)) {
            this.correlationId = reader.getString();
            return true;
        }
        if ("operationContext".equals(fieldName)) {
            this.operationContext = reader.getString();
            return true;
        }
        return false;
    }
}
