// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The EventHubs common error code Strings
 */
public class EventHubErrorCodeStrings {
    static final String ERROR_STRINGS_FILE_NAME = "eventhubsErrorStrings.properties";
    private static Properties errorStrings;

    // Package Private
    // Cannot be NULL
    static final String BATCH_CANNOT_NULL = "null_batch";
    static final String CLIENT_CANNOT_NULL = "null_client";
    static final String CONNECTION_OPTIONS_CANNOT_NULL = "null_connection_options";
    static final String CONSUMER_CANNOT_NULL = "null_consumer";
    static final String CONSUMER_GROUP_CANNOT_NULL = "null_consumer_group";
    static final String CREDENTIAL_CANNOT_NULL = "null_credential";
    static final String EVENT_CANNOT_NULL = "null_event";
    static final String EVENTHUB_ASYNC_CLIENT_CANNOT_NULL = "null_eventhub_async_client";
    static final String EVENTS_CANNOT_NULL = "null_events";
    static final String EVENT_DATA_CANNOT_NULL = "null_event_data";
    static final String EVENT_POSITION_CANNOT_NULL = "null_event_position";
    static final String HOST_CANNOT_NULL = "null_host";
    static final String INITIAL_EVENT_POSITION_CANNOT_NULL = "null_initial_event_position";
    static final String OPTIONS_CANNOT_NULL = "null_options";
    static final String PARTITION_MANAGER_CANNOT_NULL = "null_partition_manager";
    static final String PARTITION_PROCESSOR_FACTORY_CANNOT_NULL = "null_partition_processor_factory";
    static final String POLICY_NAME_CANNOT_NULL = "null_policy_name";
    static final String PRODUCER_CANNOT_NULL = "null_producer";
    static final String PROPERTY_KEY_CANNOT_NULL = "null_property_key";
    static final String PROPERTY_VALUE_CANNOT_NULL = "null_property_value";
    static final String REACTOR_HANDLER_PROVIDER_CANNOT_NULL = "null_reactor_handler_provider";
    static final String REACTOR_PROVIDER_CANNOT_NULL = "null_reactor_provider";
    static final String SHARED_ACCESS_KEY_CANNOT_NULL = "null_shared_access_key";
    static final String TOKEN_VALIDITY_CANNOT_NULL = "null_token_validity";
    static final String TRACER_PROVIDER_CANNOT_NULL = "null_tracer_provider";
    static final String TRY_TIME_OUT_CANNOT_NULL = "null_try_time_out";

    // Cannot be empty
    static final String CONSUMER_GROUP_CANNOT_EMPTY = "empty_consumer_group";
    static final String EVENTHUB_NAME_CANNOT_EMPTY = "empty_eventhub_name";
    static final String HOST_CANNOT_EMPTY = "empty_host";
    static final String PARTITION_ID_CANNOT_EMPTY = "empty_partition_id";
    static final String POLICY_NAME_CANNOT_EMPTY = "empty_policy_name";
    static final String RESOURCE_CANNOT_EMPTY = "empty_resource";
    static final String SHARED_ACCESS_KEY_CANNOT_EMPTY = "empty_shared_access_key";

    // Other error messages
    /**
     * The value of {@code BatchOptions.maximumSizeInBytes} is larger than the link size in bytes
     */
    static final String BATCH_OPTIONS_LARGER_THAN_LINK_SIZE = "batch_options_larger_than_link_size";

    /**
     * Could not create the {@code EventHubSharedAccessKeyCredential}
     */
    static final String CANNOT_CREATE_EVENTHUB_SAS_KEY_CREDENTIAL = "cannot_create_eventhub_sas_key_credential";

    /**
     * Cannot use a proxy when transport type is not AMQP
     */
    static final String CANNOT_USE_PROXY_FOR_AMQP_TRANSPORT_TYPE = "cannot_use_proxy_for_amqp_transport_type";

    /**
     * {@code EventData} does not fit into maximum number of batches
     */
    static final String EVENT_DATA_EXCEEDS_MAX_NUM_BATCHES = "event_data_exceeds_max_num_batches";

    /**
     * HTTP_PROXY cannot be parsed into a proxy
     */
    static final String HTTP_PROXY_CANNOT_PARSED_TO_PROXY = "http_proxy_cannot_parsed_to_proxy";

    /**
     * No starting position was set
     */
    static final String NO_STARTING_POSITION_SET = "no_starting_position_set";

    /**
     * {@code PartitionKey} exceeds the maximum allowed length
     */
    static final String PARTITION_KEY_EXCEEDS_MAX_LENGTH = "partition_key_exceeds_max_length";

    /**
     * The name of the resource or token audience to obtain a token for,
     * 'scopes' should only contain a single argument that is the token audience or resource name.
     */
    static final String SCOPES_RULES = "scopes_rules";
    /**
     * {@code tokenTimeToLive} has to positive and in the order-of seconds.
     */
    static final String TOKEN_TIME_TO_LIVE_ERROR_MSG = "token_time_to_live_error_msg";

    /**
     * AMQP Exception: Unable to close connection to service
     */
    static final String UNABLE_CLOSE_CONNECTION_TO_SERVICE = "unable_close_connection_to_service";

    // Public
    // Null Values
    /**
     * The value of {@code authentication} key is null
     */
    public static final String AUTHENTICATION_CANNOT_NULL = "null_authentication";

    /**
     * The value of {@code connectionString} key is null
     */
    public static final String CONNECTION_STRING_CANNOT_NULL = "null_connection_string";

    /**
     * The value of {@code consumerGroupName} key is null
     */
    public static final String CONSUMER_GROUP_NAME_CANNOT_NULL = "null_consumer_group_name";

    /**
     * The value of {@code eventHubName} key is null
     */
    public static final String EVENTHUB_NAME_CANNOT_NULL = "null_eventhub_name";

    /**
     * The value of {@code message} key is null
     */
    public static final String MESSAGE_CANNOT_NULL = "null_message";

    /**
     * The value of {@code ownerId} key is null
     */
    public static final String OWNER_ID_CANNOT_NULL = "null_owner_id";

    /**
     * The value of {@code partitionId} key is null
     */
    public static final String PARTITION_ID_CANNOT_NULL = "null_partition_id";

    // Empty strings
    /**
     * 'connectionString' cannot be an empty string.
     */
    public static final String CONNECTION_STRING_CANNOT_EMPTY = "empty_connection_string";

    // Value of property should be null
    /**
     * The message.getMessageId() should be null
     */
    public static final String MESSAGE_ID_SHOULD_BE_NULL = "message_id_should_be_null";

    /**
     * The message.getReplyTo() should be null
     */
    public static final String MESSAGE_REPLY_TO_SHOULD_BE_NULL = "message_reply_to_should_be_null";

    // Other exception error messages
    /**
     * Cannot authorize with CBS node when this token manager has been disposed of.
     */
    public static final String CANNOT_AUTHORIZE_CBS = "cannot_authorize_cbs";

    /**
     * Connection string has invalid key value pair
     */
    public static final String CONNECTION_STRING_HAS_INVALID_KV_PAIR = "connection_string_has_invalid_kv_pair";

    /**
     * Unsupported encoding type
     */
    public static final String ENCODING_TYPE_NOT_SUPPORTED = "encoding_type_not_supported";

    /**
     * AMQP Exception: Entity send operation failed
     */
    public static final String ENTITY_SEND_FAILED = "entity_send_failed";

    /**
     * AMQP Exception: Entity send operation failed while advancing delivery
     */
    public static final String ENTITY_SEND_FAILED_DELIVERY = "entity_send_failed_delivery";

    /**
     * AMQP Exception: Entity send operation failed while scheduling a retry on Reactor
     */
    public static final String ENTITY_SEND_FAILED_SCHEDULE_RETRY = "entity_send_failed_schedule_retry";

    /**
     * AMQP Exception: Entity Send operation timed out
     */
    public static final String ENTITY_SEND_TIMEOUT = "entity_send_timeout";

    /**
     * Illegal connection string parameter name
     */
    public static final String ILLEGAL_CONNECTION_STRING_PARAMS = "illegal_connection_string_params";

    /**
     * The scheme property that comprise a connection string from the Azure portal is incorrect
     */
    public static final String INCORRECT_SCHEME_ENDPOINT = "incorrect_scheme_endpoint";

    /**
     * URISyntaxException: invalid URL endpoint
     */
    public static final String INVALID_ENDPOINT_MSG = "invalid_endpoint_msg";

    /**
     * The max frame size of qpid proton reactor must be a positive number
     */
    public static final String MAX_FRAME_SIZE_REQUIRE_POSITIVE_NUM = "max_frame_size_require_positive_num";

    /**
     * The body of Message is not type of AmqpValue
     */
    public static final String MESSAGE_BODY_EXPECT_AMQP_VALUE = "message_body_expect_amqp_value";

    /**
     * The body of AmqpValue is not type of Map
     */
    public static final String MESSAGE_BODY_VALUE_EXPECT_MAP_TYPE = "message_body_value_expect_map_type";

    /**
     * Timeout should be non-negative
     */
    public static final String NON_NEGATIVE_TIMEOUT = "non_negative_timeout";

    /**
     * Size of the payload exceeded maximum message size
     */
    public static final String PAYLOAD_EXCEEDED_MAX_SIZE = "payload_exceeded_max_size";

    /**
     * ReactorDispatcher instance is closed
     */
    public static final String REACTOR_DISPATCHER_CLOSED = "reactor_dispatcher_closed";

    /**
     * Scheduling reactor failed because the executor has been shut down.
     */
    public static final String REACTOR_FAILED_EXECUTOR_DOWN = "reactor_failed_executor_down";

    /**
     * Unsupported transport type
     */
    public static final String TRANSPORT_TYPE_NOT_SUPPORTED = "transport_type_not_supported";

    /**
     * Unsupported authorization type for token audience
     */
    public static final String UNSUPPORTED_AUTHORIZATION_TYPE = "unsupported_authorization_type";

    /**
     * Property is not a recognized reserved property name
     */
    public static final String UNRESERVED_PROPERTY_NAME = "unreserved_property_name";

    /**
     *  Gets the error String for the specified property.
     *
     * @param propertyName the property name for which error string is required.
     * @return The {@link String value} containing the error message.
     */
    public static String getErrorString(String propertyName) {
        loadProperties();
        return errorStrings.getProperty(propertyName);
    }

    private static synchronized void loadProperties() {
        if (errorStrings == null) {
            try (InputStream fileInputStream = EventHubErrorCodeStrings.class.getClassLoader().getResource((ERROR_STRINGS_FILE_NAME)).openStream()) {
                errorStrings = new Properties();
                errorStrings.load(fileInputStream);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
