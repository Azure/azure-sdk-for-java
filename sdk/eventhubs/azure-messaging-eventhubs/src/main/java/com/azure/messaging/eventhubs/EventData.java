// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.amqp.models.AmqpMessageBody;
import com.azure.core.amqp.models.AmqpMessageHeader;
import com.azure.core.amqp.models.AmqpMessageId;
import com.azure.core.amqp.models.AmqpMessageProperties;
import com.azure.core.models.MessageContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.OFFSET_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.PARTITION_KEY_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.PUBLISHER_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * The data structure encapsulating the event being sent-to and received-from Event Hubs. Each Event Hub partition can
 * be visualized as a stream of {@link EventData}. This class is not thread-safe.
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
public class EventData extends MessageContent {
    /*
     * These are properties owned by the service and set when a message is received.
     */
    static final Set<String> RESERVED_SYSTEM_PROPERTIES;
    static final AmqpAnnotatedMessage EMPTY_MESSAGE = new AmqpAnnotatedMessage(AmqpMessageBody.fromData(new byte[0]));

    private static final ClientLogger LOGGER = new ClientLogger(EventData.class);
    private final Map<String, Object> properties;
    private final SystemProperties systemProperties;
    private AmqpAnnotatedMessage annotatedMessage;
    private Context context;

    static {
        final Set<String> properties = new HashSet<>();
        properties.add(OFFSET_ANNOTATION_NAME.getValue());
        properties.add(PARTITION_KEY_ANNOTATION_NAME.getValue());
        properties.add(SEQUENCE_NUMBER_ANNOTATION_NAME.getValue());
        properties.add(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue());
        properties.add(PUBLISHER_ANNOTATION_NAME.getValue());

        RESERVED_SYSTEM_PROPERTIES = Collections.unmodifiableSet(properties);
    }

    /**
     * Creates an event with an empty body.
     */
    public EventData() {
        this.context = Context.NONE;
        this.annotatedMessage = EMPTY_MESSAGE;
        this.properties = annotatedMessage.getApplicationProperties();
        this.systemProperties = new SystemProperties();
    }

    /**
     * Creates an event containing the {@code body}.
     *
     * @param body The data to set for this event.
     *
     * @throws NullPointerException if {@code body} is {@code null}.
     */
    public EventData(byte[] body) {
        this.context = Context.NONE;
        final AmqpMessageBody messageBody = AmqpMessageBody.fromData(
            Objects.requireNonNull(body, "'body' cannot be null."));

        this.annotatedMessage = new AmqpAnnotatedMessage(messageBody);
        this.properties = annotatedMessage.getApplicationProperties();
        this.systemProperties = new SystemProperties();
    }

    /**
     * Creates an event containing the {@code body}.
     *
     * @param body The data to set for this event.
     *
     * @throws NullPointerException if {@code body} is {@code null}.
     */
    public EventData(ByteBuffer body) {
        // Extract the ByteBuffer as it isn't guaranteed that the ByteBuffer will be a HeapByteBuffer and using
        // .array() on a DirectByteBuffer or read-only ByteBuffer will throw an exception. Additionally, even if the
        // ByteBuffer was a HeapByteBuffer the entire backing array may not have been written.
        //
        // Duplicate the ByteBuffer so the original body won't have its read position mutated.
        this(FluxUtil.byteBufferToArray(Objects.requireNonNull(body, "'body' cannot be null.").duplicate()));
    }

    /**
     * Creates an event by encoding the {@code body} using UTF-8 charset.
     *
     * @param body The string that will be UTF-8 encoded to create an event.
     *
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
        this(Objects.requireNonNull(body, "'body' cannot be null.").toBytes());
    }

    /**
     * Creates an event with the given {@code body}, system properties and context. Used in the case where a message
     * is received from the service.
     *
     * @param context A specified key-value pair of type {@link Context}.
     * @param amqpAnnotatedMessage Backing annotated message.
     *
     * @throws NullPointerException if {@code amqpAnnotatedMessage} or {@code context} is {@code null}.
     * @throws IllegalArgumentException if {@code amqpAnnotatedMessage}'s body type is unknown.
     */
    EventData(AmqpAnnotatedMessage amqpAnnotatedMessage, SystemProperties systemProperties, Context context) {
        this.context = Objects.requireNonNull(context, "'context' cannot be null.");
        this.properties = Collections.unmodifiableMap(amqpAnnotatedMessage.getApplicationProperties());
        this.annotatedMessage = Objects.requireNonNull(amqpAnnotatedMessage,
            "'amqpAnnotatedMessage' cannot be null.");
        this.systemProperties = systemProperties;

        switch (annotatedMessage.getBody().getBodyType()) {
            case DATA:
                break;
            case SEQUENCE:
            case VALUE:
                LOGGER.warning("Message body type '{}' is not supported in EH. "
                    + " Getting contents of body may throw.", annotatedMessage.getBody().getBodyType());
                break;
            default:
                throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                    "Body type not valid " + annotatedMessage.getBody().getBodyType()));
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
     * <!-- src_embed com.azure.messaging.eventhubs.eventdata.getProperties -->
     * <pre>
     * TelemetryEvent telemetry = new TelemetryEvent&#40;&quot;temperature&quot;, &quot;37&quot;&#41;;
     * byte[] serializedTelemetryData = telemetry.toString&#40;&#41;.getBytes&#40;UTF_8&#41;;
     *
     * EventData eventData = new EventData&#40;serializedTelemetryData&#41;;
     * eventData.getProperties&#40;&#41;.put&#40;&quot;eventType&quot;, TelemetryEvent.class.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.messaging.eventhubs.eventdata.getProperties -->
     *
     * <p>
     * The following types are supported:
     * <ul>
     *     <li>{@link java.math.BigInteger}</li>
     *     <li>{@link java.math.BigDecimal}</li>
     *     <li>{@link Boolean}</li>
     *     <li>{@link Byte}</li>
     *     <li>{@code byte[]}</li>
     *     <li>{@link Character}</li>
     *     <li>{@link java.util.Date}</li>
     *     <li>{@link Double}</li>
     *     <li>{@link Float}</li>
     *     <li>{@link Integer}</li>
     *     <li>{@link Short}</li>
     *     <li>{@link String}</li>
     *     <li>{@link Instant}</li>
     *     <li>{@link java.util.UUID}</li>
     * </ul>
     *
     *
     * @return Application properties associated with this {@link EventData}. For received {@link EventData}, the map is
     *     a read-only view.
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Properties that are populated by Event Hubs service. As these are populated by the Event Hubs service, they are
     * only present on a <b>received</b> {@link EventData}. Provides an abstraction on top of properties exposed by
     * {@link #getRawAmqpMessage()}. These properties are read-only and can be modified via
     * {@link #getRawAmqpMessage()}.
     *
     * @return An encapsulation of all system properties appended by EventHubs service into {@link EventData}. If the
     *     {@link EventData} is not received from the Event Hubs service, the values returned are {@code null}.
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
        return annotatedMessage.getBody().getFirstData();
    }

    /**
     * Returns event data as UTF-8 decoded string.
     *
     * @return UTF-8 decoded string representation of the event data.
     */
    public String getBodyAsString() {
        return new String(annotatedMessage.getBody().getFirstData(), UTF_8);
    }

    /**
     * Returns the {@link BinaryData} payload associated with this event.
     *
     * @return the {@link BinaryData} payload associated with this event.
     */
    @Override
    public BinaryData getBodyAsBinaryData() {
        return BinaryData.fromBytes(annotatedMessage.getBody().getFirstData());
    }

    /**
     * Sets a new binary body and corresponding {@link AmqpAnnotatedMessage} on the event. Contents from
     * {@link #getRawAmqpMessage()} are shallow copied to the new underlying message.
     */
    @Override
    public EventData setBodyAsBinaryData(BinaryData binaryData) {
        final AmqpAnnotatedMessage current = this.annotatedMessage;
        this.annotatedMessage = new AmqpAnnotatedMessage(AmqpMessageBody.fromData(binaryData.toBytes()));

        if (current == null) {
            return this;
        }

        this.annotatedMessage.getApplicationProperties().putAll(current.getApplicationProperties());
        this.annotatedMessage.getDeliveryAnnotations().putAll(current.getDeliveryAnnotations());
        this.annotatedMessage.getFooter().putAll(current.getFooter());
        this.annotatedMessage.getMessageAnnotations().putAll(current.getMessageAnnotations());

        final AmqpMessageHeader header = this.annotatedMessage.getHeader();
        header.setDeliveryCount(current.getHeader().getDeliveryCount())
            .setDurable(current.getHeader().isDurable())
            .setFirstAcquirer(current.getHeader().isFirstAcquirer())
            .setPriority(current.getHeader().getPriority())
            .setTimeToLive(current.getHeader().getTimeToLive());

        final AmqpMessageProperties props = this.annotatedMessage.getProperties();
        props.setAbsoluteExpiryTime(current.getProperties().getAbsoluteExpiryTime())
            .setContentEncoding(current.getProperties().getContentEncoding())
            .setContentType(current.getProperties().getContentType())
            .setCorrelationId(current.getProperties().getCorrelationId())
            .setCreationTime(current.getProperties().getCreationTime())
            .setGroupId(current.getProperties().getGroupId())
            .setGroupSequence(current.getProperties().getGroupSequence())
            .setMessageId(current.getProperties().getMessageId())
            .setReplyTo(current.getProperties().getReplyTo())
            .setReplyToGroupId(current.getProperties().getReplyToGroupId())
            .setSubject(current.getProperties().getSubject())
            .setTo(current.getProperties().getTo())
            .setUserId(current.getProperties().getUserId());

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
        return systemProperties.getOffset();
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
        return systemProperties.getPartitionKey();
    }

    /**
     * Gets the instant, in UTC, of when the event was enqueued in the Event Hub partition. This is only present on a
     * <b>received</b> {@link EventData}.
     *
     * @return The instant, in UTC, this was enqueued in the Event Hub partition. {@code null} if the {@link EventData}
     *     was not received from Event Hubs service.
     */
    public Instant getEnqueuedTime() {
        return systemProperties.getEnqueuedTime();
    }

    /**
     * Gets the sequence number assigned to the event when it was enqueued in the associated Event Hub partition. This
     * is unique for every message received in the Event Hub partition. This is only present on a <b>received</b> {@link
     * EventData}.
     *
     * @return The sequence number for this event. {@code null} if the {@link EventData} was not received from Event
     *     Hubs service.
     */
    public Long getSequenceNumber() {
        return systemProperties.getSequenceNumber();
    }

    /**
     * Gets the underlying AMQP message.
     *
     * @return The underlying AMQP message.
     */
    public AmqpAnnotatedMessage getRawAmqpMessage() {
        return annotatedMessage;
    }

    /**
     * Gets the content type.
     *
     * @return The content type.
     */
    public String getContentType() {
        return annotatedMessage.getProperties().getContentType();
    }

    /**
     * Sets the content type.
     *
     * @param contentType The content type.
     *
     * @return The updated {@link EventData}.
     */
    public EventData setContentType(String contentType) {
        annotatedMessage.getProperties().setContentType(contentType);
        return this;
    }

    /**
     * Gets the correlation id.
     *
     * @return The correlation id. {@code null} if there is none set.
     */
    public String getCorrelationId() {
        final AmqpMessageId messageId = annotatedMessage.getProperties().getCorrelationId();
        return messageId != null ? messageId.toString() : null;
    }

    /**
     * Sets the correlation id.
     *
     * @param correlationId The correlation id.
     *
     * @return The updated {@link EventData}.
     */
    public EventData setCorrelationId(String correlationId) {
        final AmqpMessageId id = correlationId != null ? new AmqpMessageId(correlationId) : null;

        annotatedMessage.getProperties().setCorrelationId(id);
        return this;
    }

    /**
     * Gets the message id.
     *
     * @return The message id. {@code null} if there is none set.
     */
    public String getMessageId() {
        final AmqpMessageId messageId = annotatedMessage.getProperties().getMessageId();
        return messageId != null ? messageId.toString() : null;
    }

    /**
     * Sets the message id.
     *
     * @param messageId The message id.
     *
     * @return The updated {@link EventData}.
     */
    public EventData setMessageId(String messageId) {
        final AmqpMessageId id = messageId != null ? new AmqpMessageId(messageId) : null;

        annotatedMessage.getProperties().setMessageId(id);
        return this;
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
        return Arrays.equals(annotatedMessage.getBody().getFirstData(),
            eventData.annotatedMessage.getBody().getFirstData());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(annotatedMessage.getBody().getFirstData());
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
     *
     * @return The updated {@link EventData}.
     *
     * @throws NullPointerException if {@code key} or {@code value} is null.
     */
    public EventData addContext(String key, Object value) {
        Objects.requireNonNull(key, "The 'key' parameter cannot be null.");
        Objects.requireNonNull(value, "The 'value' parameter cannot be null.");
        this.context = context.addData(key, value);

        return this;
    }
}
