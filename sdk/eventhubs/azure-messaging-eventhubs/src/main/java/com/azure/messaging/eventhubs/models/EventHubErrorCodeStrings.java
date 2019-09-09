// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

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
