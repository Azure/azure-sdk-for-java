// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.cryptography;

/**
 * Defines synchronous operations for wrapping and unwrapping keys with a key encryption key.
 */
public interface KeyEncryptionKey {
    /**
     * Gets the key identifier.
     *
     * @return The key identifier.
     */
    String getKeyId();

    /**
     * Wraps key material using the specified algorithm.
     *
     * @param algorithm The key-wrap algorithm.
     * @param key The key material to wrap.
     * @return The wrapped key material.
     */
    byte[] wrapKey(String algorithm, byte[] key);

    /**
     * Unwraps key material using the specified algorithm.
     *
     * @param algorithm The key-wrap algorithm.
     * @param encryptedKey The wrapped key material.
     * @return The unwrapped key material.
     */
    byte[] unwrapKey(String algorithm, byte[] encryptedKey);
}
