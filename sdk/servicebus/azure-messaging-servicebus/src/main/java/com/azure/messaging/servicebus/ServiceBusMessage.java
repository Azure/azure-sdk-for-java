// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpMessageConstant;
import com.azure.core.amqp.models.AmqpAddress;
import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.amqp.models.AmqpMessageBody;
import com.azure.core.amqp.models.AmqpMessageBodyType;
import com.azure.core.amqp.models.AmqpMessageHeader;
import com.azure.core.amqp.models.AmqpMessageId;
import com.azure.core.amqp.models.AmqpMessageProperties;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.experimental.util.BinaryData;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.azure.core.amqp.AmqpMessageConstant.DEAD_LETTER_DESCRIPTION_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.DEAD_LETTER_REASON_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.DEAD_LETTER_SOURCE_KEY_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_SEQUENCE_NUMBER_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.LOCKED_UNTIL_KEY_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.PARTITION_KEY_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.SCHEDULED_ENQUEUE_UTC_TIME_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.VIA_PARTITION_KEY_ANNOTATION_NAME;

/**
 * The data structure encapsulating the message being sent-to Service Bus.
 *
 * <p>
 * Here's how AMQP message sections map to {@link ServiceBusMessage}. For reference, the specification can be found
 * here:
 * <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-complete-v1.0-os.pdf">AMQP 1.0 specification</a>
 *
 * <ol>
 * <li>{@link #getApplicationProperties()} - AMQPMessage.ApplicationProperties section</li>
 * <li>{@link #getBody()} - if AMQPMessage.Body has Data section</li>
 * </ol>
 *
 * <p>
 * Serializing a received {@link ServiceBusMessage} with AMQP sections other than ApplicationProperties
 * (with primitive Java types) and Data section is not supported.
 * </p>
 *
 * @see ServiceBusMessageBatch
 * @see BinaryData
 */
public class ServiceBusMessage {
    private static final int MAX_MESSAGE_ID_LENGTH = 128;
    private static final int MAX_PARTITION_KEY_LENGTH = 128;
    private static final int MAX_SESSION_ID_LENGTH = 128;

    private final AmqpAnnotatedMessage amqpAnnotatedMessage;
    private final ClientLogger logger = new ClientLogger(ServiceBusMessage.class);

    private Context context;

    /**
     * Creates a {@link ServiceBusMessage} with given byte array body.
     *
     * @param body The content of the Service bus message.
     *
     * @throws NullPointerException if {@code body} is null.
     */
    public ServiceBusMessage(byte[] body) {
        this(BinaryData.fromBytes(Objects.requireNonNull(body, "'body' cannot be null.")));
    }

    /**
     * Creates a {@link ServiceBusMessage} with a {@link java.nio.charset.StandardCharsets#UTF_8 UTF_8} encoded body.
     *
     * @param body The content of the Service bus message.
     *
     * @throws NullPointerException if {@code body} is null.
     */
    public ServiceBusMessage(String body) {
        this(BinaryData.fromString(Objects.requireNonNull(body, "'body' cannot be null.")));
    }

    /**
     * Creates a {@link ServiceBusMessage} containing the {@code body}.The {@link BinaryData} provides various
     * convenience API representing byte array. It also provides a way to serialize {@link Object} into
     * {@link BinaryData}.
     *
     * @param body The data to set for this {@link ServiceBusMessage}.
     *
     * @throws NullPointerException if {@code body} is {@code null}.
     *
     * @see BinaryData
     */
    public ServiceBusMessage(BinaryData body) {
        Objects.requireNonNull(body, "'body' cannot be null.");
        this.context = Context.NONE;
        this.amqpAnnotatedMessage = new AmqpAnnotatedMessage(AmqpMessageBody.fromData(body.toBytes()));
    }

    /**
     * Creates a {@link ServiceBusMessage} using properties from {@code receivedMessage}. This is normally used when a
     * {@link ServiceBusReceivedMessage} needs to be sent to another entity.
     *
     * @param receivedMessage The received message to create new message from.
     *
     * @throws NullPointerException if {@code receivedMessage} is {@code null}.
     * @throws UnsupportedOperationException if {@link AmqpMessageBodyType} is {@link AmqpMessageBodyType#SEQUENCE} or
     * {@link AmqpMessageBodyType#VALUE}. See code sample above explaining how to check for {@link AmqpMessageBodyType}
     * before calling this constructor.
     * <p><strong>How to check for {@link AmqpMessageBodyType} before calling this constructor</strong></p>
     * {@codesnippet com.azure.messaging.servicebus.ServiceBusMessage.copyServiceBusMessage}
     * @throws IllegalStateException for invalid {@link AmqpMessageBodyType}.
     * @see AmqpAnnotatedMessage
     */
    public ServiceBusMessage(ServiceBusReceivedMessage receivedMessage) {
        Objects.requireNonNull(receivedMessage, "'receivedMessage' cannot be null.");

        final AmqpMessageBodyType bodyType = receivedMessage.getAmqpAnnotatedMessage().getBody().getBodyType();
        AmqpMessageBody amqpMessageBody;
        switch (bodyType) {
            case DATA:
                final byte[] data = receivedMessage.getAmqpAnnotatedMessage().getBody().getFirstData();
                amqpMessageBody = AmqpMessageBody.fromData(Arrays.copyOf(data, data.length));
                break;
            case SEQUENCE:
            case VALUE:
                throw logger.logExceptionAsError(new UnsupportedOperationException(
                    String.format(Locale.US, "This constructor only support body type [%s] at present. Track "
                        + "this issue, https://github.com/Azure/azure-sdk-for-java/issues/17614 for other body type "
                        + "support in future.", AmqpMessageBodyType.DATA.toString())));
            default:
                throw logger.logExceptionAsError(new IllegalStateException("Body type not valid "
                    + bodyType.toString()));
        }
        this.amqpAnnotatedMessage = new AmqpAnnotatedMessage(amqpMessageBody);

        // set properties
        final AmqpMessageProperties receivedProperties = receivedMessage.getAmqpAnnotatedMessage().getProperties();
        final AmqpMessageProperties newProperties = amqpAnnotatedMessage.getProperties();
        newProperties.setMessageId(receivedProperties.getMessageId());
        newProperties.setUserId(receivedProperties.getUserId());
        newProperties.setTo(receivedProperties.getTo());
        newProperties.setSubject(receivedProperties.getSubject());
        newProperties.setReplyTo(receivedProperties.getReplyTo());
        newProperties.setCorrelationId(receivedProperties.getCorrelationId());
        newProperties.setContentType(receivedProperties.getContentType());
        newProperties.setContentEncoding(receivedProperties.getContentEncoding());
        newProperties.setAbsoluteExpiryTime(receivedProperties.getAbsoluteExpiryTime());
        newProperties.setCreationTime(receivedProperties.getCreationTime());
        newProperties.setGroupId(receivedProperties.getGroupId());
        newProperties.setGroupSequence(receivedProperties.getGroupSequence());
        newProperties.setReplyToGroupId(receivedProperties.getReplyToGroupId());

        // copy header except for delivery count which should be set to null
        final AmqpMessageHeader receivedHeader = receivedMessage.getAmqpAnnotatedMessage().getHeader();
        final AmqpMessageHeader newHeader = amqpAnnotatedMessage.getHeader();
        newHeader.setPriority(receivedHeader.getPriority());
        newHeader.setTimeToLive(receivedHeader.getTimeToLive());
        newHeader.setDurable(receivedHeader.isDurable());
        newHeader.setFirstAcquirer(receivedHeader.isFirstAcquirer());

        // copy message annotations except for broker set ones
        final Map<String, Object> receivedAnnotations = receivedMessage.getAmqpAnnotatedMessage().getMessageAnnotations();
        final Map<String, Object> newAnnotations = amqpAnnotatedMessage.getMessageAnnotations();

        for (Map.Entry<String, Object> entry: receivedAnnotations.entrySet()) {
            if (AmqpMessageConstant.fromString(entry.getKey()) == LOCKED_UNTIL_KEY_ANNOTATION_NAME
                || AmqpMessageConstant.fromString(entry.getKey()) == SEQUENCE_NUMBER_ANNOTATION_NAME
                || AmqpMessageConstant.fromString(entry.getKey()) == DEAD_LETTER_SOURCE_KEY_ANNOTATION_NAME
                || AmqpMessageConstant.fromString(entry.getKey()) == ENQUEUED_SEQUENCE_NUMBER_ANNOTATION_NAME
                || AmqpMessageConstant.fromString(entry.getKey()) == ENQUEUED_TIME_UTC_ANNOTATION_NAME) {

                continue;
            }
            newAnnotations.put(entry.getKey(), entry.getValue());
        }

        // copy delivery annotations
        final Map<String, Object> receivedDelivery = receivedMessage.getAmqpAnnotatedMessage().getDeliveryAnnotations();
        final Map<String, Object> newDelivery = amqpAnnotatedMessage.getMessageAnnotations();

        for (Map.Entry<String, Object> entry: receivedDelivery.entrySet()) {
            newDelivery.put(entry.getKey(), entry.getValue());
        }

        // copy Footer
        final Map<String, Object> receivedFooter = receivedMessage.getAmqpAnnotatedMessage().getFooter();
        final Map<String, Object> newFooter = amqpAnnotatedMessage.getMessageAnnotations();

        for (Map.Entry<String, Object> entry: receivedFooter.entrySet()) {
            newFooter.put(entry.getKey(), entry.getValue());
        }

        // copy application properties except for broker set ones
        final Map<String, Object> receivedApplicationProperties = receivedMessage.getAmqpAnnotatedMessage()
            .getApplicationProperties();
        final Map<String, Object> newApplicationProperties = amqpAnnotatedMessage.getApplicationProperties();

        for (Map.Entry<String, Object> entry: receivedApplicationProperties.entrySet()) {
            if (AmqpMessageConstant.fromString(entry.getKey()) == DEAD_LETTER_DESCRIPTION_ANNOTATION_NAME
                || AmqpMessageConstant.fromString(entry.getKey()) == DEAD_LETTER_REASON_ANNOTATION_NAME) {

                continue;
            }
            newApplicationProperties.put(entry.getKey(), entry.getValue());
        }

        this.context = Context.NONE;
    }

    /**
     * Gets the {@link AmqpAnnotatedMessage}.
     *
     * @return the amqp message.
     */
    public AmqpAnnotatedMessage getAmqpAnnotatedMessage() {
        return amqpAnnotatedMessage;
    }

    /**
     * Gets the set of free-form {@link ServiceBusMessage} properties which may be used for passing metadata associated
     * with the {@link ServiceBusMessage} during Service Bus operations. A common use-case for
     * {@code getApplicationProperties()} is to associate serialization hints for the {@link #getBody()} as an aid to
     * consumers who wish to deserialize the binary data.
     *
     * @return Application properties associated with this {@link ServiceBusMessage}.
     */
    public Map<String, Object> getApplicationProperties() {
        return amqpAnnotatedMessage.getApplicationProperties();
    }

    /**
     * Gets the actual payload/data wrapped by the {@link ServiceBusMessage}.
     *
     * <p>The {@link BinaryData} wraps byte array and is an abstraction over many different ways it can be represented.
     * It provides many convenience API including APIs to serialize/deserialize object.
     * <p>
     * If the means for deserializing the raw data is not apparent to consumers, a common technique is to make use of
     * {@link #getApplicationProperties()} when creating the event, to associate serialization hints as an aid to
     * consumers who wish to deserialize the binary data.
     * </p>
     *
     * @return A byte array representing the data.
     */
    public BinaryData getBody() {
        final AmqpMessageBodyType type = amqpAnnotatedMessage.getBody().getBodyType();
        switch (type) {
            case DATA:
                return BinaryData.fromBytes(amqpAnnotatedMessage.getBody().getFirstData());
            case SEQUENCE:
            case VALUE:
                throw logger.logExceptionAsError(new UnsupportedOperationException("Not supported AmqpBodyType: "
                    + type.toString()));
            default:
                throw logger.logExceptionAsError(new IllegalArgumentException("Unknown AmqpBodyType: "
                    + type.toString()));
        }
    }

    /**
     * Gets the content type of the message.
     *
     * @return the contentType of the {@link ServiceBusMessage}.
     */
    public String getContentType() {
        return amqpAnnotatedMessage.getProperties().getContentType();
    }

    /**
     * Sets the content type of the {@link ServiceBusMessage}.
     *
     * @param contentType of the message.
     *
     * @return The updated {@link ServiceBusMessage}.
     */
    public ServiceBusMessage setContentType(String contentType) {
        amqpAnnotatedMessage.getProperties().setContentType(contentType);
        return this;
    }

    /**
     * Gets a correlation identifier.
     * <p>
     * Allows an application to specify a context for the message for the purposes of correlation, for example
     * reflecting the MessageId of a message that is being replied to.
     * </p>
     *
     * @return correlation id of this message
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-messages-payloads?#message-routing-and-correlation">Message
     *     Routing and Correlation</a>
     */
    public String getCorrelationId() {
        String correlationId = null;
        AmqpMessageId amqpCorrelationId = amqpAnnotatedMessage.getProperties().getCorrelationId();
        if (amqpCorrelationId != null) {
            correlationId = amqpCorrelationId.toString();
        }
        return correlationId;
    }

    /**
     * Sets a correlation identifier.
     *
     * @param correlationId correlation id of this message
     *
     * @return The updated {@link ServiceBusMessage}.
     * @see #getCorrelationId()
     */
    public ServiceBusMessage setCorrelationId(String correlationId) {
        AmqpMessageId id = null;
        if (correlationId != null) {
            id = new AmqpMessageId(correlationId);
        }
        amqpAnnotatedMessage.getProperties().setCorrelationId(id);
        return this;
    }

    /**
     * Gets the subject for the message.
     *
     * @return The subject for the message.
     */
    public String getSubject() {
        return amqpAnnotatedMessage.getProperties().getSubject();
    }

    /**
     * Sets the subject for the message.
     *
     * @param subject The subject to set.
     *
     * @return The updated {@link ServiceBusMessage} object.
     */
    public ServiceBusMessage setSubject(String subject) {
        amqpAnnotatedMessage.getProperties().setSubject(subject);
        return this;
    }

    /**
     * @return Id of the {@link ServiceBusMessage}.
     */
    public String getMessageId() {
        String messageId = null;
        AmqpMessageId amqpMessageId = amqpAnnotatedMessage.getProperties().getMessageId();
        if (amqpMessageId != null) {
            messageId = amqpMessageId.toString();
        }
        return messageId;
    }

    /**
     * Sets the message id.
     *
     * @param messageId to be set.
     *
     * @return The updated {@link ServiceBusMessage}.
     * @throws IllegalArgumentException if {@code messageId} is too long.
     */
    public ServiceBusMessage setMessageId(String messageId) {
        checkIdLength("messageId", messageId, MAX_MESSAGE_ID_LENGTH);
        AmqpMessageId id = null;
        if (messageId != null) {
            id = new AmqpMessageId(messageId);
        }
        amqpAnnotatedMessage.getProperties().setMessageId(id);
        return this;
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
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-partitioning">Partitioned
     *     entities</a>
     */
    public String getPartitionKey() {
        return (String) amqpAnnotatedMessage.getMessageAnnotations().get(PARTITION_KEY_ANNOTATION_NAME.getValue());
    }

    /**
     * Sets a partition key for sending a message to a partitioned entity
     *
     * @param partitionKey partition key of this message
     *
     * @return The updated {@link ServiceBusMessage}.
     * @see #getPartitionKey()
     * @throws IllegalArgumentException if {@code partitionKey} is too long or if the {@code partitionKey}
     * does not match the {@code sessionId}.
     */
    public ServiceBusMessage setPartitionKey(String partitionKey) {
        checkIdLength("partitionKey", partitionKey, MAX_PARTITION_KEY_LENGTH);
        checkPartitionKey(partitionKey);

        amqpAnnotatedMessage.getMessageAnnotations().put(PARTITION_KEY_ANNOTATION_NAME.getValue(), partitionKey);
        return this;
    }

    /**
     * Gets the address of an entity to send replies to.
     * <p>
     * This optional and application-defined value is a standard way to express a reply path to the receiver of the
     * message. When a sender expects a reply, it sets the value to the absolute or relative path of the queue or topic
     * it expects the reply to be sent to.
     *
     * @return ReplyTo property value of this message
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-messages-payloads?#message-routing-and-correlation">Message
     *     Routing and Correlation</a>
     */
    public String getReplyTo() {
        String replyTo = null;
        AmqpAddress amqpAddress = amqpAnnotatedMessage.getProperties().getReplyTo();
        if (amqpAddress != null) {
            replyTo = amqpAddress.toString();
        }
        return replyTo;
    }

    /**
     * Sets the address of an entity to send replies to.
     *
     * @param replyTo ReplyTo property value of this message
     *
     * @return The updated {@link ServiceBusMessage}.
     * @see #getReplyTo()
     */
    public ServiceBusMessage setReplyTo(String replyTo) {
        AmqpAddress replyToAddress = null;
        if (replyTo != null) {
            replyToAddress = new AmqpAddress(replyTo);
        }
        amqpAnnotatedMessage.getProperties().setReplyTo(replyToAddress);
        return this;
    }

    /**
     * Gets the "to" address.
     *
     * @return "To" property value of this message
     */
    public String getTo() {
        String to = null;
        AmqpAddress amqpAddress = amqpAnnotatedMessage.getProperties().getTo();
        if (amqpAddress != null) {
            to = amqpAddress.toString();
        }
        return to;
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
     *
     * @return The updated {@link ServiceBusMessage}.
     */
    public ServiceBusMessage setTo(String to) {
        AmqpAddress toAddress = null;
        if (to != null) {
            toAddress = new AmqpAddress(to);
        }
        amqpAnnotatedMessage.getProperties().setTo(toAddress);
        return this;
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
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-expiration">Message Expiration</a>
     */
    public Duration getTimeToLive() {
        return amqpAnnotatedMessage.getHeader().getTimeToLive();
    }

    /**
     * Sets the duration of time before this message expires.
     *
     * @param timeToLive Time to Live duration of this message
     *
     * @return The updated {@link ServiceBusMessage}.
     * @see #getTimeToLive()
     */
    public ServiceBusMessage setTimeToLive(Duration timeToLive) {
        amqpAnnotatedMessage.getHeader().setTimeToLive(timeToLive);
        return this;
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
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-sequencing">Message Sequencing and
     *     Timestamps</a>
     */
    public OffsetDateTime getScheduledEnqueueTime() {
        Object value = amqpAnnotatedMessage.getMessageAnnotations().get(SCHEDULED_ENQUEUE_UTC_TIME_NAME.getValue());
        return value != null
            ? ((OffsetDateTime) value).toInstant().atOffset(ZoneOffset.UTC)
            : null;
    }

    /**
     * Sets the scheduled enqueue time of this message. A {@code null} will not be set. If this value needs to be unset
     * it could be done by value removing from {@link AmqpAnnotatedMessage#getMessageAnnotations()} using key
     * {@link AmqpMessageConstant#SCHEDULED_ENQUEUE_UTC_TIME_NAME}.
     *
     * @param scheduledEnqueueTime the datetime at which this message should be enqueued in Azure Service Bus.
     *
     * @return The updated {@link ServiceBusMessage}.
     * @see #getScheduledEnqueueTime()
     */
    public ServiceBusMessage setScheduledEnqueueTime(OffsetDateTime scheduledEnqueueTime) {
        if (scheduledEnqueueTime != null) {
            amqpAnnotatedMessage.getMessageAnnotations().put(SCHEDULED_ENQUEUE_UTC_TIME_NAME.getValue(),
                scheduledEnqueueTime);
        }
        return this;
    }

    /**
     * Gets or sets a session identifier augmenting the {@link #getReplyTo() ReplyTo} address.
     * <p>
     * This value augments the ReplyTo information and specifies which SessionId should be set for the reply when sent
     * to the reply entity.
     *
     * @return ReplyToSessionId property value of this message
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-messages-payloads?#message-routing-and-correlation">Message
     *     Routing and Correlation</a>
     */
    public String getReplyToSessionId() {
        return amqpAnnotatedMessage.getProperties().getReplyToGroupId();
    }

    /**
     * Gets or sets a session identifier augmenting the {@link #getReplyTo() ReplyTo} address.
     *
     * @param replyToSessionId ReplyToSessionId property value of this message
     *
     * @return The updated {@link ServiceBusMessage}.
     */
    public ServiceBusMessage setReplyToSessionId(String replyToSessionId) {
        amqpAnnotatedMessage.getProperties().setReplyToGroupId(replyToSessionId);
        return this;
    }

    /**
     * Gets the partition key for sending a message to a entity via another partitioned transfer entity.
     *
     * If a message is sent via a transfer queue in the scope of a transaction, this value selects the transfer queue
     * partition: This is functionally equivalent to {@link #getPartitionKey()} and ensures that messages are kept
     * together and in order as they are transferred.
     *
     * @return partition key on the via queue.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-transactions#transfers-and-send-via">Transfers
     *     and Send Via</a>
     */
    public String getViaPartitionKey() {
        return (String) amqpAnnotatedMessage.getMessageAnnotations().get(VIA_PARTITION_KEY_ANNOTATION_NAME.getValue());
    }

    /**
     * Sets a via-partition key for sending a message to a destination entity via another partitioned entity
     *
     * @param viaPartitionKey via-partition key of this message
     *
     * @return The updated {@link ServiceBusMessage}.
     * @see #getViaPartitionKey()
     */
    public ServiceBusMessage setViaPartitionKey(String viaPartitionKey) {
        amqpAnnotatedMessage.getMessageAnnotations().put(VIA_PARTITION_KEY_ANNOTATION_NAME.getValue(), viaPartitionKey);
        return this;
    }

    /**
     * Gets the session id of the message.
     *
     * @return Session Id of the {@link ServiceBusMessage}.
     */
    public String getSessionId() {
        return amqpAnnotatedMessage.getProperties().getGroupId();
    }

    /**
     * Sets the session id.
     *
     * @param sessionId to be set.
     *
     * @return The updated {@link ServiceBusMessage}.
     * @throws IllegalArgumentException if {@code sessionId} is too long or if the  {@code sessionId}
     * does not match the {@code partitionKey}.
     */
    public ServiceBusMessage setSessionId(String sessionId) {
        checkIdLength("sessionId", sessionId, MAX_SESSION_ID_LENGTH);
        checkSessionId(sessionId);

        amqpAnnotatedMessage.getProperties().setGroupId(sessionId);
        return this;
    }

    /**
     * A specified key-value pair of type {@link Context} to set additional information on the {@link
     * ServiceBusMessage}.
     *
     * @return the {@link Context} object set on the {@link ServiceBusMessage}.
     */
    Context getContext() {
        return context;
    }

    /**
     * Adds a new key value pair to the existing context on Message.
     *
     * @param key The key for this context object
     * @param value The value for this context object.
     *
     * @return The updated {@link ServiceBusMessage}.
     * @throws NullPointerException if {@code key} or {@code value} is null.
     */
    public ServiceBusMessage addContext(String key, Object value) {
        Objects.requireNonNull(key, "The 'key' parameter cannot be null.");
        Objects.requireNonNull(value, "The 'value' parameter cannot be null.");
        this.context = context.addData(key, value);

        return this;
    }

    /*
     * Gets value from given map.
     */
    private void removeValues(Map<String, Object> dataMap, AmqpMessageConstant... keys) {
        for (AmqpMessageConstant key : keys) {
            dataMap.remove(key.getValue());
        }
    }

    /**
     * Checks the length of ID fields.
     *
     * Some fields within the message will cause a failure in the service without enough context information.
     */
    private void checkIdLength(String fieldName, String value, int maxLength) {
        if (value != null && value.length() > maxLength) {
            final String message = String.format("%s cannot be longer than %d characters.", fieldName, maxLength);
            throw logger.logExceptionAsError(new IllegalArgumentException(message));
        }
    }

    /**
     * Validates that the user can't set the partitionKey to a different value than the session ID.
     * (this will eventually migrate to a service-side check)
     */
    private void checkSessionId(String proposedSessionId) {
        if (proposedSessionId == null) {
            return;
        }

        if (this.getPartitionKey() != null && this.getPartitionKey().compareTo(proposedSessionId) != 0) {
            final String message = String.format(
                "sessionId:%s cannot be set to a different value than partitionKey:%s.",
                proposedSessionId,
                this.getPartitionKey());
            throw logger.logExceptionAsError(new IllegalArgumentException(message));
        }
    }

    /**
     * Validates that the user can't set the partitionKey to a different value than the session ID.
     * (this will eventually migrate to a service-side check)
     */
    private void checkPartitionKey(String proposedPartitionKey) {
        if (proposedPartitionKey == null) {
            return;
        }

        if (this.getSessionId() != null && this.getSessionId().compareTo(proposedPartitionKey) != 0) {
            final String message = String.format(
                "partitionKey:%s cannot be set to a different value than sessionId:%s.",
                proposedPartitionKey,
                this.getSessionId());

            throw logger.logExceptionAsError(new IllegalArgumentException(message));
        }
    }

}
