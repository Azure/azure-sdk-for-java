// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.implementation.util.ImplUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/**
 *  The set of properties that comprise a connection string from the Azure portal.
 */
public class ConnectionStringProperties {
    private static final String TOKEN_VALUE_SEPARATOR = "=";
    private static final String TOKEN_VALUE_PAIR_DELIMITER = ";";
    private static final String ENDPOINT = "Endpoint";
    private static final String SHARED_ACCESS_KEY_NAME = "SharedAccessKeyName";
    private static final String SHARED_ACCESS_KEY = "SharedAccessKey";
    private static final String ENTITY_PATH = "EntityPath";
    private static final String ERROR_MESSAGE_FORMAT = "Could not parse 'connectionString'. Expected format: "
        + "'Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};"
        + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubPath}'. Actual: %s";

    private final URI endpoint;
    private final String eventHubPath;
    private final String sharedAccessKeyName;
    private final String sharedAccessKey;

    /**
     * Creates a new instance by parsing the {@code connectionString} into its components.
     *
     * @param connectionString The connection string to the Event Hub instance.
     * @throws IllegalArgumentException if {@code connectionString} is {@code null} or empty, the connection string has
     * an invalid format.
     */
    public ConnectionStringProperties(String connectionString) {
        if (ImplUtils.isNullOrEmpty(connectionString)) {
            throw new IllegalArgumentException("'connectionString' cannot be null or empty");
        }

        final String[] tokenValuePairs = connectionString.split(TOKEN_VALUE_PAIR_DELIMITER);
        URI endpoint = null;
        String eventHubPath = null;
        String sharedAccessKeyName = null;
        String sharedAccessKeyValue = null;

        for (String tokenValuePair : tokenValuePairs) {
            final String[] pair = tokenValuePair.split(TOKEN_VALUE_SEPARATOR, 2);
            if (pair.length != 2) {
                throw new IllegalArgumentException(String.format(Locale.US, "Connection string has invalid key value pair: %s", tokenValuePair));
            }

            final String key = pair[0].trim();
            final String value = pair[1].trim();

            if (key.equalsIgnoreCase(ENDPOINT)) {
                try {
                    endpoint = new URI(value);
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException(String.format(Locale.US, "Invalid endpoint: %s", tokenValuePair), e);
                }
            } else if (key.equalsIgnoreCase(SHARED_ACCESS_KEY_NAME)) {
                sharedAccessKeyName = value;
            } else if (key.equalsIgnoreCase(SHARED_ACCESS_KEY)) {
                sharedAccessKeyValue = value;
            } else if (key.equalsIgnoreCase(ENTITY_PATH)) {
                eventHubPath = value;
            }
        }

        if (endpoint == null || sharedAccessKeyName == null || sharedAccessKeyValue == null || eventHubPath == null) {
            throw new IllegalArgumentException(String.format(Locale.US, ERROR_MESSAGE_FORMAT, connectionString));
        }

        this.endpoint = endpoint;
        this.eventHubPath = eventHubPath;
        this.sharedAccessKeyName = sharedAccessKeyName;
        this.sharedAccessKey = sharedAccessKeyValue;
    }

    /**
     * Gets the endpoint to be used for connecting to the Event Hubs namespace.
     *
     * @return The endpoint address, including protocol, from the connection string.
     */
    public URI endpoint() {
        return endpoint;
    }

    /**
     * Gets the name of the specific Event Hub under the namespace.
     *
     * @return The name of the specific Event Hub under the namespace.
     */
    public String eventHubPath() {
        return eventHubPath;
    }

    /**
     * Gets the name of the shared access key, either for the Event Hubs namespace or the Event Hub instance.
     *
     * @return The name of the shared access key.
     */
    public String sharedAccessKeyName() {
        return sharedAccessKeyName;
    }

    /**
     * The value of the shared access key, either for the Event Hubs namespace or the Event Hub.
     *
     * @return The value of the shared access key.
     */
    public String sharedAccessKey() {
        return sharedAccessKey;
    }
}
