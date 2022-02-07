// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

@JsonSerialize(using = RntbdChannelAcquisitionEvent.RntbdChannelAcquisitionEventJsonSerializer.class)
public class RntbdChannelAcquisitionEvent {
    private final Instant createdTime;
    private final RntbdChannelAcquisitionEventType eventType;
    private volatile Instant completeTime;

    public RntbdChannelAcquisitionEvent(RntbdChannelAcquisitionEventType eventType, Instant createdTime) {
        this.eventType = eventType;
        this.createdTime = createdTime;
    }

    public Instant getCreatedTime() {
        return createdTime;
    }

    public RntbdChannelAcquisitionEventType getEventType() {
        return eventType;
    }

    public Instant getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(Instant completeTime) {
        this.completeTime = completeTime;
    }

    public void complete(Instant completeTime) {
        this.completeTime = completeTime;
    }

    public void addDetail(Object detail) {}

    public static void addDetail(RntbdChannelAcquisitionEvent event, Object detail) {
        if (event != null) {
            event.addDetail(detail);
        }
    }

    public static class RntbdChannelAcquisitionEventJsonSerializer extends com.fasterxml.jackson.databind.JsonSerializer<RntbdChannelAcquisitionEvent> {
        @Override
        public void serialize(RntbdChannelAcquisitionEvent event,
                              JsonGenerator writer,
                              SerializerProvider serializerProvider) throws IOException {
            writer.writeStartObject();

            writer.writeStringField(event.eventType.toString(), event.createdTime.toString());
            if (event.completeTime != null) {
                writer.writeNumberField("durationInMicroSec",Duration.between(event.createdTime, event.completeTime).toNanos()/1000L);
            }

            writer.writeEndObject();
        }
    }
}
