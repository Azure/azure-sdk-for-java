// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.clienttelemetry.ClientTelemetry;
import com.azure.cosmos.implementation.clienttelemetry.ReportPayload;
import org.HdrHistogram.ConcurrentDoubleHistogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
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
        RntbdChannelAcquisitionEventType eventType,
        ClientTelemetry clientTelemetry) {

        if (timeline != null) {
            RntbdChannelAcquisitionEvent newEvent = new RntbdChannelAcquisitionEvent(eventType, Instant.now());
            timeline.addNewEvent(newEvent, clientTelemetry);

            return newEvent;
        }
        return null;
    }

    public static RntbdPollChannelEvent startNewPollEvent(
        RntbdChannelAcquisitionTimeline timeline,
        int availableChannels,
        int acquiredChannels,
        ClientTelemetry clientTelemetry) {

        if (timeline != null) {
            RntbdPollChannelEvent newEvent = new RntbdPollChannelEvent(availableChannels, acquiredChannels, Instant.now());
            timeline.addNewEvent(newEvent, clientTelemetry);
            return newEvent;
        }

        return null;
    }

    private void addNewEvent(RntbdChannelAcquisitionEvent event, ClientTelemetry clientTelemetry) {
        if (this.currentEvent != null) {
            this.currentEvent.complete(event.getCreatedTime());
            if(clientTelemetry!= null && clientTelemetry.isClientTelemetryEnabled()) {
                if (event.getEventType().equals(RntbdChannelAcquisitionEventType.ATTEMPT_TO_CREATE_NEW_CHANNEL_COMPLETE)) {
                    ReportPayload reportPayload = new ReportPayload(ClientTelemetry.TCP_NEW_CHANNEL_LATENCY_NAME,
                        ClientTelemetry.TCP_NEW_CHANNEL_LATENCY_UNIT);
                    ConcurrentDoubleHistogram newChannelLatencyHistogram =
                        clientTelemetry.getClientTelemetryInfo().getSystemInfoMap().get(reportPayload);
                    if (newChannelLatencyHistogram == null) {
                        newChannelLatencyHistogram =
                            new ConcurrentDoubleHistogram(ClientTelemetry.TCP_NEW_CHANNEL_LATENCY_MAX_MILLI_SEC,
                                ClientTelemetry.TCP_NEW_CHANNEL_LATENCY_PRECISION);
                        clientTelemetry.getClientTelemetryInfo().getSystemInfoMap().put(reportPayload, newChannelLatencyHistogram);
                    }
                    ClientTelemetry.recordValue(newChannelLatencyHistogram,
                        Duration.between(this.currentEvent.getCreatedTime(), this.currentEvent.getCompleteTime()).toMillis());
                }
            }
        }
        this.events.add(event);
        this.currentEvent = event;
    }

    public static void addDetailsToLastEvent(RntbdChannelAcquisitionTimeline timeline, Object detail) {
        if (timeline != null && timeline.currentEvent != null){
            RntbdChannelAcquisitionEvent.addDetail(timeline.currentEvent, detail);
        }
    }
}
