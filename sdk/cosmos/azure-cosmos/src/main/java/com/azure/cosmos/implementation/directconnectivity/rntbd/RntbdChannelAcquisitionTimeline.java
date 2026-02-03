// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class RntbdChannelAcquisitionTimeline {
    private final List<RntbdChannelAcquisitionEvent> events;
    private volatile RntbdChannelAcquisitionEvent currentEvent;
    private volatile boolean waitForChannelInit = false;

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
            RntbdChannelAcquisitionEvent newEvent = new RntbdChannelAcquisitionEvent(eventType, Instant.now());
            timeline.addNewEvent(newEvent);
            if (eventType == RntbdChannelAcquisitionEventType.ATTEMPT_TO_CREATE_NEW_CHANNEL_COMPLETE) {
                timeline.waitForChannelInit = true;
            }

            return newEvent;
        }
        return null;
    }

    public static RntbdPollChannelEvent startNewPollEvent(
        RntbdChannelAcquisitionTimeline timeline,
        int availableChannels,
        int acquiredChannels) {

        if (timeline != null) {
            RntbdPollChannelEvent newEvent = new RntbdPollChannelEvent(availableChannels, acquiredChannels, Instant.now());
            timeline.addNewEvent(newEvent);
            return newEvent;
        }

        return null;
    }

    private void addNewEvent(RntbdChannelAcquisitionEvent event) {
        if (this.currentEvent != null) {
            this.currentEvent.complete(event.getCreatedTime());
        }
        this.events.add(event);
        this.currentEvent = event;
    }

    public static void addDetailsToLastEvent(RntbdChannelAcquisitionTimeline timeline, Object detail) {
        if (timeline != null && timeline.currentEvent != null){
            RntbdChannelAcquisitionEvent.addDetail(timeline.currentEvent, detail);
        }
    }

    public boolean isWaitForChannelInit() {
        return this.waitForChannelInit;
    }
}
