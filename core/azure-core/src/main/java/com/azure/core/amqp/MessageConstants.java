// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Reserved well-known constants from AMQP protocol.
 *
 * @see <a href="https://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-messaging-v1.0-os.html#type-properties">
 * AMQP 1.0: Messaging Properties</a>
 */
public class MessageConstants {
    /**
     * Message-id, if set, uniquely identifies a message within the message system. The message producer is usually
     * responsible for setting the message-id in such a way that it is assured to be globally unique. A broker MAY
     * discard a message as a duplicate if the value of the message-id matches that of a previously received message
     * sent to the same node.
     */
    public static final String MESSAGE_ID = "message-id";
    /**
     * The identity of the user responsible for producing the message. The client sets this value, and it MAY be
     * authenticated by intermediaries.
     */
    public static final String USER_ID = "user-id";
    /**
     * The to field identifies the node that is the intended destination of the message. On any given transfer this
     * might not be the node at the receiving end of the link.
     */
    public static final String TO = "to";
    /**
     * A common field for summary information about the message content and purpose.
     */
    public static final String SUBJECT = "subject";
    /**
     * The address of the node to send replies to.
     */
    public static final String REPLY_TO = "reply-to";
    /**
     * This is a client-specific id that can be used to mark or identify messages between clients.
     */
    public static final String CORRELATION_ID = "correlation-id";
    /**
     * The RFC-2046 MIME type for the message's application-data section (body). As per RFC-2046 this can contain a
     * charset parameter defining the character encoding used: e.g., 'text/plain; charset="utf-8"'.
     */
    public static final String CONTENT_TYPE = "content-type";
    /**
     * The content-encoding property is used as a modifier to the content-type. When present, its value indicates what
     * additional content encodings have been applied to the application-data, and thus what decoding mechanisms need to
     * be applied in order to obtain the media-type referenced by the content-type header field.
     */
    public static final String CONTENT_ENCODING = "content-encoding";
    /**
     * An absolute time when this message is considered to be expired.
     */
    public static final String ABSOLUTE_EXPRITY_TIME = "absolute-expiry-time";
    /**
     * An absolute time when this message was created.
     */
    public static final String CREATION_TIME = "creation-time";
    /**
     * Identifies the group the message belongs to.
     */
    public static final String GROUP_ID = "group-id";
    /**
     * The relative position of this message within its group.
     */
    public static final String GROUP_SEQUENCE = "group-sequence";
    /**
     * This is a client-specific id that is used so that client can send replies to this message to a specific group.
     */
    public static final String REPLY_TO_GROUP_ID = "reply-to-group-id";

    /**
     * A readonly set of property names that are part of the AMQP protocol.
     */
    @SuppressWarnings("serial")
    public static final Set<String> RESERVED_PROPERTY_NAMES = Collections.unmodifiableSet(new HashSet<String>() {{
            add(MessageConstants.MESSAGE_ID);
            add(USER_ID);
            add(TO);
            add(SUBJECT);
            add(REPLY_TO);
            add(CORRELATION_ID);
            add(CONTENT_TYPE);
            add(CONTENT_ENCODING);
            add(ABSOLUTE_EXPRITY_TIME);
            add(CREATION_TIME);
            add(GROUP_ID);
            add(GROUP_SEQUENCE);
            add(REPLY_TO_GROUP_ID);
        }});
}
