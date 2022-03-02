// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.keyvault;

import com.azure.spring.core.aware.RetryOptionsAware;
import com.azure.spring.core.properties.AzureProperties;

/**
 * Common properties for Azure Key Vault
 */
public interface KeyVaultProperties extends AzureProperties, RetryOptionsAware {

    String getEndpoint();

}
