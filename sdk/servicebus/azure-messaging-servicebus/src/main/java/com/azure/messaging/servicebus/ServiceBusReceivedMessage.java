// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.PARTITION_KEY_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.SCHEDULED_ENQUEUE_UTC_TIME_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.VIA_PARTITION_KEY_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.LOCKED_UNTIL_KEY_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.DEAD_LETTER_SOURCE_KEY_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_SEQUENCE_NUMBER_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.DEAD_LETTER_DESCRIPTION_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.DEAD_LETTER_REASON_ANNOTATION_NAME;

import com.azure.core.amqp.AmqpMessageConstant;
import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.amqp.models.AmqpBodyType;
import com.azure.core.amqp.models.AmqpDataBody;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.models.ReceiveMode;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * This class represents a received message from Service Bus.
 */
public final class ServiceBusReceivedMessage {
    private final ClientLogger logger = new ClientLogger(ServiceBusReceivedMessage.class);

    private final AmqpAnnotatedMessage amqpAnnotatedMessage;
    private final byte[] binaryData;
    private UUID lockToken;

    /**
     * The representation of message as defined by AMQP protocol.
     *
     * @see <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-messaging-v1.0-os.html#section-message-format">
     *     Amqp Message Format.</a>
     *
     * @return the {@link AmqpAnnotatedMessage} representing amqp message.
     */
    public AmqpAnnotatedMessage getAmqpAnnotatedMessage() {
        return amqpAnnotatedMessage;
    }

    ServiceBusReceivedMessage(byte[] body) {
        binaryData = Objects.requireNonNull(body, "'body' cannot be null.");
        amqpAnnotatedMessage = new AmqpAnnotatedMessage(new AmqpDataBody(Collections.singletonList(
            binaryData)));
    }

    /**
     * Gets the actual payload/data wrapped by the {@link ServiceBusReceivedMessage}.
     *
     * <p>
     * If the means for deserializing the raw data is not apparent to consumers, a common technique is to make use of
     * {@link #getApplicationProperties()} when creating the event, to associate serialization hints as an aid to
     * consumers who wish to deserialize the binary data.
     * </p>
     *
     * @return A byte array representing the data.
     */
    public byte[] getBody() {
        final AmqpBodyType bodyType = amqpAnnotatedMessage.getBody().getBodyType();
        switch (bodyType) {
            case DATA:
                return Arrays.copyOf(binaryData, binaryData.length);
            case SEQUENCE:
            case VALUE:
                throw logger.logExceptionAsError(new UnsupportedOperationException("Body type not supported yet "
                    + bodyType.toString()));
            default:
                logger.warning("Invalid body type {}.", bodyType);
                throw logger.logExceptionAsError(new IllegalStateException("Body type not valid "
                    + bodyType.toString()));
        }
    }

    /**
     * Gets the content type of the message.
     *
     * @return the contentType of the {@link ServiceBusReceivedMessage}.
     */
    public String getContentType() {
        return amqpAnnotatedMessage.getProperties().getContentType();
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
        return amqpAnnotatedMessage.getProperties().getCorrelationId();
    }

    /**
     * Gets the description for a message that has been dead-lettered.
     *
     * @return The description for a message that has been dead-lettered.
     */
    public String getDeadLetterErrorDescription() {
        return getStringValue(amqpAnnotatedMessage.getApplicationProperties(),
            DEAD_LETTER_DESCRIPTION_ANNOTATION_NAME.getValue());
    }

    /**
     * Gets the reason for a message that has been dead-lettered.
     *
     * @return The reason for a message that has been dead-lettered.
     */
    public String getDeadLetterReason() {
        return getStringValue(amqpAnnotatedMessage.getApplicationProperties(),
            DEAD_LETTER_REASON_ANNOTATION_NAME.getValue());
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
        return getStringValue(amqpAnnotatedMessage.getMessageAnnotations(),
            DEAD_LETTER_SOURCE_KEY_ANNOTATION_NAME.getValue());
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
        return amqpAnnotatedMessage.getHeader().getDeliveryCount();
    }

    /**
     * Gets the enqueued sequence number assigned to a message by Service Bus.
     * <p>
     * The sequence number is a unique 64-bit integer first assigned to a message as it is accepted at its original
     * point of submission.
     *
     * @return enqueued sequence number of this message
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-sequencing">Message Sequencing and
     *     Timestamps</a>
     */
    public long getEnqueuedSequenceNumber() {
        return getLongValue(amqpAnnotatedMessage.getMessageAnnotations(),
            ENQUEUED_SEQUENCE_NUMBER_ANNOTATION_NAME.getValue());
    }

    /**
     * Gets the datetime at which this message was enqueued in Azure Service Bus.
     * <p>
     * The UTC datetime at which the message has been accepted and stored in the entity. For scheduled messages, this
     * reflects the time when the message was activated. This value can be used as an authoritative and neutral arrival
     * time indicator when the receiver does not want to trust the sender's clock. This property is read-only.
     *
     * @return the datetime at which the message was enqueued in Azure Service Bus
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-sequencing">Message Sequencing and
     *     Timestamps</a>
     */
    public OffsetDateTime getEnqueuedTime() {
        return getOffsetDateTimeValue(amqpAnnotatedMessage.getMessageAnnotations(),
            ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue());
    }

    /**
     * Gets the datetime at which this message will expire.
     * <p>
     * The value is the UTC datetime for when the message is scheduled for removal and will no longer available for
     * retrieval from the entity due to expiration. Expiry is controlled by the {@link #getTimeToLive() TimeToLive}
     * property. This property is computed from {@link #getEnqueuedTime() EnqueuedTime} plus {@link #getTimeToLive()
     * TimeToLive}.
     *
     * @return {@link OffsetDateTime} at which this message expires
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-expiration">Message Expiration</a>
     */
    public OffsetDateTime getExpiresAt() {
        final Duration timeToLive = getTimeToLive();
        final OffsetDateTime enqueuedTime = getEnqueuedTime();
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
        return amqpAnnotatedMessage.getProperties().getSubject();
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
     * @return Lock-token for this message. Could return {@code null} for {@link ReceiveMode#RECEIVE_AND_DELETE} mode.
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-transfers-locks-settlement">Message
     * transfers, locks, and settlement</a>
     */
    public String getLockToken() {
        return lockToken != null ? lockToken.toString() : null;
    }

    /**
     * Gets the datetime at which the lock of this message expires.
     * <p>
     * For messages retrieved under a lock (peek-lock receive mode, not pre-settled) this property reflects the UTC
     * datetime until which the message is held locked in the queue/subscription. When the lock expires, the {@link
     * #getDeliveryCount() DeliveryCount} is incremented and the message is again available for retrieval. This property
     * is read-only.
     *
     * @return the datetime at which the lock of this message expires if the message is received using {@link
     *     ReceiveMode#PEEK_LOCK} mode. Otherwise it returns null.
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-transfers-locks-settlement">Message
     *     transfers, locks, and settlement</a>
     */
    public OffsetDateTime getLockedUntil() {
        return getOffsetDateTimeValue(amqpAnnotatedMessage.getMessageAnnotations(),
            LOCKED_UNTIL_KEY_ANNOTATION_NAME.getValue());
    }

    /**
     * @return Id of the {@link ServiceBusReceivedMessage}.
     */
    public String getMessageId() {
        return amqpAnnotatedMessage.getProperties().getMessageId();
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
        return getStringValue(amqpAnnotatedMessage.getMessageAnnotations(),
            PARTITION_KEY_ANNOTATION_NAME.getValue());
    }

    /**
     * Gets the set of free-form {@link ServiceBusReceivedMessage} properties which may be used for passing metadata
     * associated with the {@link ServiceBusReceivedMessage} during Service Bus operations. A common use-case for
     * {@code properties()} is to associate serialization hints for the {@link #getBody()} as an aid to consumers
     * who wish to deserialize the binary data.
     *
     * @return Application properties associated with this {@link ServiceBusReceivedMessage}.
     */
    public Map<String, Object> getApplicationProperties() {
        return amqpAnnotatedMessage.getApplicationProperties();
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
        return amqpAnnotatedMessage.getProperties().getReplyTo();
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
        return amqpAnnotatedMessage.getProperties().getReplyToGroupId();
    }

    /**
     * Gets the scheduled enqueue time of this message.
     * <p>
     * This value is used for delayed message availability. The message is safely added to the queue, but is not
     * considered active and therefore not retrievable until the scheduled enqueue time. Mind that the message may not
     * be activated (enqueued) at the exact given datetime; the actual activation time depends on the queue's workload
     * and its state.
     * </p>
     *
     * @return the datetime at which the message will be enqueued in Azure Service Bus
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-sequencing">Message Sequencing and
     *     Timestamps</a>
     */
    public OffsetDateTime getScheduledEnqueueTime() {
        return getOffsetDateTimeValue(amqpAnnotatedMessage.getMessageAnnotations(),
            SCHEDULED_ENQUEUE_UTC_TIME_NAME.getValue());
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
        return getLongValue(amqpAnnotatedMessage.getMessageAnnotations(),
            SEQUENCE_NUMBER_ANNOTATION_NAME.getValue());
    }

    /**
     * Gets the session id of the message.
     *
     * @return Session Id of the {@link ServiceBusReceivedMessage}.
     */
    public String getSessionId() {
        return getAmqpAnnotatedMessage().getProperties().getGroupId();
    }

    /**
     * Gets the duration before this message expires.
     * <p>
     * This value is the relative duration after which the message expires, starting from the datetime the message has
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
        return amqpAnnotatedMessage.getHeader().getTimeToLive();
    }

    /**
     * Gets the "to" address.
     *
     * @return "To" property value of this message
     */
    public String getTo() {
        return amqpAnnotatedMessage.getProperties().getTo();
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
        return getStringValue(amqpAnnotatedMessage.getMessageAnnotations(),
            VIA_PARTITION_KEY_ANNOTATION_NAME.getValue());
    }

    /**
     * Sets a correlation identifier.
     *
     * @param correlationId correlation id of this message
     *
     * @see #getCorrelationId()
     */
    void setCorrelationId(String correlationId) {
        amqpAnnotatedMessage.getProperties().setCorrelationId(correlationId);
    }

    /**
     * Sets the content type of the {@link ServiceBusReceivedMessage}.
     *
     * @param contentType of the message.
     */
    void setContentType(String contentType) {
        amqpAnnotatedMessage.getProperties().setContentType(contentType);
    }

    /**
     * Sets the dead letter description.
     *
     * @param deadLetterErrorDescription Dead letter description.
     */
    void setDeadLetterErrorDescription(String deadLetterErrorDescription) {
        amqpAnnotatedMessage.getApplicationProperties().put(DEAD_LETTER_DESCRIPTION_ANNOTATION_NAME.getValue(),
            deadLetterErrorDescription);
    }

    /**
     * Sets the dead letter reason.
     *
     * @param deadLetterReason Dead letter reason.
     */
    void setDeadLetterReason(String deadLetterReason) {
        amqpAnnotatedMessage.getApplicationProperties().put(DEAD_LETTER_REASON_ANNOTATION_NAME.getValue(),
            deadLetterReason);
    }

    /**
     * Sets the name of the queue or subscription that this message was enqueued on, before it was
     * deadlettered.
     *
     * @param deadLetterSource the name of the queue or subscription that this message was enqueued on,
     * before it was deadlettered.
     */
    void setDeadLetterSource(String deadLetterSource) {
        amqpAnnotatedMessage.getMessageAnnotations().put(DEAD_LETTER_SOURCE_KEY_ANNOTATION_NAME.getValue(),
            deadLetterSource);
    }

    /**
     * Sets the number of the times this message was delivered to clients.
     *
     * @param deliveryCount the number of the times this message was delivered to clients.
     */
    void setDeliveryCount(long deliveryCount) {
        amqpAnnotatedMessage.getHeader().setDeliveryCount(deliveryCount);
    }

    void setEnqueuedSequenceNumber(long enqueuedSequenceNumber) {
        amqpAnnotatedMessage.getMessageAnnotations().put(ENQUEUED_SEQUENCE_NUMBER_ANNOTATION_NAME.getValue(),
            enqueuedSequenceNumber);
    }

    /**
     * Sets the datetime at which this message was enqueued in Azure Service Bus.
     *
     * @param enqueuedTime the datetime at which this message was enqueued in Azure Service Bus.
     */
    void setEnqueuedTime(OffsetDateTime enqueuedTime) {
        setValue(amqpAnnotatedMessage.getMessageAnnotations(), ENQUEUED_TIME_UTC_ANNOTATION_NAME, enqueuedTime);
    }

    /**
     * Sets the subject for the message.
     *
     * @param subject The subject to set.
     */
    void setSubject(String subject) {
        amqpAnnotatedMessage.getProperties().setSubject(subject);
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
     * Sets the datetime at which the lock of this message expires.
     *
     * @param lockedUntil the datetime at which the lock of this message expires.
     */
    void setLockedUntil(OffsetDateTime lockedUntil) {
        setValue(amqpAnnotatedMessage.getMessageAnnotations(), LOCKED_UNTIL_KEY_ANNOTATION_NAME, lockedUntil);
    }

    /**
     * Sets the message id.
     *
     * @param messageId to be set.
     */
    void setMessageId(String messageId) {
        amqpAnnotatedMessage.getProperties().setMessageId(messageId);
    }

    /**
     * Sets a partition key for sending a message to a partitioned entity
     *
     * @param partitionKey partition key of this message
     *
     * @see #getPartitionKey()
     */
    void setPartitionKey(String partitionKey) {
        amqpAnnotatedMessage.getMessageAnnotations().put(PARTITION_KEY_ANNOTATION_NAME.getValue(), partitionKey);
    }

    /**
     * Sets the scheduled enqueue time of this message.
     *
     * @param scheduledEnqueueTime the datetime at which this message should be enqueued in Azure Service Bus.
     *
     * @see #getScheduledEnqueueTime()
     */
    void setScheduledEnqueueTime(OffsetDateTime scheduledEnqueueTime) {
        setValue(amqpAnnotatedMessage.getMessageAnnotations(), SCHEDULED_ENQUEUE_UTC_TIME_NAME, scheduledEnqueueTime);
    }

    /**
     * Sets the unique number assigned to a message by Service Bus.
     *
     * @param sequenceNumber the unique number assigned to a message by Service Bus.
     */
    void setSequenceNumber(long sequenceNumber) {
        amqpAnnotatedMessage.getMessageAnnotations().put(SEQUENCE_NUMBER_ANNOTATION_NAME.getValue(), sequenceNumber);
    }

    /**
     * Sets the session id.
     *
     * @param sessionId to be set.
     */
    void setSessionId(String sessionId) {
        amqpAnnotatedMessage.getProperties().setGroupId(sessionId);
    }

    /**
     * Sets the duration of time before this message expires.
     *
     * @param timeToLive Time to Live duration of this message
     *
     * @see #getTimeToLive()
     */
    void setTimeToLive(Duration timeToLive) {
        amqpAnnotatedMessage.getHeader().setTimeToLive(timeToLive);
    }

    /**
     * Sets the address of an entity to send replies to.
     *
     * @param replyTo ReplyTo property value of this message
     *
     * @see #getReplyTo()
     */
    void setReplyTo(String replyTo) {
        amqpAnnotatedMessage.getProperties().setReplyTo(replyTo);
    }

    /**
     * Gets or sets a session identifier augmenting the {@link #getReplyTo() ReplyTo} address.
     *
     * @param replyToSessionId ReplyToSessionId property value of this message
     */
    void setReplyToSessionId(String replyToSessionId) {
        amqpAnnotatedMessage.getProperties().setReplyToGroupId(replyToSessionId);
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
        amqpAnnotatedMessage.getProperties().setTo(to);
    }

    /**
     * Sets a via-partition key for sending a message to a destination entity via another partitioned entity
     *
     * @param viaPartitionKey via-partition key of this message
     *
     * @see #getViaPartitionKey()
     */
    void setViaPartitionKey(String viaPartitionKey) {
        amqpAnnotatedMessage.getMessageAnnotations().put(VIA_PARTITION_KEY_ANNOTATION_NAME.getValue(), viaPartitionKey);
    }

    /*
     * Gets String value from given map and null if key does not exists.
     */
    private String getStringValue(Map<String, Object> dataMap, String key) {
        return (String) dataMap.get(key);
    }

    /*
     * Gets long value from given map and 0 if key does not exists.
     */
    private long getLongValue(Map<String, Object> dataMap, String key) {
        return dataMap.containsKey(key) ? (long) dataMap.get(key) : 0;
    }

    /*
     * Gets OffsetDateTime value from given map and null if key does not exists.
     */
    private OffsetDateTime getOffsetDateTimeValue(Map<String, Object> dataMap, String key) {
        return dataMap.containsKey(key) ? ((Date) dataMap.get(key)).toInstant().atOffset(ZoneOffset.UTC) : null;
    }

    private void setValue(Map<String, Object> dataMap, AmqpMessageConstant key, OffsetDateTime value) {
        if (value != null) {
            amqpAnnotatedMessage.getMessageAnnotations().put(key.getValue(),
                new Date(value.toInstant().toEpochMilli()));
        }
    }
}
