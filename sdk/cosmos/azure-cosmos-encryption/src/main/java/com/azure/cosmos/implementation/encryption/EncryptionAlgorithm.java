// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

/**
 * Abstract base class for all encryption algorithms.
 */
interface EncryptionAlgorithm {
    String getAlgorithmName();

    /**
     * Encrypts the plainText with a data encryption key.
     *
     * @param plainText Plain text value to be encrypted.
     * @return Encrypted value.
     */
    byte[] encryptData(byte[] plainText);

    /**
     * Decrypts the cipherText with a data encryption key.
     *
     * @param cipherText Ciphertext value to be decrypted.
     * @return Plain text.
     */
    byte[] decryptData(byte[] cipherText);
}
