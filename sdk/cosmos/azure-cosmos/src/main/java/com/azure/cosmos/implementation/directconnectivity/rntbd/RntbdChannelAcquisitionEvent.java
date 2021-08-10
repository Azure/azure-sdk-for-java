// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@JsonSerialize(using = RntbdChannelAcquisitionEvent.RntbdChannelAcquisitionEventJsonSerializer.class)
public class RntbdChannelAcquisitionEvent {
    private final Instant createdTime;
    private final RntbdChannelAcquisitionEventType eventType;
    private volatile Instant completeTime;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<Object> details;

    public RntbdChannelAcquisitionEvent(RntbdChannelAcquisitionEventType eventType, Instant createdTime) {
        this.eventType = eventType;
        this.createdTime = createdTime;
        this.details = new ArrayList<Object>();
    }

    public void complete(Instant completeTime) {
        this.completeTime = completeTime;
    }

    public static void addDetails(RntbdChannelAcquisitionEvent event, Object detail) {
        if (event != null) {
            event.details.add(detail);
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

            if (event.details != null && event.details.size() > 0) {
                writer.writeObjectField("details", event.details);
            }

            writer.writeEndObject();
        }
    }
}
