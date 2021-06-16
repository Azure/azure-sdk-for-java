// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpMessageConstant;
import com.azure.core.amqp.models.AmqpAddress;
import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.amqp.models.AmqpMessageBody;
import com.azure.core.amqp.models.AmqpMessageBodyType;
import com.azure.core.amqp.models.AmqpMessageId;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.OFFSET_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.PARTITION_KEY_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.SCHEDULED_ENQUEUE_UTC_TIME_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * The data structure encapsulating the event being sent-to and received-from Event Hubs. Each Event Hub partition can
 * be visualized as a stream of {@link EventData}.
 *
 * <p>
 * Here's how AMQP message sections map to {@link EventData}. For reference, the specification can be found here:
 * <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-complete-v1.0-os.pdf">AMQP 1.0 specification</a>
 *
 * <ol>
 * <li>{@link #getProperties()} - AMQPMessage.ApplicationProperties section</li>
 * <li>{@link #getBody()} - if AMQPMessage.Body has Data section</li>
 * </ol>
 *
 * <p>
 * Serializing a received {@link EventData} with AMQP sections other than ApplicationProperties (with primitive Java
 * types) and Data section is not supported.
 * </p>
 *
 * @see EventDataBatch
 * @see EventHubProducerClient
 * @see EventHubProducerAsyncClient
 */
public class EventData {

    private static final int MAX_MESSAGE_ID_LENGTH = 128;
    private static final int MAX_PARTITION_KEY_LENGTH = 128;
    private static final int MAX_SESSION_ID_LENGTH = 128;

    private final BinaryData body;
    private final AmqpAnnotatedMessage amqpAnnotatedMessage;
    private final ClientLogger logger = new ClientLogger(EventData.class);

    private Context context;

    /**
     * Creates an event containing the {@code body}.
     *
     * @param body The data to set for this event.
     * @throws NullPointerException if {@code body} is {@code null}.
     */
    public EventData(byte[] body) {
        this(BinaryData.fromBytes(Objects.requireNonNull(body, "'body' cannot be null.")));
    }

    /**
     * Creates an event containing the {@code body}.
     *
     * @param body The data to set for this event.
     * @throws NullPointerException if {@code body} is {@code null}.
     */
    public EventData(ByteBuffer body) {
        this(Objects.requireNonNull(body, "'body' cannot be null.").array());
    }

    /**
     * Creates an event by encoding the {@code body} using UTF-8 charset.
     *
     * @param body The string that will be UTF-8 encoded to create an event.
     * @throws NullPointerException if {@code body} is {@code null}.
     */
    public EventData(String body) {
        this(Objects.requireNonNull(body, "'body' cannot be null.").getBytes(UTF_8));
    }

    /**
     * Creates an event with the provided {@link BinaryData} as payload.
     *
     * @param body The {@link BinaryData} payload for this event.
     */
    public EventData(BinaryData body) {
        this(body, new SystemProperties(), Context.NONE);
    }

    /**
     * Creates an event with the given {@code body}, system properties and context.
     *
     * @param body The data to set for this event.
     * @param systemProperties System properties set by message broker for this event.
     * @param context A specified key-value pair of type {@link Context}.
     * @throws NullPointerException if {@code body}, {@code systemProperties}, or {@code context} is {@code null}.
     */
    EventData(BinaryData body, SystemProperties systemProperties, Context context) {
        this.body = Objects.requireNonNull(body, "'body' cannot be null.");
        this.context = Objects.requireNonNull(context, "'context' cannot be null.");
        this.amqpAnnotatedMessage = new AmqpAnnotatedMessage(AmqpMessageBody.fromData(body.toBytes()));
        if (systemProperties.getOffset() != null) {
            amqpAnnotatedMessage.getMessageAnnotations().put(OFFSET_ANNOTATION_NAME.getValue(), systemProperties.getOffset());
        }
        if (systemProperties.getEnqueuedTime()!= null) {
            amqpAnnotatedMessage.getMessageAnnotations().put(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue(), systemProperties.getEnqueuedTime());
        }
        if (systemProperties.getPartitionKey() != null) {
            amqpAnnotatedMessage.getMessageAnnotations().put(PARTITION_KEY_ANNOTATION_NAME.getValue(), systemProperties.getPartitionKey());
        }
        if (systemProperties.getSequenceNumber() != null) {
            amqpAnnotatedMessage.getMessageAnnotations().put(SEQUENCE_NUMBER_ANNOTATION_NAME.getValue(), systemProperties.getSequenceNumber());
        }
    }

    /**
     * Gets the set of free-form event properties which may be used for passing metadata associated with the event with
     * the event body during Event Hubs operations. A common use-case for {@code properties()} is to associate
     * serialization hints for the {@link #getBody()} as an aid to consumers who wish to deserialize the binary data.
     *
     * <p><strong>Adding serialization hint using {@code getProperties()}</strong></p>
     * <p>In the sample, the type of telemetry is indicated by adding an application property with key "eventType".</p>
     *
     * {@codesnippet com.azure.messaging.eventhubs.eventdata.getProperties}
     *
     * @return Application properties associated with this {@link EventData}.
     */
    public Map<String, Object> getProperties() {
        return amqpAnnotatedMessage.getApplicationProperties();
    }

    /**
     * Gets the actual payload/data wrapped by EventData.
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
        final AmqpMessageBodyType type = amqpAnnotatedMessage.getBody().getBodyType();
        switch (type) {
            case DATA:
                return amqpAnnotatedMessage.getBody().getFirstData();
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
     * Returns event data as UTF-8 decoded string.
     *
     * @return UTF-8 decoded string representation of the event data.
     */
    public String getBodyAsString() {
        return new String(body.toBytes(), UTF_8);
    }

    /**
     * Returns the {@link BinaryData} payload associated with this event.
     *
     * @return the {@link BinaryData} payload associated with this event.
     */
    public BinaryData getBodyAsBinaryData() {
        return body;
    }

    /**
     * Gets the offset of the event when it was received from the associated Event Hub partition. This is only present
     * on a <b>received</b> {@link EventData}.
     *
     * @return The offset within the Event Hub partition of the received event. {@code null} if the {@link EventData}
     *     was not received from Event Hubs service.
     */
    public Long getOffset() {
        Object value = amqpAnnotatedMessage.getMessageAnnotations().get(OFFSET_ANNOTATION_NAME.getValue());
        return value != null
            ? value instanceof String ? Long.parseLong((String)value) : (Long) value
            : null;
    }

    /**
     * Gets the partition hashing key if it was set when originally publishing the event. If it exists, this value was
     * used to compute a hash to select a partition to send the message to. This is only present on a <b>received</b>
     * {@link EventData}.
     *
     * @return A partition key for this Event Data. {@code null} if the {@link EventData} was not received from Event
     *     Hubs service or there was no partition key set when the event was sent to the Event Hub.
     */
    public String getPartitionKey() {
        return (String) amqpAnnotatedMessage.getMessageAnnotations().get(PARTITION_KEY_ANNOTATION_NAME.getValue());
    }

    /**
     * Gets the instant, in UTC, of when the event was enqueued in the Event Hub partition. This is only present on a
     * <b>received</b> {@link EventData}.
     *
     * @return The instant, in UTC, this was enqueued in the Event Hub partition. {@code null} if the {@link EventData}
     *     was not received from Event Hubs service.
     */
    public Instant getEnqueuedTime() {
        Object value = amqpAnnotatedMessage.getMessageAnnotations().get(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue());
        return value != null
            ? ((Date) value).toInstant()
            : null;
    }

    /**
     * Gets the sequence number assigned to the event when it was enqueued in the associated Event Hub partition. This
     * is unique for every message received in the Event Hub partition. This is only present on a <b>received</b>
     * {@link EventData}.
     *
     * @return The sequence number for this event. {@code null} if the {@link EventData} was not received from Event
     *     Hubs service.
     */
    public Long getSequenceNumber() {
        Object value = amqpAnnotatedMessage.getMessageAnnotations().get(SEQUENCE_NUMBER_ANNOTATION_NAME.getValue());
        return value != null
            ? (Long) value
            : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EventData eventData = (EventData) o;
        return Arrays.equals(body.toBytes(), eventData.body.toBytes());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(body.toBytes());
    }

    /**
     * A specified key-value pair of type {@link Context} to set additional information on the event.
     *
     * @return the {@link Context} object set on the event
     */
    Context getContext() {
        return context;
    }

    /**
     * Adds a new key value pair to the existing context on Event Data.
     *
     * @param key The key for this context object
     * @param value The value for this context object.
     * @throws NullPointerException if {@code key} or {@code value} is null.
     * @return The updated {@link EventData}.
     */
    public EventData addContext(String key, Object value) {
        Objects.requireNonNull(key, "The 'key' parameter cannot be null.");
        Objects.requireNonNull(value, "The 'value' parameter cannot be null.");
        this.context = context.addData(key, value);

        return this;
    }

    /**
     * Gets the content type of the message.
     *
     * <p>
     * Optionally describes the payload of the message, with a descriptor following the format of RFC2045, Section 5,
     * for example "application/json".
     * </p>
     * @return The content type of the {@link EventData}.
     */
    public String getContentType() {
        return amqpAnnotatedMessage.getProperties().getContentType();
    }

    /**
     * Sets the content type of the {@link EventData}.
     *
     * <p>
     * Optionally describes the payload of the message, with a descriptor following the format of RFC2045, Section 5,
     * for example "application/json".
     * </p>
     *
     * @param contentType RFC2045 Content-Type descriptor of the message.
     *
     * @return The updated {@link EventData}.
     */
    public EventData setContentType(String contentType) {
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
     * @return The updated {@link EventData}.
     * @see #getCorrelationId()
     */
    public EventData setCorrelationId(String correlationId) {
        AmqpMessageId id = null;
        if (correlationId != null) {
            id = new AmqpMessageId(correlationId);
        }
        amqpAnnotatedMessage.getProperties().setCorrelationId(id);
        return this;
    }

    /**
     * Gets the message id.
     *
     * <p>
     * The message identifier is an application-defined value that uniquely identifies the message and its payload. The
     * identifier is a free-form string and can reflect a GUID or an identifier derived from the application context.
     * </p>
     *
     * @return Id of the {@link EventData}.
     */
    public byte[] getUserId() {
        return amqpAnnotatedMessage.getProperties().getUserId();
    }

    /**
     * Sets the message id.
     *
     * @param userId The message id to be set.
     *
     * @return The updated {@link EventData}.
     * @throws IllegalArgumentException if {@code messageId} is too long.
     */
    public EventData setUserId(byte[] userId) {
        amqpAnnotatedMessage.getProperties().setUserId(userId);
        return this;
    }

    /**
     * Gets the message id.
     *
     * <p>
     * The message identifier is an application-defined value that uniquely identifies the message and its payload. The
     * identifier is a free-form string and can reflect a GUID or an identifier derived from the application context.
     * </p>
     *
     * @return Id of the {@link EventData}.
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
     * @return The updated {@link EventData}.
     * @throws IllegalArgumentException if {@code messageId} is too long.
     */
    public EventData setMessageId(String messageId) {
        checkIdLength("messageId", messageId, MAX_MESSAGE_ID_LENGTH);
        AmqpMessageId id = null;
        if (messageId != null) {
            id = new AmqpMessageId(messageId);
        }
        amqpAnnotatedMessage.getProperties().setMessageId(id);
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
     * @return The updated {@link EventData} object.
     */
    public EventData setSubject(String subject) {
        amqpAnnotatedMessage.getProperties().setSubject(subject);
        return this;
    }

    /**
     * Gets the "to" address.
     *
     * <p>
     * This property is reserved for future use in routing scenarios and presently ignored by the broker itself.
     * Applications can use this value in rule-driven
     * auto-forward scenarios to indicate the intended logical destination of the message.
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
     * auto-forward chaining scenarios to indicate the intended logical destination of the message.
     * </p>
     *
     * @param to To property value of this message.
     *
     * @return The updated {@link EventData}.
     */
    public EventData setTo(String to) {
        AmqpAddress toAddress = null;
        if (to != null) {
            toAddress = new AmqpAddress(to);
        }
        amqpAnnotatedMessage.getProperties().setTo(toAddress);
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
     * @return The updated {@link EventData}.
     * @see #getReplyTo()
     */
    public EventData setReplyTo(String replyTo) {
        AmqpAddress replyToAddress = null;
        if (replyTo != null) {
            replyToAddress = new AmqpAddress(replyTo);
        }
        amqpAnnotatedMessage.getProperties().setReplyTo(replyToAddress);
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
     */
    public Duration getTimeToLive() {
        return amqpAnnotatedMessage.getHeader().getTimeToLive();
    }

    /**
     * Sets the duration of time before this message expires.
     *
     * @param timeToLive Time to Live duration of this message
     *
     * @return The updated {@link EventData}.
     * @see #getTimeToLive()
     */
    public EventData setTimeToLive(Duration timeToLive) {
        amqpAnnotatedMessage.getHeader().setTimeToLive(timeToLive);
        return this;
    }

    /**
     * Gets the session identifier for a session-aware entity.
     *
     * <p>
     * For session-aware entities, this application-defined value specifies the session affiliation of the message.
     * Messages with the same session identifier are subject to summary locking and enable exact in-order processing and
     * demultiplexing. For session-unaware entities, this value is ignored. See <a
     * </p>
     *
     * @return The session id of the {@link EventData}.
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
     * @return The updated {@link EventData}.
     * @throws IllegalArgumentException if {@code sessionId} is too long or if the {@code sessionId} does not match
     *     the {@code partitionKey}.
     */
    public EventData setSessionId(String sessionId) {
        checkIdLength("sessionId", sessionId, MAX_SESSION_ID_LENGTH);
        checkSessionId(sessionId);

        amqpAnnotatedMessage.getProperties().setGroupId(sessionId);
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
     * @return The updated {@link EventData}.
     * @see #getScheduledEnqueueTime()
     */
    public EventData setScheduledEnqueueTime(OffsetDateTime scheduledEnqueueTime) {
        if (scheduledEnqueueTime != null) {
            amqpAnnotatedMessage.getMessageAnnotations().put(SCHEDULED_ENQUEUE_UTC_TIME_NAME.getValue(),
                scheduledEnqueueTime);
        }
        return this;
    }

    /**
     * Sets a partition key for sending a message to a partitioned entity
     *
     * @param partitionKey The partition key of this message.
     *
     * @return The updated {@link EventData}.
     * @throws IllegalArgumentException if {@code partitionKey} is too long or if the {@code partitionKey} does not
     *     match the {@code sessionId}.
     * @see #getPartitionKey()
     */
    public EventData setPartitionKey(String partitionKey) {
        checkIdLength("partitionKey", partitionKey, MAX_PARTITION_KEY_LENGTH);
        checkPartitionKey(partitionKey);

        amqpAnnotatedMessage.getMessageAnnotations().put(PARTITION_KEY_ANNOTATION_NAME.getValue(), partitionKey);
        return this;
    }

    /**
     * Gets or sets a session identifier augmenting the {@link #getReplyTo() ReplyTo} address.
     * <p>
     * This value augments the {@link #getReplyTo() reply to} information and specifies which {@code sessionId} should
     * be set for the reply when sent to the reply entity.
     *
     * @return The {@code getReplyToGroupId} property value of this message.
     */
    public String getReplyToSessionId() {
        return amqpAnnotatedMessage.getProperties().getReplyToGroupId();
    }

    /**
     * Gets or sets a session identifier augmenting the {@link #getReplyTo() ReplyTo} address.
     *
     * @param replyToSessionId The ReplyToGroupId property value of this message.
     *
     * @return The updated {@link EventData}.
     */
    public EventData setReplyToSessionId(String replyToSessionId) {
        amqpAnnotatedMessage.getProperties().setReplyToGroupId(replyToSessionId);
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

    /**
     * A collection of properties populated by Azure Event Hubs service.
     */
    static class SystemProperties extends HashMap<String, Object> {
        private static final long serialVersionUID = -2827050124966993723L;
        private final Long offset;
        private final String partitionKey;
        private final Instant enqueuedTime;
        private final Long sequenceNumber;

        SystemProperties() {
            super();
            offset = null;
            partitionKey = null;
            enqueuedTime = null;
            sequenceNumber = null;
        }

        SystemProperties(final Map<String, Object> map) {
            super(map);
            this.partitionKey = removeSystemProperty(PARTITION_KEY_ANNOTATION_NAME.getValue());

            final String offset = removeSystemProperty(OFFSET_ANNOTATION_NAME.getValue());
            if (offset == null) {
                throw new IllegalStateException(String.format(Locale.US,
                    "offset: %s should always be in map.", OFFSET_ANNOTATION_NAME.getValue()));
            }
            this.offset = Long.valueOf(offset);
            put(OFFSET_ANNOTATION_NAME.getValue(), this.offset);

            final Date enqueuedTimeValue = removeSystemProperty(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue());
            if (enqueuedTimeValue == null) {
                throw new IllegalStateException(String.format(Locale.US,
                    "enqueuedTime: %s should always be in map.", ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue()));
            }
            this.enqueuedTime = enqueuedTimeValue.toInstant();
            put(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue(), this.enqueuedTime);

            final Long sequenceNumber = removeSystemProperty(SEQUENCE_NUMBER_ANNOTATION_NAME.getValue());
            if (sequenceNumber == null) {
                throw new IllegalStateException(String.format(Locale.US,
                    "sequenceNumber: %s should always be in map.", SEQUENCE_NUMBER_ANNOTATION_NAME.getValue()));
            }
            this.sequenceNumber = sequenceNumber;
            put(SEQUENCE_NUMBER_ANNOTATION_NAME.getValue(), this.sequenceNumber);
        }

        /**
         * Gets the offset within the Event Hubs stream.
         *
         * @return The offset within the Event Hubs stream.
         */
        private Long getOffset() {
            return offset;
        }

        /**
         * Gets a partition key used for message partitioning. If it exists, this value was used to compute a hash to
         * select a partition to send the message to.
         *
         * @return A partition key for this Event Data.
         */
        private String getPartitionKey() {
            return partitionKey;
        }

        /**
         * Gets the time this event was enqueued in the Event Hub.
         *
         * @return The time this was enqueued in the service.
         */
        private Instant getEnqueuedTime() {
            return enqueuedTime;
        }

        /**
         * Gets the sequence number in the event stream for this event. This is unique for every message received in the
         * Event Hub.
         *
         * @return Sequence number for this event.
         * @throws IllegalStateException if {@link SystemProperties} does not contain the sequence number in a retrieved
         * event.
         */
        private Long getSequenceNumber() {
            return sequenceNumber;
        }

        @SuppressWarnings("unchecked")
        private <T> T removeSystemProperty(final String key) {
            if (this.containsKey(key)) {
                return (T) (this.remove(key));
            }

            return null;
        }
    }

}
