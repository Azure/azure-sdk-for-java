// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.eventprocessor;

import com.azure.communication.callautomation.models.events.CallAutomationEventBase;

/**
 * Container that holds a CallAutomation event and its backlog id.
 */
public final class EventWithBacklogId {
    private final CallAutomationEventBase event;
    private final String backLogEventId;

    EventWithBacklogId(String backLogEventId, CallAutomationEventBase event) {
        this.backLogEventId = backLogEventId;
        this.event = event;
    }

    /**
     * Get the backLogEventId property: The backlog event id.
     *
     * @return the backLogEventId value.
     */
    public String getBackLogEventId() {
        return backLogEventId;
    }

    /**
     * Get the event property: The event.
     *
     * @return the event value.
     */
    public CallAutomationEventBase getEvent() {
        return event;
    }
}
