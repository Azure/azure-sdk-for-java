// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties;

/**
 * Azure Key Vault JCA vault properties.
 *
 * @since 5.21.0
 */
public class AzureKeyVaultJcaVaultProperties extends AzureKeyVaultJcaVaultCommonProperties {

    /**
     * Azure Key Vault endpoint.
     */
    private String endpoint;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
