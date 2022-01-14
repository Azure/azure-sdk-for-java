// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.eventhubs.properties;

import java.time.Instant;

/**
 * The starting position from which to consume events.
 */
public class StartPositionProperties implements EventProcessorClientProperties.StartPosition {

    private String offset;
    private Long sequenceNumber;
    private Instant enqueuedDateTime;
    private boolean inclusive;

    @Override
    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    @Override
    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public Instant getEnqueuedDateTime() {
        return enqueuedDateTime;
    }

    public void setEnqueuedDateTime(Instant enqueuedDateTime) {
        this.enqueuedDateTime = enqueuedDateTime;
    }

    public boolean isInclusive() {
        return inclusive;
    }

    public void setInclusive(boolean inclusive) {
        this.inclusive = inclusive;
    }
}
