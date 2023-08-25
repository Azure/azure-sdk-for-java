// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.implementation;

import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.keys.models.KeyVaultKey;

public final class KeyVaultKeyHelper {
    private static KeyVaultKeyAccessor accessor;

    public interface KeyVaultKeyAccessor {
        KeyVaultKey createKeyVaultKey();
        void setKey(KeyVaultKey keyVaultKey, JsonWebKey jsonWebKey);
    }

    public static KeyVaultKey createKeyVaultKey() {
        // If the class hasn't been loaded yet the accessor won't be set. Attempt to load the class before using the
        // accessor.
        if (accessor == null) {
            try {
                KeyVaultKeyHelper.class.getClassLoader().loadClass(KeyVaultKey.class.getName());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        return accessor.createKeyVaultKey();
    }

    public static void setKey(KeyVaultKey keyVaultKey, JsonWebKey jsonWebKey) {
        accessor.setKey(keyVaultKey, jsonWebKey);
    }

    public static void setAccessor(KeyVaultKeyAccessor accessor) {
        KeyVaultKeyHelper.accessor = accessor;
    }

    private KeyVaultKeyHelper() {
    }
}
