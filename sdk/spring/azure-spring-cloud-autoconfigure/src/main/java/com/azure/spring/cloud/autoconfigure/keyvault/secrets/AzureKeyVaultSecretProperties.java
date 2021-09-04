// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault.secrets;

import com.azure.security.keyvault.secrets.SecretServiceVersion;
import com.azure.spring.cloud.autoconfigure.keyvault.AzureKeyVaultProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties for Azure Key Vault Secrets.
 */
@ConfigurationProperties(prefix = "spring.cloud.azure.keyvault.secret")
public class AzureKeyVaultSecretProperties extends AzureKeyVaultProperties {

    private SecretServiceVersion serviceVersion;

    public SecretServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(SecretServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
    }
}
