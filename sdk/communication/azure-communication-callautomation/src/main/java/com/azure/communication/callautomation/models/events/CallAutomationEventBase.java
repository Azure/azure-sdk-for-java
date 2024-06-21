// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The base event interface. */
public abstract class CallAutomationEventBase implements JsonSerializable<CallAutomationEventBase> {
    /*
     * Call connection ID.
     */
    private String callConnectionId;

    /*
     * Server call ID.
     */
    private String serverCallId;

    /*
     * Correlation ID for event to call correlation.
     */
    private String correlationId;

    /*
     * Operation context
     */
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

    void writeFields(JsonWriter writer) throws IOException {
        writer.writeStringField("callConnectionId", this.callConnectionId);
        writer.writeStringField("serverCallId", this.serverCallId);
        writer.writeStringField("correlationId", this.correlationId);
        writer.writeStringField("operationContext", this.operationContext);
    }

    boolean readField(String fieldName, JsonReader reader) throws IOException {
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
