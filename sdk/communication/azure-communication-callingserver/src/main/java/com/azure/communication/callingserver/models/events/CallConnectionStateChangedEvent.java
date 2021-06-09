// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.communication.callingserver.models.CallConnectionState;
import com.azure.core.annotation.Fluent;
import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The call connection state changed event. */
@Fluent
public final class CallConnectionStateChangedEvent  extends CallingServerEventBase {
    /*
     * The server call.id.
     */
    @JsonProperty(value = "serverCallId")
    private String serverCallId;

    /*
     * The call connection id.
     */
    @JsonProperty(value = "callConnectionId")
    private String callConnectionId;

    /*
     * The call connection state.
     */
    @JsonProperty(value = "callConnectionState")
    private CallConnectionState callConnectionState;

    /**
     * Get the serverCallId property: The server call.id.
     *
     * @return the serverCallId value.
     */
    public String getServerCallId() {
        return this.serverCallId;
    }

    /**
     * Set the serverCallId property: The server call.id.
     *
     * @param serverCallId the serverCallId value to set.
     * @return the CallConnectionStateChangedEvent object itself.
     */
    public CallConnectionStateChangedEvent setServerCallId(String serverCallId) {
        this.serverCallId = serverCallId;
        return this;
    }

    /**
     * Get the callConnectionId property: The call connection id.
     *
     * @return the callConnectionId value.
     */
    public String getCallConnectionId() {
        return this.callConnectionId;
    }

    /**
     * Set the callConnectionId property: The call connection id.
     *
     * @param callConnectionId the callConnectionId value to set.
     * @return the CallConnectionStateChangedEvent object itself.
     */
    public CallConnectionStateChangedEvent setCallConnectionId(String callConnectionId) {
        this.callConnectionId = callConnectionId;
        return this;
    }

    /**
     * Get the callConnectionState property: The call connection state.
     *
     * @return the callConnectionState value.
     */
    public CallConnectionState getCallConnectionState() {
        return this.callConnectionState;
    }

    /**
     * Set the callConnectionState property: The call connection state.
     *
     * @param callConnectionState the callConnectionState value to set.
     * @return the CallConnectionStateChangedEvent object itself.
     */
    public CallConnectionStateChangedEvent setCallConnectionState(CallConnectionState callConnectionState) {
        this.callConnectionState = callConnectionState;
        return this;
    }

    /**
     * Deserialize {@link CallConnectionStateChangedEvent} event.
     *
     * @param eventData binary data for event
     * @return {@link CallConnectionStateChangedEvent} event.
     */
    public static CallConnectionStateChangedEvent deserialize(BinaryData eventData) {
        return eventData == null ? null : eventData.toObject(CallConnectionStateChangedEvent.class);
    }
}
