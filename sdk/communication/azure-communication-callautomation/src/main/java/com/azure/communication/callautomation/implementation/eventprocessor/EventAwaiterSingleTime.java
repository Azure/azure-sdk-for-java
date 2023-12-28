// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.eventprocessor;

import com.azure.communication.callautomation.models.events.CallAutomationEventBase;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.function.Predicate;

/**
 * A class that represents an event awaiter that will only wait for a single event.
 */
public final class EventAwaiterSingleTime extends EventAwaiter {
    private final Predicate<CallAutomationEventBase> predicate;
    private final Sinks.One<EventWithBacklogId> task;

    public EventAwaiterSingleTime(Predicate<CallAutomationEventBase> predicate) {
        super();
        this.predicate = predicate;
        this.task = Sinks.one();
    }

    @Override
    public void onEventsReceived(EventWithBacklogId event) {
        if (predicate.test(event.getEvent())) {
            task.tryEmitValue(event);
        }
    }

    public Mono<EventWithBacklogId> getEventWithBacklogId() {
        return task.asMono();
    }
}
