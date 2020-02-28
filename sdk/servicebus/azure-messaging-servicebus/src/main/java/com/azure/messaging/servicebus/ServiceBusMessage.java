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

import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.PARTITION_KEY_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.PUBLISHER_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * The data structure encapsulating the message being sent-to and received-from Service Bus.
 * Each Service Bus entity can be visualized as a stream of {@link ServiceBusMessage}.
 *
 * <p>
 * Here's how AMQP message sections map to {@link ServiceBusMessage}. For reference, the specification
 * can be found here:
 * <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-complete-v1.0-os.pdf">AMQP 1.0 specification</a>
 *
 * <ol>
 * <li>{@link #getProperties()} - AMQPMessage.ApplicationProperties section</li>
 * <li>{@link #getBody()} - if AMQPMessage.Body has Data section</li>
 * </ol>
 *
 * <p>
 * Serializing a received {@link ServiceBusMessage} with AMQP sections other than ApplicationProperties
 * (with primitive Java types) and Data section is not supported.
 * </p>
 *
 * @see MessageBatch
 */
public class ServiceBusMessage {
    /*
     * These are properties owned by the service and set when a message is received.
     */
    static final Set<String> RESERVED_SYSTEM_PROPERTIES;
    private static final Charset DEFAULT_CHAR_SET = Charset.forName("UTF-8");
    private final Map<String, Object> properties;
    private final byte[] body;
    private final SystemProperties systemProperties;
    private String messageId;
    private Context context;
    private String contentType;
    private String sessionId;


    static {
        final Set<String> properties = new HashSet<>();
        properties.add(SEQUENCE_NUMBER_ANNOTATION_NAME.getValue());
        properties.add(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue());
        properties.add(PUBLISHER_ANNOTATION_NAME.getValue());
        RESERVED_SYSTEM_PROPERTIES = Collections.unmodifiableSet(properties);
    }

    /**
     * Creates a {@link ServiceBusMessage} containing the {@code body}.
     *
     * @param body The data to set for this {@link ServiceBusMessage}.
     * @throws NullPointerException if {@code body} is {@code null}.
     */
    public ServiceBusMessage(byte[] body) {
        this.body = Objects.requireNonNull(body, "'body' cannot be null.");
        this.context = Context.NONE;
        this.properties = new HashMap<>();
        this.systemProperties = new SystemProperties();
    }

    /**
     * Creates a {@link ServiceBusMessage} containing the {@code body}.
     *
     * @param body The data to set for this {@link ServiceBusMessage}.
     * @throws NullPointerException if {@code body} is {@code null}.
     */
    public ServiceBusMessage(ByteBuffer body) {
        this(body.array());
    }

    /**
     * Creates a {@link ServiceBusMessage} by encoding the {@code body} using UTF-8 charset.
     *
     * @param body The string that will be UTF-8 encoded to create a {@link ServiceBusMessage}.
     * @throws NullPointerException if {@code body} is {@code null}.
     */
    public ServiceBusMessage(String body) {
        this(Objects.requireNonNull(body, "'body' cannot be null.").getBytes(UTF_8));
    }

    /**
     * Creates a message from a string. For backward compatibility reasons, the string is converted to a byte array
     * and message body type is set to binary.
     * @param body body of the message
     * @param contentType body type of the message
     */
    public ServiceBusMessage(String body, String contentType) {
        this(body.getBytes(DEFAULT_CHAR_SET), contentType);
    }

    /**
     * Creates a message from a byte array. Message body type is set to binary.
     * @param body body of the message
     * @param contentType content type of the message
     */
    public ServiceBusMessage(byte[] body, String contentType) {
        this(body);
        this.contentType = contentType;
    }

    /**
     * Creates a message from a string. For backward compatibility reasons, the string is converted to a byte array.
     *
     * @param messageId id of the {@link ServiceBusMessage}.
     * @param body body of the {@link ServiceBusMessage}.
     * @param contentType content type of the {@link ServiceBusMessage}.
     */
    public ServiceBusMessage(String messageId, String body, String contentType) {
        this(messageId, body.getBytes(DEFAULT_CHAR_SET), contentType);
    }

    /**
     * Creates a {@link ServiceBusMessage} from a byte array.
     *
     * @param messageId id of the {@link ServiceBusMessage}.
     * @param body body of the {@link ServiceBusMessage}.
     * @param contentType content type of the {@link ServiceBusMessage}.
     */
    public ServiceBusMessage(String messageId, byte[] body, String contentType) {
        this(body, contentType);
        this.messageId = messageId;
    }

    /**
     * Creates a {@link ServiceBusMessage} with the given {@code body}, system properties and context.
     *
     * @param body The data to set for this {@link ServiceBusMessage}.
     * @param systemProperties System properties set by message broker for this {@link ServiceBusMessage}.
     * @param context A specified key-value pair of type {@link Context}.
     * @throws NullPointerException if {@code body}, {@code systemProperties}, or {@code context} is {@code null}.
     */
    public ServiceBusMessage(byte[] body, Map<String, Object> systemProperties, Context context) {
        this.body = Objects.requireNonNull(body, "'body' cannot be null.");
        this.context = Objects.requireNonNull(context, "'context' cannot be null.");
        this.systemProperties = new SystemProperties(Objects.requireNonNull(systemProperties,
            "'systemProperties' cannot be null."));
        this.properties = new HashMap<>();
    }

    /**
     * Creates a {@link ServiceBusMessage} with the given {@code body}, system properties and context.
     *
     * @param body The data to set for this {@link ServiceBusMessage}.
     * @param systemProperties System properties set by message broker for this {@link ServiceBusMessage}.
     * @param context A specified key-value pair of type {@link Context}.
     * @param sessionId The sesson id assigned to the message.
     * @throws NullPointerException if {@code body}, {@code systemProperties}, {@code sessionId} or {@code context}
     * is {@code null}.
     */
    public ServiceBusMessage(byte[] body, Map<String, Object> systemProperties, Context context, String sessionId) {
        this.body = Objects.requireNonNull(body, "'body' cannot be null.");
        this.sessionId = Objects.requireNonNull(sessionId, "'sessonId' cannot be null.");
        this.context = Objects.requireNonNull(context, "'context' cannot be null.");
        this.systemProperties = new SystemProperties(Objects.requireNonNull(systemProperties,
            "'systemProperties' cannot be null."));
        this.properties = new HashMap<>();
    }
    /**
     * Gets the set of free-form {@link ServiceBusMessage} properties which may be used for passing metadata
     * associated with the {@link ServiceBusMessage}  during Service Bus operations. A common use-case for
     * {@code properties()} is to associate serialization hints for the {@link #getBody()} as an aid to consumers
     * who wish to deserialize the binary data.
     *
     * <p><strong>Adding serialization hint using {@code getProperties()}</strong></p>
     * <p>In the sample, the type of telemetry is indicated by adding an application property
     * with key "messageType".</p>
     *
     * @return Application properties associated with this {@link ServiceBusMessage}.
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Properties that are populated by Service Bus. As these are populated by the Service Bus, they are
     * only present on a <b>received</b> {@link ServiceBusMessage}.
     *
     * @return An encapsulation of all system properties appended by Service Bus into {@link ServiceBusMessage}.
     * {@code null} if the {@link ServiceBusMessage} is not received from the Service Bus service.
     */
    public Map<String, Object> getSystemProperties() {
        return systemProperties;
    }

    /**
     *
     * @return Id of the {@link ServiceBusMessage}.
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Sets the message id.
     * @param messageId to be set.
     * @return The updted {@link ServiceBusMessage}.
     */
    public ServiceBusMessage setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    /**
     * Sets the session id.
     * @param sessionId to be set.
     * @return The updted {@link ServiceBusMessage}.
     */
    public ServiceBusMessage setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }


    /**
     *
     * @return Session Id of the {@link ServiceBusMessage}.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     *
     * @return the contentType of the {@link ServiceBusMessage}.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the content type of the {@link ServiceBusMessage}.
     * @param contentType of the message.
     *
     * @return The updted {@link ServiceBusMessage}.
     */
    public ServiceBusMessage setContentType(String contentType) {
        this.contentType = contentType;
        return this;
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
     * A specified key-value pair of type {@link Context} to set additional information
     * on the {@link ServiceBusMessage}.
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
     * @throws NullPointerException if {@code key} or {@code value} is null.
     * @return The updated {@link ServiceBusMessage}.
     */
    public ServiceBusMessage addContext(String key, Object value) {
        Objects.requireNonNull(key, "The 'key' parameter cannot be null.");
        Objects.requireNonNull(value, "The 'value' parameter cannot be null.");
        this.context = context.addData(key, value);

        return this;
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
