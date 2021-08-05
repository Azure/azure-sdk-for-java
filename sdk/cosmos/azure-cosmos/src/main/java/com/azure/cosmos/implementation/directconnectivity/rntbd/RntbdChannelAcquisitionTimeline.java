// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RntbdChannelAcquisitionTimeline {
    private static final Logger logger = LoggerFactory.getLogger(RntbdChannelAcquisitionTimeline.class);
    private final List<RntbdChannelAcquisitionEvent> events;

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
            RntbdChannelAcquisitionEvent newEvent = new RntbdChannelAcquisitionEvent(eventType);
            timeline.getEvents().add(newEvent);
            return newEvent;
        }

        return null;
    }

    public static void addDetailsToLastEvent(RntbdChannelAcquisitionTimeline timeline, Object detail) {
        if (timeline != null){
            RntbdChannelAcquisitionEvent lastEvent = timeline.events.get(timeline.events.size()-1);
            if (lastEvent == null) {
                logger.warn("Can not find last event");
            }

            RntbdChannelAcquisitionEvent.addDetails(lastEvent, detail);
        }
    }
}
