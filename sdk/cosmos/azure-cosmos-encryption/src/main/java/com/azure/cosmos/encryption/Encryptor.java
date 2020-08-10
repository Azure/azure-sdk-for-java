// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

/**
 * Abstraction for performing client-side encryption.
 * See https://aka.ms/CosmosClientEncryption for more information on client-side encryption support in Azure Cosmos DB.
 */
public interface Encryptor {

    /**
     * Encrypts the plainText using the key and algorithm provided.
     *
     * @param plainText           Plain text.
     * @param dataEncryptionKeyId Identifier of the data encryption key.
     * @param encryptionAlgorithm Identifier for the encryption algorithm.
     * @return Cipher text.
     */
    byte[] encryptAsync(
        byte[] plainText,
        String dataEncryptionKeyId,
        String encryptionAlgorithm);

    /**
     * Decrypts the cipherText using the key and algorithm provided.
     *
     * @param cipherText          Ciphertext to be decrypted.
     * @param dataEncryptionKeyId Identifier of the data encryption key.
     * @param encryptionAlgorithm Identifier for the encryption algorithm.
     * @return Plain text.
     */
    byte[] decryptAsync(
        byte[] cipherText,
        String dataEncryptionKeyId,
        String encryptionAlgorithm);
}
// TODO: how to do async api call?
