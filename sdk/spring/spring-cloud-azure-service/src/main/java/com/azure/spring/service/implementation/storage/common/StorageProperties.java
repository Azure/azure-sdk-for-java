// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.storage.common;

import com.azure.spring.core.aware.authentication.ConnectionStringAware;
import com.azure.spring.core.aware.authentication.SasTokenAware;
import com.azure.spring.core.properties.AzureProperties;

/**
 * Common properties for all Azure Storage services.
 */
public interface StorageProperties extends AzureProperties, SasTokenAware, ConnectionStringAware {

    String getEndpoint();

    String getAccountName();

    String getAccountKey();

}
