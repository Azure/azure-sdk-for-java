// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.keyvault.secrets;

import com.azure.security.keyvault.secrets.SecretServiceVersion;
import com.azure.spring.cloud.service.implementation.keyvault.KeyVaultProperties;

/**
 * Properties for Azure Key Vault Secrets.
 */
public interface SecretClientProperties extends KeyVaultProperties {

    SecretServiceVersion getServiceVersion();

}
