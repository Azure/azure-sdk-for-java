// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.amqp.models.AmqpMessageBody;
import com.azure.core.amqp.models.AmqpMessageBodyType;
import com.azure.core.amqp.models.AmqpMessageId;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.OFFSET_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.PARTITION_KEY_ANNOTATION_NAME;
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

    private final BinaryData body;
    private final AmqpAnnotatedMessage amqpAnnotatedMessage;
    private final ClientLogger logger = new ClientLogger(EventData.class);
    private final SystemProperties systemProperties;
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
        this.systemProperties =  Objects.requireNonNull(systemProperties, "'systemProperties' cannot be null.");
        this.amqpAnnotatedMessage = new AmqpAnnotatedMessage(AmqpMessageBody.fromData(body.toBytes()));
        if (Objects.nonNull(this.systemProperties.getOffset())) {
            amqpAnnotatedMessage.getMessageAnnotations().put(OFFSET_ANNOTATION_NAME.getValue(),
                this.systemProperties.getOffset());
        }
        if (Objects.nonNull(this.systemProperties.getSequenceNumber())) {
            amqpAnnotatedMessage.getMessageAnnotations().put(SEQUENCE_NUMBER_ANNOTATION_NAME.getValue(),
                this.systemProperties.getSequenceNumber());
        }
        if (Objects.nonNull(this.systemProperties.getPartitionKey())) {
            String partitionKey = this.systemProperties.getPartitionKey();
            checkIdLength("partitionKey", partitionKey, MAX_PARTITION_KEY_LENGTH);
            checkPartitionKey(partitionKey);
            amqpAnnotatedMessage.getMessageAnnotations().put(PARTITION_KEY_ANNOTATION_NAME.getValue(), partitionKey);
        }
        if (Objects.nonNull(this.systemProperties.getEnqueuedTime())) {
            amqpAnnotatedMessage.getMessageAnnotations().put(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue(),
                this.systemProperties.getEnqueuedTime());
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
     * Properties that are populated by Event Hubs service. As these are populated by the Event Hubs service, they are
     * only present on a <b>received</b> {@link EventData}.
     *
     * @return An encapsulation of all system properties appended by EventHubs service into {@link EventData}.
     *     {@code null} if the {@link EventData} is not received from the Event Hubs service.
     */
    public Map<String, Object> getSystemProperties() {
        return systemProperties;
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
     * Gets the offset of the event when it was received from the associated Event Hub partition. This is only present
     * on a <b>received</b> {@link EventData}.
     *
     * @return The offset within the Event Hub partition of the received event. {@code null} if the {@link EventData}
     *     was not received from Event Hubs service.
     */
    public Long getOffset() {
        Object value = amqpAnnotatedMessage.getMessageAnnotations().get(OFFSET_ANNOTATION_NAME.getValue());
        return value != null
            ? (Long) value
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
    public int hashCode() {
        return Arrays.hashCode(body.toBytes());
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
     * Gets the {@link AmqpAnnotatedMessage}.
     *
     * @return The raw AMQP message.
     */
    public AmqpAnnotatedMessage getRawAmqpMessage() {
        return amqpAnnotatedMessage;
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
    EventData addContext(String key, Object value) {
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
    private void checkPartitionKey(String proposedPartitionKey) {
        if (proposedPartitionKey == null) {
            return;
        }

        if (amqpAnnotatedMessage.getProperties().getGroupId() != null && amqpAnnotatedMessage.getProperties().getGroupId().compareTo(proposedPartitionKey) != 0) {
            final String message = String.format(
                "partitionKey:%s cannot be set to a different value than sessionId:%s.",
                proposedPartitionKey,
                amqpAnnotatedMessage.getProperties().getGroupId());

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

            final Long offset = removeSystemProperty(OFFSET_ANNOTATION_NAME.getValue());
            if (offset == null) {
                throw new IllegalStateException(String.format(Locale.US,
                    "offset: %s should always be in map.", OFFSET_ANNOTATION_NAME.getValue()));
            }
            this.offset = offset;
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
