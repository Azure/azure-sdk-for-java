// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.spring;

import org.springframework.core.env.EnumerablePropertySource;

import static com.microsoft.azure.utils.Constants.AZURE_KEYVAULT_PROPERTYSOURCE_NAME;
import org.springframework.core.env.PropertySource;
/**
 * A key vault implementation of {@link EnumerablePropertySource} to enumerate all property pairs in Key Vault.
 */
public class KeyVaultPropertySource extends PropertySource<KeyVaultOperation> {

    private final KeyVaultOperation operations;

    public KeyVaultPropertySource(String keyVaultName, KeyVaultOperation operation) {
        super(keyVaultName, operation);
        this.operations = operation;
    }

    public KeyVaultPropertySource(KeyVaultOperation operation) {
        super(AZURE_KEYVAULT_PROPERTYSOURCE_NAME, operation);
        this.operations = operation;
    }


    public String[] getPropertyNames() {
        return this.operations.getPropertyNames();
    }


    public Object getProperty(String name) {
        return operations.get(name);
    }
}
