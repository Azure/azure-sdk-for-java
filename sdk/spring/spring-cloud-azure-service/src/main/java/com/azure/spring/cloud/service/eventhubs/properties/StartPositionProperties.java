// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.eventhubs.properties;

import com.azure.spring.cloud.service.implementation.eventhubs.properties.EventProcessorClientProperties;

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

    /**
     * Set the offset.
     * @param offset The offset.
     */
    public void setOffset(String offset) {
        this.offset = offset;
    }

    @Override
    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Set the sequence number.
     * @param sequenceNumber the sequence number.
     */
    public void setSequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public Instant getEnqueuedDateTime() {
        return enqueuedDateTime;
    }

    /**
     * Set the enqueued date time.
     * @param enqueuedDateTime The enqueued date time.
     */
    public void setEnqueuedDateTime(Instant enqueuedDateTime) {
        this.enqueuedDateTime = enqueuedDateTime;
    }

    @Override
    public boolean isInclusive() {
        return inclusive;
    }

    /**
     * Whether the position is inclusive.
     * @param inclusive Whether the position is inclusive.
     */
    public void setInclusive(boolean inclusive) {
        this.inclusive = inclusive;
    }
}
