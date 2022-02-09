// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.communication.callingserver.implementation.models.CallConnectionStateChangedEventInternal;
import com.azure.communication.callingserver.models.CallConnectionState;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;

/** The call connection state changed event. */
@Immutable
public final class CallConnectionStateChangedEvent extends CallingServerEventBase {
    /*
     * The server call id.
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
     * Get the serverCallId property: The server call id.
     *
     * @return the serverCallId value.
     */
    public String getServerCallId() {
        return serverCallId;
    }

    /**
     * Get the callConnectionId property: The call connection id.
     *
     * @return the callConnectionId value.
     */
    public String getCallConnectionId() {
        return callConnectionId;
    }

    /**
     * Get the callConnectionState property: The call connection state.
     *
     * @return the callConnectionState value.
     */
    public CallConnectionState getCallConnectionState() {
        return callConnectionState;
    }

    /**
     * Initializes a new instance of CallConnectionStateChangedEvent.
     *
     * @param serverCallId the serverCallId value.
     * @param callConnectionId the callConnectionId value.
     * @param callConnectionState the callConnectionState value.
     */
    CallConnectionStateChangedEvent(
        String serverCallId,
        String callConnectionId,
        CallConnectionState callConnectionState) {
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



