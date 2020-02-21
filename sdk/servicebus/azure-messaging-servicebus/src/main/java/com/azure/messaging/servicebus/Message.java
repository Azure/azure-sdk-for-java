// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.Context;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.PARTITION_KEY_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.PUBLISHER_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * The data structure encapsulating the message being sent-to and received-from Service Bus.
 * Each Service Bus entity can be visualized as a stream of {@link Message}.
 *
 * <p>
 * Here's how AMQP message sections map to {@link Message}. For reference, the specification can be found here:
 * <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-complete-v1.0-os.pdf">AMQP 1.0 specification</a>
 *
 * <ol>
 * <li>{@link #getProperties()} - AMQPMessage.ApplicationProperties section</li>
 * <li>{@link #getBody()} - if AMQPMessage.Body has Data section</li>
 * </ol>
 *
 * <p>
 * Serializing a received {@link Message} with AMQP sections other than ApplicationProperties (with primitive Java
 * types) and Data section is not supported.
 * </p>
 *
 * @see MessageBatch
 */
public class Message {
    /*
     * These are properties owned by the service and set when a message is received.
     */
    static final Set<String> RESERVED_SYSTEM_PROPERTIES;
    private static final Charset DEFAULT_CHAR_SET = Charset.forName("UTF-8");
    private final Map<String, Object> properties;
    private final byte[] body;
    private final SystemProperties systemProperties;
    private Context context;
    private UUID lockToken;
    private String messageId;
    private String contentType;
    private String sessionId;
    private long sequenceNumber;


    static {
        final Set<String> properties = new HashSet<>();
        properties.add(PARTITION_KEY_ANNOTATION_NAME.getValue());
        properties.add(SEQUENCE_NUMBER_ANNOTATION_NAME.getValue());
        properties.add(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue());
        properties.add(PUBLISHER_ANNOTATION_NAME.getValue());

        RESERVED_SYSTEM_PROPERTIES = Collections.unmodifiableSet(properties);
    }

    /**
     * Creates a {@link Message} containing the {@code body}.
     *
     * @param body The data to set for this {@link Message}.
     * @throws NullPointerException if {@code body} is {@code null}.
     */
    public Message(byte[] body) {
        this.body = Objects.requireNonNull(body, "'body' cannot be null.");
        this.context = Context.NONE;
        this.properties = new HashMap<>();
        this.systemProperties = new SystemProperties();
        this.messageId = null;
    }

    /**
     * Creates a {@link Message} containing the {@code body}.
     *
     * @param body The data to set for this {@link Message}.
     * @throws NullPointerException if {@code body} is {@code null}.
     */
    public Message(ByteBuffer body) {
        this(Objects.requireNonNull(body, "'body' cannot be null.").array());
    }

    /**
     * Creates a {@link Message} by encoding the {@code body} using UTF-8 charset.
     *
     * @param body The string that will be UTF-8 encoded to create a {@link Message}.
     * @throws NullPointerException if {@code body} is {@code null}.
     */
    public Message(String body) {
        this(Objects.requireNonNull(body, "'body' cannot be null.").getBytes(UTF_8));
    }

    /**
     * Creates a message from a string. For backward compatibility reasons, the string is converted to a byte array
     * and message body type is set to binary.
     * @param body body of the message
     * @param contentType body type of the message
     */
    public Message(String body, String contentType) {
        this(body.getBytes(DEFAULT_CHAR_SET), contentType);
    }

    /**
     * Creates a message from a byte array. Message body type is set to binary.
     * @param body body of the message
     * @param contentType content type of the message
     */
    public Message(byte[] body, String contentType) {
        this(body);
        this.contentType = contentType;
    }

    /**
     * Creates a message from a string. For backward compatibility reasons, the string is converted to a byte array.
     *
     * @param messageId id of the {@link Message}.
     * @param body body of the {@link Message}.
     * @param contentType content type of the {@link Message}.
     */
    public Message(String messageId, String body, String contentType) {
        this(messageId, body.getBytes(DEFAULT_CHAR_SET), contentType);
    }

    /**
     * Creates a {@link Message} from a byte array.
     *
     * @param messageId id of the {@link Message}.
     * @param body body of the {@link Message}.
     * @param contentType content type of the {@link Message}.
     */
    public Message(String messageId, byte[] body, String contentType) {
        this(body, contentType);
        this.messageId = messageId;
    }

    /**
     * Creates a {@link Message} with the given {@code body}, system properties and context.
     *
     * @param body The data to set for this {@link Message}.
     * @param systemProperties System properties set by message broker for this {@link Message}.
     * @param context A specified key-value pair of type {@link Context}.
     * @throws NullPointerException if {@code body}, {@code systemProperties}, or {@code context} is {@code null}.
     */
    public Message(byte[] body, Map<String, Object> systemProperties, Context context) {
        this.body = Objects.requireNonNull(body, "'body' cannot be null.");
        this.context = Objects.requireNonNull(context, "'context' cannot be null.");
        this.systemProperties = new SystemProperties(Objects.requireNonNull(systemProperties,
            "'systemProperties' cannot be null."));
        this.properties = new HashMap<>();
    }

    /**
     * Gets the set of free-form {@link Message} properties which may be used for passing metadata associated with
     * the {@link Message}  during Service Bus operations. A common use-case for {@code properties()} is to associate
     * serialization hints for the {@link #getBody()} as an aid to consumers who wish to deserialize the binary data.
     *
     * <p><strong>Adding serialization hint using {@code getProperties()}</strong></p>
     * <p>In the sample, the type of telemetry is indicated by adding an application property
     * with key "messageType".</p>
     *
     * @return Application properties associated with this {@link Message}.
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Properties that are populated by Service Bus. As these are populated by the Service Bus, they are
     * only present on a <b>received</b> {@link Message}.
     *
     * @return An encapsulation of all system properties appended by Service Bus into {@link Message}. {@code null} if
     * the {@link Message} is not received from the Service Bus service.
     */
    public Map<String, Object> getSystemProperties() {
        return systemProperties;
    }

    /**
     *
     * @return Id of the {@link Message}.
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Sets the message id.
     * @param messageId of the {@link Message}.
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     *
     * @return the contentType of the {@link Message}.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the content type of the {@link Message}.
     * @param contentType of the message.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
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
        return Arrays.copyOf(body, body.length);
    }

    /**
     * Returns message body as UTF-8 decoded string.
     *
     * @return UTF-8 decoded string representation of the event data.
     */
    public String getBodyAsString() {
        return new String(body, UTF_8);
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

        Message message = (Message) o;
        return Arrays.equals(body, message.body);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(body);
    }

    /**
     * A specified key-value pair of type {@link Context} to set additional information on the {@link Message}.
     *
     * @return the {@link Context} object set on the {@link Message}.
     */
    Context getContext() {
        return context;
    }

    /**
     * Adds a new key value pair to the existing context on Message.
     *
     * @param key The key for this context object
     * @param value The value for this context object.
     * @throws NullPointerException if {@code key} or {@code value} is null.
     * @return The updated {@link Message}.
     */
    public Message addContext(String key, Object value) {
        Objects.requireNonNull(key, "The 'key' parameter cannot be null.");
        Objects.requireNonNull(value, "The 'value' parameter cannot be null.");
        this.context = context.addData(key, value);

        return this;
    }

    /**
     * Gets the unique number assigned to a message by Service Bus.
     *
     * The sequence number is a unique 64-bit integer assigned to a message as it is accepted and stored by the broker
     * and functions as its true identifier. For partitioned entities, the topmost 16 bits reflect the
     * partition identifier. Sequence numbers monotonically increase and are gapless. They roll over to 0 when
     * the 48-64 bit range is exhausted. This property is read-only.
     *
     * @return sequence number of this message
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-sequencing">Message Sequencing and Timestamps</a>
     */
    public long getSequenceNumber() {
        return this.sequenceNumber;
    }

    /**
     *
     * @return The lockToken.
     */
    public UUID getLockToken() {
        return lockToken;
    }

    /**
     * A collection of properties populated by Azure Service Bus service.
     */
    static class SystemProperties extends HashMap<String, Object> {
        private static final long serialVersionUID = -2827050124966993723L;

        private final String partitionKey;

        SystemProperties() {
            super();

            partitionKey = null;

        }

        SystemProperties(final Map<String, Object> map) {
            super(map);
            this.partitionKey = removeSystemProperty(PARTITION_KEY_ANNOTATION_NAME.getValue());


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

