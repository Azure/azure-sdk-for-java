// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties;

import org.springframework.boot.autoconfigure.ssl.SslBundleProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Azure Key Vault SSL Bundle properties.
 *
 * @since 5.21.0
 */
public class AzureKeyVaultSslBundleProperties extends SslBundleProperties {

    /**
     * Key Vault keystore properties.
     */
    @NestedConfigurationProperty
    private final AzureKeyVaultSslBundleKeyStoreProperties keystore = new AzureKeyVaultSslBundleKeyStoreProperties();

    /**
     * Key Vault truststore properties.
     */
    @NestedConfigurationProperty
    private final AzureKeyVaultSslBundleKeyStoreProperties truststore = new AzureKeyVaultSslBundleKeyStoreProperties();

    public AzureKeyVaultSslBundleKeyStoreProperties getKeystore() {
        return keystore;
    }

    public AzureKeyVaultSslBundleKeyStoreProperties getTruststore() {
        return truststore;
    }
}
