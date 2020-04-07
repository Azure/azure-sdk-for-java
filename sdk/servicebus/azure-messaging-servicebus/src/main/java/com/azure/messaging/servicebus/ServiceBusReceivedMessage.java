// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.ReceiveMode;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * This class represents a received message from Service Bus.
 */
public final class ServiceBusReceivedMessage implements MessageLockToken {
    private UUID lockToken;
    private long sequenceNumber;
    private long deliveryCount;
    private Instant enqueuedTime;
    private Instant lockedUntil;
    private String deadLetterSource;

    private final Map<String, Object> properties;
    private final byte[] body;
    private String contentType;
    private String correlationId;
    private String label;
    private String messageId;
    private String partitionKey;
    private String replyTo;
    private String replyToSessionId;
    private Instant scheduledEnqueueTime;
    private String sessionId;
    private Duration timeToLive;
    private String to;
    private String viaPartitionKey;

    ServiceBusReceivedMessage(byte[] body) {
        this.body = Objects.requireNonNull(body, "'body' cannot be null.");
        this.properties = new HashMap<>();
    }

    /**
     * Gets the actual payload/data wrapped by the {@link ServiceBusReceivedMessage}.
     *
     * <p>
     * If the means for deserializing the raw data is not apparent to consumers, a common technique is to make use of
     * {@link #getProperties()} when creating the event, to associate serialization hints as an aid to consumers who
     * wish to deserialize the binary data.
     * </p>
     *
     * @return A byte array representing the data.
     */
    public byte[] getBody() {
        return Arrays.copyOf(body, body.length);
    }

    /**
     * Gets the content type of the message.
     *
     * @return the contentType of the {@link ServiceBusReceivedMessage}.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Gets a correlation identifier.
     * <p>
     * Allows an application to specify a context for the message for the purposes of correlation, for example
     * reflecting the MessageId of a message that is being replied to.
     * </p>
     *
     * @return correlation id of this message
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-messages-payloads?#message-routing-and-correlation">Message
     *     Routing and Correlation</a>
     */
    public String getCorrelationId() {
        return correlationId;
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
     * Gets the instant at which this message will expire.
     * <p>
     * The value is the UTC instant for when the message is scheduled for removal and will no longer available for
     * retrieval from the entity due to expiration. Expiry is controlled by the {@link #getTimeToLive() TimeToLive}
     * property. This property is computed from {@link #getEnqueuedTime() EnqueuedTime} plus {@link #getTimeToLive()
     * TimeToLive}.
     *
     * @return {@link Instant} at which this message expires
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-expiration">Message Expiration</a>
     */
    public Instant getExpiresAt() {
        final Duration timeToLive = getTimeToLive();
        return enqueuedTime != null && timeToLive != null
            ? enqueuedTime.plus(timeToLive)
            : null;
    }

    /**
     * Gets the label for the message.
     *
     * @return The label for the message.
     */
    public String getLabel() {
        return label;
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
     * @return Lock-token for this message.
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-transfers-locks-settlement">Message
     * transfers, locks, and settlement</a>
     */
    @Override
    public String getLockToken() {
        return lockToken.toString();
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
     * @return Id of the {@link ServiceBusReceivedMessage}.
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Gets the partition key for sending a message to a partitioned entity.
     * <p>
     * For <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-partitioning">partitioned
     * entities</a>, setting this value enables assigning related messages to the same internal partition, so that
     * submission sequence order is correctly recorded. The partition is chosen by a hash function over this value and
     * cannot be chosen directly. For session-aware entities, the {@link #getSessionId() sessionId} property overrides
     * this value.
     *
     * @return The partition key of this message
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-partitioning">Partitioned
     *     entities</a>
     */
    public String getPartitionKey() {
        return partitionKey;
    }

    /**
     * Gets the set of free-form {@link ServiceBusReceivedMessage} properties which may be used for passing metadata
     * associated with the {@link ServiceBusReceivedMessage} during Service Bus operations. A common use-case for
     * {@code properties()} is to associate serialization hints for the {@link #getBody()} as an aid to consumers
     * who wish to deserialize the binary data.
     *
     * @return Application properties associated with this {@link ServiceBusReceivedMessage}.
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Gets the address of an entity to send replies to.
     * <p>
     * This optional and application-defined value is a standard way to express a reply path to the receiver of the
     * message. When a sender expects a reply, it sets the value to the absolute or relative path of the queue or topic
     * it expects the reply to be sent to.
     *
     * @return ReplyTo property value of this message
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-messages-payloads?#message-routing-and-correlation">Message
     *     Routing and Correlation</a>
     */
    public String getReplyTo() {
        return replyTo;
    }

    /**
     * Gets or sets a session identifier augmenting the {@link #getReplyTo() ReplyTo} address.
     * <p>
     * This value augments the ReplyTo information and specifies which SessionId should be set for the reply when sent
     * to the reply entity.
     *
     * @return ReplyToSessionId property value of this message
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-messages-payloads?#message-routing-and-correlation">Message
     *     Routing and Correlation</a>
     */
    public String getReplyToSessionId() {
        return replyToSessionId;
    }

    /**
     * Gets the scheduled enqueue time of this message.
     * <p>
     * This value is used for delayed message availability. The message is safely added to the queue, but is not
     * considered active and therefore not retrievable until the scheduled enqueue time. Mind that the message may not
     * be activated (enqueued) at the exact given instant; the actual activation time depends on the queue's workload
     * and its state.
     * </p>
     *
     * @return the instant at which the message will be enqueued in Azure Service Bus
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-sequencing">Message Sequencing and
     *     Timestamps</a>
     */
    public Instant getScheduledEnqueueTime() {
        return scheduledEnqueueTime;
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
     * Gets the session id of the message.
     *
     * @return Session Id of the {@link ServiceBusReceivedMessage}.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Gets the duration before this message expires.
     * <p>
     * This value is the relative duration after which the message expires, starting from the instant the message has
     * been accepted and stored by the broker, as captured in {@link #getScheduledEnqueueTime()}. When not set
     * explicitly, the assumed value is the DefaultTimeToLive set for the respective queue or topic. A message-level
     * TimeToLive value cannot be longer than the entity's DefaultTimeToLive setting and it is silently adjusted if it
     * does.
     *
     * @return Time to live duration of this message
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-expiration">Message Expiration</a>
     */
    public Duration getTimeToLive() {
        return timeToLive;
    }

    /**
     * Gets the "to" address.
     *
     * @return "To" property value of this message
     */
    public String getTo() {
        return to;
    }

    /**
     * Gets the partition key for sending a message to a entity via another partitioned transfer entity.
     *
     * If a message is sent via a transfer queue in the scope of a transaction, this value selects the
     * transfer queue partition: This is functionally equivalent to {@link #getPartitionKey()} and ensures that
     * messages are kept together and in order as they are transferred.
     *
     * @return partition key on the via queue.
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-transactions#transfers-and-send-via">Transfers and Send Via</a>
     */
    public String getViaPartitionKey() {
        return viaPartitionKey;
    }

    /**
     * Sets a correlation identifier.
     *
     * @param correlationId correlation id of this message
     *
     * @see #getCorrelationId()
     */
    void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    /**
     * Sets the content type of the {@link ServiceBusReceivedMessage}.
     *
     * @param contentType of the message.
     */
    void setContentType(String contentType) {
        this.contentType = contentType;
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
     * Sets the number of the times this message was delivered to clients.
     *
     * @param deliveryCount the number of the times this message was delivered to clients.
     */
    void setDeliveryCount(long deliveryCount) {
        this.deliveryCount = deliveryCount;
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
     * Sets the label for the message.
     *
     * @param label The label to set.
     */
    void setLabel(String label) {
        this.label = label;
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
     * Sets the instant at which the lock of this message expires.
     *
     * @param lockedUntil the instant at which the lock of this message expires.
     */
    void setLockedUntil(Instant lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    /**
     * Sets the message id.
     *
     * @param messageId to be set.
     */
    void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * Sets a partition key for sending a message to a partitioned entity
     *
     * @param partitionKey partition key of this message
     *
     * @see #getPartitionKey()
     */
    void setPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
    }

    /**
     * Sets the scheduled enqueue time of this message.
     *
     * @param scheduledEnqueueTime the instant at which this message should be enqueued in Azure Service Bus.
     *
     * @see #getScheduledEnqueueTime()
     */
    void setScheduledEnqueueTime(Instant scheduledEnqueueTime) {
        this.scheduledEnqueueTime = scheduledEnqueueTime;
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
     * Sets the session id.
     *
     * @param sessionId to be set.
     */
    void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Sets the duration of time before this message expires.
     *
     * @param timeToLive Time to Live duration of this message
     *
     * @see #getTimeToLive()
     */
    void setTimeToLive(Duration timeToLive) {
        this.timeToLive = timeToLive;
    }

    /**
     * Sets the address of an entity to send replies to.
     *
     * @param replyTo ReplyTo property value of this message
     *
     * @see #getReplyTo()
     */
    void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    /**
     * Gets or sets a session identifier augmenting the {@link #getReplyTo() ReplyTo} address.
     *
     * @param replyToSessionId ReplyToSessionId property value of this message
     */
    void setReplyToSessionId(String replyToSessionId) {
        this.replyToSessionId = replyToSessionId;
    }

    /**
     * Sets the "to" address.
     * <p>
     * This property is reserved for future use in routing scenarios and presently ignored by the broker itself.
     * Applications can use this value in rule-driven
     * <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-auto-forwarding">auto-forward
     * chaining</a> scenarios to indicate the intended logical destination of the message.
     *
     * @param to To property value of this message
     */
    void setTo(String to) {
        this.to = to;
    }

    /**
     * Sets a via-partition key for sending a message to a destination entity via another partitioned entity
     *
     * @param viaPartitionKey via-partition key of this message
     *
     * @see #getViaPartitionKey()
     */
    void setViaPartitionKey(String viaPartitionKey) {
        this.viaPartitionKey = viaPartitionKey;
    }

    /**
     * Takes the {@link ServiceBusReceivedMessage} and create an instance of {@link ServiceBusMessage}.
     * This is normally used when a {@link ServiceBusReceivedMessage} needs to be sent to another entity.
     *
     * @return Created {@link ServiceBusMessage} instance.
     */
    public ServiceBusMessage toServiceBusMessage() {
        ServiceBusMessage message =  new ServiceBusMessage(body);
        message.setMessageId(getMessageId());
        message.setScheduledEnqueueTime(getScheduledEnqueueTime());
        message.setContentType(getContentType());
        message.setCorrelationId(getCorrelationId());
        message.setLabel(getLabel());
        message.setPartitionKey(getPartitionKey());
        message.setReplyTo(getReplyTo());
        message.setReplyToSessionId(getReplyToSessionId());
        message.setTimeToLive(getTimeToLive());
        message.setTo(getTo());
        message.setSessionId(getSessionId());
        message.setViaPartitionKey(getViaPartitionKey());
        return message;
    }
}
