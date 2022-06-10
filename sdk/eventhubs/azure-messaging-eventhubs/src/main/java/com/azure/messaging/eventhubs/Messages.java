// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.CoreUtils;
import java.util.Map;

/**
 * I18n messages loaded from the messages.properties file located within the same package.
 */
public enum Messages {
    ;
    private static final String MESSAGES_PROPERTIES_PATH = "eventhubs-messages.properties";
    private static final Map<String, String> PROPERTIES = CoreUtils.getProperties(MESSAGES_PROPERTIES_PATH);

    /**
     * Class not a supported type message.
     */
    public static final String CLASS_NOT_A_SUPPORTED_TYPE = getMessage("CLASS_NOT_A_SUPPORTED_TYPE");

    /**
     * Encoding type not supported message.
     */
    public static final String ENCODING_TYPE_NOT_SUPPORTED = getMessage("ENCODING_TYPE_NOT_SUPPORTED");

    /**
     * Process span scope type error message.
     */
    public static final String PROCESS_SPAN_SCOPE_TYPE_ERROR = getMessage("PROCESS_SPAN_SCOPE_TYPE_ERROR");

    /**
     * Message not of type message.
     */
    public static final String MESSAGE_NOT_OF_TYPE = getMessage("MESSAGE_NOT_OF_TYPE");

    /**
     * Request value not valid message.
     */
    public static final String REQUEST_VALUE_NOT_VALID = getMessage("REQUEST_VALUE_NOT_VALID");

    /**
     * Event data doesn't fit message.
     */
    public static final String EVENT_DATA_DOES_NOT_FIT = getMessage("EVENT_DATA_DOES_NOT_FIT");

    /**
     * Cannot send empty event batch message.
     */
    public static final String CANNOT_SEND_EVENT_BATCH_EMPTY = getMessage("CANNOT_SEND_EVENT_BATCH_EMPTY");

    /**
     * Error sending batch message.
     */
    public static final String ERROR_SENDING_BATCH = getMessage("ERROR_SENDING_BATCH");

    /**
     * Failed to claim ownership message.
     */
    public static final String FAILED_TO_CLAIM_OWNERSHIP = getMessage("FAILED_TO_CLAIM_OWNERSHIP");

    /**
     * Load balancing failed message.
     */
    public static final String LOAD_BALANCING_FAILED = getMessage("LOAD_BALANCING_FAILED");

    /**
     * Event processor run end message.
     */
    public static final String EVENT_PROCESSOR_RUN_END = getMessage("EVENT_PROCESSOR_RUN_END");

    /**
     * Failed to close consumer partition message.
     */
    public static final String FAILED_CLOSE_CONSUMER_PARTITION = getMessage("FAILED_CLOSE_CONSUMER_PARTITION");

    /**
     * Error occurred in subscriber error message.
     */
    public static final String ERROR_OCCURRED_IN_SUBSCRIBER_ERROR = getMessage("ERROR_OCCURRED_IN_SUBSCRIBER_ERROR");

    /**
     * Exception occurred while emitting message.
     */
    public static final String EXCEPTION_OCCURRED_WHILE_EMITTING = getMessage("EXCEPTION_OCCURRED_WHILE_EMITTING");

    /**
     * @param key the key of the message to retrieve
     * @return the message matching the given key
     */
    public static String getMessage(String key) {
        return PROPERTIES.getOrDefault(key, key);
    }
}
