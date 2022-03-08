// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.storage.common;

import com.azure.spring.cloud.core.aware.RetryOptionsProvider;
import com.azure.spring.cloud.core.aware.authentication.ConnectionStringAware;
import com.azure.spring.cloud.core.aware.authentication.SasTokenAware;
import com.azure.spring.cloud.core.properties.AzureProperties;

/**
 * Common properties for all Azure Storage services.
 */
public interface StorageProperties extends AzureProperties, RetryOptionsProvider, SasTokenAware, ConnectionStringAware {

    /**
     * Get the storage endpoint.
     * @return the storage endpoint.
     */
    String getEndpoint();

    /**
     * Get the storage account name.
     * @return the storage account name.
     */
    String getAccountName();

    /**
     * Get the storage account key.
     * @return the storage account key.
     */
    String getAccountKey();

}
