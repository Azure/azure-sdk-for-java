// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.aware.authentication;

/**
 * Interface to be implemented by classes that wish to be aware of the connection string.
 */
public interface ConnectionStringAware {

    /**
     * Get the connection string
     * @return the connection string
     */
    String getConnectionString();

}
