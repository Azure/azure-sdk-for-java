// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Objects;

/**
 * The set of properties that comprise a connection string from the Azure portal.
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
        + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}'. Actual: %s";

    private final URI endpoint;
    private final String entityPath;
    private final String sharedAccessKeyName;
    private final String sharedAccessKey;

    /**
     * Creates a new instance by parsing the {@code connectionString} into its components.
     *
     * @param connectionString The connection string to the Event Hub instance.
     * @throws NullPointerException if {@code connectionString} is null.
     * @throws IllegalArgumentException if {@code connectionString} is an empty string or the connection string has
     *     an invalid format.
     */
    public ConnectionStringProperties(String connectionString) {
        Objects.requireNonNull(connectionString, "'connectionString' cannot be null.");
        if (connectionString.isEmpty()) {
            throw new IllegalArgumentException("'connectionString' cannot be an empty string.");
        }

        final String[] tokenValuePairs = connectionString.split(TOKEN_VALUE_PAIR_DELIMITER);
        URI endpoint = null;
        String entityPath = null;
        String sharedAccessKeyName = null;
        String sharedAccessKeyValue = null;

        for (String tokenValuePair : tokenValuePairs) {
            final String[] pair = tokenValuePair.split(TOKEN_VALUE_SEPARATOR, 2);
            if (pair.length != 2) {
                throw new IllegalArgumentException(String.format(
                    Locale.US,
                    "Connection string has invalid key value pair: %s",
                    tokenValuePair));
            }

            final String key = pair[0].trim();
            final String value = pair[1].trim();

            if (key.equalsIgnoreCase(ENDPOINT)) {
                try {
                    endpoint = new URI(value);
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException(
                        String.format(Locale.US, "Invalid endpoint: %s", tokenValuePair), e);
                }
            } else if (key.equalsIgnoreCase(SHARED_ACCESS_KEY_NAME)) {
                sharedAccessKeyName = value;
            } else if (key.equalsIgnoreCase(SHARED_ACCESS_KEY)) {
                sharedAccessKeyValue = value;
            } else if (key.equalsIgnoreCase(ENTITY_PATH)) {
                entityPath = value;
            } else {
                throw new IllegalArgumentException(
                    String.format(Locale.US, "Illegal connection string parameter name: %s", key));
            }
        }

        if (endpoint == null || sharedAccessKeyName == null || sharedAccessKeyValue == null) {
            throw new IllegalArgumentException(String.format(Locale.US, ERROR_MESSAGE_FORMAT, connectionString));
        }

        this.endpoint = endpoint;
        this.entityPath = entityPath;
        this.sharedAccessKeyName = sharedAccessKeyName;
        this.sharedAccessKey = sharedAccessKeyValue;
    }

    /**
     * Gets the endpoint to be used for connecting to the AMQP message broker.
     *
     * @return The endpoint address, including protocol, from the connection string.
     */
    public URI getEndpoint() {
        return endpoint;
    }

    /**
     * Gets the entity path to connect to in the message broker.
     *
     * @return The entity path to connect to in the message broker.
     */
    public String getEntityPath() {
        return entityPath;
    }

    /**
     * Gets the name of the shared access key, either for the Event Hubs namespace or the Event Hub instance.
     *
     * @return The name of the shared access key.
     */
    public String getSharedAccessKeyName() {
        return sharedAccessKeyName;
    }

    /**
     * The value of the shared access key, either for the Event Hubs namespace or the Event Hub.
     *
     * @return The value of the shared access key.
     */
    public String getSharedAccessKey() {
        return sharedAccessKey;
    }
}
