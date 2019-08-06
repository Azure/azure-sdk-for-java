// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.models;

import com.azure.core.implementation.util.ImplUtils;

/**
 * Represents the details of wrap operation result.
 */
public final class KeyWrapResult {

    /**
     * Creates the instance of KeyWrapResult holding the key wrap operation response details.
     * @param encryptedKey The unwrapped key content.
     * @param algorithm The algorithm used to wrap the key content.
     */
    public KeyWrapResult(byte[] encryptedKey, KeyWrapAlgorithm algorithm) {
        this.encryptedKey = ImplUtils.clone(encryptedKey);
        this.algorithm = algorithm;
    }

    /**
     * The encrypted key content
     */
    private byte[] encryptedKey;

    /**
     * The key wrap algorithm used to wrap the key content.
     */
    private KeyWrapAlgorithm algorithm;

    /**
     * Get the encrypted key content.
     * @return The encrypted key.
     */
    public byte[] encryptedKey() {
        return ImplUtils.clone(encryptedKey);
    }

    /**
     * Get the key wrap algorithm used to wrap the key content.
     * @return The key wrap algorithm.
     */
    public KeyWrapAlgorithm algorithm() {
        return algorithm;
    }
}
