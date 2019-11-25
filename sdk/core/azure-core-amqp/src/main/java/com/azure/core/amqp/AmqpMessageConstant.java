// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Reserved well-known constants from AMQP protocol.
 *
 * @see <a href="https://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-messaging-v1.0-os.html#type-properties">
 * AMQP 1.0: Messaging Properties</a>
 */
public enum AmqpMessageConstant {
    /**
     * Message-id, if set, uniquely identifies a message within the message system. The message producer is usually
     * responsible for setting the message-id in such a way that it is assured to be globally unique. A broker MAY
     * discard a message as a duplicate if the value of the message-id matches that of a previously received message
     * sent to the same node.
     */
    MESSAGE_ID("message-id"),
    /**
     * The identity of the user responsible for producing the message. The client sets this value, and it MAY be
     * authenticated by intermediaries.
     */
    USER_ID("user-id"),
    /**
     * The to field identifies the node that is the intended destination of the message. On any given transfer this
     * might not be the node at the receiving end of the link.
     */
    TO("to"),
    /**
     * A common field for summary information about the message content and purpose.
     */
    SUBJECT("subject"),
    /**
     * The address of the node to send replies to.
     */
    REPLY_TO("reply-to"),
    /**
     * This is a client-specific id that can be used to mark or identify messages between clients.
     */
    CORRELATION_ID("correlation-id"),
    /**
     * The RFC-2046 MIME type for the message's application-data section (body). As per RFC-2046 this can contain a
     * charset parameter defining the character encoding used: e.g., 'text/plain), charset="utf-8"'.
     */
    CONTENT_TYPE("content-type"),
    /**
     * The content-encoding property is used as a modifier to the content-type. When present, its value indicates what
     * additional content encodings have been applied to the application-data, and thus what decoding mechanisms need to
     * be applied in order to obtain the media-type referenced by the content-type header field.
     */
    CONTENT_ENCODING("content-encoding"),
    /**
     * An absolute time when this message is considered to be expired.
     */
    ABSOLUTE_EXPIRY_TIME("absolute-expiry-time"),
    /**
     * An absolute time when this message was created.
     */
    CREATION_TIME("creation-time"),
    /**
     * Identifies the group the message belongs to.
     */
    GROUP_ID("group-id"),
    /**
     * The relative position of this message within its group.
     */
    GROUP_SEQUENCE("group-sequence"),
    /**
     * This is a client-specific id that is used so that client can send replies to this message to a specific group.
     */
    REPLY_TO_GROUP_ID("reply-to-group-id"),
    /**
     * The offset of a message within a given partition.
     */
    OFFSET_ANNOTATION_NAME("x-opt-offset"),
    /**
     * The date and time, in UTC, that a message was enqueued.
     */
    ENQUEUED_TIME_UTC_ANNOTATION_NAME("x-opt-enqueued-time"),
    /**
     * The identifier associated with a given partition.
     */
    PARTITION_KEY_ANNOTATION_NAME("x-opt-partition-key"),
    /**
     * The sequence number assigned to a message.
     */
    SEQUENCE_NUMBER_ANNOTATION_NAME("x-opt-sequence-number"),
    /**
     * The name of the entity that published a message.
     */
    PUBLISHER_ANNOTATION_NAME("x-opt-publisher");

    private static final Map<String, AmqpMessageConstant> RESERVED_CONSTANTS_MAP = new HashMap<>();
    private final String constant;

    static {
        for (AmqpMessageConstant error : AmqpMessageConstant.values()) {
            RESERVED_CONSTANTS_MAP.put(error.getValue(), error);
        }
    }

    AmqpMessageConstant(String value) {
        this.constant = value;
    }

    /**
     * Gets the AMQP messaging header value.
     *
     * @return The AMQP header value for this messaging constant.
     */
    public String getValue() {
        return constant;
    }

    /**
     * Parses an header value to its message constant.
     *
     * @param value the messaging header value to parse.
     * @return the parsed MessageConstant object, or {@code null} if unable to parse.
     * @throws NullPointerException if {@code constant} is {@code null}.
     */
    public static AmqpMessageConstant fromString(String value) {
        Objects.requireNonNull(value, "'value' cannot be null.");

        return RESERVED_CONSTANTS_MAP.get(value);
    }
}
