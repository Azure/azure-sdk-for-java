// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.eventprocessor;

import com.azure.communication.callautomation.models.events.CallAutomationEventBase;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Event awaiter for ongoing rules.
 */
public final class EventAwaiterOngoing<TEvent extends CallAutomationEventBase> extends EventAwaiter {
    private final String callConnectionId;
    private final Consumer<TEvent> eventProcessor;
    private final Class<TEvent> clazz;

    public EventAwaiterOngoing(Class<TEvent> clazz, String callConnectionId, Consumer<TEvent> eventProcessor) {
        super();
        this.clazz = clazz;
        this.callConnectionId = callConnectionId;
        this.eventProcessor = eventProcessor;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onEventsReceived(EventWithBacklogId event) {
        if (event.getEvent().getClass() == clazz
            && Objects.equals(event.getEvent().getCallConnectionId(), callConnectionId)) {
            eventProcessor.accept((TEvent) event.getEvent());
        }
    }
}
