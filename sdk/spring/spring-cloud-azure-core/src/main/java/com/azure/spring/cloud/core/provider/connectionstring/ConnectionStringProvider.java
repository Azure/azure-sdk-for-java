// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.provider.connectionstring;

/**
 * Interface to be implemented by classes that wish to provide the connection string.
 */
public interface ConnectionStringProvider {

    /**
     * Get the connection string
     * @return the connection string
     */
    String getConnectionString();

}
