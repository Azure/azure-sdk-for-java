// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.storage.common;

import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.aware.credential.SasTokenAware;

/**
 * Common properties for all Azure Storage services.
 */
public interface StorageProperties extends AzureProperties, SasTokenAware {

    String getEndpoint();

    String getAccountName();

    String getAccountKey();

    String getConnectionString();

}
