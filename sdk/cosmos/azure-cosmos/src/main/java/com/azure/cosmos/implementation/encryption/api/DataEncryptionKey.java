// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption.api;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * Abstraction for a data encryption key for use in client-side encryption.
 * See https://aka.ms/CosmosClientEncryption for more information on client-side encryption support in Azure Cosmos DB.
 */
public interface DataEncryptionKey {

    /**
     * Gets Raw key bytes of the data encryption key.
     * @return
     */
    byte[] getRawKey();


    /**
     * Gets Encryption algorithm to be used with this data encryption key.
     * @return encryption algorithm.
     */
    String getEncryptionAlgorithm();

    /**
     * Encrypts the plainText with a data encryption key.
     * @param plainText >Plain text value to be encrypted.
     * @return encrypted data.
     */
    byte[] encryptData(byte[] plainText);

    /**
     * Decrypts the cipherText with a data encryption key.
     * @param cipherText Ciphertext value to be decrypted.
     * @return Plain text.
     */
    byte[] decryptData(byte[] cipherText);

    /**
     * Generates raw data encryption key bytes suitable for use with the provided encryption algorithm.
     * @param encryptionAlgorithm Encryption algorithm the returned key is intended to be used with.
     * @return New instance of data encryption key.
     */
    static byte[] generate(String encryptionAlgorithm) {
        if (!StringUtils.equals(encryptionAlgorithm, CosmosEncryptionAlgorithm.AEAes256CbcHmacSha256Randomized)) {
            throw new IllegalArgumentException(String.format("Encryption algorithm not supported: {%s}", encryptionAlgorithm));
        }

        byte[] rawKey = new byte[32];

        try {
            DataEncryptionKey.class.getClassLoader()
                .loadClass("com.azure.cosmos.implementation.encryption.AeadAes256CbcHmac256AlgorithmProvider")
                .getDeclaredMethod("generateRandomBytes", byte[].class)
                .invoke(null, rawKey);

        } catch (IllegalAccessException|InvocationTargetException|NoSuchMethodException|ClassNotFoundException e) {
            throw new IllegalStateException("azure-cosmos-encryption is not in the classpath");
        }

        return rawKey;
    }

    /**
     * Creates a new instance of data encryption key given the raw key bytes
     * suitable for use with the provided encryption algorithm.
     * @param rawKey Raw key bytes.
     * @param encryptionAlgorithm Encryption algorithm the returned key is intended to be used with.
     * @return New instance of data encryption key.
     */
    static DataEncryptionKey create(
        byte[] rawKey,
        String encryptionAlgorithm) {
        if (rawKey == null) {
            throw new NullPointerException("rawKey");
        }

        if (!StringUtils.equals(encryptionAlgorithm, CosmosEncryptionAlgorithm.AEAes256CbcHmacSha256Randomized)) {
            throw new IllegalArgumentException(String.format("Encryption algorithm not supported: {%s}", encryptionAlgorithm));
        }

        try {
            return (DataEncryptionKey) DataEncryptionKey.class.getClassLoader()
                .loadClass("com.azure.cosmos.implementation.encryption.AeadAes256CbcHmac256AlgorithmProvider")
                .getDeclaredMethod("createAlgorithm", byte[].class, EncryptionType.class, byte.class)
                .invoke(null, rawKey, EncryptionType.RANDOMIZED, /** algorithmVersion **/(byte) 1);

        } catch (IllegalAccessException|InvocationTargetException|NoSuchMethodException|ClassNotFoundException e) {
            throw new IllegalStateException("azure-cosmos-encryption is not in the classpath");
        }
    }
}
