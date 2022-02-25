// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault.environment;

import org.springframework.core.env.EnumerablePropertySource;


/**
 * A key vault implementation of {@link EnumerablePropertySource} to enumerate all property pairs in Key Vault.
 *
 * @since 4.0.0
 */
public class KeyVaultPropertySource extends EnumerablePropertySource<KeyVaultOperation> {

    private final KeyVaultOperation operations;
    public static final String DEFAULT_AZURE_KEYVAULT_PROPERTYSOURCE_NAME = "azurekv";

    /**
     * Creates a new instance of {@link KeyVaultPropertySource}.
     *
     * @param keyVaultName the KeyVault name
     * @param operation the KeyVault operation
     */
    public KeyVaultPropertySource(String keyVaultName, KeyVaultOperation operation) {
        super(keyVaultName, operation);
        this.operations = operation;
    }

    /**
     * Creates a new instance of {@link KeyVaultPropertySource}.
     *
     * @param operation the KeyVault operation
     */
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
