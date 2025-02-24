// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties;

import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Azure Key Vault JCA common properties.
 *
 * @since 5.21.0
 */
public class AzureKeyVaultJcaCommonProperties {

    @NestedConfigurationProperty
    private final AzureKeyVaultJcaCertificatePathsProperties certificatePaths = new AzureKeyVaultJcaCertificatePathsProperties();

    @NestedConfigurationProperty
    private final AzureKeyVaultJcaTokenCredentialConfigurationProperties credential = new AzureKeyVaultJcaTokenCredentialConfigurationProperties();

    @NestedConfigurationProperty
    private final AzureKeyVaultJcaProfileConfigurationProperties profile = new AzureKeyVaultJcaProfileConfigurationProperties();

    public AzureKeyVaultJcaCertificatePathsProperties getCertificatePaths() {
        return certificatePaths;
    }

    public AzureKeyVaultJcaTokenCredentialConfigurationProperties getCredential() {
        return credential;
    }

    public AzureKeyVaultJcaProfileConfigurationProperties getProfile() {
        return profile;
    }
}
