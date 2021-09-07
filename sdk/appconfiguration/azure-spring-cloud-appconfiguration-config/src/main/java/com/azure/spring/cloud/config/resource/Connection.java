// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.resource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.Assert;

/**
 * Connection information for connecting to a Azure APp Configuration Store.
 */
public final class Connection {

    public static final String NON_EMPTY_MSG =
        "%s property should not be null or empty in the connection string of Azure Config Service.";
    private static final String CONN_STRING_REGEXP = "Endpoint=([^;]+);Id=([^;]+);Secret=([^;]+)";
    public static final String ENDPOINT_ERR_MSG = String.format("Connection string does not follow format %s.",
        CONN_STRING_REGEXP);
    private static final Pattern CONN_STRING_PATTERN = Pattern.compile(CONN_STRING_REGEXP);
    private final String endpoint;

    private final String connectionString;

    private final String clientId;

    /**
     * Creates a Connection that can be used to connect to an Azure App Configuration Store.
     * 
     * @param connectionString App Configuration Connection String
     * @throws IllegalStateException Connection String format is invalid.
     */
    public Connection(String connectionString) {
        Assert.hasText(connectionString, String.format("Connection string cannot be empty."));

        Matcher matcher = CONN_STRING_PATTERN.matcher(connectionString);
        if (!matcher.find()) {
            throw new IllegalStateException(ENDPOINT_ERR_MSG);
        }

        this.endpoint = matcher.group(1);

        Assert.hasText(endpoint, String.format(NON_EMPTY_MSG, "Endpoint"));

        this.connectionString = connectionString;
        this.clientId = "";
    }

    public Connection(String endpoint, String clientId) {
        this.endpoint = endpoint;
        this.clientId = clientId;
        this.connectionString = "";
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getConnectionString() {
        return connectionString;
    }

    /**
     * @return the clientId
     */
    public String getClientId() {
        return clientId;
    }

}
