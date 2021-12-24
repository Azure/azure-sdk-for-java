// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.keyvault.secrets;

import com.azure.security.keyvault.secrets.SecretServiceVersion;
import com.azure.spring.service.keyvault.KeyVaultProperties;

/**
 * Properties for Azure Key Vault Secrets.
 */
public interface SecretClientProperties extends KeyVaultProperties {

    SecretServiceVersion getServiceVersion();

}
