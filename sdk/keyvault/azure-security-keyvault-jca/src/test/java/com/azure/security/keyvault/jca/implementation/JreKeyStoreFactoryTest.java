// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation;

import com.azure.security.keyvault.jca.KeyVaultKeyStore;
import org.junit.jupiter.api.Test;

import java.security.KeyStore;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class JreKeyStoreFactoryTest {
    @Test
    public void test() {
        KeyStore jreKeyStore = JreKeyStoreFactory.getDefaultKeyStore();
        assertFalse(jreKeyStore.getType().equals(KeyVaultKeyStore.KEY_STORE_TYPE));
    }
}
