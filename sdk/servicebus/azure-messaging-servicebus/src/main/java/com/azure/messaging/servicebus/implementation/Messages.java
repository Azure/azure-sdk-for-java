// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.util.logging.ClientLogger;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Define various messages for different error conditions.
 */
public class Messages {

    private static Properties properties;

    public static final String MESSAGES_PROPERTIES_PATH = "azure-messaging-servicebus.properties";
    private static final ClientLogger LOGGER = initializeLogger();

    public static final String CLASS_NOT_A_SUPPORTED_TYPE = getMessage("CLASS_NOT_A_SUPPORTED_TYPE");
    public static final String INVALID_OPERATION_DISPOSED_RECEIVER = getMessage("INVALID_OPERATION_DISPOSED_RECEIVER");
    public static final String INVALID_OPERATION_DISPOSED_SENDER = getMessage("INVALID_OPERATION_DISPOSED_SENDER");
    public static final String INVALID_OPERATION_DISPOSED_RULE_MANAGER = getMessage("INVALID_OPERATION_DISPOSED_RULE_MANAGER");
    public static final String MESSAGE_NOT_OF_TYPE = getMessage("MESSAGE_NOT_OF_TYPE");

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
                LOGGER.error("Message properties not found"); //NON-NLS
            }
        } catch (IOException exception) {
            LOGGER.error("Error loading message properties", exception); //NON-NLS
        }
        return properties;
    }

    private static ClientLogger initializeLogger() {
        Map<String,  Object> loggingContext = new HashMap<>(1);
        loggingContext.put("propertiesPath", MESSAGES_PROPERTIES_PATH);
        return new ClientLogger(Messages.class, loggingContext);
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
