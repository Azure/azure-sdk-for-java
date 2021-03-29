// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common.implementation;

import java.util.Objects;

/**
 * Parse and store Connection String values
 */
public class CommunicationConnectionString {
    private static final String TOKEN_VALUE_SEPARATOR = "=";
    private static final String TOKEN_VALUE_PAIR_DELIMITER = ";";
    private static final String ENDPOINT_NAME = "endpoint";
    private static final String ACCESS_KEY_NAME = "accessKey";
    private final String endpoint;
    private final String accessKey;

    /**
     * Creates a new instance by parsing the {@code connectionString} into its
     * components.
     *
     * @param connectionString The connection string to the Event Hub instance.
     *
     * @throws NullPointerException if {@code connectionString} is null.
     * @throws IllegalArgumentException if {@code connectionString} is an empty
     *                                  string or the connection string has an
     *                                  invalid format.
     */
    public CommunicationConnectionString(String connectionString) {
        Objects.requireNonNull(connectionString, "'connectionString' cannot be null.");
        if (connectionString.isEmpty()) {
            throw new IllegalArgumentException("'connectionString' cannot be an empty string.");
        }
        final String[] tokenValuePairs = connectionString.split(TOKEN_VALUE_PAIR_DELIMITER);
        String endpoint = null;
        String accessKey = null;
        for (String tokenValuePair : tokenValuePairs) {
            final String[] pair = tokenValuePair.split(TOKEN_VALUE_SEPARATOR, 2);
            if (pair.length != 2) {
                throw new IllegalArgumentException(
                        String.format("Connection string has invalid key value pair: %s", tokenValuePair));
            }
            final String key = pair[0].trim();
            final String value = pair[1].trim();

            if (key.equalsIgnoreCase(ENDPOINT_NAME)) {
                endpoint = Objects.requireNonNull(value, "'endpoint' cannot be null.");
            } else if (key.equalsIgnoreCase(ACCESS_KEY_NAME)) {
                accessKey = Objects.requireNonNull(value, "'accessKey' cannot be null.");
            } else {
                throw new IllegalArgumentException(String.format("Illegal connection string parameter name: %s", key));
            }
        }
        this.endpoint = endpoint;
        this.accessKey = accessKey;
    }

    /**
     * Gets the endpoint to be used for connecting to Azure
     * @return The endpoint address, including protocol, from the connection string.
     */
    public String getEndpoint() {
        return endpoint;
    }

      /**
     * The value of the access key to be used for connecting to Azure
     * @return The value of the access key.
     */
    public String getAccessKey() {
        return accessKey;
    }


}
