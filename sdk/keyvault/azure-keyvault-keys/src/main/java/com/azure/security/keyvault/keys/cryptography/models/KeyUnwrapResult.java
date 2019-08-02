// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.models;

/**
 * Represents the details of key unwrap operation result.
 */
public final class KeyUnwrapResult {
    /**
     * The unwrapped key content.
     */
    private byte[] key;

    public KeyUnwrapResult(byte[] key) {
        this.key = key;
    }

    /**
     * Get the unwrapped key content.
     * @return The unwrapped key content.
     */
    public byte[] key() {
        return key;
    }
}
