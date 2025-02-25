// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Azure Key Vault JCA properties.
 *
 * @since 5.21.0
 */
public class AzureKeyVaultJcaProperties {

    public static final String PREFIX = "spring.cloud.azure.keyvault.jca";
    // propertySources
    private final Map<String, AzureKeyVaultJcaVaultProperties> vaults = new LinkedHashMap<>();

    public Map<String, AzureKeyVaultJcaVaultProperties> getVaults() {
        return vaults;
    }
}
