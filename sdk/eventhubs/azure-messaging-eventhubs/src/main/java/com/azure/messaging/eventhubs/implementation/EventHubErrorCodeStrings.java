// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The EventHubs common error code Strings
 */
class EventHubErrorCodeStrings {
    private static final String ERROR_STRINGS_FILE_NAME = "eventhubsErrorStrings.properties";
    private static Properties errorStrings;

    /**
     * The property cannot be null.
     */
    static final String CANNOT_BE_NULL = "cannot_be_null_error_message";

    /**
     * The property cannot be empty.
     */
    static final String CANNOT_BE_EMPTY = "cannot_be_empty_error_message";

    /**
     * Cannot authorize with CBS node when this token manager has been disposed of.
     */
    static final String CANNOT_AUTHORIZE_CBS = "cannot_authorize_cbs";

    /**
     * Connection string has invalid key value pair
     */
    static final String CONNECTION_STRING_HAS_INVALID_KV_PAIR = "connection_string_has_invalid_kv_pair";

    /**
     * Unsupported encoding type
     */
    static final String ENCODING_TYPE_NOT_SUPPORTED = "encoding_type_not_supported";

    /**
     * AMQP Exception: Entity send operation failed
     */
    static final String ENTITY_SEND_FAILED = "entity_send_failed";

    /**
     * AMQP Exception: Entity send operation failed while advancing delivery
     */
    static final String ENTITY_SEND_FAILED_DELIVERY = "entity_send_failed_delivery";

    /**
     * AMQP Exception: Entity send operation failed while scheduling a retry on Reactor
     */
    static final String ENTITY_SEND_FAILED_SCHEDULE_RETRY = "entity_send_failed_schedule_retry";

    /**
     * AMQP Exception: Entity Send operation timed out
     */
    static final String ENTITY_SEND_TIMEOUT = "entity_send_timeout";

    /**
     * Illegal connection string parameter name
     */
    static final String ILLEGAL_CONNECTION_STRING_PARAMS = "illegal_connection_string_params";

    /**
     * The scheme property that comprise a connection string from the Azure portal is incorrect
     */
    static final String INCORRECT_SCHEME_ENDPOINT = "incorrect_scheme_endpoint";

    /**
     * URISyntaxException: invalid URL endpoint
     */
    static final String INVALID_ENDPOINT_MSG = "invalid_endpoint_msg";

    /**
     * The max frame size of qpid proton reactor must be a positive number
     */
    static final String MAX_FRAME_SIZE_REQUIRE_POSITIVE_NUM = "max_frame_size_require_positive_num";

    /**
     * The body of Message is not type of AmqpValue
     */
    static final String MESSAGE_BODY_EXPECT_AMQP_VALUE = "message_body_expect_amqp_value";

    /**
     * The body of AmqpValue is not type of Map
     */
    static final String MESSAGE_BODY_VALUE_EXPECT_MAP_TYPE = "message_body_value_expect_map_type";

    /**
     * The message.getMessageId() should be null
     */
    static final String MESSAGE_ID_SHOULD_BE_NULL = "message_id_should_be_null";

    /**
     * The message.getReplyTo() should be null
     */
    static final String MESSAGE_REPLY_TO_SHOULD_BE_NULL = "message_reply_to_should_be_null";

    /**
     * Timeout should be non-negative
     */
    static final String NON_NEGATIVE_TIMEOUT = "non_negative_timeout";

    /**
     * Size of the payload exceeded maximum message size
     */
    static final String PAYLOAD_EXCEEDED_MAX_SIZE = "payload_exceeded_max_size";

    /**
     * ReactorDispatcher instance is closed
     */
    static final String REACTOR_DISPATCHER_CLOSED = "reactor_dispatcher_closed";

    /**
     * Scheduling reactor failed because the executor has been shut down.
     */
    static final String REACTOR_FAILED_EXECUTOR_DOWN = "reactor_failed_executor_down";

    /**
     * Unsupported transport type
     */
    static final String TRANSPORT_TYPE_NOT_SUPPORTED = "transport_type_not_supported";

    /**
     * Unsupported authorization type for token audience
     */
    static final String UNSUPPORTED_AUTHORIZATION_TYPE = "unsupported_authorization_type";

    /**
     * Property is not a recognized reserved property name
     */
    static final String UNRESERVED_PROPERTY_NAME = "unreserved_property_name";

    /**
     *  Gets the error String for the specified property.
     *
     * @param propertyName the property name for which error string is required.
     * @return The {@link String value} containing the error message.
     */
    static String getErrorString(String propertyName) {
        loadProperties();
        return errorStrings.getProperty(propertyName);
    }

    private static synchronized void loadProperties() {
        if (errorStrings == null) {
            try (InputStream fileInputStream = EventHubErrorCodeStrings.class.getClassLoader().getResource(ERROR_STRINGS_FILE_NAME).openStream()) {
                errorStrings = new Properties();
                errorStrings.load(fileInputStream);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
