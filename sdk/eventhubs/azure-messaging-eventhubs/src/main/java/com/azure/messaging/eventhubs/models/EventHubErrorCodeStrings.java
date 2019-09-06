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
     * The value of {@code authentication} key is null
     */
    static final String AUTHENTICATION_CANNOT_NULL = "null_authentication";

    /**
     * The value of {@code consumerGroup} key is null
     */
    static final String CONSUMER_GROUP_CANNOT_NULL = "null_consumer_group";

    /**
     * The value of {@code consumerGroupName} key is null
     */
    static final String CONSUMER_GROUP_NAME_CANNOT_NULL = "null_consumer_group_name";

    /**
     * The value of {@code eventHubName} key is null
     */
    static final String EVENTHUB_NAME_CANNOT_NULL = "null_eventhub_name";

    /**
     * The value of {@code ownerId} key is null
     */
    static final String OWNER_ID_CANNOT_NULL = "null_owner_id";

    /**
     * The value of {@code partitionId} key is null
     */
    static final String PARTITION_ID_CANNOT_NULL = "null_partition_id";

    /**
     * The value of {@code partitionManager} key is null
     */
    static final String PARTITION_MANAGER_CANNOT_NULL = "null_partition_manager";

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
