// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.util.logging.ClientLogger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Define various messages for different error conditions.
 */
public class Messages {
    private static final ClientLogger LOGGER = new ClientLogger(Messages.class);
    private static Properties properties;

    public static final String MESSAGES_PROPERTIES_PATH = "azure-messaging-servicebus.properties";

    public static final String CLASS_NOT_A_SUPPORTED_TYPE = getMessage("CLASS_NOT_A_SUPPORTED_TYPE");
    public static final String INVALID_OPERATION_DISPOSED_RECEIVER = getMessage("INVALID_OPERATION_DISPOSED_RECEIVER");
    public static final String INVALID_OPERATION_DISPOSED_SENDER = getMessage("INVALID_OPERATION_DISPOSED_SENDER");
    public static final String MESSAGE_NOT_OF_TYPE = getMessage("MESSAGE_NOT_OF_TYPE");
    public static final String REQUEST_VALUE_NOT_VALID = getMessage("REQUEST_VALUE_NOT_VALID");
    public static final String PROCESS_SPAN_SCOPE_TYPE_ERROR = getMessage("PROCESS_SPAN_SCOPE_TYPE_ERROR");
    public static final String MESSAGE_PROCESSOR_RUN_END = getMessage("MESSAGE_PROCESSOR_RUN_END");

    private static synchronized Properties getProperties() {
        if (properties != null) {
            return properties;
        }
        properties = new Properties();
        try (InputStream inputStream =
                 Thread.currentThread().getContextClassLoader().getResourceAsStream(MESSAGES_PROPERTIES_PATH)) {
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                LOGGER.error("Message properties [{}] not found", MESSAGES_PROPERTIES_PATH); //NON-NLS
            }
        } catch (IOException exception) {
            LOGGER.error("Error loading message properties [{}]", MESSAGES_PROPERTIES_PATH, exception); //NON-NLS
        }
        return properties;
    }

    /**
     * Retrieve the message given a key.
     * @param key the key of the message to retrieve.
     * @return the message matching the given key.
     */
    public static String getMessage(String key) {
        return String.valueOf(getProperties().getOrDefault(key, key));
    }
}
