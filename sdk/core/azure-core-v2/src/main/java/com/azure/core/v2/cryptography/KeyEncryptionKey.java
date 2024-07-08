// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.cryptography;

/**
 * A KeyEncryptionKey defines synchronous methods for encrypting and decrypting keys, also
 * known as key wrapping and unwrapping. It also supports signing and verifying data using the configured key.
 */
public interface KeyEncryptionKey {

    /**
     * Retrieves the key identifier.
     *
     * @return The key identifier.
     */
    String getKeyId();

    /**
     * Encrypts the specified key using the specified algorithm.
     *
     * @param algorithm The key wrap algorithm used to encrypt the specified key.
     * @param key The key content to be encrypted.
     * @return The encrypted key bytes.
     */
    byte[] wrapKey(String algorithm, byte[] key);

    /**
     * Decrypts the specified encrypted key using the specified algorithm.
     *
     * @param algorithm The key wrap algorithm which was used to encrypt the specified encrypted key.
     * @param encryptedKey The encrypted key content to be decrypted.
     * @return The decrypted key bytes.
     */
    byte[] unwrapKey(String algorithm, byte[] encryptedKey);
}
