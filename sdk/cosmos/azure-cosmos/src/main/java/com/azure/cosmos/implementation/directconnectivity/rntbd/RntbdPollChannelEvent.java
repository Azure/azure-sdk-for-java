// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@JsonSerialize(using = RntbdPollChannelEvent.RntbdPollChannelEventJsonSerializer.class)
public class RntbdPollChannelEvent extends RntbdChannelAcquisitionEvent {
    private final int availableChannels;
    private final int acquiredChannels;
    private final List<Object> details;

    public RntbdPollChannelEvent(int availableChannels, int acquiredChannels, Instant createdTime) {

        super(RntbdChannelAcquisitionEventType.ATTEMPT_TO_POLL_CHANNEL, createdTime);
        this.availableChannels = availableChannels;
        this.acquiredChannels = acquiredChannels;
        this.details = new ArrayList<>();
    }

    @Override
    public void addDetail(Object detail) {
        this.details.add(detail);
    }

    public static class RntbdPollChannelEventJsonSerializer extends com.fasterxml.jackson.databind.JsonSerializer<RntbdPollChannelEvent> {
        @Override
        public void serialize(RntbdPollChannelEvent event,
                              JsonGenerator writer,
                              SerializerProvider serializerProvider) throws IOException {
            writer.writeStartObject();

            writer.writeStringField(event.getEventType().toString(), event.getCreatedTime().toString());
            if (event.availableChannels > 0) {
                writer.writeNumberField("availableChannels", event.availableChannels);
            }

            if (event.acquiredChannels > 0) {
                writer.writeNumberField("acquiredChannels", event.acquiredChannels);
            }

            if (event.getCompleteTime() != null) {
                writer.writeNumberField(
                    "durationInMicroSec",
                    Duration.between(event.getCompleteTime(), event.getCompleteTime()).toNanos()/1000L);
            }

            if (event.details != null && event.details.size() > 0) {
                writer.writeObjectField("details", event.details);
            }

            writer.writeEndObject();
        }
    }
}
