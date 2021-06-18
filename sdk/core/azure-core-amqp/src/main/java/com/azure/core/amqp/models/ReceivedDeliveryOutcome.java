// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

/**
 * Represents a partial message that was received.
 *
 * @see DeliveryState
 * @see <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-messaging-v1.0-os.html#type-received">Received
 *     outcome</a>
 */
public final class ReceivedDeliveryOutcome extends DeliveryOutcome {
    private final int sectionNumber;
    private final long sectionOffset;

    /**
     * Creates an instance of the delivery outcome with its state.
     *
     * @param sectionNumber Section number within the message that can be resent or may not have been received.
     * @param sectionOffset First byte of the section where data can be resent, or first byte of the section where
     *     it may not have been received.
     */
    public ReceivedDeliveryOutcome(int sectionNumber, long sectionOffset) {
        super(DeliveryState.RECEIVED);
        this.sectionNumber = sectionNumber;
        this.sectionOffset = sectionOffset;
    }

    /**
     * Gets the section number.
     * <p>
     * When sent by the sender this indicates the first section of the message (with section-number 0 being the first
     * section) for which data can be resent. Data from sections prior to the given section cannot be retransmitted for
     * this delivery.
     * <p>
     * When sent by the receiver this indicates the first section of the message for which all data might not yet have
     * been received.
     *
     * @return Gets the section number of this outcome.
     */
    public int getSectionNumber() {
        return sectionNumber;
    }

    /**
     * Gets the section offset.
     * <p>
     * When sent by the sender this indicates the first byte of the encoded section data of the section given by
     * section-number for which data can be resent (with section-offset 0 being the first byte). Bytes from the same
     * section prior to the given offset section cannot be retransmitted for this delivery.
     * <p>
     * When sent by the receiver this indicates the first byte of the given section which has not yet been received.
     * Note that if a receiver has received all of section number X (which contains N bytes of data), but none of
     * section number X + 1, then it can indicate this by sending either Received(section-number=X, section-offset=N) or
     * Received(section-number=X+1, section-offset=0). The state Received(section-number=0, section-offset=0) indicates
     * that no message data at all has been transferred.
     *
     * @return The section offset.
     */
    public long getSectionOffset() {
        return sectionOffset;
    }
}
