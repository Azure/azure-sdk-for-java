// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.models;

/**
 * Represents the details of decrypt operation result.
 */
public final class DecryptResult {
    /**
     * The decrypted content.
     */
    private byte[] plainText;

    public DecryptResult(byte[] plainText) {
        this.plainText = plainText;
    }

    /**
     * Get the encrypted content.
     * @return The decrypted content.
     */
    public byte[] plainText() {
        return plainText;
    }
}
