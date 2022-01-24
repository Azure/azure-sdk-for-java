// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.resource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Container for connection string of stores
 */
public final class ConnectionPool {

    private final Map<String, Connection> connectionStringMap = new ConcurrentHashMap<>();

    /**
     * Adds new connection to the Connection Pool
     * @param endpoint App Configuration Store Endpoint
     * @param connectionString Connection String to App Configuration
     */
    public void put(String endpoint, Connection connectionString) {
        Assert.hasText(endpoint, "Config store endpoint cannot be null or empty.");
        Assert.notNull(connectionString, "Connection string should not be null.");
        this.connectionStringMap.put(endpoint, connectionString);
    }

    /**
     * Adds new connection to the Connection Pool
     * @param endpoint App Configuration Store Endpoint
     * @param connectionString Connection String to App Configuration
     */
    public void put(String endpoint, String connectionString) {
        this.put(endpoint, new Connection(connectionString));
    }

    /**
     * Gets Connection used to connect to the given endpoint.
     * @param endpoint App Configuration Endpoint
     * @return Connection to App Configuration
     */
    @Nullable
    public Connection get(String endpoint) {
        return this.connectionStringMap.get(endpoint);
    }

    /**
     * Returns all Connections to App Configuration
     * @return Map{@literal <}String, Connection{@literal >}
     */
    public Map<String, Connection> getAll() {
        return this.connectionStringMap;
    }
}

