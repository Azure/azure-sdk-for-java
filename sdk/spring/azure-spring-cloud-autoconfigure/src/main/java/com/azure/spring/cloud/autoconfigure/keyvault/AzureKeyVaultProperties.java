// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault;

import com.azure.spring.cloud.autoconfigure.properties.AzureHttpConfigurationProperties;
import org.springframework.beans.factory.annotation.Value;

/**
 * Common properties for Azure Key Vault
 */
public class AzureKeyVaultProperties extends AzureHttpConfigurationProperties {

    @Value("${:}")
    private String vaultUrl;

    public String getVaultUrl() {
        return vaultUrl;
    }

    public void setVaultUrl(String vaultUrl) {
        this.vaultUrl = vaultUrl;
    }
}
