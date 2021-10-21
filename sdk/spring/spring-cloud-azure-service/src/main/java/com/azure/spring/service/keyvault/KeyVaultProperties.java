// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.keyvault;

import com.azure.spring.core.properties.AzureProperties;

/**
 * Common properties for Azure Key Vault
 */
public interface KeyVaultProperties extends AzureProperties {

    String getEndpoint();

}
