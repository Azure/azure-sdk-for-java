// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.connectionstring;

/**
 *
 */
public interface ConnectionStringProvider<T> {

    String getConnectionString();

    T getServiceType();

}
