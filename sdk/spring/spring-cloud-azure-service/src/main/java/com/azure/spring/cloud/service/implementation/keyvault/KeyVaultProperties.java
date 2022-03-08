// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.keyvault;

import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;

/**
 * Common properties for Azure Key Vault
 */
public interface KeyVaultProperties extends AzureProperties, RetryOptionsProvider {

    String getEndpoint();

}
