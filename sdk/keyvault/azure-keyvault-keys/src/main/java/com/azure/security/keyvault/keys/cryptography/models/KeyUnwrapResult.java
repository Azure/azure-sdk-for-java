// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.models;

import com.azure.core.implementation.util.ImplUtils;

/**
 * Represents the details of key unwrap operation result.
 */
public final class KeyUnwrapResult {
    /**
     * The unwrapped key content.
     */
    private byte[] key;

    /**
     * Creates the instance of KeyUnwrap Result holding the unwrapped key content.
     * @param key The unwrapped key content.
     */
    public KeyUnwrapResult(byte[] key) {
        this.key = ImplUtils.clone(key);
    }

    /**
     * Get the unwrapped key content.
     * @return The unwrapped key content.
     */
    public byte[] key() {
        return ImplUtils.clone(key);
    }
}
