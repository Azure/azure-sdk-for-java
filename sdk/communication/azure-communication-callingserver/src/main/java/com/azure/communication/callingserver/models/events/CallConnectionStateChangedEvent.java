// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.communication.callingserver.implementation.models.CallConnectionStateChangedEventInternal;
import com.azure.communication.callingserver.models.CallConnectionState;
import com.azure.core.util.BinaryData;

/** The call connection state changed event. */
public final class CallConnectionStateChangedEvent {
    /*
     * The server call.id.
     */
    private final String serverCallId;

    /*
     * The call connection id.
     */
    private final String callConnectionId;

    /*
     * The call connection state.
     */
    private final CallConnectionState callConnectionState;

    /**
     * Get the serverCallId property: The server call.id.
     *
     * @return the serverCallId value.
     */
    public String getServerCallId() {
        return this.serverCallId;
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
     * Get the callConnectionState property: The call connection state.
     *
     * @return the callConnectionState value.
     */
    public CallConnectionState getCallConnectionState() {
        return this.callConnectionState;
    }

    /**
     * Initializes a new instance of PlayAudioResult.
     *
     * @param serverCallId the serverCallId value.
     * @param callConnectionId the callConnectionId value.
     * @param callConnectionState the callConnectionState value.
     */
    public CallConnectionStateChangedEvent(String serverCallId, String callConnectionId, CallConnectionState callConnectionState) {
        this.serverCallId = serverCallId;
        this.callConnectionId = callConnectionId;
        this.callConnectionState = callConnectionState;
    }

    /**
     * Deserialize {@link CallConnectionStateChangedEvent} event.
     *
     * @param eventData binary data for event
     * @return {@link CallConnectionStateChangedEvent} event.
     */
    public static CallConnectionStateChangedEvent deserialize(BinaryData eventData) {
        if (eventData == null) {
            return null;
        }
        CallConnectionStateChangedEventInternal callConnectionStateChangedEventInternal =
            eventData.toObject(CallConnectionStateChangedEventInternal.class);
        return new CallConnectionStateChangedEvent(
            callConnectionStateChangedEventInternal.getServerCallId(),
            callConnectionStateChangedEventInternal.getCallConnectionId(),
            callConnectionStateChangedEventInternal.getCallConnectionState());
    }
}



