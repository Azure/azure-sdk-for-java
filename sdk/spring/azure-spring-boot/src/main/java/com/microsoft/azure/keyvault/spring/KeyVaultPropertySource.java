// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.spring;

import com.microsoft.azure.utils.Constants;
import org.springframework.core.env.EnumerablePropertySource;

/**
 * A key vault implementation of {@link EnumerablePropertySource} to enumerate all property pairs in Key Vault.
 */
public class KeyVaultPropertySource extends EnumerablePropertySource<KeyVaultOperation> {

    private final KeyVaultOperation operations;

    public KeyVaultPropertySource(KeyVaultOperation operation) {
        super(Constants.AZURE_KEYVAULT_PROPERTYSOURCE_NAME, operation);
        this.operations = operation;
    }


    public String[] getPropertyNames() {
        return this.operations.list();
    }


    public Object getProperty(String name) {
        return operations.get(name);
    }
}
