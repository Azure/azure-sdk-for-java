// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties;

import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Azure Key Vault JCA vault common properties.
 *
 * @since 5.21.0
 */
public class AzureKeyVaultJcaVaultCommonProperties {

    @NestedConfigurationProperty
    private final AzureKeyVaultJcaVaultTokenCredentialConfigurationProperties credential = new AzureKeyVaultJcaVaultTokenCredentialConfigurationProperties();

    @NestedConfigurationProperty
    private final AzureKeyVaultJcaVaultProfileConfigurationProperties profile = new AzureKeyVaultJcaVaultProfileConfigurationProperties();

    public AzureKeyVaultJcaVaultTokenCredentialConfigurationProperties getCredential() {
        return credential;
    }

    public AzureKeyVaultJcaVaultProfileConfigurationProperties getProfile() {
        return profile;
    }
}
