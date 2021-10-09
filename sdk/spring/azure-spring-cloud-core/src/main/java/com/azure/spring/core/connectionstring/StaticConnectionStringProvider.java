// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.connectionstring;

/**
 *
 */
public class StaticConnectionStringProvider<T> implements ConnectionStringProvider<T> {

    private final String connectionString;
    private final T serviceType;

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
