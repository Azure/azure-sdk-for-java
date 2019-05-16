// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.eventhubs.implementation.AmqpConstants;
import com.azure.eventhubs.implementation.EventDataUtil;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.azure.eventhubs.implementation.AmqpConstants.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.eventhubs.implementation.AmqpConstants.OFFSET_ANNOTATION_NAME;
import static com.azure.eventhubs.implementation.AmqpConstants.PARTITION_KEY_ANNOTATION_NAME;
import static com.azure.eventhubs.implementation.AmqpConstants.PUBLISHER_ANNOTATION_NAME;
import static com.azure.eventhubs.implementation.AmqpConstants.SEQUENCE_NUMBER_ANNOTATION_NAME;

/**
 * The data structure encapsulating the Event being sent-to and received-from EventHubs.
 * Each EventHubs partition can be visualized as a Stream of {@link EventData}.
 *
 * <p>
 * Serializing a received {@link EventData} with AMQP sections other than ApplicationProperties (with primitive Java
 * types) and Data section is not supported.
 * </p>
 *
 * <p>
 * Here's how AMQP message sections map to {@link EventData}. Here's the reference used for AMQP 1.0 specification:
 * http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-complete-v1.0-os.pdf
 *
 * <ol>
 *      <li>{@link #properties()} - AMQPMessage.ApplicationProperties section</li>
 *      <li>{@link #data()} - if AMQPMessage.Body has Data section</li>
 * </ol>
 *
 * While using client libraries released by Microsoft Azure EventHubs, sections (i) and (ii) alone are sufficient.
 */
public class EventData implements Serializable, Comparable<EventData> {
    private static final long serialVersionUID = -5631628195600014255L;
    private static final int BODY_DATA_NULL = -1;

    private final Map<String, Object> properties = new HashMap<>();
    private transient ByteBuffer data;

    private SystemProperties systemProperties = new SystemProperties(Collections.emptyMap());
    private String partitionKey;

    /**
     * Creates an event containing the {@code data}.
     *
     * @param data The data to set for this event.
     */
    public EventData(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("data cannot be null");
        }

        this.data = ByteBuffer.wrap(data);
    }

    /**
     * Creates an event containing the {@code data}.
     *
     * @param data The data to set for this event.
     */
    public EventData(ByteBuffer data) {
        if (data == null) {
            throw new IllegalArgumentException("'data' cannot be null");
        }

        this.data = data;
    }

    /**
     * Adds an application property associated with this event. If the {@code key} exists in the map, its existing value
     * is overwritten.
     *
     * @param key The key for this application property
     * @param value The value for this application property.
     * @return The updated EventData object.
     * @throws NullPointerException if {@code key} or {@code value} is null.
     */
    public EventData addProperty(String key, Object value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);

        properties.put(key, value);
        return this;
    }

    /**
     * Sets the partitionKey for this EventData
     *
     * @param partitionKey Key to set for this EventData.
     * @return The updated EventData object.
     */
    public EventData partitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
        return this;
    }

    /**
     * Gets the application property bag
     *
     * @return Application properties associated with this EventData.
     */
    public Map<String, Object> properties() {
        return properties;
    }

    /**
     * Gets the partitionKey for this EventData.
     *
     * @return The partitionKey for this EventData.
     */
    public String partitionKey() {
        return this.partitionKey;
    }

    /**
     * Properties that are populated by EventHubService. As these are populated by Service, they are only present on a
     * <b>received</b> EventData.
     * <p>
     * Usage:
     * <code>
     * final String offset = eventData.systemProperties().offset();
     * </code>
     * </p>
     *
     * @return an encapsulation of all SystemProperties appended by EventHubs service into EventData. {@code null} if
     * the {@link EventData} is not received and is created by the public constructors.
     * @see SystemProperties#offset()
     * @see SystemProperties#sequenceNumber()
     * @see SystemProperties#partitionKey()
     * @see SystemProperties#enqueuedTime()
     */
    public EventData.SystemProperties systemProperties() {
        return systemProperties;
    }

    /**
     * Gets the actual payload/data wrapped by EventData.
     *
     * @return ByteBuffer representing the data.
     */
    public ByteBuffer data() {
        return data;
    }

    /**
     * Creates the AMQP message represented by this EventData.
     *
     * @return A new AMQP message for this EventData.
     */
    Message createAmqpMessage() {
        final Message message = Proton.message();

        setApplicationProperties(message);
        setSystemProperties(message);
        setPartitionKey(message);

        if (data() != null) {
            message.setBody(new Data(Binary.create(data())));
        }

        return message;
    }

    private void setPartitionKey(Message message) {
        if (partitionKey() == null) {
            return;
        }

        final MessageAnnotations messageAnnotations = (message.getMessageAnnotations() == null)
            ? new MessageAnnotations(new HashMap<>())
            : message.getMessageAnnotations();
        messageAnnotations.getValue().put(AmqpConstants.PARTITION_KEY, partitionKey());
        message.setMessageAnnotations(messageAnnotations);
    }

    private void setApplicationProperties(Message message) {
        if (properties() == null || properties().isEmpty()) {
            return;
        }

        final ApplicationProperties applicationProperties = new ApplicationProperties(properties());
        message.setApplicationProperties(applicationProperties);
    }

    private void setSystemProperties(Message message) {
        if (systemProperties() == null || systemProperties().isEmpty()) {
            return;
        }

        systemProperties().forEach((key, value) -> {
            if (EventDataUtil.RESERVED_SYSTEM_PROPERTIES.contains(key)) {
                return;
            }

            if (AmqpConstants.RESERVED_PROPERTY_NAMES.contains(key)) {
                switch (key) {
                    case AmqpConstants.AMQP_PROPERTY_MESSAGE_ID:
                        message.setMessageId(value);
                        break;
                    case AmqpConstants.AMQP_PROPERTY_USER_ID:
                        message.setUserId((byte[]) value);
                        break;
                    case AmqpConstants.AMQP_PROPERTY_TO:
                        message.setAddress((String) value);
                        break;
                    case AmqpConstants.AMQP_PROPERTY_SUBJECT:
                        message.setSubject((String) value);
                        break;
                    case AmqpConstants.AMQP_PROPERTY_REPLY_TO:
                        message.setReplyTo((String) value);
                        break;
                    case AmqpConstants.AMQP_PROPERTY_CORRELATION_ID:
                        message.setCorrelationId(value);
                        break;
                    case AmqpConstants.AMQP_PROPERTY_CONTENT_TYPE:
                        message.setContentType((String) value);
                        break;
                    case AmqpConstants.AMQP_PROPERTY_CONTENT_ENCODING:
                        message.setContentEncoding((String) value);
                        break;
                    case AmqpConstants.AMQP_PROPERTY_ABSOLUTE_EXPRITY_TIME:
                        message.setExpiryTime((long) value);
                        break;
                    case AmqpConstants.AMQP_PROPERTY_CREATION_TIME:
                        message.setCreationTime((long) value);
                        break;
                    case AmqpConstants.AMQP_PROPERTY_GROUP_ID:
                        message.setGroupId((String) value);
                        break;
                    case AmqpConstants.AMQP_PROPERTY_GROUP_SEQUENCE:
                        message.setGroupSequence((long) value);
                        break;
                    case AmqpConstants.AMQP_PROPERTY_REPLY_TO_GROUP_ID:
                        message.setReplyToGroupId((String) value);
                        break;
                    default:
                        throw new IllegalArgumentException(String.format(Locale.US, "Property is not a recognized reserved property name: %s", key));
                }
            } else {
                final MessageAnnotations messageAnnotations = (message.getMessageAnnotations() == null)
                    ? new MessageAnnotations(new HashMap<>())
                    : message.getMessageAnnotations();
                messageAnnotations.getValue().put(Symbol.getSymbol(key), value);
                message.setMessageAnnotations(messageAnnotations);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(EventData other) {
        return Long.compare(
            this.systemProperties().sequenceNumber(),
            other.systemProperties().sequenceNumber()
        );
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
        return Objects.equals(data, eventData.data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    /**
     * A collection of properties populated by Azure Event Hubs service.
     */
    public class SystemProperties extends HashMap<String, Object> {
        private static final long serialVersionUID = -2827050124966993723L;

        SystemProperties(final Map<String, Object> map) {
            super(Collections.unmodifiableMap(map));
        }

        SystemProperties(final long sequenceNumber, final Instant enqueuedTimeUtc, final String offset, final String partitionKey) {
            this.put(SEQUENCE_NUMBER_ANNOTATION_NAME, sequenceNumber);
            this.put(ENQUEUED_TIME_UTC_ANNOTATION_NAME, new Date(enqueuedTimeUtc.toEpochMilli()));
            this.put(OFFSET_ANNOTATION_NAME, offset);
            this.put(PARTITION_KEY_ANNOTATION_NAME, partitionKey);
        }

        /**
         * Gets the offset within the Event Hubs stream.
         *
         * @return The offset within the Event Hubs stream.
         */
        public String offset() {
            return this.getSystemProperty(OFFSET_ANNOTATION_NAME);
        }

        /**
         * Gets a partition key used for message partitioning. If it exists, this value was used to compute a hash to
         * select a partition to send the message to.
         *
         * @return A partition key for this Event Data.
         */
        public String partitionKey() {
            return this.getSystemProperty(PARTITION_KEY_ANNOTATION_NAME);
        }

        /**
         * Gets the time this event was enqueued in the Event Hub.
         *
         * @return The time this was enqueued in the service.
         */
        public Instant enqueuedTime() {
            final Date enqueuedTimeValue = this.getSystemProperty(ENQUEUED_TIME_UTC_ANNOTATION_NAME);
            return enqueuedTimeValue != null ? enqueuedTimeValue.toInstant() : null;
        }

        /**
         * Gets the sequence number in the event stream for this event. This is unique for every message received in the
         * Event Hub.
         *
         * @return Sequence number for this event.
         */
        public long sequenceNumber() {
            final Long sequenceNumber = this.getSystemProperty(SEQUENCE_NUMBER_ANNOTATION_NAME);

            if (sequenceNumber == null) {
                throw new IllegalStateException(String.format(Locale.US, "sequenceNumber: %s should always be in map.", SEQUENCE_NUMBER_ANNOTATION_NAME));
            }

            return sequenceNumber;
        }

        /**
         * Gets the name of the publisher if this was sent to a publisher endpoint.
         *
         * @return The name of the publisher. Or {@code null} if this was not sent to a publisher endpoint.
         */
        public String publisher() {
            return this.getSystemProperty(PUBLISHER_ANNOTATION_NAME);
        }

        @SuppressWarnings("unchecked")
        private <T> T getSystemProperty(final String key) {
            if (this.containsKey(key)) {
                return (T) (this.get(key));
            }

            return null;
        }
    }
}
