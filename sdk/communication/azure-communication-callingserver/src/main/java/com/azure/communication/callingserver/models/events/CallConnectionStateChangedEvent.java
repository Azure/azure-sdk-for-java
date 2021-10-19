// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.communication.callingserver.implementation.converters.CallLocatorConverter;
import com.azure.communication.callingserver.implementation.models.CallConnectionStateChangedEventInternal;
import com.azure.communication.callingserver.models.CallConnectionState;
import com.azure.communication.callingserver.models.CallLocator;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;

/** The call connection state changed event. */
@Immutable
public final class CallConnectionStateChangedEvent extends CallingServerEventBase {
    /*
     * The call locator.
     */
    private final CallLocator callLocator;

    /*
     * The call connection id.
     */
    private final String callConnectionId;

    /*
     * The call connection state.
     */
    private final CallConnectionState callConnectionState;

    /**
     * Get the callLocator property: The call locator.
     *
     * @return the callLocator value.
     */
    public CallLocator getCallLocator() {
        return callLocator;
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
     * @param callLocator the callLocator value.
     * @param callConnectionId the callConnectionId value.
     * @param callConnectionState the callConnectionState value.
     */
    CallConnectionStateChangedEvent(
        CallLocator callLocator,
        String callConnectionId,
        CallConnectionState callConnectionState) {
        this.callLocator = callLocator;
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
            CallLocatorConverter.convert(callConnectionStateChangedEventInternal.getCallLocator()),
            callConnectionStateChangedEventInternal.getCallConnectionId(),
            callConnectionStateChangedEventInternal.getCallConnectionState());
    }
}



