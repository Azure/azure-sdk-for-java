// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault.secrets;

import com.azure.security.keyvault.secrets.SecretServiceVersion;
import com.azure.spring.cloud.autoconfigure.keyvault.AzureKeyVaultProperties;

/**
 * Properties for Azure Key Vault Secrets.
 */
public class AzureKeyVaultSecretProperties extends AzureKeyVaultProperties {

    public static final String PREFIX = "spring.cloud.azure.keyvault.secret";

    private SecretServiceVersion serviceVersion;

    public SecretServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(SecretServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
    }
}
