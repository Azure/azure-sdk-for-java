// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.eventprocessor;

import com.azure.communication.callautomation.models.events.CallAutomationEventBase;

/**
 * Container class to hold the call connection ID and the class of the event, used as a key for the ongoing event awaiters.
 */
public class OngoingEventAwaiterKey<TEvent extends CallAutomationEventBase> {
    private final String callConnectionId;
    private final Class<TEvent> clazz;

    public OngoingEventAwaiterKey(String callConnectionId, Class<TEvent> clazz) {
        this.clazz = clazz;
        this.callConnectionId = callConnectionId;
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
     * Get the clazz property: The class of the event.
     *
     * @return the clazz value.
     */
    public Class<TEvent> getClazz() {
        return clazz;
    }
}
