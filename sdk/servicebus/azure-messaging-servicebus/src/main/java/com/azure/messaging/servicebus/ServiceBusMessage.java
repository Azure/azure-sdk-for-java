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
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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

/**
 * The data structure encapsulating the message being sent to Service Bus. The message structure is discussed in detail
 * in the <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-messages-payloads">product
 * documentation</a>.
 *
 * @see ServiceBusReceivedMessage
 * @see ServiceBusMessageBatch
 * @see ServiceBusSenderAsyncClient#sendMessage(ServiceBusMessage)
 * @see ServiceBusSenderClient#sendMessage(ServiceBusMessage)
 * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-messages-payloads">Service Bus
 *      message payloads</a>
 * @see <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-complete-v1.0-os.pdf">AMQP 1.0 specification
 *     </a>
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
     * Creates a {@link ServiceBusMessage} with a {@link StandardCharsets#UTF_8 UTF-8} encoded body.
     *
     * @param body The content of the Service Bus message.
     *
     * @throws NullPointerException if {@code body} is null.
     */
    public ServiceBusMessage(String body) {
        this(BinaryData.fromString(Objects.requireNonNull(body, "'body' cannot be null.")));
    }

    /**
     * Creates a {@link ServiceBusMessage} containing the {@code body}.The {@link BinaryData} provides various
     * convenience API representing byte array. It also provides a way to serialize {@link Object} into {@link
     * BinaryData}.
     *
     * @param body The data to set for this {@link ServiceBusMessage}.
     *
     * @throws NullPointerException if {@code body} is {@code null}.
     * @see BinaryData
     */
    public ServiceBusMessage(BinaryData body) {
        Objects.requireNonNull(body, "'body' cannot be null.");
        this.context = Context.NONE;
        this.amqpAnnotatedMessage = new AmqpAnnotatedMessage(AmqpMessageBody.fromData(body.toBytes()));
    }

    /**
     * This constructor provides an easy way to create {@link ServiceBusMessage} with message body as AMQP Data types
     * {@code SEQUENCE} and {@code VALUE}.
     * In case of {@code SEQUENCE}, tt support sending and receiving of only one AMQP Sequence at present.
     * If you are sending message with single byte array or String data, you can also use other constructor.
     *
     * @param amqpMessageBody amqp message body.
     *
     * @throws NullPointerException if {@code amqpMessageBody} is {@code null}.
     */
    public ServiceBusMessage(AmqpMessageBody amqpMessageBody) {
        Objects.requireNonNull(amqpMessageBody, "'body' cannot be null.");
        this.context = Context.NONE;
        this.amqpAnnotatedMessage = new AmqpAnnotatedMessage(amqpMessageBody);
    }

    /**
     * Creates a {@link ServiceBusMessage} using properties from {@code receivedMessage}. This is normally used when a
     * {@link ServiceBusReceivedMessage} needs to be sent to another entity.
     *
     * @param receivedMessage The received message to create new message from.
     *
     * @throws NullPointerException if {@code receivedMessage} is {@code null}.
     * @throws IllegalStateException for invalid {@link AmqpMessageBodyType}.
     */
    public ServiceBusMessage(ServiceBusReceivedMessage receivedMessage) {
        Objects.requireNonNull(receivedMessage, "'receivedMessage' cannot be null.");

        final AmqpMessageBodyType bodyType = receivedMessage.getRawAmqpMessage().getBody().getBodyType();
        AmqpMessageBody amqpMessageBody;
        switch (bodyType) {
            case DATA:
                amqpMessageBody = AmqpMessageBody.fromData(receivedMessage.getRawAmqpMessage().getBody()
                    .getFirstData());
                break;
            case SEQUENCE:
                amqpMessageBody = AmqpMessageBody.fromSequence(receivedMessage.getRawAmqpMessage().getBody()
                    .getSequence());
                break;
            case VALUE:
                amqpMessageBody = AmqpMessageBody.fromValue(receivedMessage.getRawAmqpMessage().getBody()
                    .getValue());
                break;
            default:
                throw logger.logExceptionAsError(new IllegalStateException("Body type not valid "
                    + bodyType.toString()));
        }
        this.amqpAnnotatedMessage = new AmqpAnnotatedMessage(amqpMessageBody);

        // set properties
        final AmqpMessageProperties receivedProperties = receivedMessage.getRawAmqpMessage().getProperties();
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
        final AmqpMessageHeader receivedHeader = receivedMessage.getRawAmqpMessage().getHeader();
        final AmqpMessageHeader newHeader = this.amqpAnnotatedMessage.getHeader();
        newHeader.setPriority(receivedHeader.getPriority());
        newHeader.setTimeToLive(receivedHeader.getTimeToLive());
        newHeader.setDurable(receivedHeader.isDurable());
        newHeader.setFirstAcquirer(receivedHeader.isFirstAcquirer());

        // copy message annotations except for broker set ones
        final Map<String, Object> receivedAnnotations = receivedMessage.getRawAmqpMessage()
            .getMessageAnnotations();
        final Map<String, Object> newAnnotations = this.amqpAnnotatedMessage.getMessageAnnotations();

        for (Map.Entry<String, Object> entry : receivedAnnotations.entrySet()) {
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
        final Map<String, Object> receivedDelivery = receivedMessage.getRawAmqpMessage().getDeliveryAnnotations();
        final Map<String, Object> newDelivery = this.amqpAnnotatedMessage.getDeliveryAnnotations();

        for (Map.Entry<String, Object> entry : receivedDelivery.entrySet()) {
            newDelivery.put(entry.getKey(), entry.getValue());
        }

        // copy Footer
        final Map<String, Object> receivedFooter = receivedMessage.getRawAmqpMessage().getFooter();
        final Map<String, Object> newFooter = this.amqpAnnotatedMessage.getFooter();

        for (Map.Entry<String, Object> entry : receivedFooter.entrySet()) {
            newFooter.put(entry.getKey(), entry.getValue());
        }

        // copy application properties except for broker set ones
        final Map<String, Object> receivedApplicationProperties = receivedMessage.getRawAmqpMessage()
            .getApplicationProperties();
        final Map<String, Object> newApplicationProperties = this.amqpAnnotatedMessage.getApplicationProperties();

        for (Map.Entry<String, Object> entry : receivedApplicationProperties.entrySet()) {
            if (AmqpMessageConstant.fromString(entry.getKey()) == DEAD_LETTER_DESCRIPTION_ANNOTATION_NAME
                || AmqpMessageConstant.fromString(entry.getKey()) == DEAD_LETTER_REASON_ANNOTATION_NAME) {

                continue;
            }
            newApplicationProperties.put(entry.getKey(), entry.getValue());
        }

        this.context = Context.NONE;
    }

    /**
     * Gets the set of free-form {@link ServiceBusMessage} properties which may be used for passing metadata associated
     * with the {@link ServiceBusMessage} during Service Bus operations. A common use-case for {@code
     * getApplicationProperties()} is to associate serialization hints for the {@link #getBody()} as an aid to consumers
     * who wish to deserialize the binary data.
     *
     * @return Application properties associated with this {@link ServiceBusMessage}.
     */
    public Map<String, Object> getApplicationProperties() {
        return amqpAnnotatedMessage.getApplicationProperties();
    }

    /**
     * Gets the actual payload wrapped by the {@link ServiceBusMessage}.
     *
     * <p>The {@link BinaryData} wraps byte array and is an abstraction over many different ways it can be represented.
     * It provides convenience APIs to serialize/deserialize the object.</p>
     *
     * <p>If the means for deserializing the raw data is not apparent to consumers, a common technique is to make use
     * of {@link #getApplicationProperties()} when creating the event, to associate serialization hints as an aid to
     * consumers who wish to deserialize the binary data.</p>
     *
     * @throws IllegalStateException if called for the messages which are not of binary data type.
     * @return Binary data representing the payload.
     */
    public BinaryData getBody() {
        final AmqpMessageBodyType type = amqpAnnotatedMessage.getBody().getBodyType();
        switch (type) {
            case DATA:
                return BinaryData.fromBytes(amqpAnnotatedMessage.getBody().getFirstData());
            case SEQUENCE:
            case VALUE:
                throw logger.logExceptionAsError(new IllegalStateException("Message  body type is not DATA, instead "
                    + "it is: " + type.toString()));
            default:
                throw logger.logExceptionAsError(new IllegalArgumentException("Unknown AmqpBodyType: "
                    + type.toString()));
        }
    }

    /**
     * Gets the content type of the message.
     *
     * <p>
     * Optionally describes the payload of the message, with a descriptor following the format of RFC2045, Section 5,
     * for example "application/json".
     * </p>
     * @return The content type of the {@link ServiceBusMessage}.
     */
    public String getContentType() {
        return amqpAnnotatedMessage.getProperties().getContentType();
    }

    /**
     * Sets the content type of the {@link ServiceBusMessage}.
     *
     * <p>
     * Optionally describes the payload of the message, with a descriptor following the format of RFC2045, Section 5,
     * for example "application/json".
     * </p>
     *
     * @param contentType RFC2045 Content-Type descriptor of the message.
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
     * @return The correlation id of this message.
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
     * <p>
     * This property enables the application to indicate the purpose of the message to the receiver in a standardized
     * fashion, similar to an email subject line. The mapped AMQP property is "subject".
     * </p>
     *
     * @return The subject for the message.
     */
    public String getSubject() {
        return amqpAnnotatedMessage.getProperties().getSubject();
    }

    /**
     * Sets the subject for the message.
     *
     * @param subject The application specific subject.
     *
     * @return The updated {@link ServiceBusMessage} object.
     */
    public ServiceBusMessage setSubject(String subject) {
        amqpAnnotatedMessage.getProperties().setSubject(subject);
        return this;
    }

    /**
     * Gets the message id.
     *
     * <p>
     * The message identifier is an application-defined value that uniquely identifies the message and its payload. The
     * identifier is a free-form string and can reflect a GUID or an identifier derived from the application context. If
     * enabled, the
     * <a href="https://docs.microsoft.com/azure/service-bus-messaging/duplicate-detection">duplicate detection</a>
     * feature identifies and removes second and further submissions of messages with the same {@code messageId}.
     * </p>
     *
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
     * @param messageId The message id to be set.
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
     * </p>
     *
     * @return The partition key of this message.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-partitioning">Partitioned
     *     entities</a>
     */
    public String getPartitionKey() {
        return (String) amqpAnnotatedMessage.getMessageAnnotations().get(PARTITION_KEY_ANNOTATION_NAME.getValue());
    }

    /**
     * Sets a partition key for sending a message to a partitioned entity
     *
     * @param partitionKey The partition key of this message.
     *
     * @return The updated {@link ServiceBusMessage}.
     * @throws IllegalArgumentException if {@code partitionKey} is too long or if the {@code partitionKey} does not
     *     match the {@code sessionId}.
     * @see #getPartitionKey()
     */
    public ServiceBusMessage setPartitionKey(String partitionKey) {
        checkIdLength("partitionKey", partitionKey, MAX_PARTITION_KEY_LENGTH);
        checkPartitionKey(partitionKey);

        amqpAnnotatedMessage.getMessageAnnotations().put(PARTITION_KEY_ANNOTATION_NAME.getValue(), partitionKey);
        return this;
    }

    /**
     * Gets the {@link AmqpAnnotatedMessage}.
     *
     * @return The raw AMQP message.
     */
    public AmqpAnnotatedMessage getRawAmqpMessage() {
        return amqpAnnotatedMessage;
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
     * <p>
     * This property is reserved for future use in routing scenarios and presently ignored by the broker itself.
     * Applications can use this value in rule-driven
     * <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-auto-forwarding">auto-forward
     * chaining</a> scenarios to indicate the intended logical destination of the message.
     * </p>
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
     *
     * <p>
     * This property is reserved for future use in routing scenarios and presently ignored by the broker itself.
     * Applications can use this value in rule-driven
     * <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-auto-forwarding">auto-forward
     * chaining</a> scenarios to indicate the intended logical destination of the message.
     * </p>
     *
     * @param to To property value of this message.
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
     * it could be done by value removing from {@link AmqpAnnotatedMessage#getMessageAnnotations()} using key {@link
     * AmqpMessageConstant#SCHEDULED_ENQUEUE_UTC_TIME_NAME}.
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
     * This value augments the {@link #getReplyTo() reply to} information and specifies which {@code sessionId} should
     * be set for the reply when sent to the reply entity.
     *
     * @return The {@code getReplyToGroupId} property value of this message.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-messages-payloads?#message-routing-and-correlation">Message
     *     Routing and Correlation</a>
     */
    public String getReplyToSessionId() {
        return amqpAnnotatedMessage.getProperties().getReplyToGroupId();
    }

    /**
     * Gets or sets a session identifier augmenting the {@link #getReplyTo() ReplyTo} address.
     *
     * @param replyToSessionId The ReplyToGroupId property value of this message.
     *
     * @return The updated {@link ServiceBusMessage}.
     */
    public ServiceBusMessage setReplyToSessionId(String replyToSessionId) {
        amqpAnnotatedMessage.getProperties().setReplyToGroupId(replyToSessionId);
        return this;
    }

    /**
     * Gets the session identifier for a session-aware entity.
     *
     * <p>
     * For session-aware entities, this application-defined value specifies the session affiliation of the message.
     * Messages with the same session identifier are subject to summary locking and enable exact in-order processing and
     * demultiplexing. For session-unaware entities, this value is ignored. See <a
     * href="https://docs.microsoft.com/azure/service-bus-messaging/message-sessions">Message Sessions</a>.
     * </p>
     *
     * @return The session id of the {@link ServiceBusMessage}.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-sessions">Message Sessions</a>
     */
    public String getSessionId() {
        return amqpAnnotatedMessage.getProperties().getGroupId();
    }

    /**
     * Sets the session identifier for a session-aware entity.
     *
     * @param sessionId The session identifier to be set.
     *
     * @return The updated {@link ServiceBusMessage}.
     * @throws IllegalArgumentException if {@code sessionId} is too long or if the {@code sessionId} does not match
     *     the {@code partitionKey}.
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
     * Validates that the user can't set the partitionKey to a different value than the session ID. (this will
     * eventually migrate to a service-side check)
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
     * Validates that the user can't set the partitionKey to a different value than the session ID. (this will
     * eventually migrate to a service-side check)
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
