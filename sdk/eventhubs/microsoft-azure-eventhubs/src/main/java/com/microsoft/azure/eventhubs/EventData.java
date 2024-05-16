// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs;

import com.microsoft.azure.eventhubs.impl.AmqpConstants;
import com.microsoft.azure.eventhubs.impl.EventDataImpl;
import org.apache.qpid.proton.amqp.Binary;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

/**
 * The data structure encapsulating the Event being sent-to and received-from EventHubs.
 * Each EventHubs partition can be visualized as a Stream of {@link EventData}.
 * <p>
 * Serializing a received {@link EventData} with AMQP sections other than ApplicationProperties (with primitive java types) and Data section is not supported.
 * <p>
 * Here's how AMQP message sections map to {@link EventData}. Here's the reference used for AMQP 1.0 specification: http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-complete-v1.0-os.pdf
 * <pre>
 * i.   {@link #getProperties()} - AMQPMessage.ApplicationProperties section
 * ii.  {@link #getBytes()} - if AMQPMessage.Body has Data section
 * iii. {@link #getObject()} - if AMQPMessage.Body has AMQPValue or AMQPSequence sections
 * </pre>
 * While using client libraries released by Microsoft Azure EventHubs, sections (i) and (ii) alone are sufficient.
 * Section (iii) is used for advanced scenarios, where the sending application uses third-party AMQP library to send the message to EventHubs and the receiving application
 * uses this client library to receive {@link EventData}.
 */
public interface EventData extends Serializable, Comparable<EventData> {

    /**
     * Construct EventData to Send to EventHubs.
     * Typical pattern to create a Sending EventData is:
     * <pre>
     * i.   Serialize the sending ApplicationEvent to be sent to EventHubs into bytes.
     * ii.  If complex serialization logic is involved (for example: multiple types of data) - add a Hint using the {@link #getProperties()} for the Consumer.
     * </pre>
     * <p> Sample Code:
     * <pre>
     * EventData eventData = EventData.create(telemetryEventBytes);
     * eventData.getProperties().put("eventType", "com.microsoft.azure.monitoring.EtlEvent");
     * partitionSender.Send(eventData);
     * </pre>
     *
     * @param data the actual payload of data in bytes to be Sent to EventHubs.
     * @return EventData the created {@link EventData} to send to EventHubs.
     * @see EventHubClient#createFromConnectionString(String, ScheduledExecutorService)
     */
    static EventData create(final byte[] data) {
        return new EventDataImpl(data);
    }

    /**
     * Construct EventData to Send to EventHubs.
     * Typical pattern to create a Sending EventData is:
     * <pre>
     * i.   Serialize the sending ApplicationEvent to be sent to EventHubs into bytes.
     * ii.  If complex serialization logic is involved (for example: multiple types of data) - add a Hint using the {@link #getProperties()} for the Consumer.
     *  </pre>
     * <p> Illustration:
     * <pre> {@code
     *  EventData eventData = EventData.create(telemetryEventBytes, offset, length);
     *  eventData.getProperties().put("eventType", "com.microsoft.azure.monitoring.EtlEvent");
     *  partitionSender.Send(eventData);
     *  }</pre>
     *
     * @param data   the byte[] where the payload of the Event to be sent to EventHubs is present
     * @param offset Offset in the byte[] to read from ; inclusive index
     * @param length length of the byte[] to be read, starting from offset
     * @return EventData the created {@link EventData} to send to EventHubs.
     * @see EventHubClient#createFromConnectionString(String, ScheduledExecutorService)
     */
    static EventData create(final byte[] data, final int offset, final int length) {
        return new EventDataImpl(data, offset, length);
    }

    /**
     * Construct EventData to Send to EventHubs.
     * Typical pattern to create a Sending EventData is:
     * <pre>
     * i.   Serialize the sending ApplicationEvent to be sent to EventHubs into bytes.
     * ii.  If complex serialization logic is involved (for example: multiple types of data) - add a Hint using the {@link #getProperties()} for the Consumer.
     *  </pre>
     * <p> Illustration:
     * <pre> {@code
     *  EventData eventData = EventData.create(telemetryEventByteBuffer);
     *  eventData.getProperties().put("eventType", "com.microsoft.azure.monitoring.EtlEvent");
     *  partitionSender.Send(eventData);
     *  }</pre>
     *
     * @param buffer ByteBuffer which references the payload of the Event to be sent to EventHubs
     * @return EventData the created {@link EventData} to send to EventHubs.
     * @see EventHubClient#createFromConnectionString(String, ScheduledExecutorService)
     */
    static EventData create(final ByteBuffer buffer) {
        return new EventDataImpl(buffer);
    }

    /**
     * Use this method only if, the sender could be sending messages using third-party AMQP libraries.
     * <p>If all the senders of EventHub use client libraries released and maintained by Microsoft Azure EventHubs, use {@link #getBytes()} method.
     * <p>Get the value of AMQP messages' Body section on the received {@link EventData}.
     * <p>If the AMQP message Body is always guaranteed to have Data section, use {@link #getBytes()} method.
     *
     * @return returns the Object which could represent either Data or AmqpValue or AmqpSequence.
     * <p>{@link Binary} if the Body is Data section
     * <p>{@link java.util.List} if the Body is AmqpSequence
     * <p>package org.apache.qpid.proton.amqp contains various AMQP types that could be returned.
     */
    Object getObject();

    /**
     * Get Actual Payload/Data wrapped by EventData.
     *
     * @return byte[] of the actual data
     * <p>null if the body of the message has other inter-operable AMQP messages, whose body does not represent byte[].
     * In that case use {@link #getObject()}.
     */
    byte[] getBytes();

    /**
     * Application property bag
     *
     * @return returns Application properties
     */
    Map<String, Object> getProperties();

    /**
     * SystemProperties that are populated by EventHubService.
     * <p>As these are populated by Service, they are only present on a Received EventData.
     * <p>Usage:<p>
     * <code>
     * final String offset = eventData.getSystemProperties().getOffset();
     * </code>
     *
     * @return an encapsulation of all SystemProperties appended by EventHubs service into EventData.
     * <code>null</code> if the {@link EventData} is not received and is created by the public constructors.
     * @see SystemProperties#getOffset
     * @see SystemProperties#getSequenceNumber
     * @see SystemProperties#getPartitionKey
     * @see SystemProperties#getEnqueuedTime
     */
    SystemProperties getSystemProperties();
    void setSystemProperties(SystemProperties props);

    class SystemProperties extends HashMap<String, Object> {
        private static final long serialVersionUID = -2827050124966993723L;

        public SystemProperties(final HashMap<String, Object> map) {
            super(Collections.unmodifiableMap(map));
        }

        public SystemProperties(final long sequenceNumber, final Instant enqueuedTimeUtc, final String offset, final String partitionKey) {
            this.put(AmqpConstants.SEQUENCE_NUMBER_ANNOTATION_NAME, sequenceNumber);
            this.put(AmqpConstants.ENQUEUED_TIME_UTC_ANNOTATION_NAME, new Date(enqueuedTimeUtc.toEpochMilli()));
            this.put(AmqpConstants.OFFSET_ANNOTATION_NAME, offset);
            this.put(AmqpConstants.PARTITION_KEY_ANNOTATION_NAME, partitionKey);
        }

        public String getOffset() {
            return this.getSystemProperty(AmqpConstants.OFFSET_ANNOTATION_NAME);
        }

        public String getPartitionKey() {
            return this.getSystemProperty(AmqpConstants.PARTITION_KEY_ANNOTATION_NAME);
        }

        public Instant getEnqueuedTime() {
            final Date enqueuedTimeValue = this.getSystemProperty(AmqpConstants.ENQUEUED_TIME_UTC_ANNOTATION_NAME);
            return enqueuedTimeValue != null ? enqueuedTimeValue.toInstant() : null;
        }

        public long getSequenceNumber() {
            return this.getSystemProperty(AmqpConstants.SEQUENCE_NUMBER_ANNOTATION_NAME);
        }

        public String getPublisher() {
            return this.getSystemProperty(AmqpConstants.PUBLISHER_ANNOTATION_NAME);
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
