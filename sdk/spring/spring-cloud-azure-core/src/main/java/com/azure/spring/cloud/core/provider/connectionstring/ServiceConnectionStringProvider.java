// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.provider.connectionstring;

/**
 * Service connection string provider to provide the connection string for a service type.
 *
 * @param <T> The service type.
 */
public interface ServiceConnectionStringProvider<T> extends ConnectionStringProvider {

    /**
     * Provide the service type this provider supports.
     * @return The service type.
     */
    T getServiceType();

}
