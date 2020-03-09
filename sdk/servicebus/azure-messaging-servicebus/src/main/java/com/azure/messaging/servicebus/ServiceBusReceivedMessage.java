// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.ReceiveMode;

import java.time.Instant;
import java.util.UUID;

/**
 * This class represents a received message from Service Bus.
 */
public final class ServiceBusReceivedMessage extends ServiceBusMessage {
    private UUID lockToken;
    private long sequenceNumber;
    private long deliveryCount;
    private Instant enqueuedTime;
    private Instant expiresAt;
    private Instant lockedUntil;
    private String deadLetterSource;

    ServiceBusReceivedMessage(byte[] body) {
        super(body);
    }

    /**
     * Gets the number of the times this message was delivered to clients.
     * <p>
     * The count is incremented when a message lock expires, or the message is explicitly abandoned by
     * the receiver. This property is read-only.
     *
     * @return delivery count of this message.
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-transfers-locks-settlement">Message
     *     transfers, locks, and settlement.</a>
     */
    public long getDeliveryCount() {
        return deliveryCount;
    }

    /**
     * Sets the number of the times this message was delivered to clients.
     *
     * @param deliveryCount the number of the times this message was delivered to clients.
     */
    void setDeliveryCount(long deliveryCount) {
        this.deliveryCount = deliveryCount;
    }

    /**
     * Gets the name of the queue or subscription that this message was enqueued on, before it was
     * deadlettered.
     * <p>
     * This value is only set in messages that have been dead-lettered and subsequently auto-forwarded
     * from the dead-letter queue  to another entity. Indicates the entity in which the message
     * was dead-lettered. This property is read-only.
     *
     * @return dead letter source of this message
     *
     * @see <a href="https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-dead-letter-queues">Dead-letter
     *     queues</a>
     */
    public String getDeadLetterSource() {
        return deadLetterSource;
    }

    /**
     * Sets the name of the queue or subscription that this message was enqueued on, before it was
     * deadlettered.
     *
     * @param deadLetterSource the name of the queue or subscription that this message was enqueued on,
     * before it was deadlettered.
     */
    void setDeadLetterSource(String deadLetterSource) {
        this.deadLetterSource = deadLetterSource;
    }

    /**
     * Gets the lock token for the current message.
     * <p>
     * The lock token is a reference to the lock that is being held by the broker in
     * {@link ReceiveMode#PEEK_LOCK} mode.
     * Locks are used to explicitly settle messages as explained in the
     * <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-transfers-locks-settlement">product
     * documentation in more detail</a>. The token can also be used to pin the lock permanently
     * through the <a
     * href="https://docs.microsoft.com/azure/service-bus-messaging/message-deferral">Deferral API</a> and, with that,
     * take the message out of the regular delivery state flow. This property is read-only.
     *
     * @return lock token of this message.
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-transfers-locks-settlement">Message
     * transfers, locks, and settlement</a>
     */
    public UUID getLockToken() {
        return lockToken;
    }

    /**
     * Sets the lock token for the current message.
     *
     * @param lockToken the lock token for the current message.
     */
    void setLockToken(UUID lockToken) {
        this.lockToken = lockToken;
    }

    /**
     * Gets the instant at which this message was enqueued in Azure Service Bus.
     * <p>
     * The UTC instant at which the message has been accepted and stored in the entity. For scheduled messages, this
     * reflects the time when the message was activated. This value can be used as an authoritative and neutral arrival
     * time indicator when the receiver does not want to trust the sender's clock. This property is read-only.
     *
     * @return the instant at which the message was enqueued in Azure Service Bus
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-sequencing">Message Sequencing and
     *     Timestamps</a>
     */
    public Instant getEnqueuedTime() {
        return enqueuedTime;
    }

    /**
     * Sets the instant at which this message was enqueued in Azure Service Bus.
     *
     * @param enqueuedTime the instant at which this message was enqueued in Azure Service Bus.
     */
    void setEnqueuedTime(Instant enqueuedTime) {
        this.enqueuedTime = enqueuedTime;
    }

    /**
     * Gets the unique number assigned to a message by Service Bus.
     * <p>
     * The sequence number is a unique 64-bit integer assigned to a message as it is accepted and stored by the broker
     * and functions as its true identifier. For partitioned entities, the topmost 16 bits reflect the partition
     * identifier. Sequence numbers monotonically increase and are gapless. They roll over to 0 when the 48-64 bit range
     * is exhausted. This property is read-only.
     *
     * @return sequence number of this message
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-sequencing">Message Sequencing and
     *     Timestamps</a>
     */
    public long getSequenceNumber() {
        return this.sequenceNumber;
    }

    /**
     * Sets the unique number assigned to a message by Service Bus.
     *
     * @param sequenceNumber the unique number assigned to a message by Service Bus.
     */
    void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    /**
     * Gets the instant at which this message will expire.
     * <p>
     * The value is the UTC instant for when the message is scheduled for removal and will no longer available for
     * retrieval from the entity due to expiration. Expiry is controlled by the {@link #getTimeToLive() TimeToLive}
     * property. This property is computed from {@link #getEnqueuedTime() EnqueuedTime} plus {@link #getTimeToLive()
     * TimeToLive}.
     *
     * @return instant at which this message expires
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-expiration">Message Expiration</a>
     */
    public Instant getExpiresAt() {
        return expiresAt;
    }

    /**
     * Sets the instant at which this message will expire.
     *
     * @param expiresAt the instant at which this message will expire.
     */
    void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    /**
     * Gets the instant at which the lock of this message expires.
     * <p>
     * For messages retrieved under a lock (peek-lock receive mode, not pre-settled) this property reflects the UTC
     * instant until which the message is held locked in the queue/subscription. When the lock expires, the {@link
     * #getDeliveryCount() DeliveryCount} is incremented and the message is again available for retrieval. This property
     * is read-only.
     *
     * @return the instant at which the lock of this message expires if the message is received using {@link
     *     ReceiveMode#PEEK_LOCK} mode. Otherwise it returns null.
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-transfers-locks-settlement">Message
     *     transfers, locks, and settlement</a>
     */
    public Instant getLockedUntil() {
        return lockedUntil;
    }

    /**
     * Sets the instant at which the lock of this message expires.
     *
     * @param lockedUntil the instant at which the lock of this message expires.
     */
    void setLockedUntil(Instant lockedUntil) {
        this.lockedUntil = lockedUntil;
    }
}
