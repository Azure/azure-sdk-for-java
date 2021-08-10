// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class RntbdChannelAcquisitionTimeline {
    private static final Logger logger = LoggerFactory.getLogger(RntbdChannelAcquisitionTimeline.class);
    private final List<RntbdChannelAcquisitionEvent> events;
    private volatile RntbdChannelAcquisitionEvent currentEvent;

    public RntbdChannelAcquisitionTimeline() {
        this.events = new ArrayList<>();
    }

    public List<RntbdChannelAcquisitionEvent> getEvents() {
        return events;
    }

    public static RntbdChannelAcquisitionEvent startNewEvent(
        RntbdChannelAcquisitionTimeline timeline,
        RntbdChannelAcquisitionEventType eventType) {

        if (timeline != null) {
            Instant now = Instant.now();

            if (timeline.currentEvent != null) {
                timeline.currentEvent.complete(now);
            }

            RntbdChannelAcquisitionEvent newEvent = new RntbdChannelAcquisitionEvent(eventType, now);
            timeline.getEvents().add(newEvent);
            timeline.currentEvent = newEvent;

            return newEvent;
        }

        return null;
    }

    public static void addDetailsToLastEvent(RntbdChannelAcquisitionTimeline timeline, Object detail) {
        if (timeline != null && timeline.events.size() > 0){
            RntbdChannelAcquisitionEvent lastEvent = timeline.events.get(timeline.events.size()-1);
            RntbdChannelAcquisitionEvent.addDetails(lastEvent, detail);
        }
    }
}
