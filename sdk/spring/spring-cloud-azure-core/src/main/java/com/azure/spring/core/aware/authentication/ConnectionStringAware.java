// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.aware.authentication;

/**
 * Interface to be implemented by classes that wish to be aware of the connection string.
 */
public interface ConnectionStringAware {

    String getConnectionString();

}
