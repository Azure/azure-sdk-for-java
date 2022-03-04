// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.connectionstring;

/**
 *
 */
public interface ConnectionStringProvider<T> {

    /**
     * Provide the connection string of the associated Azure service.
     * @return The connection string.
     */
    String getConnectionString();

    /**
     * Provide the service type this provider supports.
     * @return The service type.
     */
    T getServiceType();

}
