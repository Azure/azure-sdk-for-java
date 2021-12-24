// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault.common;

import com.azure.spring.cloud.autoconfigure.properties.core.AbstractAzureHttpCP;

/**
 * Azure Key Vault properties.
 *
 * @since 4.0.0
 */
public class AzureKeyVaultProperties extends AbstractAzureHttpCP {

    /**
     * Azure Key Vault endpoint.
     */
    private String endpoint;

    /**
     *
     * @return The Azure Key Vault endpoint.
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     *
     * @param endpoint The Azure Key Vault endpoint.
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
