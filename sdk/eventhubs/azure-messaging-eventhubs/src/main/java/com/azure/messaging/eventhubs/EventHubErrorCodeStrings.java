// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

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
     * The value of {@code batch} key is null
     */
    static final String BATCH_CANNOT_NULL = "null_batch";

    /**
     * The value of {@code client} key is null
     */
    static final String CLIENT_CANNOT_NULL = "null_client";

    /**
     * The value of {@code connectionOptions} key is null
     */
    static final String CONNECTION_OPTIONS_CANNOT_NULL = "null_connection_options";

    /**
     * The value of {@code connectionString} key is null
     */
    static final String CONNECTION_STRING_CANNOT_NULL = "null_connection_string";

    /**
     * The value of {@code consumer} key is null
     */
    static final String CONSUMER_CANNOT_NULL = "null_consumer";

    /**
     * The value of {@code consumerGroup} key is null
     */
    static final String CONSUMER_GROUP_CANNOT_NULL = "null_consumer_group";

    /**
     * The value of {@code credential} key is null
     */
    static final String CREDENTIAL_CANNOT_NULL = "null_credential";

    /**
     * The value of {@code event} key is null
     */
    static final String EVENT_CANNOT_NULL = "null_event";

    /**
     * The value of {@code eventHubAsyncClient} key is null
     */
    static final String EVENTHUB_ASYNC_CLIENT_CANNOT_NULL = "null_eventhub_async_client";

    /**
     * The value of {@code events} key is null
     */
    static final String EVENTS_CANNOT_NULL = "null_events";

    /**
     * The value of {@code eventData} key is null
     */
    static final String EVENT_DATA_CANNOT_NULL = "null_event_data";

    /**
     * The value of {@code eventPosition} key is null
     */
    static final String EVENT_POSITION_CANNOT_NULL = "null_event_position";

    /**
     * The value of {@code eventHubName} key is null
     */
    static final String EVENTHUB_NAME_CANNOT_NULL = "null_eventhub_name";

    /**
     * The value of {@code host} key is null
     */
    static final String HOST_CANNOT_NULL = "null_host";

    /**
     * The value of {@code initialEventPosition} key is null
     */
    static final String INITIAL_EVENT_POSITION_CANNOT_NULL = "null_initial_event_position";

    /**
     * The value of {@code message} key is null
     */
    static final String MESSAGE_CANNOT_NULL = "null_message";

    /**
     * The value of {@code options} key is null
     */
    static final String OPTIONS_CANNOT_NULL = "null_options";

    /**
     * The value of {@code partitionId} key is null
     */
    static final String PARTITION_ID_CANNOT_NULL = "null_partition_id";

    /**
     * The value of {@code partitionProcessorFactory} key is null
     */
    static final String PARTITION_PROCESSOR_FACTORY_CANNOT_NULL = "null_partition_processor_factory";

    /**
     * The value of {@code policyName} key is null
     */
    static final String POLICY_NAME_CANNOT_NULL = "null_policy_name";

    /**
     * The value of {@code producer} key is null
     */
    static final String PRODUCER_CANNOT_NULL = "null_producer";

    /**
     * The value of {@code key} key is null
     */
    static final String PROPERTY_KEY_CANNOT_NULL = "null_property_key";

    /**
     * The value of {@code value} key is null
     */
    static final String PROPERTY_VALUE_CANNOT_NULL = "null_property_value";

    /**
     * The value of {@code handlerProvider} key is null
     */
    static final String REACTOR_HANDLER_PROVIDER_CANNOT_NULL = "null_reactor_handler_provider";

    /**
     * The value of {@code provider} key is null
     */
    static final String REACTOR_PROVIDER_CANNOT_NULL = "null_reactor_provider";

    /**
     * The value of {@code sharedAccessKey} key is null
     */
    static final String SHARED_ACCESS_KEY_CANNOT_NULL = "null_shared_access_key";

    /**
     * The value of {@code tokenValidity} key is null
     */
    static final String TOKEN_VALIDITY_CANNOT_NULL = "null_token_validity";

    /**
     * The value of {@code tracerProvider} key is null
     */
    static final String TRACER_PROVIDER_CANNOT_NULL = "null_tracer_provider";

    /**
     * The value of {@code tryTimeout} key is null
     */
    static final String TRY_TIME_OUT_CANNOT_NULL = "null_try_time_out";


    // EMPTY STRINGS
    /**
     * 'connectionString' cannot be an empty string.
     */
    static final String CONNECTION_STRING_CANNOT_EMPTY = "empty_connection_string";

    /**
     * 'consumerGroup' cannot be an empty string.
     */
    static final String CONSUMER_GROUP_CANNOT_EMPTY = "empty_consumer_group";

    /**
     * 'eventHubName' cannot be an empty string.
     */
    static final String EVENTHUB_NAME_CANNOT_EMPTY = "empty_eventhub_name";

    /**
     * 'host' cannot be an empty string.
     */
    static final String HOST_CANNOT_EMPTY = "empty_host";

    /**
     * 'partitionId' cannot be an empty string.
     */
    static final String PARTITION_ID_CANNOT_EMPTY = "empty_partition_id";

    /**
     * 'policyName' cannot be an empty string.
     */
    static final String POLICY_NAME_CANNOT_EMPTY = "empty_policy_name";

    /**
     * 'resource' cannot be an empty string.
     */
    static final String RESOURCE_CANNOT_EMPTY = "empty_resource";

    /**
     * 'sharedAccessKey' cannot be an empty string.
     */
    static final String SHARED_ACCESS_KEY_CANNOT_EMPTY = "empty_shared_access_key";


    // Other error messages
    /**
     * Size of the payload exceeded maximum message size
     */
    static final String PAYLOAD_EXCEEDED_MAX_SIZE = "payload_exceeded_max_size";

    /**
     * Property is not a recognized reserved property name
     */
    static final String UNRESERVED_PROPERTY_NAME = "unreserved_property_name";

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
            try (InputStream fileInputStream = EventHubErrorCodeStrings.class.getClassLoader().getResource((ERROR_STRINGS_FILE_NAME)).openStream()) {
                errorStrings = new Properties();
                errorStrings.load(fileInputStream);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
