/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.keyvault.config;

import org.springframework.core.env.EnumerablePropertySource;

/**
 * Retrieve all properties from the Azure Key Vault instance.
 */
// TODO: Use Key Vault source class here.
public class KeyVaultPropertySource extends EnumerablePropertySource<Object> {
    public KeyVaultPropertySource(String name, Object source) {
        super(name, source);
    }

    @Override
    public String[] getPropertyNames() {
        return new String[0];
    }

    @Override
    public Object getProperty(String s) {
        return null;
    }
}
