// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.spring;


import static com.microsoft.azure.utils.Constants.AZURE_KEYVAULT_PROPERTYSOURCE_NAME;

import org.springframework.core.env.PropertySource;

public class KeyVaultPropertySource extends PropertySource<KeyVaultOperation> {

    private final KeyVaultOperation operations;

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
