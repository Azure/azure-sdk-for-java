// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.provider.connectionstring;

/**
 * A static implementation of {@link ServiceConnectionStringProvider}, with static value of connection string.
 *
 * @param <T> The service type.
 */
public final class StaticConnectionStringProvider<T> implements ServiceConnectionStringProvider<T> {

    private final String connectionString;
    private final T serviceType;

    /**
     * Create a {@link StaticConnectionStringProvider} instance of {@link T} type and with provided connection string.
     * @param serviceType The service type.
     * @param connectionString The value of the connection string.
     */
    public StaticConnectionStringProvider(T serviceType, String connectionString) {
        this.serviceType = serviceType;
        this.connectionString = connectionString;
    }

    @Override
    public String getConnectionString() {
        return this.connectionString;
    }

    @Override
    public T getServiceType() {
        return this.serviceType;
    }
}
