// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

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
     * Correlation ID for event to call correlation. Also called ChainId for skype chain ID.
     */
    private String correlationId;

    CallAutomationEventBase() {
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

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return toJsonShared(jsonWriter.writeStartObject()).writeEndObject();
    }

    JsonWriter toJsonShared(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStringField("callConnectionId", callConnectionId)
            .writeStringField("serverCallId", serverCallId)
            .writeStringField("correlationId", callConnectionId);
    }

    static boolean fromJsonShared(CallAutomationEventBase event, String fieldName, JsonReader jsonReader)
        throws IOException {
        if ("callConnectionId".equals(fieldName)) {
            event.callConnectionId = jsonReader.getString();
            return true;
        } else if ("serverCallId".equals(fieldName)) {
            event.serverCallId = jsonReader.getString();
            return true;
        } else if ("correlationId".equals(fieldName)) {
            event.correlationId = jsonReader.getString();
            return true;
        }

        return false;
    }
}
