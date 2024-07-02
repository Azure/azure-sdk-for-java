// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.environment;

import org.springframework.core.env.EnumerablePropertySource;


/**
 * A key vault implementation of {@link EnumerablePropertySource} to enumerate all property pairs in Key Vault.
 *
 * @since 4.0.0
 */
public class KeyVaultPropertySource extends EnumerablePropertySource<KeyVaultOperation> {

    private final KeyVaultOperation operations;

    /**
     * Create a new {@code KeyVaultPropertySource} with the given name and {@link KeyVaultOperation}.
     * @param name the associated name
     * @param operation the {@link KeyVaultOperation}
     */
    public KeyVaultPropertySource(String name, KeyVaultOperation operation) {
        super(name, operation);
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

    @Override
    public boolean containsProperty(String name) {
        return getProperty(name) != null;
    }

}
