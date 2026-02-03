// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.keys.implementation;

import com.azure.v2.security.keyvault.keys.models.JsonWebKey;
import com.azure.v2.security.keyvault.keys.models.KeyVaultKey;

/**
 * Helper class used to create instances of {@link KeyVaultKey}. This class is used to allow the implementation to be
 * hidden from the public API.
 */
public final class KeyVaultKeyHelper {
    private static KeyVaultKeyAccessor accessor;

    /**
     * Interface to be implemented by the client to provide the logic for creating {@link KeyVaultKey} instances.
     */
    public interface KeyVaultKeyAccessor {
        /**
         * Creates a new instance of {@link KeyVaultKey} using the provided {@link JsonWebKey}.
         *
         * @param jsonWebKey The {@link JsonWebKey} to create the {@link KeyVaultKey} from.
         * @return A new instance of {@link KeyVaultKey}.
         */
        KeyVaultKey createKeyVaultKey(JsonWebKey jsonWebKey);
    }

    /**
     * Creates a {@link KeyVaultKey} instance from the given {@link JsonWebKey}.
     *
     * @param jsonWebKey The {@link JsonWebKey} to create the key vault key from.
     * @return A {@link KeyVaultKey} instance.
     */
    public static KeyVaultKey createKeyVaultKey(JsonWebKey jsonWebKey) {
        // If the class hasn't been loaded yet the accessor won't be set. Attempt to load the class before using the
        // accessor.
        if (accessor == null) {
            new KeyVaultKey();
        }

        assert accessor != null;
        return accessor.createKeyVaultKey(jsonWebKey);
    }

    /**
     * Sets the accessor to be used for creating {@link KeyVaultKey} instances.
     *
     * @param accessor The accessor to set.
     */
    public static void setAccessor(KeyVaultKeyAccessor accessor) {
        KeyVaultKeyHelper.accessor = accessor;
    }

    private KeyVaultKeyHelper() {
        // Private constructor to prevent instantiation.
    }
}
