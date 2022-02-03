// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.connectionstring;

import com.azure.core.util.Configuration;

/**
 *
 */
public interface ConnectionStringProviderConfiguration<T> {

    /**
     * Provide the connection string of the associated Azure service.
     * @return The connection string.
     */
    String getConnectionString(Configuration configuration);

}
