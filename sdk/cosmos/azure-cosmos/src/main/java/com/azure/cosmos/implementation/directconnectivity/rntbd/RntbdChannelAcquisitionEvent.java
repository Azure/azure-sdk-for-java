// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class RntbdChannelAcquisitionEvent {
    @JsonSerialize(using = ToStringSerializer.class)
    private final Instant timestamp;

    private final RntbdChannelAcquisitionEventType eventType;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<Object> details;

    public RntbdChannelAcquisitionEvent(RntbdChannelAcquisitionEventType eventType) {
        this.eventType = eventType;
        this.timestamp = Instant.now();
        this.details = new ArrayList<Object>();
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public List<Object> getDetails() {
        return details;
    }

    public RntbdChannelAcquisitionEventType getEventType() {
        return eventType;
    }

    public static void addDetails(RntbdChannelAcquisitionEvent event, Object detail) {
        if (event != null) {
            event.getDetails().add(detail);
        }
    }
}
