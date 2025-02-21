// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Azure Key Vault SSL Bundles properties.
 *
 * @since 5.21.0
 */
public class AzureKeyVaultSslBundlesProperties {

    public static final String PREFIX = "spring.ssl.bundle";

    private final Map<String, AzureKeyVaultSslBundleProperties> azureKeyvault = new LinkedHashMap<>();

    public Map<String, AzureKeyVaultSslBundleProperties> getAzureKeyvault() {
        return azureKeyvault;
    }
}
