// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.blob;

import com.azure.core.util.logging.ClientLogger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * I18n messages loaded from the messages.properties file located within the same package.
 */
public enum Messages {
    ;
    private static final ClientLogger LOGGER = new ClientLogger(Messages.class);
    private static Properties properties;
    private static final String PATH = "com/azure/messaging/eventhubs/checkpointstore/blob/messages.properties";
    public static final String NO_METADATA_AVAILABLE_FOR_BLOB = "No metadata available for blob {}";
    public static final String CLAIM_ERROR = "Couldn't claim ownership of partition {}, error {}";
    public static final String FOUND_BLOB_FOR_PARTITION = "Found blob for partition {}";

    private static synchronized Properties getProperties() {
        if (properties != null) {
            return properties;
        }
        properties = new Properties();
        try (InputStream inputStream =
                 Thread.currentThread().getContextClassLoader().getResourceAsStream(PATH)) {
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                LOGGER.error("Message properties [{}] not found", PATH); //NON-NLS
            }
        } catch (IOException exception) {
            LOGGER.error("Error loading message properties [{}]", PATH, exception); //NON-NLS
        }
        return properties;
    }

    /**
     * @param key the key of the message to retrieve
     * @return the message matching the given key
     */
    public static String getMessage(String key) {
        return String.valueOf(getProperties().getOrDefault(key, key));
    }

}

