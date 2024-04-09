// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.implementation;

import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyVaultKey;

public final class KeyVaultKeyHelper {
    private static KeyVaultKeyAccessor accessor;

    public interface KeyVaultKeyAccessor {
        KeyVaultKey createKeyVaultKey(JsonWebKey jsonWebKey);
    }

    public static KeyVaultKey createKeyVaultKey(JsonWebKey jsonWebKey) {
        // If the class hasn't been loaded yet the accessor won't be set. Attempt to load the class before using the
        // accessor.
        if (accessor == null) {
            try {
                Class.forName(KeyVaultKey.class.getName(), true, KeyVaultKeyHelper.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        return accessor.createKeyVaultKey(jsonWebKey);
    }

    public static void setAccessor(KeyVaultKeyAccessor accessor) {
        KeyVaultKeyHelper.accessor = accessor;
    }

    private KeyVaultKeyHelper() {
    }
}
