// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties;

import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Azure Key Vault JCA properties.
 *
 * @since 5.21.0
 */
public class AzureKeyVaultJcaProperties {

    @NestedConfigurationProperty
    private final AzureKeyVaultJcaTokenCredentialConfigurationProperties credential = new AzureKeyVaultJcaTokenCredentialConfigurationProperties();

    @NestedConfigurationProperty
    private final AzureKeyVaultJcaProfileConfigurationProperties profile = new AzureKeyVaultJcaProfileConfigurationProperties();

    /**
     * Azure Key Vault endpoint.
     */
    private String endpoint;

    public AzureKeyVaultJcaTokenCredentialConfigurationProperties getCredential() {
        return credential;
    }

    public AzureKeyVaultJcaProfileConfigurationProperties getProfile() {
        return profile;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
