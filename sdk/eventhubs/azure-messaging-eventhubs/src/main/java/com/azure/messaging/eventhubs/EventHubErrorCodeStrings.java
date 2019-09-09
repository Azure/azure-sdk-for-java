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
     * The property cannot be null.
     */
    static final String CANNOT_BE_NULL = "cannot_be_null_error_message";

    /**
     * The property cannot be empty.
     */
    static final String CANNOT_BE_EMPTY = "cannot_be_empty_error_message";

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
            try (InputStream fileInputStream = EventHubErrorCodeStrings.class.getClassLoader().getResource(ERROR_STRINGS_FILE_NAME).openStream()) {
                errorStrings = new Properties();
                errorStrings.load(fileInputStream);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
