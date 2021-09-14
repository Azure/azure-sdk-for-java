// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault;

import com.azure.spring.cloud.autoconfigure.properties.AzureHttpConfigurationProperties;

/**
 * Common properties for Azure Key Vault
 */
public class AzureKeyVaultProperties extends AzureHttpConfigurationProperties {

    // TODO (xiada): the default vault url
    private String vaultUrl;

    public String getVaultUrl() {
        return vaultUrl;
    }

    public void setVaultUrl(String vaultUrl) {
        this.vaultUrl = vaultUrl;
    }
}
