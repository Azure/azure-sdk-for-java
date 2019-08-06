// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.models;

import com.azure.core.implementation.util.ImplUtils;

/**
 * Represents the details of decrypt operation result.
 */
public final class DecryptResult {
    /**
     * The decrypted content.
     */
    private byte[] plainText;

    /**
     * Creates the instance of Decrypt Result holding decrypted content.
     * @param plainText The decrypted content.
     */
    public DecryptResult(byte[] plainText) {
        this.plainText = ImplUtils.clone(plainText);
    }

    /**
     * Get the encrypted content.
     * @return The decrypted content.
     */
    public byte[] plainText() {
        return ImplUtils.clone(plainText);
    }
}
