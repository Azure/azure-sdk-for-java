// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.eventprocessor;

import com.azure.communication.callautomation.models.events.CallAutomationEventBase;

import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Event backlog that saves event for cases where events arrive earlier than the response of the method call.
 */
public final class EventBacklog {
    private static final long DEFAULT_BACKLOG_EVENT_TIMEOUT_SECONDS = 5;
    private static final int MAXIMUM_EVENTBACKLOGS_AT_ONCE = 10000;
    private final long expiringTimeout;

    // Key: Backlog event ID, Value: Event
    private final ConcurrentHashMap<String, CallAutomationEventBase> eventBacklog;

    public EventBacklog() {
        this(DEFAULT_BACKLOG_EVENT_TIMEOUT_SECONDS);
    }

    public EventBacklog(long expiringTimeout) {
        this.expiringTimeout = expiringTimeout;
        eventBacklog = new ConcurrentHashMap<>();
    }

    public EventWithBacklogId addEvent(String backlogEventId, CallAutomationEventBase eventToBeSaved) {
        if (eventBacklog.size() >= MAXIMUM_EVENTBACKLOGS_AT_ONCE) {
            return null;
        }

        // Add the event into the backlog.
        eventBacklog.put(backlogEventId, eventToBeSaved);

        // Remove the event after a preset timeout.
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                eventBacklog.remove(backlogEventId);
            }
        }, expiringTimeout * 1000);

        return new EventWithBacklogId(backlogEventId, eventToBeSaved);
    }

    public EventWithBacklogId tryGetAndRemoveMatchedEvent(Predicate<CallAutomationEventBase> predicate) {
        Entry<String, CallAutomationEventBase> searchResult = eventBacklog.searchEntries(1, (entry) -> {
            return predicate.test(entry.getValue()) ? entry : null;
        });

        if (searchResult != null && eventBacklog.remove(searchResult.getKey(), searchResult.getValue())) {
            return new EventWithBacklogId(searchResult.getKey(), searchResult.getValue());
        } else {
            return null;
        }
    }

    public EventWithBacklogId removeEvent(String backlogEventId) {
        return backlogEventId != null
            ? new EventWithBacklogId(backlogEventId, eventBacklog.remove(backlogEventId))
            : null;
    }
}
