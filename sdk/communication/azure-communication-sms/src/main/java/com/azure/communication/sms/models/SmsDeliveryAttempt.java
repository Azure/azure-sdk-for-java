// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms.models;

import com.azure.core.annotation.Immutable;
import java.time.OffsetDateTime;

/**
 * Details of an SMS delivery attempt.
 */
@Immutable
public final class SmsDeliveryAttempt {
    private final OffsetDateTime timestamp;
    private final int segmentsSucceeded;
    private final int segmentsFailed;

    /**
     * Creates an instance of SmsDeliveryAttempt.
     *
     * @param timestamp         The timestamp of the delivery attempt.
     * @param segmentsSucceeded Number of message segments that were successfully
     *                          delivered.
     * @param segmentsFailed    Number of message segments that failed to be
     *                          delivered.
     */
    public SmsDeliveryAttempt(OffsetDateTime timestamp, int segmentsSucceeded, int segmentsFailed) {
        this.timestamp = timestamp;
        this.segmentsSucceeded = segmentsSucceeded;
        this.segmentsFailed = segmentsFailed;
    }

    /**
     * Gets the timestamp of the delivery attempt.
     *
     * @return the timestamp of the delivery attempt.
     */
    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the number of message segments that were successfully delivered.
     *
     * @return the number of message segments that were successfully delivered.
     */
    public int getSegmentsSucceeded() {
        return segmentsSucceeded;
    }

    /**
     * Gets the number of message segments that failed to be delivered.
     *
     * @return the number of message segments that failed to be delivered.
     */
    public int getSegmentsFailed() {
        return segmentsFailed;
    }
}
