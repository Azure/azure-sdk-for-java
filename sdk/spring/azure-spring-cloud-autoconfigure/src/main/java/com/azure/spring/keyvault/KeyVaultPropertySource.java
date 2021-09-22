// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.keyvault;

import org.springframework.core.env.EnumerablePropertySource;


/**
 * A key vault implementation of {@link EnumerablePropertySource} to enumerate all property pairs in Key Vault.
 */
public class KeyVaultPropertySource extends EnumerablePropertySource<KeyVaultOperation> {

    private final KeyVaultOperation operations;
    public static final String DEFAULT_AZURE_KEYVAULT_PROPERTYSOURCE_NAME = "azurekv";

    public KeyVaultPropertySource(String keyVaultName, KeyVaultOperation operation) {
        super(keyVaultName, operation);
        this.operations = operation;
    }

    public KeyVaultPropertySource(KeyVaultOperation operation) {
        super(DEFAULT_AZURE_KEYVAULT_PROPERTYSOURCE_NAME, operation);
        this.operations = operation;
    }

    @Override
    public String[] getPropertyNames() {
        return this.operations.getPropertyNames();
    }

    @Override
    public Object getProperty(String name) {
        return operations.getProperty(name);
    }

    boolean isUp() {
        return operations.isUp();
    }
}
