// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.aware.credential;

/**
 * Interface to be implemented by classes that wish to be aware of the connection string.
 */
public interface ConnectionStringAware {

    void setConnectionString(String connectionString);

    String getConnectionString();

}
