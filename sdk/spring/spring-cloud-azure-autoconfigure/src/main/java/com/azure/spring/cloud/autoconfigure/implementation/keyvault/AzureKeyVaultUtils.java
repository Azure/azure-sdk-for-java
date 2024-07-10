// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault;

/**
 * Azure Key Vault utilities.
 */
public abstract class AzureKeyVaultUtils {

    private AzureKeyVaultUtils() {

    }


    /**
     * Default Azure Key Vault properties bean name.
     */
    public static final String DEFAULT_KEY_VAULT_PROPERTIES_BEAN_NAME = "defaultAzureKeyVaultProperties";

}
